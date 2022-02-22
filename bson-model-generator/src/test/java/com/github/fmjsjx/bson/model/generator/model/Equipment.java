package com.github.fmjsjx.bson.model.generator.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fmjsjx.bson.model.core.BsonUtil;
import com.github.fmjsjx.bson.model.core.DefaultMapValueModel;
import com.github.fmjsjx.libcommon.util.ObjectUtil;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.mongodb.client.model.Updates;

public class Equipment extends DefaultMapValueModel<String, Equipment> {

    public static final String BNAME_ID = "id";
    public static final String BNAME_REF_ID = "rid";
    public static final String BNAME_ATK = "atk";
    public static final String BNAME_DEF = "def";
    public static final String BNAME_HP = "hp";

    private String id = "";
    private int refId;
    private int atk;
    private int def;
    private int hp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (ObjectUtil.isNotEquals(this.id, id)) {
            this.id = id;
            fieldUpdated(1);
        }
    }

    public int getRefId() {
        return refId;
    }

    public void setRefId(int refId) {
        if (this.refId != refId) {
            this.refId = refId;
            fieldUpdated(2);
        }
    }

    public int getAtk() {
        return atk;
    }

    public void setAtk(int atk) {
        if (this.atk != atk) {
            this.atk = atk;
            fieldUpdated(3);
        }
    }

    public int getDef() {
        return def;
    }

    public void setDef(int def) {
        if (this.def != def) {
            this.def = def;
            fieldUpdated(4);
        }
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        if (this.hp != hp) {
            this.hp = hp;
            fieldUpdated(5);
        }
    }

    @Override
    public BsonDocument toBson() {
        var bson = new BsonDocument();
        bson.append("id", new BsonString(id));
        bson.append("rid", new BsonInt32(refId));
        bson.append("atk", new BsonInt32(atk));
        bson.append("def", new BsonInt32(def));
        bson.append("hp", new BsonInt32(hp));
        return bson;
    }

    @Override
    public Document toDocument() {
        var doc = new Document();
        doc.append("id", id);
        doc.append("rid", refId);
        doc.append("atk", atk);
        doc.append("def", def);
        doc.append("hp", hp);
        return doc;
    }

    @Override
    public Map<String, ?> toData() {
        var data = new LinkedHashMap<String, Object>();
        data.put("id", id);
        data.put("rid", refId);
        data.put("atk", atk);
        data.put("def", def);
        data.put("hp", hp);
        return data;
    }

    @Override
    public void load(Document src) {
        id = BsonUtil.stringValue(src, "id").get();
        refId = BsonUtil.intValue(src, "rid").getAsInt();
        atk = BsonUtil.intValue(src, "atk").getAsInt();
        def = BsonUtil.intValue(src, "def").getAsInt();
        hp = BsonUtil.intValue(src, "hp").getAsInt();
    }

    @Override
    public void load(BsonDocument src) {
        id = BsonUtil.stringValue(src, "id").get();
        refId = BsonUtil.intValue(src, "rid").getAsInt();
        atk = BsonUtil.intValue(src, "atk").getAsInt();
        def = BsonUtil.intValue(src, "def").getAsInt();
        hp = BsonUtil.intValue(src, "hp").getAsInt();
    }

    @Override
    public void load(Any src) {
        if (src.valueType() != ValueType.OBJECT) {
            reset();
            return;
        }
        id = BsonUtil.stringValue(src, "id").get();
        refId = BsonUtil.intValue(src, "rid").getAsInt();
        atk = BsonUtil.intValue(src, "atk").getAsInt();
        def = BsonUtil.intValue(src, "def").getAsInt();
        hp = BsonUtil.intValue(src, "hp").getAsInt();
    }

    @Override
    public void load(JsonNode src) {
        if (!src.isObject()) {
            reset();
            return;
        }
        id = BsonUtil.stringValue(src, "id").get();
        refId = BsonUtil.intValue(src, "rid").getAsInt();
        atk = BsonUtil.intValue(src, "atk").getAsInt();
        def = BsonUtil.intValue(src, "def").getAsInt();
        hp = BsonUtil.intValue(src, "hp").getAsInt();
    }

    public boolean idUpdated() {
        return updatedFields.get(1);
    }

    public boolean refIdUpdated() {
        return updatedFields.get(2);
    }

    public boolean atkUpdated() {
        return updatedFields.get(3);
    }

    public boolean defUpdated() {
        return updatedFields.get(4);
    }

    public boolean hpUpdated() {
        return updatedFields.get(5);
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {
        var updatedFields = this.updatedFields;
        if (updatedFields.get(1)) {
            updates.add(Updates.set(xpath().resolve("id").value(), id));
        }
        if (updatedFields.get(2)) {
            updates.add(Updates.set(xpath().resolve("rid").value(), refId));
        }
        if (updatedFields.get(3)) {
            updates.add(Updates.set(xpath().resolve("atk").value(), atk));
        }
        if (updatedFields.get(4)) {
            updates.add(Updates.set(xpath().resolve("def").value(), def));
        }
        if (updatedFields.get(5)) {
            updates.add(Updates.set(xpath().resolve("hp").value(), hp));
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
            update.put("refId", refId);
        }
        if (updatedFields.get(3)) {
            update.put("atk", atk);
        }
        if (updatedFields.get(4)) {
            update.put("def", def);
        }
        if (updatedFields.get(5)) {
            update.put("hp", hp);
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
        return "Equipment(" + "id=" + id + ", " + "refId=" + refId + ", " + "atk=" + atk + ", " + "def=" + def + ", " + "hp=" + hp + ")";
    }

}
