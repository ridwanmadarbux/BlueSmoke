package com.bluesmoke.farm.exception;

public class TickEditingLockedException extends Exception {

    @Override
    public void printStackTrace()
    {
        System.err.println("Tick has been finalised and cannot be edited");
    }
}
