package jot.chapter3.thing;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;

public class GPIOPinOutputPoint extends OutputPoint {

    private int pinId;
    private GPIOPin pin;

    public GPIOPinOutputPoint(int pinId) {
        super();
        this.pinId = pinId;
    }

    @Override
    public void open() {
        try {
            pin = (GPIOPin) DeviceManager.open(pinId);
        } catch (IOException ex) {
            Logger.getLogger(GPIOPinOutputPoint.class.getName())
                    .log(Level.SEVERE, null, ex);
            return;
        }

        setPresentValue(0); // initialize
    }

    @Override
    public boolean isEnabled() {
        return pin != null && pin.isOpen();
    }

    @Override
    public void close() {
        if (pin != null && pin.isOpen()) {
            try {
                pin.close();
            } catch (IOException ignored) {
            }
            pin = null;
        }
    }

    @Override
    public Type getType() {
        return Type.DO;
    }

    @Override
    public synchronized void setPresentValue(int value) {
        int oldValue = presentValue.get();
        
        if(writeDigital(value) && oldValue != presentValue.get())
            fireChanged();
    }
    
    private boolean writeDigital(int value){
        try {
            pin.setValue((value == 1));
            presentValue.set(value);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(GPIOPinOutputPoint.class.getName())
                    .log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
