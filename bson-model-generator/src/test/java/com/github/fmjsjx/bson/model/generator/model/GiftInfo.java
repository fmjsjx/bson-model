package com.github.fmjsjx.bson.model.generator.model;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fmjsjx.bson.model.core.BsonUtil;
import com.github.fmjsjx.bson.model.core.DefaultListValueModel;
import com.github.fmjsjx.libcommon.util.DateTimeUtil;
import com.github.fmjsjx.libcommon.util.ObjectUtil;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.mongodb.client.model.Updates;

public class GiftInfo extends DefaultListValueModel<GiftInfo> {

    public static final String BNAME_ID = "id";
    public static final String BNAME_PRICE = "prc";
    public static final String BNAME_CREATE_TIME = "ct";

    private int id;
    private int price;
    @JsonIgnore
    private LocalDateTime createTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (this.id != id) {
            this.id = id;
            updatedFields.set(1);
            emitUpdated();
        }
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        if (this.price != price) {
            this.price = price;
            updatedFields.set(2);
            emitUpdated();
        }
    }

    @JsonIgnore
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        if (ObjectUtil.isNotEquals(this.createTime, createTime)) {
            this.createTime = createTime;
            updatedFields.set(3);
            emitUpdated();
        }
    }

    @Override
    public BsonDocument toBson() {
        var bson = new BsonDocument();
        bson.append("id", new BsonInt32(id));
        bson.append("prc", new BsonInt32(price));
        if (createTime != null) {
            bson.append("ct", BsonUtil.toBsonDateTime(createTime));
        }
        return bson;
    }

    @Override
    public Document toDocument() {
        var doc = new Document();
        doc.append("id", id);
        doc.append("prc", price);
        if (createTime != null) {
            doc.append("ct", DateTimeUtil.toLegacyDate(createTime));
        }
        return doc;
    }

    @Override
    public Map<String, ?> toData() {
        var data = new LinkedHashMap<String, Object>();
        data.put("id", id);
        data.put("prc", price);
        if (createTime != null) {
            data.put("ct", DateTimeUtil.toEpochMilli(createTime));
        }
        return data;
    }

    @Override
    public void load(Document src) {
        id = BsonUtil.intValue(src, "id").orElse(0);
        price = BsonUtil.intValue(src, "prc").orElse(0);
        createTime = BsonUtil.dateTimeValue(src, "ct").orElse(null);
    }

    @Override
    public void load(BsonDocument src) {
        id = BsonUtil.intValue(src, "id").orElse(0);
        price = BsonUtil.intValue(src, "prc").orElse(0);
        createTime = BsonUtil.dateTimeValue(src, "ct").orElse(null);
    }

    @Override
    public void load(Any src) {
        if (src.valueType() != ValueType.OBJECT) {
            reset();
            return;
        }
        id = BsonUtil.intValue(src, "id").orElse(0);
        price = BsonUtil.intValue(src, "prc").orElse(0);
        createTime = BsonUtil.dateTimeValue(src, "ct").orElse(null);
    }

    @Override
    public void load(JsonNode src) {
        if (!src.isObject()) {
            reset();
            return;
        }
        id = BsonUtil.intValue(src, "id").orElse(0);
        price = BsonUtil.intValue(src, "prc").orElse(0);
        createTime = BsonUtil.dateTimeValue(src, "ct").orElse(null);
    }

    public boolean idUpdated() {
        return updatedFields.get(1);
    }

    public boolean priceUpdated() {
        return updatedFields.get(2);
    }

    public boolean createTimeUpdated() {
        return updatedFields.get(3);
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {
        var updatedFields = this.updatedFields;
        if (updatedFields.get(1)) {
            updates.add(Updates.set(xpath().resolve("id").value(), id));
        }
        if (updatedFields.get(2)) {
            updates.add(Updates.set(xpath().resolve("prc").value(), price));
        }
        if (updatedFields.get(3)) {
            updates.add(Updates.set(xpath().resolve("ct").value(), BsonUtil.toBsonDateTime(createTime)));
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
            update.put("id", id);
        }
        if (updatedFields.get(2)) {
            update.put("price", price);
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
        return "GiftInfo(" + "id=" + id + ", " + "price=" + price + ", " + "createTime=" + createTime + ")";
    }

}
