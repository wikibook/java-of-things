package jot.chapter3;

import java.io.IOException;
import java.nio.ByteBuffer;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.spibus.SPIDevice;

public class MCP4911 {

    private static final int CMD_BIT = 0b0011000000000000;

    private SPIDevice spi;
    private GPIOPin ssPin;

    public MCP4911(int spiId, int ssPinId) throws IOException {
        spi = (SPIDevice) DeviceManager.open(spiId);
        ssPin = (GPIOPin) DeviceManager.open(ssPinId);
    }

    public void run() throws IOException, InterruptedException {
        int[] number = {700, 900, 1023};
        for (int i = 0; i < number.length; i++) {
            System.out.println(number[i] + " --------------------");
            writeAnalog(number[i]);
            Thread.sleep(2000);
        }

        clear();
        close();
    }

    private void writeAnalog(int number) throws IOException, InterruptedException {
        System.out.println("input->" + Integer.toBinaryString(number));
        number = number << 2;
        System.out.println("shift->" + Integer.toBinaryString(number));
        int data = CMD_BIT | number;
        System.out.println("data->" + Integer.toBinaryString(data));
        ByteBuffer out = ByteBuffer.allocate(2);
        out.put((byte) ((data & 0xff00) >> 8));
        out.put((byte) (data & 0xff));
        out.flip();
        ssPin.setValue(false);
        spi.write(out);
        ssPin.setValue(true);
    }

    private void clear() throws IOException, InterruptedException {
        writeAnalog(0);
    }

    private void close() throws IOException {
        spi.close();
        ssPin.close();
    }

    public static void main(String[] args) throws Exception {
        MCP4911 mcp4911 = new MCP4911(300, 8);
        mcp4911.run();
    }
}
