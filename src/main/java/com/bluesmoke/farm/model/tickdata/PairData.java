package com.bluesmoke.farm.model.tickdata;

import com.bluesmoke.farm.enumeration.Pair;

public class PairData {
    private Pair pair;
    private double bid;
    private double ask;
    private double mid;
    private double askVol;
    private double bidVol;

    public PairData(Pair pair, double ask, double bid, double askVol, double bidVol)
    {
        this.pair = pair;
        this.bid = bid;
        this.ask = ask;
        this.mid = (bid + ask)/2;

        this.askVol = askVol;
        this.bidVol = bidVol;
    }

    public String getPair() {
        return pair.name();
    }

    public double getBid() {
        return bid;
    }

    public double getAsk() {
        return ask;
    }

    public double getMid() {
        return mid;
    }

    public double getAskVol() {
        return askVol;
    }

    public double getBidVol() {
        return bidVol;
    }

    public String toString()
    {
        return      "{pair=" + pair + ", "
                +   "ask=" + ask + ", "
                +   "bid=" + bid + ", "
                +   "askVol=" + askVol + ", "
                +   "bidVol=" + bidVol + "}";
    }
}
