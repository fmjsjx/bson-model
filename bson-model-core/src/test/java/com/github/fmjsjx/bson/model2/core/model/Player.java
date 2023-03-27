package com.github.fmjsjx.bson.model2.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fmjsjx.bson.model.core.BsonUtil;
import com.github.fmjsjx.bson.model2.core.DefaultMapModel;
import com.github.fmjsjx.bson.model2.core.RootModel;
import com.github.fmjsjx.bson.model2.core.SingleValueMapModel;
import com.github.fmjsjx.bson.model2.core.SingleValueTypes;
import com.github.fmjsjx.libcommon.util.DateTimeUtil;
import com.mongodb.client.model.Updates;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.conversions.Bson;

import java.time.LocalDateTime;
import java.util.*;

public class Player extends RootModel<Player> {

    public static final String BNAME_UID = "_id";
    public static final String BNAME_BASIC_INFO = "bi";
    public static final String BNAME_WALLET = "w";
    public static final String BNAME_EQUIPMENTS = "e";
    public static final String BNAME_ITEMS = "i";
    public static final String BNAME_UPDATE_VERSION = "_uv";
    public static final String BNAME_CREATE_TIME = "_ct";
    public static final String BNAME_UPDATE_TIME = "_ut";
    public static final String BNAME_FRIENDS = "f";

