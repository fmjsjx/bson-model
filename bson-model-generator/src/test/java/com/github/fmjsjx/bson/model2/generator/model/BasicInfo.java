package com.github.fmjsjx.bson.model2.generator.model;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.fmjsjx.bson.model.core.BsonUtil;
import com.github.fmjsjx.bson.model2.core.*;
import com.github.fmjsjx.libcommon.util.DateTimeUtil;
import com.mongodb.client.model.Updates;
import org.bson.*;
import org.bson.conversions.Bson;

import java.time.LocalDateTime;
import java.util.*;

public class BasicInfo extends ObjectModel<BasicInfo> {

    public static final String BNAME_NAME = "n";
    public static final String BNAME_AVATAR = "a";
    public static final String BNAME_LAST_LOGIN_TIME = "llt";
    public static final String BNAME_GIS = "g";

    private String name = "";
    private String avatar;
    private LocalDateTime lastLoginTime = LocalDateTime.now();
    private GisCoordinates gis;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Objects.requireNonNull(name, "name must not be null");
        if (!name.equals(this.name)) {
            this.name = name;
            fieldChanged(0);
        }
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        if (!Objects.equals(avatar, this.avatar)) {
            this.avatar = avatar;
            fieldChanged(1);
        }
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        Objects.requireNonNull(lastLoginTime, "lastLoginTime must not be null");
        if (!lastLoginTime.equals(this.lastLoginTime)) {
            this.lastLoginTime = lastLoginTime;
            fieldsChanged(2, 3);
        }
    }

    public long getLastLoginAt() {
        return DateTimeUtil.toEpochMilli(lastLoginTime);
    }

    public GisCoordinates getGis() {
        return gis;
    }

    public void setGis(GisCoordinates gis) {
        if (gis != null) {
            gis.mustUnbound();
            this.gis = gis.parent(this).key(BNAME_GIS).index(4).fullyUpdate(true);
            fieldChanged(4);
        } else {
            gis = this.gis;
            if (gis != null) {
                gis.unbind();
                this.gis = null;
                fieldChanged(4);
            }
        }
    }

    public boolean nameChanged() {
        return changedFields.get(0);
    }

    public boolean avatarChanged() {
        return changedFields.get(1);
    }

    public boolean lastLoginTimeChanged() {
        return changedFields.get(2);
    }

    public boolean lastLoginAtChanged() {
        return changedFields.get(3);
    }

    public boolean gisChanged() {
        return changedFields.get(4);
    }

    @Override
    public BsonDocument toBson() {
        var bson = new BsonDocument();
        bson.append(BNAME_NAME, new BsonString(name));
        var avatar = this.avatar;
        if (avatar != null) {
            bson.append(BNAME_AVATAR, new BsonString(avatar));
        }
        bson.append(BNAME_LAST_LOGIN_TIME, BsonUtil.toBsonDateTime(lastLoginTime));
        var gis = this.gis;
        if (gis != null) {
            bson.append(BNAME_GIS, gis.toBson());
        }
        return bson;
    }

    @Override
    public BasicInfo load(BsonDocument src) {
        resetStates();
        name = BsonUtil.stringValue(src, BNAME_NAME).orElse("");
        avatar = BsonUtil.stringValue(src, BNAME_AVATAR).orElse(null);
        lastLoginTime = BsonUtil.dateTimeValue(src, BNAME_LAST_LOGIN_TIME).orElseGet(LocalDateTime::now);
        BsonUtil.documentValue(src, BNAME_GIS).ifPresentOrElse(
                v -> {
                    var gis = this.gis;
                    if (gis != null) {
                        gis.unbind();
                    }
                    this.gis = new GisCoordinates().load(v).parent(this).key(BNAME_GIS).index(4);
                },
                () -> {
                    var gis = this.gis;
                    if (gis != null) {
                        gis.unbind();
                        this.gis = null;
                    }
                }
        );
        return this;
    }

    @Override
    public JsonNode toJsonNode() {
        var jsonNode = JsonNodeFactory.instance.objectNode();
        jsonNode.put(BNAME_NAME, name);
        var avatar = this.avatar;
        if (avatar != null) {
            jsonNode.put(BNAME_AVATAR, avatar);
        }
        jsonNode.put(BNAME_LAST_LOGIN_TIME, DateTimeUtil.toEpochMilli(lastLoginTime));
        var gis = this.gis;
        if (gis != null) {
            jsonNode.set(BNAME_GIS, gis.toJsonNode());
        }
        return jsonNode;
    }

    @Override
    public JSONObject toFastjson2Node() {
        var jsonObject = new JSONObject();
        jsonObject.put(BNAME_NAME, name);
        var avatar = this.avatar;
        if (avatar != null) {
            jsonObject.put(BNAME_AVATAR, avatar);
        }
        jsonObject.put(BNAME_LAST_LOGIN_TIME, DateTimeUtil.toEpochMilli(lastLoginTime));
        var gis = this.gis;
        if (gis != null) {
            jsonObject.put(BNAME_GIS, gis.toFastjson2Node());
        }
        return jsonObject;
    }

    @Override
    public Map<Object, Object> toData() {
        var data = new LinkedHashMap<>();
        data.put("name", name);
        var avatar = this.avatar;
        if (avatar != null) {
            data.put("avatar", avatar);
        }
        data.put("lastLoginAt", getLastLoginAt());
        var gis = this.gis;
        if (gis != null) {
            data.put("gis", gis.toData());
        }
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
        if (changedFields.get(1) && avatar != null) {
            return true;
        }
        if (changedFields.get(2)) {
            return true;
        }
        if (changedFields.get(4)) {
            var gis = this.gis;
            if (gis != null && gis.anyUpdated()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void resetChildren() {
        var gis = this.gis;
        if (gis != null) {
            gis.reset();
        }
    }

    @Override
    protected int deletedSize() {
        var changedFields = this.changedFields;
        if (changedFields.isEmpty()) {
            return 0;
        }
        var n = 0;
        if (changedFields.get(1) && avatar == null) {
            n++;
        }
        if (changedFields.get(4)) {
            var gis = this.gis;
            if (gis == null || gis.anyDeleted()) {
                n++;
            }
        }
        return n;
    }

    @Override
    public boolean anyDeleted() {
        var changedFields = this.changedFields;
        if (changedFields.isEmpty()) {
            return false;
        }
        if (changedFields.get(1) && avatar == null) {
            return true;
        }
        if (changedFields.get(4)) {
            var gis = this.gis;
            if (gis == null || gis.anyDeleted()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public BasicInfo clean() {
        name = "";
        avatar = null;
        lastLoginTime = LocalDateTime.now();
        var gis = this.gis;
        if (gis != null) {
            gis.clean().unbind();
            this.gis = null;
        }
        resetStates();
        return this;
    }

    @Override
    public BasicInfo deepCopy() {
        var copy = new BasicInfo();
        deepCopyTo(copy, false);
        return copy;
    }

    @Override
    public void deepCopyFrom(BasicInfo src) {
        name = src.name;
        avatar = src.avatar;
        lastLoginTime = src.lastLoginTime;
        var gis = src.gis;
        if (gis != null) {
            this.gis = gis.deepCopy().parent(this).key(BNAME_GIS).index(4);
        } else {
            gis = this.gis;
            if (gis != null) {
                gis.unbind();
                this.gis = null;
            }
        }
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {
        var changedFields = this.changedFields;
        if (changedFields.isEmpty()) {
            return;
        }
        if (changedFields.get(0)) {
            updates.add(Updates.set(path().resolve(BNAME_NAME).value(), name));
        }
        if (changedFields.get(1)) {
            var avatar = this.avatar;
            if (avatar == null) {
                updates.add(Updates.unset(path().resolve(BNAME_AVATAR).value()));
            } else {
                updates.add(Updates.set(path().resolve(BNAME_AVATAR).value(), avatar));
            }
        }
        if (changedFields.get(2)) {
            updates.add(Updates.set(path().resolve(BNAME_LAST_LOGIN_TIME).value(), BsonUtil.toBsonDateTime(lastLoginTime)));
        }
        if (changedFields.get(4)) {
            var gis = this.gis;
            if (gis == null) {
                updates.add(Updates.unset(path().resolve(BNAME_GIS).value()));
            } else {
                gis.appendUpdates(updates);
            }
        }
    }

    @Override
    protected void loadObjectNode(JsonNode src) {
        resetStates();
        name = BsonUtil.stringValue(src, BNAME_NAME).orElse("");
        avatar = BsonUtil.stringValue(src, BNAME_AVATAR).orElse(null);
        lastLoginTime = BsonUtil.dateTimeValue(src, BNAME_LAST_LOGIN_TIME).orElseGet(LocalDateTime::now);
        BsonUtil.objectValue(src, BNAME_GIS).ifPresentOrElse(
                v -> {
                    var gis = this.gis;
                    if (gis != null) {
                        gis.unbind();
                    }
                    this.gis = new GisCoordinates().load(v).parent(this).key(BNAME_GIS).index(4);
                },
                () -> {
                    var gis = this.gis;
                    if (gis != null) {
                        gis.unbind();
                        this.gis = null;
                    }
                }
        );
    }

    @Override
    protected void loadJSONObject(JSONObject src) {
        resetStates();
        name = BsonUtil.stringValue(src, BNAME_NAME).orElse("");
        avatar = BsonUtil.stringValue(src, BNAME_AVATAR).orElse(null);
        lastLoginTime = BsonUtil.dateTimeValue(src, BNAME_LAST_LOGIN_TIME).orElseGet(LocalDateTime::now);
        BsonUtil.objectValue(src, BNAME_GIS).ifPresentOrElse(
                v -> {
                    var gis = this.gis;
                    if (gis != null) {
                        gis.unbind();
                    }
                    this.gis = new GisCoordinates().loadFastjson2Node(v).parent(this).key(BNAME_GIS).index(4);
                },
                () -> {
                    var gis = this.gis;
                    if (gis != null) {
                        gis.unbind();
                        this.gis = null;
                    }
                }
        );
    }

    @Override
    protected void appendUpdateData(Map<Object, Object> data) {
        var changedFields = this.changedFields;
        if (changedFields.isEmpty()) {
            return;
        }
        if (changedFields.get(0)) {
            data.put("name", name);
        }
        if (changedFields.get(1)) {
            var avatar = this.avatar;
            if (avatar != null) {
                data.put("avatar", avatar);
            }
        }
        if (changedFields.get(3)) {
            data.put("lastLoginAt", getLastLoginAt());
        }
        if (changedFields.get(4)) {
            var gis = this.gis;
            if (gis != null) {
                var gisUpdateData = gis.toUpdateData();
                if (gisUpdateData != null) {
                    data.put("gis", gisUpdateData);
                }
            }
        }
    }

    @Override
    protected void appendDeletedData(Map<Object, Object> data) {
        var changedFields = this.changedFields;
        if (changedFields.get(1) && avatar == null) {
            data.put("avatar", 1);
        }
        if (changedFields.get(4)) {
            var gis = this.gis;
            if (gis == null) {
                data.put("gis", 1);
            } else {
                var gisDeletedData = gis.toDeletedData();
                if (gisDeletedData != null) {
                    data.put("gis", gisDeletedData);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "BasicInfo(" + "name=" + name +
                ", avatar=" + avatar +
                ", lastLoginTime=" + lastLoginTime +
                ", gis=" + gis +
                ")";
    }

}
