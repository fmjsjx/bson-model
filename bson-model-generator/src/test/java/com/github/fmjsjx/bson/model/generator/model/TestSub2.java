package com.github.fmjsjx.bson.model.generator.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fmjsjx.bson.model.core.BsonUtil;
import com.github.fmjsjx.bson.model.core.DefaultMapModel;
import com.github.fmjsjx.bson.model.core.DotNotation;
import com.github.fmjsjx.bson.model.core.ObjectModel;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;

public class TestSub2 extends ObjectModel<TestSub2> {

    public static final String BNAME_TEST_SUB3 = "ts3";

    private final TestSub1 parent;

    private final DefaultMapModel<Integer, TestSub3, TestSub2> testSub3 = DefaultMapModel.integerKeys(this, "ts3", TestSub3::new);

    public TestSub2(TestSub1 parent) {
        this.parent = parent;
    }

    public DefaultMapModel<Integer, TestSub3, TestSub2> getTestSub3() {
        return testSub3;
    }

    @Override
    public TestSub1 parent() {
        return parent;
    }

    @Override
    public DotNotation xpath() {
        return parent().xpath().resolve("ts2");    }

    @Override
    public boolean updated() {
        return testSub3.updated();
    }

    @Override
    public BsonDocument toBson() {
        var bson = new BsonDocument();
        bson.append("ts3", testSub3.toBson());
        return bson;
    }

    @Override
    public Document toDocument() {
        var doc = new Document();
        doc.append("ts3", testSub3.toDocument());
        return doc;
    }

    @Override
    public Map<String, ?> toData() {
        var data = new LinkedHashMap<String, Object>();
        data.put("ts3", testSub3.toData());
        return data;
    }

    @Override
    public void load(Document src) {
        BsonUtil.documentValue(src, "ts3").ifPresentOrElse(testSub3::load, testSub3::clear);
    }

    @Override
    public void load(BsonDocument src) {
        BsonUtil.documentValue(src, "ts3").ifPresentOrElse(testSub3::load, testSub3::clear);
    }

    @Override
    public void load(Any src) {
        if (src.valueType() != ValueType.OBJECT) {
            reset();
            return;
        }
        BsonUtil.objectValue(src, "ts3").ifPresentOrElse(testSub3::load, testSub3::clear);
    }

    @Override
    public void load(JsonNode src) {
        if (!src.isObject()) {
            reset();
            return;
        }
        BsonUtil.objectValue(src, "ts3").ifPresentOrElse(testSub3::load, testSub3::clear);
    }

    public boolean testSub3Updated() {
        return testSub3.updated();
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {
        var testSub3 = this.testSub3;
        if (testSub3.updated()) {
            testSub3.appendUpdates(updates);
        }
    }

    @Override
    protected void resetChildren() {
        testSub3.reset();
    }

    @Override
    public Object toSubUpdate() {
        var update = new LinkedHashMap<>();
        if (testSub3.updated()) {
            update.put("testSub3", testSub3.toUpdate());
        }
        return update;
    }

    @Override
    public Map<Object, Object> toDelete() {
        var delete = new LinkedHashMap<>();
        var testSub3 = this.testSub3;
        if (testSub3.deletedSize() > 0) {
            delete.put("testSub3", testSub3.toDelete());
        }
        return delete;
    }

    @Override
    protected int deletedSize() {
        var n = 0;
        if (testSub3.deletedSize() > 0) {
            n++;
        }
        return n;
    }

    @Override
    public String toString() {
        return "TestSub2(" + "testSub3=" + testSub3 + ")";
    }

}