    private int uid;
    private final BasicInfo basicInfo = new BasicInfo().parent(this).key(BNAME_BASIC_INFO).index(1);
    private final Wallet wallet = new Wallet().parent(this).key(BNAME_WALLET).index(2);
    private final DefaultMapModel<String, Equipment> equipments = DefaultMapModel.stringKeysMap(Equipment::new).parent(this).key(BNAME_EQUIPMENTS).index(3);
    private final SingleValueMapModel<Integer, Integer> items = SingleValueMapModel.integerKeysMap(SingleValueTypes.INTEGER).parent(this).key(BNAME_ITEMS).index(4);
    private int updateVersion;
    private LocalDateTime createTime = LocalDateTime.MIN;
    private LocalDateTime updateTime = LocalDateTime.MIN;
    private List<Player> friends;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        if (this.uid != uid) {
            this.uid = uid;
            fieldChanged(0);
        }
    }

    public BasicInfo getBasicInfo() {
        return basicInfo;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public DefaultMapModel<String, Equipment> getEquipments() {
        return equipments;
    }

    public SingleValueMapModel<Integer, Integer> getItems() {
        return items;
    }

    public int getUpdateVersion() {
        return updateVersion;
    }

    public void setUpdateVersion(int updateVersion) {
        if (this.updateVersion != updateVersion) {
            this.updateVersion = updateVersion;
            fieldChanged(5);
        }
    }

    public int increaseUpdateVersion() {
        fieldChanged(5);
        return ++updateVersion;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        Objects.requireNonNull(createTime, "createTime must not be null");
        if (!this.createTime.equals(createTime)) {
            this.createTime = createTime;
            fieldsChanged(6, 8);
        }
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        Objects.requireNonNull(updateTime, "updateTime must not be null");
        if (!this.updateTime.equals(updateTime)) {
            this.updateTime = updateTime;
            fieldsChanged(7, 9);
        }
    }

    public long getCreatedAt() {
        return DateTimeUtil.toEpochMilli(createTime);
    }

    public long getUpdatedAt() {
        return DateTimeUtil.toEpochMilli(updateTime);
    }

    public List<Player> getFriends() {
        return friends;
    }

    public void setFriends(List<Player> friends) {
        this.friends = friends;
    }

    public boolean uidChanged() {
        return changedFields.get(0);
    }

    public boolean basicInfoChanged() {
        return changedFields.get(1);
    }

    public boolean walletChanged() {
        return changedFields.get(2);
    }

    public boolean equipmentsChanged() {
        return changedFields.get(3);
    }

    public boolean itemsChanged() {
        return changedFields.get(4);
    }

    public boolean updateVersionChanged() {
        return changedFields.get(5);
    }

    public boolean createTimeChanged() {
        return changedFields.get(6);
    }

    public boolean updateTimeChanged() {
        return changedFields.get(7);
    }

    public boolean createdAtChanged() {
        return changedFields.get(8);
    }

    public boolean updatedAtChanged() {
        return changedFields.get(9);
    }

    @Override
    public BsonDocument toBson() {
        var bson = new BsonDocument();
        bson.append(BNAME_UID, new BsonInt32(uid));
        bson.append(BNAME_BASIC_INFO, basicInfo.toBson());
        bson.append(BNAME_WALLET, wallet.toBson());
        bson.append(BNAME_EQUIPMENTS, equipments.toBson());
        bson.append(BNAME_ITEMS, items.toBson());
        bson.append(BNAME_UPDATE_VERSION, new BsonInt32(updateVersion));
        bson.append(BNAME_CREATE_TIME, BsonUtil.toBsonDateTime(createTime));
        bson.append(BNAME_UPDATE_TIME, BsonUtil.toBsonDateTime(updateTime));
        return bson;
    }

    @Override
    public Player load(BsonDocument src) {
        resetStates();
        uid = BsonUtil.intValue(src, BNAME_UID).orElseThrow();
        BsonUtil.documentValue(src, BNAME_BASIC_INFO).ifPresentOrElse(basicInfo::load, basicInfo::clean);
        BsonUtil.documentValue(src, BNAME_WALLET).ifPresentOrElse(wallet::load, wallet::clean);
        BsonUtil.documentValue(src, BNAME_EQUIPMENTS).ifPresentOrElse(equipments::load, equipments::clean);
        BsonUtil.documentValue(src, BNAME_ITEMS).ifPresentOrElse(items::load, items::clean);
        updateVersion = BsonUtil.intValue(src, BNAME_UPDATE_VERSION).orElse(0);
        createTime = BsonUtil.dateTimeValue(src, BNAME_CREATE_TIME).orElseThrow();
        updateTime = BsonUtil.dateTimeValue(src, BNAME_UPDATE_TIME).orElseThrow();
        BsonUtil.arrayValue(src, BNAME_FRIENDS, (BsonDocument v) -> new Player().load(v));
        return this;
    }

    @Override
    public JsonNode toJsonNode() {
        var jsonNode = JsonNodeFactory.instance.objectNode();
        jsonNode.put(BNAME_UID, uid);
        jsonNode.set(BNAME_BASIC_INFO, basicInfo.toJsonNode());
        jsonNode.set(BNAME_WALLET, wallet.toJsonNode());
        jsonNode.set(BNAME_EQUIPMENTS, equipments.toJsonNode());
        jsonNode.set(BNAME_ITEMS, items.toJsonNode());
        jsonNode.put(BNAME_UPDATE_VERSION, updateVersion);
        jsonNode.put(BNAME_CREATE_TIME, DateTimeUtil.toEpochMilli(createTime));
        jsonNode.put(BNAME_UPDATE_TIME, DateTimeUtil.toEpochMilli(updateTime));
        return jsonNode;
    }

    @Override
    public Object toData() {
        var data = new LinkedHashMap<>();
        data.put("uid", uid);
        data.put("basicInfo", basicInfo.toData());
        data.put("wallet", wallet.toData());
        data.put("equipments", equipments.toData());
        data.put("items", items.toData());
        data.put("createdAt", getCreatedAt());
        data.put("updatedAt", getUpdatedAt());
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
        if (changedFields.get(1) && basicInfo.anyUpdated()) {
            return true;
        }
        if (changedFields.get(2) && wallet.anyUpdated()) {
            return true;
        }
        if (changedFields.get(3) && equipments.anyUpdated()) {
            return true;
        }
        if (changedFields.get(4) && items.anyUpdated()) {
            return true;
        }
        if (changedFields.get(5)) {
            return true;
        }
        if (changedFields.get(6)) {
            return true;
        }
        if (changedFields.get(7)) {
            return true;
        }
        if (changedFields.get(8)) {
            return true;
        }
        if (changedFields.get(9)) {
            return true;
        }
        return false;
    }

    @Override
    protected void resetChildren() {
        basicInfo.reset();
        wallet.reset();
        equipments.reset();
        items.reset();
    }

    @Override
    protected int deletedSize() {
        var changedFields = this.changedFields;
        if (changedFields.isEmpty()) {
            return 0;
        }
        var n = 0;
        if (changedFields.get(1) && basicInfo.anyDeleted()) {
            n++;
        }
        if (changedFields.get(2) && wallet.anyDeleted()) {
            n++;
        }
        if (changedFields.get(3) && equipments.anyDeleted()) {
            n++;
        }
        if (changedFields.get(4) && items.anyChanged()) {
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
        if (changedFields.get(1) && basicInfo.anyDeleted()) {
            return true;
        }
        if (changedFields.get(2) && wallet.anyDeleted()) {
            return true;
        }
        if (changedFields.get(3) && equipments.anyDeleted()) {
            return true;
        }
        if (changedFields.get(4) && items.anyDeleted()) {
            return true;
        }
        return false;
    }

    @Override
    public Player clean() {
        resetStates();
        uid = 0;
        basicInfo.clean();
        wallet.clean();
        equipments.clean();
        items.clean();
        updateVersion = 0;
        createTime = LocalDateTime.MIN;
        updateTime = LocalDateTime.MIN;
        return this;
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {
        var changeFields = this.changedFields;
        if (changeFields.get(0)) {
            updates.add(Updates.set(path().resolve(BNAME_UID).value(), uid));
        }
        if (changeFields.get(1)) {
            basicInfo.appendUpdates(updates);
        }
        if (changeFields.get(2)) {
            wallet.appendUpdates(updates);
        }
        if (changeFields.get(3)) {
            equipments.appendUpdates(updates);
        }
        if (changeFields.get(4)) {
            items.appendUpdates(updates);
        }
        if (changeFields.get(5)) {
            updates.add(Updates.set(path().resolve(BNAME_UPDATE_VERSION).value(), updateVersion));
        }
        if (changeFields.get(6)) {
            updates.add(Updates.set(path().resolve(BNAME_CREATE_TIME).value(), BsonUtil.toBsonDateTime(createTime)));
        }
        if (changeFields.get(7)) {
            updates.add(Updates.set(path().resolve(BNAME_UPDATE_TIME).value(), BsonUtil.toBsonDateTime(updateTime)));
        }
    }

    @Override
    protected void loadObjectNode(ObjectNode src) {
        resetStates();
        uid = BsonUtil.intValue(src, BNAME_UID).orElseThrow();
        BsonUtil.objectValue(src, BNAME_BASIC_INFO).ifPresentOrElse(basicInfo::load, basicInfo::clean);
        BsonUtil.objectValue(src, BNAME_WALLET).ifPresentOrElse(wallet::load, wallet::clean);
        BsonUtil.objectValue(src, BNAME_EQUIPMENTS).ifPresentOrElse(equipments::load, equipments::clean);
        BsonUtil.objectValue(src, BNAME_ITEMS).ifPresentOrElse(items::load, items::clean);
        updateVersion = BsonUtil.intValue(src, BNAME_UPDATE_VERSION).orElse(0);
        createTime = BsonUtil.dateTimeValue(src, BNAME_CREATE_TIME).orElseThrow();
        updateTime = BsonUtil.dateTimeValue(src, BNAME_UPDATE_TIME).orElseThrow();
    }

    @Override
    protected void appendUpdateData(Map<Object, Object> data) {
        var changedFields = this.changedFields;
        if (changedFields.get(0)) {
            data.put("uid", uid);
        }
        if (changedFields.get(1)) {
            var updateData = basicInfo.toUpdateData();
            if (updateData != null) {
                data.put("basicInfo", updateData);
            }
        }
        if (changedFields.get(2)) {
            var updateData = wallet.toUpdateData();
            if (updateData != null) {
                data.put("wallet", updateData);
            }
        }
        if (changedFields.get(3)) {
            var updateData = equipments.toUpdateData();
            if (updateData != null) {
                data.put("equipments", updateData);
            }
        }
        if (changedFields.get(4)) {
            var updateData = items.toUpdateData();
            if (updateData != null) {
                data.put("items", updateData);
            }
        }
        if (changedFields.get(8)) {
            data.put("createdAt", getCreatedAt());
        }
        if (changedFields.get(9)) {
            data.put("updatedAt", getUpdatedAt());
        }
    }

    @Override
    protected void appendDeletedData(Map<Object, Object> data) {
        var changedFields = this.changedFields;
        if (changedFields.get(1)) {
            var deletedData = basicInfo.toDeletedData();
            if (deletedData != null) {
                data.put("basicInfo", deletedData);
            }
        }
        if (changedFields.get(2)) {
            var deletedData = wallet.toDeletedData();
            if (deletedData != null) {
                data.put("wallet", deletedData);
            }
        }
        if (changedFields.get(3)) {
            var deletedData = equipments.toDeletedData();
            if (deletedData != null) {
                data.put("equipments", deletedData);
            }
        }
        if (changedFields.get(4)) {
            var deletedData = items.toDeletedData();
            if (deletedData != null) {
                data.put("items", deletedData);
            }
        }
    }

    @Override
    public String toString() {
        return "Player(" + "uid=" + uid +
                ", basicInfo=" + basicInfo +
                ", wallet=" + wallet +
                ", equipments=" + equipments +
                ", items=" + items +
                ", updateVersion=" + updateVersion +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", friends=" + friends +
                ")";
    }

}
