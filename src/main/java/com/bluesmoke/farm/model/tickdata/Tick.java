package com.bluesmoke.farm.model.tickdata;

import com.bluesmoke.farm.exception.TickEditingLockedException;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class Tick {
    private boolean locked = false;
    private long timeStamp;
    private HashMap<String, PairData> pairsData = new HashMap<String, PairData>();
    private HashSet<NewsData> newsData = new HashSet<NewsData>();

    public Tick(long timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    public void addPairData(PairData pairData) throws TickEditingLockedException {
        if(locked)
        {
            throw new TickEditingLockedException();
        }
        else
        {
            pairsData.put(pairData.getPair(), pairData);
        }
    }

    public void addNewsData(String title, String news) throws TickEditingLockedException {
        if(locked)
        {
            throw new TickEditingLockedException();
        }
        else
        {
            newsData.add(new NewsData(title, news));
        }
    }

    public PairData getPairData(String pair)
    {
        return pairsData.get(pair);
    }

    public Iterator getNews()
    {
        return newsData.iterator();
    }

    public void lock()
    {
        locked = true;
    }

    public String toString()
    {
        return "{timeStamp=" + timeStamp + ", pairsData=" + pairsData.values() + "}";
    }

    public Date getTimeStamp()
    {
        return new Date(timeStamp);
    }
}
