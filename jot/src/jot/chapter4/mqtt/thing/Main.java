package jot.chapter4.mqtt.thing;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jot.chapter3.thing.Point;
import jot.chapter3.thing.PointHandler;

public class Main {
    public static void main(String[] args) throws IOException {
        try {
            PointHandler pointHandler = PointHandler.getInstance();
            pointHandler.start();
            MqttConsoleHandler console = new MqttConsoleHandler();
            for(Point point: pointHandler.getPoints()){
                point.addObserver(console);
            }
            
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    pointHandler.stop();
                    console.close();
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
}
