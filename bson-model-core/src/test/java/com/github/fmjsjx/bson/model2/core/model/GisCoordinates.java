package com.github.fmjsjx.bson.model2.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fmjsjx.bson.model.core.BsonUtil;
import com.github.fmjsjx.bson.model2.core.ObjectModel;
import com.mongodb.client.model.Updates;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.conversions.Bson;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class GisCoordinates extends ObjectModel<GisCoordinates> {

    public static final String BNAME_LONGITUDE = "lo";
    public static final String BNAME_LATITUDE = "la";
    public static final String BNAME_HEIGHT = "h";

    private double longitude;
    private double latitude;
    private Double height;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        if (this.longitude != longitude) {
            this.longitude = longitude;
            fieldChanged(0);
        }
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        if (this.latitude != latitude) {
            this.latitude = latitude;
            fieldChanged(1);
        }
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        if (!Objects.equals(this.height, height)) {
            this.height = height;
            fieldChanged(2);
        }
    }

    @Override
    protected void resetChildren() {
    }

    @Override
    protected int deletedSize() {
        var n = 0;
        var changeFields = this.changedFields;
        if (changeFields.get(2) && this.height == null) {
            n++;
        }
        return n;
    }

    @Override
    protected Object toSubUpdateData() {
        var data = new LinkedHashMap<>();
        var changeFields = this.changedFields;
        if (changeFields.get(0)) {
            data.put("longitude", longitude);
        }
        if (changeFields.get(1)) {
            data.put("latitude", latitude);
        }
        var height = this.height;
        if (changeFields.get(2) && height != null) {
            data.put("height", height);
        }
        return data;
    }

    @Override
    public BsonDocument toBson() {
        var bson = new BsonDocument();
        bson.append(BNAME_LONGITUDE, new BsonDouble(longitude));
        bson.append(BNAME_LATITUDE, new BsonDouble(latitude));
        var height = this.height;
        if (height != null) {
            bson.append(BNAME_HEIGHT, new BsonDouble(height));
        }
        return bson;
    }

    @Override
    public void load(BsonDocument src) {
        resetStates();
        longitude = BsonUtil.doubleValue(src, BNAME_LONGITUDE).orElseThrow();
        latitude = BsonUtil.doubleValue(src, BNAME_LATITUDE).orElseThrow();
        var height = BsonUtil.doubleValue(src, BNAME_HEIGHT);
        if (height.isPresent()) {
            this.height = height.getAsDouble();
        } else {
            this.height = null;
        }
    }

    @Override
    public JsonNode toJsonNode() {
        var jsonNode = JsonNodeFactory.instance.objectNode();
        jsonNode.put(BNAME_LONGITUDE, longitude);
        jsonNode.put(BNAME_LATITUDE, latitude);
        var height = this.height;
        if (height != null) {
            jsonNode.put(BNAME_HEIGHT, height);
        }
        return jsonNode;
    }

    @Override
    public Object toData() {
        var data = new LinkedHashMap<>();
        data.put("longitude", longitude);
        data.put("latitude", latitude);
        var height = this.height;
        if (height != null) {
            data.put("height", height);
        }
        return data;
    }

    @Override
    public Object toDeletedData() {
        var data = new LinkedHashMap<>();
        var changeFields = this.changedFields;
        if (changeFields.get(2) && this.height == null) {
            data.put("height", 1);
        }
        return data;
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {
        var changeFields = this.changedFields;
        if (changeFields.get(0)) {
            updates.add(Updates.set(path().resolve(BNAME_LONGITUDE).value(), longitude));
        }
        if (changeFields.get(1)) {
            updates.add(Updates.set(path().resolve(BNAME_LATITUDE).value(), latitude));
        }
        var height = this.height;
        if (changeFields.get(2)) {
            if (height == null) {
                updates.add(Updates.unset(path().resolve(BNAME_HEIGHT).value()));
            } else {
                updates.add(Updates.set(path().resolve(BNAME_HEIGHT).value(), height));
            }
        }
    }

    @Override
    protected void loadObjectNode(ObjectNode src) {
        resetStates();
        longitude = BsonUtil.doubleValue(src, BNAME_LONGITUDE).orElseThrow();
        latitude = BsonUtil.doubleValue(src, BNAME_LATITUDE).orElseThrow();
        var height = BsonUtil.doubleValue(src, BNAME_HEIGHT);
        this.height = height.isPresent() ? height.getAsDouble() : null;
    }

    @Override
    public String toString() {
        return "GisCoordinates(" + "longitude=" + longitude + ", " + "latitude=" + latitude + ", " + "height=" + height + ")";
    }

}
