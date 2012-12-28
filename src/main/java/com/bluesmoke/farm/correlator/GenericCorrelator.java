package com.bluesmoke.farm.correlator;

import com.bluesmoke.farm.correlator.builder.CorrelatorBuilderManager;
import com.bluesmoke.farm.enumeration.PairResolution;
import com.bluesmoke.farm.enumeration.descendant.config.PassiveParentConfig;
import com.bluesmoke.farm.enumeration.Pair;
import com.bluesmoke.farm.exception.IllegalStateValueDataModificationException;
import com.bluesmoke.farm.listener.FeedListener;
import com.bluesmoke.farm.model.correlatordata.StateValueData;
import com.bluesmoke.farm.model.pnl.OpenOrder;
import com.bluesmoke.farm.model.tickdata.Tick;
import com.bluesmoke.farm.service.feed.FeedService;
import com.bluesmoke.farm.util.FixedSizeStackArrayList;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class GenericCorrelator extends Thread{

    private String id;
    private int generation = 0;
    private String passCode;
    private long age = 0;

    private long breedingAge;

    private double pnl = 0;
    private boolean toInterrupt = false;

    private int stateComponentsNumber = -1;

    protected String currentStateValueID;
    protected TreeMap<String, Object> currentUnderlyingComponents;

    protected Pair pair;
    protected char pairMetric;

    protected GenericCorrelator aggressiveParent;
    protected GenericCorrelator passiveParent;
    protected HashMap<String, Object> config = new HashMap<String, Object>();
    protected ArrayList<GenericCorrelator> children = new ArrayList<GenericCorrelator>();

    protected int numTicksObserved;
    protected int numTicksGapWithHorizon;
    protected int numTicksToWait;
    protected double resolution;

    protected ArrayList<Tick> ticks = new ArrayList<Tick>();
    protected Tick currentTick;

    private char processingStage = 'F';
    private boolean alive = true;

    protected ConcurrentHashMap<String, Object> correlatorData = new ConcurrentHashMap<String, Object>();
    protected TreeMap<String, StateValueData> memory = new TreeMap<String, StateValueData>();
    protected List<Map.Entry<StateValueData, Double>> historyStatValueData;
    protected FixedSizeStackArrayList<StateValueData> stackStateValueData = new FixedSizeStackArrayList<StateValueData>(5000);
    protected FixedSizeStackArrayList<TreeMap<String, Object>> stackUnderlyingComponents = new FixedSizeStackArrayList<TreeMap<String, Object>>(5000);

    private TreeMap<Integer, TreeMap<Integer, Long>> successGrid = new TreeMap<Integer, TreeMap<Integer, Long>>();

    private List<OpenOrder> openOrders = new LinkedList<OpenOrder>();

    protected CorrelatorPool pool;
    protected FeedService feed;
    protected CorrelatorBuilderManager correlatorBuilderManager;

    public GenericCorrelator(String id, CorrelatorBuilderManager correlatorBuilderManager, CorrelatorPool pool, FeedService feed, GenericCorrelator aggressiveParent, GenericCorrelator passiveParent, HashMap<String, Object> config)
    {
        this.id = id;
        this.pool = pool;
        this.feed = feed;
        this.correlatorBuilderManager = correlatorBuilderManager;

        passCode = "" + Math.random();

        this.aggressiveParent = aggressiveParent;
        this.passiveParent = passiveParent;
        if(aggressiveParent != null)
        {
            generation = aggressiveParent.getGeneration() + 1;
            aggressiveParent.addChild(this);
        }
        if(passiveParent != null)
        {
            if(passiveParent.getGeneration() >= generation)
            {
                generation = passiveParent.getGeneration() + 1;
            }
            passiveParent.addChild(this);
        }
        if(config != null)
        {
            this.config = config;
        }
        //stackStateValueData.setSize(10000);
        //stackUnderlyingComponents.setSize(10000);

        if(aggressiveParent != null)
        {
            this.setBreedingAge(aggressiveParent.getBreedingAge());
            this.setNumberTicksGapWithHorizon(aggressiveParent.getNumTicksGapWithHorizon());
            this.setNumberTicksObserved(aggressiveParent.getNumTicksGapWithHorizon());
            this.setNumberTicksToWait(aggressiveParent.getNumTicksToWait());
        }
        else {
            Random rand = new Random();
            setNumberTicksObserved((int) Math.pow(10,rand.nextInt(2) + 1));
            setNumberTicksGapWithHorizon((int) Math.pow(10, rand.nextInt(2) + 1));
            setNumberTicksToWait((int) Math.pow(10, rand.nextInt(2) + 1));
            setBreedingAge(10000);
        }

        TreeMap<Double, Pair> randMap = new TreeMap<Double, Pair>();
        for(Pair pair : Pair.values())
        {
            randMap.put(Math.random(), pair);
        }

        Pair trackedPair = randMap.ceilingEntry(0.0).getValue();

        TreeMap<Double, Character> randMetric = new TreeMap<Double, Character>();
        randMetric.put(Math.random(), 'A');
        randMetric.put(Math.random(), 'B');
        randMetric.put(Math.random(), 'M');

        Character trackedMetric = randMetric.ceilingEntry(0.0).getValue();
        setTrackedPairAndMetric(trackedPair, trackedMetric);

        pool.addCorrelator(this);
    }

    public void toInterrupt()
    {
        toInterrupt = true;
    }

    public void reset()
    {
        correlatorData.clear();
        historyStatValueData.clear();
        stackStateValueData.clear();
        stackUnderlyingComponents.clear();
        memory.clear();
        processingStage = 'F';
        age = 0;
    }

    public void addChild(GenericCorrelator child)
    {
        children.add(child);
    }

    public void setTrackedPairAndMetric(Pair pair, char metric)
    {
        this.pair = pair;
        this.pairMetric = metric;
        config.put("pair", pair);
        config.put("pairMetric", pairMetric);

        this.resolution = PairResolution.getResolution(pair);
        config.put("resolution", resolution);
    }

    public void setNumberTicksObserved(int num)
    {
        this.numTicksObserved = num;
        config.put("numTicksObserved", num);
    }

    public void setNumberTicksGapWithHorizon(int num)
    {
        this.numTicksGapWithHorizon = num;
        config.put("numTicksGapWithHorizon", num);
    }

    public void setNumberTicksToWait(int num)
    {
        this.numTicksToWait = num;
        config.put("numTicksToWait", num);
        historyStatValueData = new ArrayList<Map.Entry<StateValueData, Double>>();
    }

    public void setBreedingAge(long breedingAge)
    {
        this.breedingAge = breedingAge;
        config.put("breedingAge", breedingAge);
    }

    public StateValueData getCurrentStateValueData()
    {
        if(stackStateValueData.size() > 0)
        {
            return stackStateValueData.getHeadData();
        }
        return null;
    }

    public ConcurrentHashMap<String, Object> getCorrelatorData()
    {
        return correlatorData;
    }

    public String getID()
    {
        return id;
    }

    public int getGeneration()
    {
        return generation;
    }

    public void spawn() {
        if(Math.random() > 0.5)
        {
            correlatorBuilderManager.build(this);
        }
    }

    public void createOrder(String state, double takeProfitPips, double stopLossPips)
    {
        double open =  currentTick.getPairData(pair.name()).getMid();
        switch (pairMetric)
        {
            case 'A' : open =  currentTick.getPairData(pair.name()).getAsk(); break;
            case 'B' : open =  currentTick.getPairData(pair.name()).getBid(); break;
        }

        openOrders.add(new OpenOrder(state, open, open + takeProfitPips, open + stopLossPips));
        //System.out.println("Order created: " + id + ", Target: " + takeProfitPips);
    }

    public abstract void createMutant();

    public abstract String createState();

    @Override
    public void run()
    {
        System.out.println(id + " started...");
        while (alive)
        {
            while (processingStage != 'R')
            {
                try {
                    Thread.sleep(0,10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(toInterrupt)
                {
                    toInterrupt = false;
                    interrupt();
                }
            }
            processingStage = 'P';
            Tick tick = pool.getCurrentTick();
            age++;

            if(memory.size() > 100 && generation > 0)
            {
                if(Math.log(memory.size())/5 > stateComponentsNumber + 1)
                {
                    killLineage();
                }
            }
            else if(tick != null)
            {
                currentTick = tick;
                ticks.add(tick);
                if(ticks.size() > (numTicksObserved + numTicksGapWithHorizon))
                {
                    ticks.remove(0);

                    //TODO
                    currentUnderlyingComponents = new TreeMap<String, Object>();
                    currentStateValueID = createState();
                    if(stateComponentsNumber < currentUnderlyingComponents.size())
                    {
                        stateComponentsNumber = currentUnderlyingComponents.size();
                    }
                    //System.out.println(id + " " + currentStateValueID);

                    stackUnderlyingComponents.addToStack(currentUnderlyingComponents);

                    double currentValueOfTracked =  tick.getPairData(pair.name()).getMid();
                    switch (pairMetric)
                    {
                        case 'A' : currentValueOfTracked =  tick.getPairData(pair.name()).getAsk(); break;
                        case 'B' : currentValueOfTracked =  tick.getPairData(pair.name()).getBid(); break;
                    }

                    if(currentStateValueID != null)
                    {
                        if(!memory.containsKey(currentStateValueID))
                        {
                            memory.put(currentStateValueID, new StateValueData(currentStateValueID, resolution, passCode));
                        }

                        StateValueData currentStateValueData = memory.get(currentStateValueID);
                        if(stateComponentsNumber == -1)
                        {
                            stateComponentsNumber = currentStateValueData.getStateComponentNumber();
                        }
                        historyStatValueData.add(new AbstractMap.SimpleEntry<StateValueData, Double>(currentStateValueData, currentValueOfTracked));
                        stackStateValueData.addToStack(currentStateValueData);

                        if(currentStateValueData.getCount() > 50 && currentStateValueData.getSharpe() > 1)
                        {
                            int mode = 0;
                            long freq = 0;
                            int modeP = 0;
                            long freqP = 0;
                            int modeN = 0;
                            long freqN = 0;
                            for(TreeMap<Integer, Long> dist : currentStateValueData.getDist().values())
                            {
                                if(dist != null)
                                {
                                    for(int actionClass : dist.keySet())
                                    {
                                        if(dist.get(actionClass) > freq)
                                        {
                                            freq = dist.get(actionClass);
                                            mode = actionClass;
                                        }

                                        if(actionClass > 0)
                                        {
                                            if(dist.get(actionClass) > freqP)
                                            {
                                                freqP = dist.get(actionClass);
                                                modeP = actionClass;
                                            }
                                        }

                                        if(actionClass < 0)
                                        {
                                            if(dist.get(actionClass) > freqN)
                                            {
                                                freqN = dist.get(actionClass);
                                                modeN = actionClass;
                                            }
                                        }
                                    }
                                }
                            }

                            double average = currentStateValueData.getAverage();

                            int direction = 1;
                            if(Math.signum(mode) == Math.signum(modeN))
                            {
                                direction = -1;
                            }

                            if(Math.signum(average) == direction)
                            {
                                double takeProfitPips = 0;
                                if(direction == 1)
                                {
                                    takeProfitPips = modeP * PairResolution.getResolution(pair);
                                }
                                else
                                {
                                    takeProfitPips = modeN * PairResolution.getResolution(pair);
                                }
                                createOrder(currentStateValueID, takeProfitPips, -takeProfitPips);
                            }
                        }
                    }
                    else {
                        historyStatValueData.add(null);
                        stackStateValueData.addToStack(null);
                    }

                    if(historyStatValueData.size() > numTicksToWait)
                    {
                        historyStatValueData.remove(0);
                    }

                    int size = historyStatValueData.size();
                    int i = 0;
                    for(Map.Entry<StateValueData, Double> entry : historyStatValueData)
                    {
                        if(entry != null)
                        {
                            try
                            {
                                entry.getKey().addObservedResult(currentValueOfTracked - entry.getValue(), size - i, passCode);
                            }
                            catch (IllegalStateValueDataModificationException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        i++;
                    }

                    LinkedList<OpenOrder> toRemove = new LinkedList<OpenOrder>();
                    for(OpenOrder order : openOrders)
                    {
                        if(order.newPrice(currentValueOfTracked))
                        {
                            pnl += order.getPnL();
                            toRemove.add(order);
                            //System.out.println("Order closed: " + id + ", PnL: " + pnl);

                            double[] successData = order.getSuccessData();
                            int target = (int)(successData[0]/resolution);
                            int observed = (int)(successData[1]/resolution);

                            if(!successGrid.containsKey(target))
                            {
                                successGrid.put(target, new TreeMap<Integer, Long>());
                            }
                            if(!successGrid.get(target).containsKey(observed))
                            {
                                successGrid.get(target).put(observed, 0L);
                            }
                            successGrid.get(target).put(observed, successGrid.get(target).get(observed) + 1);
                        }
                    }
                    openOrders.removeAll(toRemove);
                }
            }
            processingStage = 'F';
            if(toInterrupt)
            {
                toInterrupt = false;
                interrupt();
            }
        }
        if(toInterrupt)
        {
            toInterrupt = false;
            interrupt();
        }
        System.out.println("Correlator " + id + " ended...");
    }

    public void setReady()
    {
        processingStage = 'R';
    }

    public void die()
    {
        if(pool.size() > 10 && Math.random()*age > 1000)
        {
            if(children.size() == 0)
            {
                pool.toKill(this);

                if(aggressiveParent != null)
                {
                    aggressiveParent.childDeath(this);
                }
                if(passiveParent != null)
                {
                    passiveParent.childDeath(this);
                }
                processingStage = 'F';
                alive = false;
                System.out.println("Correlator " + id + " dies");
            }
        }
    }

    public void forcedie()
    {
        if(children.size() == 0)
        {
            pool.toKill(this);

            if(aggressiveParent != null)
            {
                aggressiveParent.childDeath(this);
            }
            if(passiveParent != null)
            {
                passiveParent.childDeath(this);
            }
            processingStage = 'F';
            alive = false;
            System.out.println("Correlator " + id + " dies");
        }
    }

    public void killLineage()
    {
        for(GenericCorrelator child : children)
        {
            child.killLineage();
        }
        forcedie();
    }

    public void childDeath(GenericCorrelator child)
    {
        children.remove(child);
    }

    public int getNumTicksObserved() {
        return numTicksObserved;
    }

    public int getNumTicksGapWithHorizon() {
        return numTicksGapWithHorizon;
    }

    public int getNumTicksToWait() {
        return numTicksToWait;
    }

    public long getBreedingAge()
    {
        return breedingAge;
    }

    public long getAge()
    {
        return age;
    }

    public GenericCorrelator getAggressiveParent()
    {
        return aggressiveParent;
    }

    public GenericCorrelator getPassiveParent()
    {
        return passiveParent;
    }

    public Pair getPair()
    {
        return pair;
    }

    public double getResolution()
    {
        return resolution;
    }

    public char getProcessingStage()
    {
        return processingStage;
    }

    public int getStateComponentsNumber()
    {
        return stateComponentsNumber;
    }

    public double getSharpe()
    {
        double sharpe = 0;
        int i = 0;
        for(StateValueData stateValueData : memory.values())
        {
            if(stateValueData != null)
            {
                sharpe += stateValueData.getSharpe();
                i++;
            }
        }
        sharpe = sharpe/i;
        return sharpe;
    }

    public Map<String, Object> getCurrentUnderlyingComponents()
    {
        return currentUnderlyingComponents;
    }

    public Set<String> getUnderlyingComponentNames()
    {
        return currentUnderlyingComponents.keySet();
    }

    public Map<String, Object> getConfig()
    {
        return new TreeMap<String, Object>(config);
    }

    public double getPnL()
    {
        return pnl;
    }

    public TreeMap<Integer, TreeMap<Integer, Long>> getSuccssGrid()
    {
        return successGrid;
    }

    public String getHandlesInfo()
    {
        String info = "Correlator: " + id + "\n";
        info += "Children: " + children.size() + "\n";
        info += "States: " + memory.size() + "\n";
        info += "History: " + historyStatValueData.size() + "\n";
        info += "Stack: " + stackStateValueData.size() + "\n";
        info += "Stack Underlying: " + stackUnderlyingComponents.size() + "\n";
        info += "Open Orders: " + openOrders.size() + "\n";
        info += "Success Grid: " + successGrid.size() + "\n";
        info += "Ticks: " + ticks.size() + "\n\n";

        return info;
    }
}
