package com.bluesmoke.farm.model.correlatordata;

import com.bluesmoke.farm.exception.IllegalStateValueDataModificationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

public class StateValueData {

    //TODO ordered list of state component type

    final private String state;
    final private double res;
    final private String passCode;

    final private List<String> stateComponents;
    final private int stateComponentNumber;

    private double sum = 0;
    private double sum2 = 0;
    private long count = 0;

    private double average = 0;
    private double sdev = 0;
    private double sharpe = 0;

    private TreeMap<Integer,TreeMap<Integer, Long>> dist = new TreeMap<Integer, TreeMap<Integer, Long>>();

    public StateValueData(String state, double res, String passCode)
    {
        this.state = state;
        stateComponents = Arrays.asList(state.split(","));
        stateComponentNumber = stateComponents.size();
        this.res = res;
        this.passCode = passCode;
    }

    public void addObservedResult(double result, int timeElapsed, String passCode) throws IllegalStateValueDataModificationException {
        if(!this.passCode.equals(passCode))
        {
            throw new IllegalStateValueDataModificationException();
        }
        sum += result;
        sum2 += (result*result);

        count ++;

        average = sum/count;
        double variance = Math.abs(sum2/count - average*average);
        sdev = Math.sqrt(variance);
        sharpe = average*average / variance;

        int timeClass = (int)(Math.log10(timeElapsed));
        int distClass = (int)(result/res);
        if(!dist.containsKey(timeClass))
        {
            dist.put(timeClass, new TreeMap<Integer, Long>());
        }
        if(!dist.get(timeClass).containsKey(distClass))
        {
            dist.get(timeClass).put(distClass, 0L);
        }
        dist.get(timeClass).put(distClass, dist.get(timeClass).get(distClass) + 1);
    }

    public String getState() {
        return state;
    }

    public double getRes() {
        return res;
    }

    public double getCount() {
        return count;
    }

    public double getAverage() {
        return average;
    }

    public double getSDev() {
        return sdev;
    }

    public double getSharpe() {
        return sharpe;
    }

    public String getStateComponent(int index)
    {
        return stateComponents.get(index);
    }

    public int getStateComponentNumber()
    {
        return stateComponentNumber;
    }

    public TreeMap<Integer,TreeMap<Integer, Long>> getDist() {
        return dist;
    }
}
