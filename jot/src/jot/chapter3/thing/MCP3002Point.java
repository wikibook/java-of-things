package jot.chapter3.thing;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.spibus.SPIDevice;

public class MCP3002Point extends Point {

    // 0, start bit, config[mode=1, channel=?], msbf=1
    private static final int CMD_BIT = 0b01101000;

    private int ssPinId;
    private int channel;

    private SPIDevice spi;
    private GPIOPin ssPin;

    private Future pollingFuture;

    public MCP3002Point(SPIDevice spi, int ssPinId, int channel) {
        super();
        this.spi = spi;
        this.ssPinId = ssPinId;
        this.channel = channel;
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

        pollingFuture = POLLING.scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                try {
                    readPresentValue();
                } catch (IOException ex) {
                    Logger.getLogger(MCP3002Point.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }

        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public boolean isEnabled() {
        return (spi != null && spi.isOpen()) 
                && (ssPin != null && ssPin.isOpen());
    }

    @Override
    public void close() {
        pollingFuture.cancel(false);
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
        return Type.AI;
    }

    protected void readPresentValue() throws IOException {
        int oldValue = presentValue.get();
        int newValue = readAnalog();
        presentValue.set(newValue);
        if (oldValue != newValue) {
            fireChanged();
        }
    }

    private int readAnalog() throws IOException {
        ByteBuffer out = ByteBuffer.allocate(2);
        ByteBuffer in = ByteBuffer.allocate(2);

        out.put((byte) (CMD_BIT | (channel << 4)));
        out.put((byte) 0);
        out.flip();
        try {
            ssPin.setValue(false);
            spi.writeAndRead(out, in);
            ssPin.setValue(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // convert 10-bits to integer 
        int high = (int) (0x0003 & in.get(0));
        int low = (int) (0x00ff & in.get(1));

        return (high << 8) + low;
    }
}
