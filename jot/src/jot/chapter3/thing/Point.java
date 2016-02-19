package jot.chapter3.thing;

import java.util.Observable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Point extends Observable {

    public static enum Type {
        DI, DO, AI, AO
    };
    
    protected static final ScheduledExecutorService POLLING = 
            Executors.newSingleThreadScheduledExecutor();
    protected static final AtomicInteger COUNT = new AtomicInteger(0);
    
    private int id;
    private String name;
    protected AtomicInteger presentValue = new AtomicInteger();

    public Point() {
        id = COUNT.getAndIncrement();
        name = getClass().getName() + "-" + id;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
        fireChanged("name");
    }

    public String getName() {
        return name;
    }
    
    public int getPresentValue(){
        return presentValue.get();
    }

    protected void fireChanged(){
        fireChanged(null);
    }
    
    protected void fireChanged(String propertyName) {
        setChanged();
        notifyObservers(propertyName);
    }

    @Override
    public String toString() {
        return getName() + "(" + getId() + ") [type=" + getType() + ", enabled=" + isEnabled() + "]";
    }

    public abstract void open();

    public abstract boolean isEnabled();

    public abstract void close();

    public abstract Type getType();
}
