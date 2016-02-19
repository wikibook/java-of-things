package jot.chapter4.coap.thing;

import java.util.Collection;

import jot.chapter3.thing.Point;
import jot.chapter3.thing.PointHandler;

import org.eclipse.californium.core.CoapServer;

public class PointCoapServer extends CoapServer {

    @Override
    public void start() {
        PointHandler pointHandler = PointHandler.getInstance();
        pointHandler.start();
        Collection<Point> points = pointHandler.getPoints();
        for (Point point : points) {
            this.add(new PointResource(point));
        }

        super.start();
    }

    @Override
    public void stop() {
        PointHandler.getInstance().stop();
        super.stop();
    }
}
