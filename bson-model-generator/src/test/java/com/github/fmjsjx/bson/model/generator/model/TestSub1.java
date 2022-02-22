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
import com.github.fmjsjx.bson.model.core.DefaultMapValueModel;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.mongodb.client.model.Updates;

public class TestSub1 extends DefaultMapValueModel<Integer, TestSub1> {

    public static final String BNAME_ID = "id";
    public static final String BNAME_TEST_SUB2 = "ts2";

    private int id;
    private final TestSub2 testSub2 = new TestSub2(this);

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (this.id != id) {
            this.id = id;
            fieldUpdated(1);
        }
    }

    public TestSub2 getTestSub2() {
        return testSub2;
    }

    @Override
    public BsonDocument toBson() {
        var bson = new BsonDocument();
        bson.append("id", new BsonInt32(id));
        bson.append("ts2", testSub2.toBson());
        return bson;
    }

    @Override
    public Document toDocument() {
        var doc = new Document();
        doc.append("id", id);
        doc.append("ts2", testSub2.toDocument());
        return doc;
    }

    @Override
    public Map<String, ?> toData() {
        var data = new LinkedHashMap<String, Object>();
        data.put("id", id);
        data.put("ts2", testSub2.toData());
        return data;
    }

    @Override
    public void load(Document src) {
        id = BsonUtil.intValue(src, "id").orElse(0);
        BsonUtil.documentValue(src, "ts2").ifPresentOrElse(testSub2::load, testSub2::reset);
    }

    @Override
    public void load(BsonDocument src) {
        id = BsonUtil.intValue(src, "id").orElse(0);
        BsonUtil.documentValue(src, "ts2").ifPresentOrElse(testSub2::load, testSub2::reset);
    }

    @Override
    public void load(Any src) {
        if (src.valueType() != ValueType.OBJECT) {
            reset();
            return;
        }
        id = BsonUtil.intValue(src, "id").orElse(0);
        BsonUtil.objectValue(src, "ts2").ifPresentOrElse(testSub2::load, testSub2::reset);
    }

    @Override
    public void load(JsonNode src) {
        if (!src.isObject()) {
            reset();
            return;
        }
        id = BsonUtil.intValue(src, "id").orElse(0);
        BsonUtil.objectValue(src, "ts2").ifPresentOrElse(testSub2::load, testSub2::reset);
    }

    public boolean idUpdated() {
        return updatedFields.get(1);
    }

    public boolean testSub2Updated() {
        return testSub2.updated();
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {
        var updatedFields = this.updatedFields;
        if (updatedFields.get(1)) {
            updates.add(Updates.set(xpath().resolve("id").value(), id));
        }
        var testSub2 = this.testSub2;
        if (testSub2.updated()) {
            testSub2.appendUpdates(updates);
        }
    }

    @Override
    protected void resetChildren() {
        testSub2.reset();
    }

    @Override
    public Object toSubUpdate() {
        var update = new LinkedHashMap<>();
        var updatedFields = this.updatedFields;
        if (updatedFields.get(1)) {
            update.put("id", id);
        }
        if (testSub2.updated()) {
            update.put("testSub2", testSub2.toUpdate());
        }
        return update;
    }

    @Override
    public Map<Object, Object> toDelete() {
        var delete = new LinkedHashMap<>();
        var testSub2 = this.testSub2;
        if (testSub2.deletedSize() > 0) {
            delete.put("testSub2", testSub2.toDelete());
        }
        return delete;
    }

    @Override
    protected int deletedSize() {
        var n = 0;
        if (testSub2.deletedSize() > 0) {
            n++;
        }
        return n;
    }

    @Override
    public String toString() {
        return "TestSub1(" + "id=" + id + ", " + "testSub2=" + testSub2 + ")";
    }

}
