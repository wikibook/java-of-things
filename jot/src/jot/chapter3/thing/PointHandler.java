package jot.chapter3.thing;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.dio.DeviceManager;
import jdk.dio.spibus.SPIDevice;

public class PointHandler {

    private static AtomicReference<PointHandler> instance = 
            new AtomicReference<>();
    
    public static PointHandler getInstance(){
        if(instance.get() == null)
            instance.set(new PointHandler());
        
        return instance.get();
    }
    
    private Map<Integer, Point> points = new HashMap<Integer, Point>();

    private PointHandler(){}
    
    public void start() {
        createPoints();
        openPoints();
    }

    private void createPoints() {
        Point point = new GPIOPinPoint(23); // Push Button
        points.put(point.getId(), point);

        point = new GPIOPinPoint(24); // Push Button
        points.put(point.getId(), point);

        point = new GPIOPinOutputPoint(18); // LED
        points.put(point.getId(), point);

        try {
            SPIDevice spi = (SPIDevice) DeviceManager.open(300);
            point = new MCP3002Point(spi, 7, 0); // MCP3002, channel 0
            points.put(point.getId(), point);

            point = new MCP4911OutputPoint(spi, 8); // MCP4911
            points.put(point.getId(), point);
        } catch (IOException ex) {
            Logger.getLogger(PointHandler.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    private void openPoints() {
        for (Point point : points.values()) {
            point.open();
        }
    }

    public void stop() {
        for (Point point : points.values()) {
            point.close();
        }
        points.clear();
        Point.POLLING.shutdown();
    }

    public Collection<Point> getPoints() {
        return Collections.unmodifiableCollection(points.values());
    }

    public Point getPoint(int pointId) {
        return points.get(pointId);
    }
}
