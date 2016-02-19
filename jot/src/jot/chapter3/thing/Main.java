package jot.chapter3.thing;

import java.io.IOException;
import jdk.dio.uart.UARTConfig;

public class Main {
    public static void main(String[] args) throws IOException {
        PointHandler pointHandler = PointHandler.getInstance();
        pointHandler.start();
        UARTConfig config = new UARTConfig(
                "ttyAMA0",
                1,
                9600,
                UARTConfig.DATABITS_8,
                UARTConfig.PARITY_NONE,
                UARTConfig.STOPBITS_1,
                UARTConfig.FLOWCONTROL_NONE
        );
        UARTConsole console = new UARTConsole(config);
        for(Point point: pointHandler.getPoints()){
            point.addObserver(console);
        }
        console.run();
        pointHandler.stop();
    }
}
