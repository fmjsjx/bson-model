package com.github.fmjsjx.bson.model.generator.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fmjsjx.bson.model.core.BsonUtil;
import com.github.fmjsjx.bson.model.core.DotNotation;
import com.github.fmjsjx.bson.model.core.ObjectModel;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.mongodb.client.model.Updates;

public class TestSub4 extends ObjectModel<TestSub4> {

    public static final String BNAME_TEST = "tst";

    private final TestSub3 parent;

    private int test;

    public TestSub4(TestSub3 parent) {
        this.parent = parent;
    }

    public int getTest() {
        return test;
    }

    public void setTest(int test) {
        if (this.test != test) {
            this.test = test;
            fieldUpdated(1);
        }
    }

    public int increaseTest() {
        var test = this.test += 1;
        fieldUpdated(1);
        return test;
    }

    @Override
    public TestSub3 parent() {
        return parent;
    }

    @Override
    public DotNotation xpath() {
        return parent().xpath().resolve("ts4");    }

    @Override
    public BsonDocument toBson() {
        var bson = new BsonDocument();
        bson.append("tst", new BsonInt32(test));
        return bson;
    }

    @Override
    public Document toDocument() {
        var doc = new Document();
        doc.append("tst", test);
        return doc;
    }

    @Override
    public Map<String, ?> toData() {
        var data = new LinkedHashMap<String, Object>();
        data.put("tst", test);
        return data;
    }

    @Override
    public void load(Document src) {
        test = BsonUtil.intValue(src, "tst").orElse(0);
    }

    @Override
    public void load(BsonDocument src) {
        test = BsonUtil.intValue(src, "tst").orElse(0);
    }

    @Override
    public void load(Any src) {
        if (src.valueType() != ValueType.OBJECT) {
            reset();
            return;
        }
        test = BsonUtil.intValue(src, "tst").orElse(0);
    }

    @Override
    public void load(JsonNode src) {
        if (!src.isObject()) {
            reset();
            return;
        }
        test = BsonUtil.intValue(src, "tst").orElse(0);
    }

    public boolean testUpdated() {
        return updatedFields.get(1);
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {
        var updatedFields = this.updatedFields;
        if (updatedFields.get(1)) {
            updates.add(Updates.set(xpath().resolve("tst").value(), test));
        }
    }

    @Override
    protected void resetChildren() {
    }

    @Override
    public Object toSubUpdate() {
        var update = new LinkedHashMap<>();
        var updatedFields = this.updatedFields;
        if (updatedFields.get(1)) {
            update.put("test", test);
        }
        return update;
    }

    @Override
    public Map<Object, Object> toDelete() {
        return Map.of();
    }

    @Override
    protected int deletedSize() {
        return 0;
    }

    @Override
    public String toString() {
        return "TestSub4(" + "test=" + test + ")";
    }

}
