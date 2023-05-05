package com.github.fmjsjx.bson.model2.generator.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.fmjsjx.bson.model.core.BsonUtil;
import com.github.fmjsjx.bson.model2.core.*;
import com.mongodb.client.model.Updates;
import org.bson.*;
import org.bson.conversions.Bson;

import java.util.*;

public class Equipment extends ObjectModel<Equipment> {

    public static final String BNAME_ID = "i";
    public static final String BNAME_REF_ID = "ri";
    public static final String BNAME_ATK = "a";
    public static final String BNAME_DEF = "d";
    public static final String BNAME_HP = "h";

    private String id = "";
    private int refId;
    private int atk;
    private int def;
    private int hp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        Objects.requireNonNull(id, "id must not be null");
        if (!id.equals(this.id)) {
            this.id = id;
            fieldChanged(0);
        }
    }

    public int getRefId() {
        return refId;
    }

    public void setRefId(int refId) {
        if (refId != this.refId) {
            this.refId = refId;
            fieldChanged(1);
        }
    }

    public int getAtk() {
        return atk;
    }

    public void setAtk(int atk) {
        if (atk != this.atk) {
            this.atk = atk;
            fieldChanged(2);
        }
    }

    public int getDef() {
        return def;
    }

    public void setDef(int def) {
        if (def != this.def) {
            this.def = def;
            fieldChanged(3);
        }
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        if (hp != this.hp) {
            this.hp = hp;
            fieldChanged(4);
        }
    }

    public boolean idChanged() {
        return changedFields.get(0);
    }

    public boolean refIdChanged() {
        return changedFields.get(1);
    }

    public boolean atkChanged() {
        return changedFields.get(2);
    }

    public boolean defChanged() {
        return changedFields.get(3);
    }

    public boolean hpChanged() {
        return changedFields.get(4);
    }

    @Override
    public BsonDocument toBson() {
        var bson = new BsonDocument();
        bson.append(BNAME_ID, new BsonString(id));
        bson.append(BNAME_REF_ID, new BsonInt32(refId));
        bson.append(BNAME_ATK, new BsonInt32(atk));
        bson.append(BNAME_DEF, new BsonInt32(def));
        bson.append(BNAME_HP, new BsonInt32(hp));
        return bson;
    }

    @Override
    public Equipment load(BsonDocument src) {
        resetStates();
        id = BsonUtil.stringValue(src, BNAME_ID).orElse("");
        refId = BsonUtil.intValue(src, BNAME_REF_ID).orElse(0);
        atk = BsonUtil.intValue(src, BNAME_ATK).orElse(0);
        def = BsonUtil.intValue(src, BNAME_DEF).orElse(0);
        hp = BsonUtil.intValue(src, BNAME_HP).orElse(0);
        return this;
    }

    @Override
    public JsonNode toJsonNode() {
        var jsonNode = JsonNodeFactory.instance.objectNode();
        jsonNode.put(BNAME_ID, id);
        jsonNode.put(BNAME_REF_ID, refId);
        jsonNode.put(BNAME_ATK, atk);
        jsonNode.put(BNAME_DEF, def);
        jsonNode.put(BNAME_HP, hp);
        return jsonNode;
    }

    @Override
    public Object toData() {
        var data = new LinkedHashMap<>();
        data.put("id", id);
        data.put("refId", refId);
        data.put("atk", atk);
        data.put("def", def);
        data.put("hp", hp);
        return data;
    }

    @Override
    public boolean anyUpdated() {
        var changedFields = this.changedFields;
        if (changedFields.isEmpty()) {
            return false;
        }
        if (changedFields.get(0)) {
            return true;
        }
        if (changedFields.get(1)) {
            return true;
        }
        if (changedFields.get(2)) {
            return true;
        }
        if (changedFields.get(3)) {
            return true;
        }
        if (changedFields.get(4)) {
            return true;
        }
        return false;
    }

    @Override
    protected void resetChildren() {
    }

    @Override
    protected int deletedSize() {
        return 0;
    }

    @Override
    public boolean anyDeleted() {
        return false;
    }

    @Override
    public Equipment clean() {
        id = "";
        refId = 0;
        atk = 0;
        def = 0;
        hp = 0;
        resetStates();
        return this;
    }

    @Override
    public Equipment deepCopy() {
        var copy = new Equipment();
        deepCopyTo(copy, false);
        return copy;
    }

    @Override
    public void deepCopyFrom(Equipment src) {
        id = src.id;
        refId = src.refId;
        atk = src.atk;
        def = src.def;
        hp = src.hp;
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {
        var changedFields = this.changedFields;
        if (changedFields.isEmpty()) {
            return;
        }
        if (changedFields.get(0)) {
            updates.add(Updates.set(path().resolve(BNAME_ID).value(), id));
        }
        if (changedFields.get(1)) {
            updates.add(Updates.set(path().resolve(BNAME_REF_ID).value(), refId));
        }
        if (changedFields.get(2)) {
            updates.add(Updates.set(path().resolve(BNAME_ATK).value(), atk));
        }
        if (changedFields.get(3)) {
            updates.add(Updates.set(path().resolve(BNAME_DEF).value(), def));
        }
        if (changedFields.get(4)) {
            updates.add(Updates.set(path().resolve(BNAME_HP).value(), hp));
        }
    }

    @Override
    protected void loadObjectNode(JsonNode src) {
        resetStates();
        id = BsonUtil.stringValue(src, BNAME_ID).orElse("");
        refId = BsonUtil.intValue(src, BNAME_REF_ID).orElse(0);
        atk = BsonUtil.intValue(src, BNAME_ATK).orElse(0);
        def = BsonUtil.intValue(src, BNAME_DEF).orElse(0);
        hp = BsonUtil.intValue(src, BNAME_HP).orElse(0);
    }

    @Override
    protected void appendUpdateData(Map<Object, Object> data) {
        var changedFields = this.changedFields;
        if (changedFields.isEmpty()) {
            return;
        }
        if (changedFields.get(0)) {
            data.put("id", id);
        }
        if (changedFields.get(1)) {
            data.put("refId", refId);
        }
        if (changedFields.get(2)) {
            data.put("atk", atk);
        }
        if (changedFields.get(3)) {
            data.put("def", def);
        }
        if (changedFields.get(4)) {
            data.put("hp", hp);
        }
    }

    @Override
    public Object toDeletedData() {
        return null;
    }

    @Override
    protected void appendDeletedData(Map<Object, Object> data) {
    }

    @Override
    public String toString() {
        return "Equipment(" + "id=" + id +
                ", refId=" + refId +
                ", atk=" + atk +
                ", def=" + def +
                ", hp=" + hp +
                ")";
    }

}
