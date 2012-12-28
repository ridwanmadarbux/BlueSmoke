package com.bluesmoke.farm.exception;

public class IllegalStateValueDataModificationException extends Exception {

    @Override
    public void printStackTrace()
    {
        System.err.println("An unauthorised entity tried to modify the state value pair");
    }
}
