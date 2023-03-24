package com.github.fmjsjx.bson.model2.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fmjsjx.bson.model2.core.ObjectModel;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import java.util.List;

public class Equipment extends ObjectModel<Equipment> {

    @Override
    protected void resetChildren() {

    }

    @Override
    protected int deletedSize() {
        return 0;
    }

    @Override
    protected Object toSubUpdateData() {
        return null;
    }

    @Override
    public Equipment clean() {
        return null;
    }

    @Override
    public BsonDocument toBson() {
        return null;
    }

    @Override
    public void load(BsonDocument src) {

    }

    @Override
    public JsonNode toJsonNode() {
        return null;
    }

    @Override
    public Object toData() {
        return null;
    }

    @Override
    public Object toDeletedData() {
        return null;
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {

    }

    @Override
    protected void loadObjectNode(ObjectNode src) {

    }
}
