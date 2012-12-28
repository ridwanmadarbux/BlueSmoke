package com.bluesmoke.farm.util;

public class ListElement<T> {
    private T data;
    private ListElement<T> previous;
    private ListElement<T> next;

    private ListElement<T> head;
    private ListElement<T> tail;

    public ListElement(T data, ListElement<T> previous, ListElement<T> next)
    {
        this.data = data;

        setPrevious(previous);
        setNext(next);

        if(previous != null)
        {
            previous.setNext(this);
        }
        if(next != null)
        {
            next.setPrevious(this);
        }

        if(previous == null)
        {
            tail = this;
        }
        else {
            tail = previous.getTail();
        }

        if(next == null)
        {
            head = this;
        }
        else {
            head = next.getHead();
        }
    }

    public T getData()
    {
        return data;
    }

    public void setPrevious(ListElement<T> previous)
    {
        this.previous = previous;
    }

    public void setNext(ListElement<T> next)
    {
        this.next = next;
    }

    public void setHead(ListElement<T> head)
    {
        this.head = head;
    }

    public void setTail(ListElement<T> tail)
    {
        this.tail = tail;
    }

    public ListElement<T> getNext()
    {
        return next;
    }

    public ListElement<T> getPrevious()
    {
        return previous;
    }

    public ListElement<T> getHead()
    {
        return head;
    }

    public ListElement<T> getTail()
    {
        return tail;
    }

    public void delete()
    {
        if(next != null)
        {
            next.setPrevious(previous);
        }
        if(previous != null)
        {
            previous.setNext(next);
        }

        if(previous == null)
        {
            next.setTail(next);
        }

        if(next == null)
        {
            previous.setHead(previous);
        }
    }
}
