package jot.chapter3.thing;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.PinEvent;
import jdk.dio.gpio.PinListener;

public class GPIOPinPoint extends Point {

    private int pinId;
    private GPIOPin pin;

    public GPIOPinPoint(int pinId) {
        super();
        this.pinId = pinId;
    }

    @Override
    public void open() {
        try {
            pin = (GPIOPin) DeviceManager.open(pinId);
            presentValue.set(pin.getValue() ? 1 : 0); // initialize
        } catch (IOException ex) {
            Logger.getLogger(GPIOPinPoint.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        try {
            pin.setInputListener(new PinListener() {

                @Override
                public void valueChanged(PinEvent event) {
                    int oldValue = presentValue.get();
                    int newValue = event.getValue() ? 1 : 0;
                    presentValue.set(newValue);
                    if (oldValue != newValue) {
                        fireChanged();
                    }
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(GPIOPinPoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean isEnabled() {
        return (pin != null && pin.isOpen());
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
        return Type.DI;
    }
}
