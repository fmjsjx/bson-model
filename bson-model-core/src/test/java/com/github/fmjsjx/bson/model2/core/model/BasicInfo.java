package com.github.fmjsjx.bson.model2.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fmjsjx.bson.model.core.BsonUtil;
import com.github.fmjsjx.bson.model2.core.ObjectModel;
import com.github.fmjsjx.libcommon.util.DateTimeUtil;
import com.mongodb.client.model.Updates;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.conversions.Bson;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        if (!this.name.equals(name)) {
            this.name = name;
            fieldChanged(0);
        }
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        if (!Objects.equals(this.avatar, avatar)) {
            this.avatar = avatar;
            fieldChanged(1);
        }
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        Objects.requireNonNull(name, "lastLoginTime must not be null");
        if (!this.lastLoginTime.equals(lastLoginTime)) {
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
            this.gis = gis.parent(this).key(BNAME_GIS).index(4);
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
    protected void resetChildren() {
        var gis = this.gis;
        if (gis != null) {
            gis.reset();
        }
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
        if (changedFields.get(4) && gis != null) {
            return true;
        }
        return false;
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
    protected void appendUpdateData(Map<Object, Object> data) {
        var changedFields = this.changedFields;
        if (changedFields.get(0)) {
            data.put("name", name);
        }
        if (changedFields.get(1)) {
            var avatar = this.avatar;
            if (avatar != null) {
                data.put("avatar", avatar);
            }
        }
        if (changedFields.get(2)) {
            data.put("lastLoginAt", getLastLoginAt());
        }
        if (changedFields.get(4)) {
            var gis = this.gis;
            if (gis != null) {
                var updateData = gis.toUpdateData();
                if (updateData != null) {
                    data.put("gis", updateData);
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
                var deletedData = gis.toDeletedData();
                if (deletedData != null) {
                    data.put("gis", deletedData);
                }
            }
        }
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
    public void load(BsonDocument src) {
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
                    gis = new GisCoordinates();
                    gis.load(v);
                    this.gis = gis.parent(this).key(BNAME_GIS).index(4);
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
    public Object toData() {
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
    protected void appendFieldUpdates(List<Bson> updates) {
        var changeFields = this.changedFields;
        if (changeFields.get(0)) {
            updates.add(Updates.set(path().resolve(BNAME_NAME).value(), name));
        }
        if (changeFields.get(1)) {
            var avatar = this.avatar;
            if (avatar == null) {
                updates.add(Updates.unset(path().resolve(BNAME_AVATAR).value()));
            } else {
                updates.add(Updates.set(path().resolve(BNAME_AVATAR).value(), avatar));
            }
        }
        if (changeFields.get(2)) {
            updates.add(Updates.set(path().resolve(BNAME_LAST_LOGIN_TIME).value(), BsonUtil.toBsonDateTime(lastLoginTime)));
        }
        if (changeFields.get(4)) {
            var gis = this.gis;
            if (gis == null) {
                updates.add(Updates.unset(path().resolve(BNAME_GIS).value()));
            } else {
                gis.appendUpdates(updates);
            }
        }
    }

    @Override
    protected void loadObjectNode(ObjectNode src) {
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
                    gis = new GisCoordinates();
                    gis.load(v);
                    this.gis = gis.parent(this).key(BNAME_GIS).index(4);
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
}
