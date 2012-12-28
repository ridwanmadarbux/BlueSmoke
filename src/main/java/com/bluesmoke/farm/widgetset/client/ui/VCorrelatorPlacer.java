package com.bluesmoke.farm.widgetset.client.ui;

import org.vaadin.gwtgraphics.client.shape.Text;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class VCorrelatorPlacer implements Refreshable{

    private VCorrelatorPoolAnalytics canvas;
    private HashMap<String, VCorrelator> correlators = new HashMap<String, VCorrelator>();
    private TreeMap<Integer, TreeMap<Integer, Long>> dist = new TreeMap<Integer, TreeMap<Integer, Long>>();

    private Set<String> preBatchSnapShot = new TreeSet<String>();
    private Set<String> currentBatch = new TreeSet<String>();

    private int maxGen = 0;
    private double maxLogSharpe = 0;
    private double maxPnL = 0;
    private double minPnL = 0;

    private TreeMap<Integer, Text> yAxis = new TreeMap<Integer, Text>();

    public VCorrelatorPlacer(VCorrelatorPoolAnalytics canvas)
    {
        this.canvas = canvas;
        canvas.addRefreshable(this);
    }

    public void batch()
    {
        preBatchSnapShot.removeAll(currentBatch);
        for(String c : preBatchSnapShot)
        {
            try
            {
                correlators.get(c).die();
            }
            catch (Exception e)
            {
                canvas.logText("ERROR: " + c  + " not found...");
            }
        }

        preBatchSnapShot.addAll(correlators.keySet());
        currentBatch.clear();

        for(VCorrelator correlator : correlators.values())
        {
            int genC = 0;
            double sharpe = 0;
            double pnl = 0;
            try
            {
                genC = (Integer) correlator.information.get("GEN");
                sharpe = Math.log((Double) correlator.information.get("SHARPE") + 1);
                pnl = (Double) correlator.information.get("PNL");

                correlator.setDestX(((genC + 1) * canvas.width) / (maxGen + 2) + (int)canvas.random(5));

                correlator.setDestY(canvas.height - (int)(sharpe*(canvas.height - 100)/maxLogSharpe));

                double r = 2;
                if(maxPnL > minPnL)
                {
                  r += (5.0 * (pnl - minPnL)/(maxPnL - minPnL));
                }
                correlator.setR((int)r);
            }
            catch (Exception e)
            {
                canvas.logText("ERROR: " + correlator.id + "," + sharpe + "," + pnl + "," + genC);
            }
        }
        double maxSharpe = Math.exp(maxLogSharpe) - 1;
        for(int i = 0; i < 20; i++)
        {
            double sharpe = (double)((int)(((i*maxSharpe)/20)*100))/100;
            if(!yAxis.containsKey(i))
            {
                Text t = canvas.text("", 2, 10);
                t.setStrokeOpacity(0);
                t.setFillColor("white");
                yAxis.put(i, t);
            }
            yAxis.get(i).setText("" + sharpe);
            yAxis.get(i).setY(canvas.height - (int)(Math.log(sharpe + 1)*(canvas.height - 100)/maxLogSharpe));
        }
    }

    public void addCorrelatorData(String data)
    {
        try
        {
            String property = null;
            String value = null;
            for(String info : data.split(";"))
            {
                property = info.split("=")[0];
                value = info.split("=")[1];

                if(property.equals("ID"))
                {
                    currentBatch.add(value);
                    break;
                }
            }

            if(!correlators.containsKey(value))
            {
                correlators.put(value, new VCorrelator(canvas, correlators));
            }

            correlators.get(value).setData(data);
            int gen = (Integer)correlators.get(value).information.get("GEN");
            if(gen > maxGen)
            {
                maxGen = gen;
            }

            double logSharpe = Math.log((Double) correlators.get(value).information.get("SHARPE") + 1);
            if(logSharpe > maxLogSharpe)
            {
                maxLogSharpe = logSharpe;
            }

            double pnl = (Double)correlators.get(value).information.get("PNL");
            if(pnl < minPnL)
            {
                minPnL = pnl;
            }
            if(pnl > maxPnL)
            {
                maxPnL = pnl;
            }
        }
        catch (Exception e)
        {
            canvas.logText("ERROR IN: " + data);
        }
    }

    public void refresh() {
        /*for(VCorrelator correlator : correlators.values())
        {
            correlator.setDestX((int) canvas.random(canvas.width));
            correlator.setDestY((int) canvas.random(canvas.height));
        }*/
    }
}
