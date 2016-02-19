package jot.chapter3.thing;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.spibus.SPIDevice;

public class MCP4911OutputPoint extends OutputPoint {

    private static final int CMD_BIT = 0b0011000000000000;

    private int ssPinId;

    private SPIDevice spi;
    private GPIOPin ssPin;

    public MCP4911OutputPoint(SPIDevice spi, int ssPinId) {
        super();
        this.spi = spi;
        this.ssPinId = ssPinId;
    }

    @Override
    public void open() {
        try {
            ssPin = (GPIOPin) DeviceManager.open(ssPinId);
        } catch (IOException ex) {
            Logger.getLogger(MCP3002Point.class.getName())
                    .log(Level.SEVERE, null, ex);
            return;
        }

        setPresentValue(0); // initialize
    }

    @Override
    public boolean isEnabled() {
        return (spi != null && spi.isOpen())
                && (ssPin != null && ssPin.isOpen());
    }

    @Override
    public void close() {
        if (spi != null && spi.isOpen()) {
            try {
                spi.close();
            } catch (IOException ignored) {
            }
            spi = null;
        }

        if (ssPin != null && ssPin.isOpen()) {
            try {
                ssPin.close();
            } catch (IOException ignored) {
            }
            ssPin = null;
        }
    }

    @Override
    public Type getType() {
        return Type.AO;
    }

    @Override
    public synchronized void setPresentValue(int value) {
        int oldValue = presentValue.get();
        if (writeAnalog(value) && oldValue != presentValue.get()) {
            fireChanged();
        }
    }

    private boolean writeAnalog(int value) {
        value = value << 2;
        int data = CMD_BIT | value;
        
        ByteBuffer out = ByteBuffer.allocate(2);
        out.put((byte) ((data & 0xff00) >> 8));
        out.put((byte) (data & 0xff));
        out.flip();
        try {
            ssPin.setValue(false);
            spi.write(out);
            ssPin.setValue(true);
            
            presentValue.set(value);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(MCP4911OutputPoint.class.getName())
                    .log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
