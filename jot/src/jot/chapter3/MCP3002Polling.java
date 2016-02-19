package jot.chapter3;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.spibus.SPIDevice;

public class MCP3002Polling implements Runnable{
    
    // 0, start bit, config[mode=1, channel=?], msbf=1
    private static final int CMD_BIT = 0b01101000;
    
    private SPIDevice spi;
    private GPIOPin ssPin;
    private int channel;

    public MCP3002Polling(int spiId, int ssPinId, int channel) throws IOException {
        spi = (SPIDevice) DeviceManager.open(spiId);
        ssPin = (GPIOPin) DeviceManager.open(ssPinId);
        this.channel = channel;
    }
    
    public void start(){
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture future = executor.scheduleWithFixedDelay(this, 0, 1,TimeUnit.SECONDS);
        executor.schedule(new Runnable(){

            @Override
            public void run(){
                future.cancel(false);
                executor.shutdown();
                try {
                    close();
                } catch (IOException ignored) {}
            }
        
        }, 10, TimeUnit.SECONDS);
    }

    public void run() {
        try {
            System.out.println("Channel(" + channel + "): " + readAnalog(channel));
        } catch (IOException ex) {
            System.out.println("Cannot read Channel(" + channel + ")");
        }
    }

    private int readAnalog(int channel) throws IOException {
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
    
    private void close() throws IOException {
        spi.close();
        ssPin.close();
    }

    public static void main(String[] args) throws Exception {
        MCP3002Polling mcp3002 = new MCP3002Polling(300, 7, 0);
        mcp3002.start();
    }
}
