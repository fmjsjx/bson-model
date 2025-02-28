package com.github.fmjsjx.bson.model2.generator.model;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.fmjsjx.bson.model.core.BsonUtil;
import com.github.fmjsjx.bson.model2.core.*;
import com.github.fmjsjx.libcommon.util.DateTimeUtil;
import com.mongodb.client.model.Updates;
import org.bson.*;
import org.bson.conversions.Bson;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class BasicInfo extends ObjectModel<BasicInfo> {

    public static final String BNAME_NAME = "n";
    public static final String BNAME_AVATAR = "a";
    public static final String BNAME_LAST_LOGIN_TIME = "llt";
    public static final String BNAME_LOGIN_DAYS = "ld";
    public static final String BNAME_WORK_TIMES = "wt";
    public static final String BNAME_GIS = "g";
    public static final String BNAME_BIRTHDAY = "b";
    public static final String BNAME_BIRTHTIME = "bt";

    private String name = "";
    private String avatar;
    private LocalDateTime lastLoginTime = LocalDateTime.now();
    private List<LocalDate> loginDays;
    private List<LocalTime> workTimes;
    private GisCoordinates gis;
    private LocalDate birthday;
    private LocalTime birthtime;

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
            fieldsChanged(2, 4);
        }
    }

    public List<LocalDate> getLoginDays() {
        return loginDays;
    }

    public void setLoginDays(List<LocalDate> loginDays) {
        if (!Objects.equals(loginDays, this.loginDays)) {
            this.loginDays = loginDays;
            fieldChanged(3);
        }
    }

    public long getLastLoginAt() {
        return DateTimeUtil.toEpochMilli(lastLoginTime);
    }

    public List<LocalTime> getWorkTimes() {
        return workTimes;
    }

    public void setWorkTimes(List<LocalTime> workTimes) {
        if (!Objects.equals(workTimes, this.workTimes)) {
            this.workTimes = workTimes;
            fieldChanged(5);
        }
    }

    public GisCoordinates getGis() {
        return gis;
    }

    public void setGis(GisCoordinates gis) {
        if (gis != null) {
            gis.mustUnbound();
            this.gis = gis.parent(this).key(BNAME_GIS).index(6).fullyUpdate(true);
            fieldChanged(6);
        } else {
            gis = this.gis;
            if (gis != null) {
                gis.unbind();
                this.gis = null;
                fieldChanged(6);
            }
        }
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        if (!Objects.equals(birthday, this.birthday)) {
            this.birthday = birthday;
            fieldChanged(7);
        }
    }

    public LocalTime getBirthtime() {
        return birthtime;
    }

    public void setBirthtime(LocalTime birthtime) {
        if (!Objects.equals(birthtime, this.birthtime)) {
            this.birthtime = birthtime;
            fieldChanged(8);
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

    public boolean loginDaysChanged() {
        return changedFields.get(3);
    }

    public boolean lastLoginAtChanged() {
        return changedFields.get(4);
    }

    public boolean workTimesChanged() {
        return changedFields.get(5);
    }

    public boolean gisChanged() {
        return changedFields.get(6);
    }

    public boolean birthdayChanged() {
        return changedFields.get(7);
    }

    public boolean birthtimeChanged() {
        return changedFields.get(8);
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
        var loginDays = this.loginDays;
        if (loginDays != null) {
            bson.append(BNAME_LOGIN_DAYS, BsonUtil.toBsonArray(loginDays, v -> new BsonInt32(DateTimeUtil.toNumber(v))));
        }
        var workTimes = this.workTimes;
        if (workTimes != null) {
            bson.append(BNAME_WORK_TIMES, BsonUtil.toBsonArray(workTimes, v -> new BsonInt32(DateTimeUtil.toNumber(v))));
        }
        var gis = this.gis;
        if (gis != null) {
            bson.append(BNAME_GIS, gis.toBson());
        }
        var birthday = this.birthday;
        if (birthday != null) {
            bson.append(BNAME_BIRTHDAY, new BsonInt32(DateTimeUtil.toNumber(birthday)));
        }
        var birthtime = this.birthtime;
        if (birthtime != null) {
            bson.append(BNAME_BIRTHTIME, new BsonInt32(DateTimeUtil.toNumber(birthtime)));
        }
        return bson;
    }

    @Override
    public BasicInfo load(BsonDocument src) {
        resetStates();
        name = BsonUtil.stringValue(src, BNAME_NAME).orElse("");
        avatar = BsonUtil.stringValue(src, BNAME_AVATAR).orElse(null);
        lastLoginTime = BsonUtil.dateTimeValue(src, BNAME_LAST_LOGIN_TIME).orElseGet(LocalDateTime::now);
        loginDays = BsonUtil.arrayValue(src, BNAME_LOGIN_DAYS, (BsonNumber v) -> DateTimeUtil.toDate(v.intValue())).orElse(null);
        workTimes = BsonUtil.arrayValue(src, BNAME_WORK_TIMES, (BsonNumber v) -> DateTimeUtil.toTime(v.intValue())).orElse(null);
        BsonUtil.documentValue(src, BNAME_GIS).ifPresentOrElse(
                v -> {
                    var gis = this.gis;
                    if (gis != null) {
                        gis.unbind();
                    }
                    this.gis = new GisCoordinates().load(v).parent(this).key(BNAME_GIS).index(6);
                },
                () -> {
                    var gis = this.gis;
                    if (gis != null) {
                        gis.unbind();
                        this.gis = null;
                    }
                }
        );
        birthday = BsonUtil.intValue(src, BNAME_BIRTHDAY).stream().mapToObj(DateTimeUtil::toDate).findFirst().orElse(null);
        birthtime = BsonUtil.intValue(src, BNAME_BIRTHTIME).stream().mapToObj(DateTimeUtil::toTime).findFirst().orElse(null);
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
        var loginDays = this.loginDays;
        if (loginDays != null) {
            var loginDaysArrayNode = jsonNode.arrayNode(loginDays.size());
            loginDays.stream().map(DateTimeUtil::toNumber).forEach(loginDaysArrayNode::add);
            jsonNode.set(BNAME_LOGIN_DAYS, loginDaysArrayNode);
        }
        var workTimes = this.workTimes;
        if (workTimes != null) {
            var workTimesArrayNode = jsonNode.arrayNode(workTimes.size());
            workTimes.stream().map(DateTimeUtil::toNumber).forEach(workTimesArrayNode::add);
            jsonNode.set(BNAME_WORK_TIMES, workTimesArrayNode);
        }
        var gis = this.gis;
        if (gis != null) {
            jsonNode.set(BNAME_GIS, gis.toJsonNode());
        }
        var birthday = this.birthday;
        if (birthday != null) {
            jsonNode.put(BNAME_BIRTHDAY, DateTimeUtil.toNumber(birthday));
        }
        var birthtime = this.birthtime;
        if (birthtime != null) {
            jsonNode.put(BNAME_BIRTHTIME, DateTimeUtil.toNumber(birthtime));
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
        var loginDays = this.loginDays;
        if (loginDays != null) {
            var loginDaysJsonArray = new JSONArray(loginDays.size());
            loginDays.stream().map(DateTimeUtil::toNumber).forEach(loginDaysJsonArray::add);
            jsonObject.put(BNAME_LOGIN_DAYS, loginDaysJsonArray);
        }
        var workTimes = this.workTimes;
        if (workTimes != null) {
            var workTimesJsonArray = new JSONArray(workTimes.size());
            workTimes.stream().map(DateTimeUtil::toNumber).forEach(workTimesJsonArray::add);
            jsonObject.put(BNAME_WORK_TIMES, workTimesJsonArray);
        }
        var gis = this.gis;
        if (gis != null) {
            jsonObject.put(BNAME_GIS, gis.toFastjson2Node());
        }
        var birthday = this.birthday;
        if (birthday != null) {
            jsonObject.put(BNAME_BIRTHDAY, DateTimeUtil.toNumber(birthday));
        }
        var birthtime = this.birthtime;
        if (birthtime != null) {
            jsonObject.put(BNAME_BIRTHTIME, DateTimeUtil.toNumber(birthtime));
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
        var loginDays = this.loginDays;
        if (loginDays != null) {
            data.put("loginDays", loginDays.stream().map(LocalDate::toString).toList());
        }
        data.put("lastLoginAt", getLastLoginAt());
        var workTimes = this.workTimes;
        if (workTimes != null) {
            data.put("workTimes", workTimes.stream().map(LocalTime::toString).toList());
        }
        var gis = this.gis;
        if (gis != null) {
            data.put("gis", gis.toData());
        }
        var birthday = this.birthday;
        if (birthday != null) {
            data.put("birthday", birthday.toString());
        }
        var birthtime = this.birthtime;
        if (birthtime != null) {
            data.put("birthtime", birthtime.toString());
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
        if (changedFields.get(3) && loginDays != null) {
            return true;
        }
        if (changedFields.get(5) && workTimes != null) {
            return true;
        }
        if (changedFields.get(6)) {
            var gis = this.gis;
            if (gis != null && gis.anyUpdated()) {
                return true;
            }
        }
        if (changedFields.get(7) && birthday != null) {
            return true;
        }
        if (changedFields.get(8) && birthtime != null) {
            return true;
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
        if (changedFields.get(3) && loginDays == null) {
            n++;
        }
        if (changedFields.get(5) && workTimes == null) {
            n++;
        }
        if (changedFields.get(6)) {
            var gis = this.gis;
            if (gis == null || gis.anyDeleted()) {
                n++;
            }
        }
        if (changedFields.get(7) && birthday == null) {
            n++;
        }
        if (changedFields.get(8) && birthtime == null) {
            n++;
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
        if (changedFields.get(3) && loginDays == null) {
            return true;
        }
        if (changedFields.get(5) && workTimes == null) {
            return true;
        }
        if (changedFields.get(6)) {
            var gis = this.gis;
            if (gis == null || gis.anyDeleted()) {
                return true;
            }
        }
        if (changedFields.get(7) && birthday == null) {
            return true;
        }
        if (changedFields.get(8) && birthtime == null) {
            return true;
        }
        return false;
    }

    @Override
    public BasicInfo clean() {
        name = "";
        avatar = null;
        lastLoginTime = LocalDateTime.now();
        loginDays = null;
        workTimes = null;
        var gis = this.gis;
        if (gis != null) {
            gis.clean().unbind();
            this.gis = null;
        }
        birthday = null;
        birthtime = null;
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
        var loginDays = src.loginDays;
        if (loginDays != null) {
            this.loginDays = new ArrayList<>(src.loginDays);
        } else {
            this.loginDays = null;
        }
        var workTimes = src.workTimes;
        if (workTimes != null) {
            this.workTimes = new ArrayList<>(src.workTimes);
        } else {
            this.workTimes = null;
        }
        var gis = src.gis;
        if (gis != null) {
            this.gis = gis.deepCopy().parent(this).key(BNAME_GIS).index(6);
        } else {
            gis = this.gis;
            if (gis != null) {
                gis.unbind();
                this.gis = null;
            }
        }
        birthday = src.birthday;
        birthtime = src.birthtime;
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
        if (changedFields.get(3)) {
            var loginDays = this.loginDays;
            if (loginDays == null) {
                updates.add(Updates.unset(path().resolve(BNAME_LOGIN_DAYS).value()));
            } else {
                updates.add(Updates.set(path().resolve(BNAME_LOGIN_DAYS).value(), BsonUtil.toBsonArray(loginDays, v -> new BsonInt32(DateTimeUtil.toNumber(v)))));
            }
        }
        if (changedFields.get(5)) {
            var workTimes = this.workTimes;
            if (workTimes == null) {
                updates.add(Updates.unset(path().resolve(BNAME_WORK_TIMES).value()));
            } else {
                updates.add(Updates.set(path().resolve(BNAME_WORK_TIMES).value(), BsonUtil.toBsonArray(workTimes, v -> new BsonInt32(DateTimeUtil.toNumber(v)))));
            }
        }
        if (changedFields.get(6)) {
            var gis = this.gis;
            if (gis == null) {
                updates.add(Updates.unset(path().resolve(BNAME_GIS).value()));
            } else {
                gis.appendUpdates(updates);
            }
        }
        if (changedFields.get(7)) {
            var birthday = this.birthday;
            if (birthday == null) {
                updates.add(Updates.unset(path().resolve(BNAME_BIRTHDAY).value()));
            } else {
                updates.add(Updates.set(path().resolve(BNAME_BIRTHDAY).value(), DateTimeUtil.toNumber(birthday)));
            }
        }
        if (changedFields.get(8)) {
            var birthtime = this.birthtime;
            if (birthtime == null) {
                updates.add(Updates.unset(path().resolve(BNAME_BIRTHTIME).value()));
            } else {
                updates.add(Updates.set(path().resolve(BNAME_BIRTHTIME).value(), DateTimeUtil.toNumber(birthtime)));
            }
        }
    }

    @Override
    protected void loadObjectNode(JsonNode src) {
        resetStates();
        name = BsonUtil.stringValue(src, BNAME_NAME).orElse("");
        avatar = BsonUtil.stringValue(src, BNAME_AVATAR).orElse(null);
        lastLoginTime = BsonUtil.dateTimeValue(src, BNAME_LAST_LOGIN_TIME).orElseGet(LocalDateTime::now);
        loginDays = BsonUtil.listValue(src, BNAME_LOGIN_DAYS, v -> DateTimeUtil.toDate(Math.max(v.intValue(), 101))).orElse(null);
        workTimes = BsonUtil.listValue(src, BNAME_WORK_TIMES, v -> DateTimeUtil.toTime(Math.max(v.intValue(), 0))).orElse(null);
        BsonUtil.objectValue(src, BNAME_GIS).ifPresentOrElse(
                v -> {
                    var gis = this.gis;
                    if (gis != null) {
                        gis.unbind();
                    }
                    this.gis = new GisCoordinates().load(v).parent(this).key(BNAME_GIS).index(6);
                },
                () -> {
                    var gis = this.gis;
                    if (gis != null) {
                        gis.unbind();
                        this.gis = null;
                    }
                }
        );
        birthday = BsonUtil.intValue(src, BNAME_BIRTHDAY).stream().mapToObj(DateTimeUtil::toDate).findFirst().orElse(null);
        birthtime = BsonUtil.intValue(src, BNAME_BIRTHTIME).stream().mapToObj(DateTimeUtil::toTime).findFirst().orElse(null);
    }

    @Override
    protected void loadJSONObject(JSONObject src) {
        resetStates();
        name = BsonUtil.stringValue(src, BNAME_NAME).orElse("");
        avatar = BsonUtil.stringValue(src, BNAME_AVATAR).orElse(null);
        lastLoginTime = BsonUtil.dateTimeValue(src, BNAME_LAST_LOGIN_TIME).orElseGet(LocalDateTime::now);
        loginDays = BsonUtil.listValue(src, BNAME_LOGIN_DAYS, v -> DateTimeUtil.toDate(v instanceof Number n ? n.intValue() : 101)).orElse(null);
        workTimes = BsonUtil.listValue(src, BNAME_WORK_TIMES, v -> DateTimeUtil.toTime(v instanceof Number n ? n.intValue() : 0)).orElse(null);
        BsonUtil.objectValue(src, BNAME_GIS).ifPresentOrElse(
                v -> {
                    var gis = this.gis;
                    if (gis != null) {
                        gis.unbind();
                    }
                    this.gis = new GisCoordinates().loadFastjson2Node(v).parent(this).key(BNAME_GIS).index(6);
                },
                () -> {
                    var gis = this.gis;
                    if (gis != null) {
                        gis.unbind();
                        this.gis = null;
                    }
                }
        );
        birthday = BsonUtil.intValue(src, BNAME_BIRTHDAY).stream().mapToObj(DateTimeUtil::toDate).findFirst().orElse(null);
        birthtime = BsonUtil.intValue(src, BNAME_BIRTHTIME).stream().mapToObj(DateTimeUtil::toTime).findFirst().orElse(null);
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
            var loginDays = this.loginDays;
            if (loginDays != null) {
                data.put("loginDays", loginDays.stream().map(LocalDate::toString).toList());
            }
        }
        if (changedFields.get(4)) {
            data.put("lastLoginAt", getLastLoginAt());
        }
        if (changedFields.get(5)) {
            var workTimes = this.workTimes;
            if (workTimes != null) {
                data.put("workTimes", workTimes.stream().map(LocalTime::toString).toList());
            }
        }
        if (changedFields.get(6)) {
            var gis = this.gis;
            if (gis != null) {
                var gisUpdateData = gis.toUpdateData();
                if (gisUpdateData != null) {
                    data.put("gis", gisUpdateData);
                }
            }
        }
        if (changedFields.get(7)) {
            var birthday = this.birthday;
            if (birthday != null) {
                data.put("birthday", birthday.toString());
            }
        }
        if (changedFields.get(8)) {
            var birthtime = this.birthtime;
            if (birthtime != null) {
                data.put("birthtime", birthtime.toString());
            }
        }
    }

    @Override
    protected void appendDeletedData(Map<Object, Object> data) {
        var changedFields = this.changedFields;
        if (changedFields.get(1) && avatar == null) {
            data.put("avatar", 1);
        }
        if (changedFields.get(3) && loginDays == null) {
            data.put("loginDays", 1);
        }
        if (changedFields.get(5) && workTimes == null) {
            data.put("workTimes", 1);
        }
        if (changedFields.get(6)) {
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
        if (changedFields.get(7) && birthday == null) {
            data.put("birthday", 1);
        }
        if (changedFields.get(8) && birthtime == null) {
            data.put("birthtime", 1);
        }
    }

    @Override
    public String toString() {
        return "BasicInfo(" + "name=" + name +
                ", avatar=" + avatar +
                ", lastLoginTime=" + lastLoginTime +
                ", loginDays=" + loginDays +
                ", workTimes=" + workTimes +
                ", gis=" + gis +
                ", birthday=" + birthday +
                ", birthtime=" + birthtime +
                ")";
    }

}
