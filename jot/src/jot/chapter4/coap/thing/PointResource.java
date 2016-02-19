package jot.chapter4.coap.thing;

import java.io.StringReader;
import java.util.Observable;
import java.util.Observer;
import javax.json.Json;
import javax.json.JsonObject;
import jot.chapter3.thing.Point;
import jot.chapter3.thing.OutputPoint;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class PointResource extends CoapResource {

    private Point point;

    public PointResource(Point point) {
        super(String.valueOf(point.getId()));
        this.point = point;
        addChildResource();
    }

    private void addChildResource() {
        add(new CoapResource("properties") {

            private CoapResource intialize() {
                setObservable(true);
                setObserveType(CoAP.Type.CON);
                getAttributes().setObservable();
                point.addObserver(new Observer() {

                    @Override
                    public void update(Observable o, Object arg) {
                        if (arg != null) {
                            changed();
                        }
                    }
                });
                
                return this;
            }

            @Override
            public void handleGET(CoapExchange exchange) {
                JsonObject json = Json.createObjectBuilder()
                        .add("Id", point.getId())
                        .add("Type", point.getType().name())
                        .add("Name", point.getName())
                        .add("Enabled", point.isEnabled())
                        .build();
                exchange.respond(json.toString());
            }

            @Override
            public void handlePOST(CoapExchange exchange) {
                String jsonStr = exchange.getRequestText();
                if (jsonStr != null) {
                    try {
                        JsonObject json
                                = Json.createReader(
                                        new StringReader(jsonStr))
                                .readObject();
                        point.setName(json.getString("Name"));
                        exchange.respond("true");
                    } catch (Throwable ex) {
                        exchange.respond("false");
                    }
                } else {
                    exchange.respond("false");
                }

            }
        }.intialize());

        add(new CoapResource("presentValue") {

            private CoapResource intialize() {
                setObservable(true);
                setObserveType(CoAP.Type.CON);
                getAttributes().setObservable();
                point.addObserver(new Observer() {

                    @Override
                    public void update(Observable o, Object arg) {
                        if (arg == null) {
                            changed();
                        }
                    }
                });
                
                return this;
            }

            @Override
            public void handleGET(CoapExchange exchange) {
                exchange.respond(
                        String.valueOf(point.getPresentValue()));
            }

            @Override
            public void handlePOST(CoapExchange exchange) {
                if (point instanceof OutputPoint) {
                    try {
                        int presentValue = 
                                Integer.parseInt(exchange.getRequestText());
                        ((OutputPoint) point)
                                .setPresentValue(presentValue);
                        exchange.respond("true");
                    } catch (Throwable ex) {
                        exchange.respond("false");
                    }
                } else {
                    exchange.respond("false");
                }
            }
        }.intialize());
    }
}
