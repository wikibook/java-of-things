package jot.chapter4.mqtt.thing;

import javax.json.Json;
import javax.json.JsonObject;

public class ChangeOfValue {

    public static final String TYPE = "cov";

    private String handlerId;
    private int pointId;
    private String pointName;
    private int presentValue;

    public ChangeOfValue(String handlerId, int pointId,
            String pointName, int presentValue) {
        this.handlerId = handlerId;
        this.pointId = pointId;
        this.pointName = pointName;
        this.presentValue = presentValue;
    }

    public ChangeOfValue(JsonObject json) {
        handlerId = json.getString("handlerId");
        pointId = json.getInt("pointId");
        pointName = json.getString("pointName");
        presentValue = json.getInt("pv");
    }

    public String getHandlerId() {
        return handlerId;
    }

    public int getPointId() {
        return pointId;
    }

    public String getPointName() {
        return pointName;
    }

    public int getPresentValue() {
        return presentValue;
    }

    @Override
    public String toString() {
        return Json.createObjectBuilder()
                .add("type", TYPE)
                .add("handlerId", handlerId)
                .add("pointId", pointId)
                .add("pointName", pointName)
                .add("pv", presentValue)
                .build().toString();
    }
}
