package com.bluesmoke.farm.service.feed;

import com.bluesmoke.farm.enumeration.Pair;
import com.bluesmoke.farm.exception.TickEditingLockedException;
import com.bluesmoke.farm.model.tickdata.PairData;
import com.bluesmoke.farm.model.tickdata.Tick;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;


public class CSVFeed extends FeedService
{
    private Map<Pair, String> feedPaths = new TreeMap<Pair, String>();

    private Map<Pair, BufferedReader> feeds = new TreeMap<Pair, BufferedReader>();
    private Map<Pair, Date> feedTimeStamp = new TreeMap<Pair, Date>();
    private Map<Pair, Date> feedNextTimeStamp = new TreeMap<Pair, Date>();
    private Map<Pair, PairData> feedData = new TreeMap<Pair, PairData>();
    private Map<Pair, PairData> feedNextData = new TreeMap<Pair, PairData>();

    private SimpleDateFormat f = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");

    private String feedsPath;

    private long totalSize = 0;
    private long readSize = 0;

    private Pair leader = null;
    private long currentTimeStamp = 0;

    public void setFeedsPath(String feedsPath)
    {
        this.feedsPath = feedsPath;
    }

    public void addPairFeed(Pair pair, String feedFileName)
    {
        feedPaths.put(pair, feedFileName);
        try
        {
            FileInputStream fstrm = new FileInputStream(feedsPath + "/" + feedFileName);
            DataInputStream instrm = new DataInputStream(fstrm);
            BufferedReader brstrm = new BufferedReader(new InputStreamReader(instrm));
            brstrm.readLine();

            System.out.println("Found feed: " + pair.name());
            feeds.put(pair, brstrm);
            totalSize += fstrm.available();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public Tick getNextTick() {

        try
        {
            while(true)
            {
                Date timeGuide = new Date();

                if(leader == null)
                {
                    for(Pair pair : feeds.keySet())
                    {
                        String line = feeds.get(pair).readLine();
                        readSize += line.length() + 1;
                        createData(pair, line);
                    }
                }
                else
                {
                    String line = feeds.get(leader).readLine();
                    if(line == null)
                    {
                        hasNext = false;
                        return null;
                    }
                    readSize += line.length() + 1;

                    feedTimeStamp.put(leader, feedNextTimeStamp.get(leader));
                    feedData.put(leader,feedNextData.get(leader));
                    createData(leader, line);
                }

                for(Pair feed : feeds.keySet())
                {
                    Date t = feedNextTimeStamp.get(feed);
                    if(t.before(timeGuide))
                    {
                        timeGuide = t;
                        leader = feed;
                    }
                }

                if(feeds.size() == feedData.size())
                {
                    break;
                }
            }

            for(Date date : feedTimeStamp.values())
            {
                if(date.getTime() > currentTimeStamp)
                {
                    currentTimeStamp = date.getTime();
                }
            }

            currentTick = new Tick(currentTimeStamp);
            try
            {
                for(PairData pairData : feedData.values())
                {
                    currentTick.addPairData(pairData);
                }
            }
            catch (TickEditingLockedException e)
            {
                e.printStackTrace();
            }
            currentTick.lock();

            return currentTick;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public void reset()
    {
        feeds = new TreeMap<Pair, BufferedReader>();
        feedTimeStamp = new TreeMap<Pair, Date>();
        feedNextTimeStamp = new TreeMap<Pair, Date>();
        feedData = new TreeMap<Pair, PairData>();
        feedNextData = new TreeMap<Pair, PairData>();

        hasNext = true;
        totalSize = 0;
        readSize = 0;

        leader = null;
        currentTimeStamp = 0;

        for(Pair pair : feedPaths.keySet())
        {
            addPairFeed(pair, feedPaths.get(pair));
        }
    }

    private void createData(Pair pair, String line) throws ParseException
    {
        String[] parts = line.split(",");
        Date t = f.parse(parts[0]);
        feedNextTimeStamp.put(pair,t);

        PairData pairData = new PairData(pair, Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), Double.parseDouble(parts[4]));

        feedNextData.put(pair, pairData);
    }


    @Override
    public double getPercentageComplete() {
        return (double)((int)((readSize * 10000)/totalSize))/100.0;
    }
}
