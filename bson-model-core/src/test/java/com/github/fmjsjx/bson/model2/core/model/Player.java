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
import java.util.LinkedHashMap;
import java.util.List;

public class Player extends RootModel<Player> {

    public static final String BNAME_UID = "_id";
    public static final String BNAME_BASIC_INFO = "bi";
    public static final String BNAME_WALLET = "w";
    public static final String BNAME_EQUIPMENTS = "e";
    public static final String BNAME_ITEMS = "i";
    public static final String BNAME_UPDATE_VERSION = "_uv";
    public static final String BNAME_CREATE_TIME = "_ct";
    public static final String BNAME_UPDATE_TIME = "_ut";

    private int uid;
    private final BasicInfo basicInfo = new BasicInfo().parent(this).key(BNAME_BASIC_INFO).index(1);
    private final Wallet wallet = new Wallet().parent(this).key(BNAME_WALLET).index(2);
    private final DefaultMapModel<String, Equipment> equipments = DefaultMapModel.stringKeysMap(Equipment::new).parent(this).key(BNAME_EQUIPMENTS).index(3);
    private final SingleValueMapModel<Integer, Integer> items = SingleValueMapModel.integerKeysMap(SingleValueTypes.INTEGER).parent(this).key(BNAME_ITEMS).index(4);
    private int updateVersion;
    private LocalDateTime createTime = LocalDateTime.MIN;
    private LocalDateTime updateTime = LocalDateTime.MIN;

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
        if (!this.createTime.equals(createTime)) {
            this.createTime = createTime;
            fieldChanged(6);
        }
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        if (!this.updateTime.equals(updateTime)) {
            this.updateTime = updateTime;
            fieldChanged(7);
        }
    }

    public long getCreatedAt() {
        return DateTimeUtil.toEpochMilli(createTime);
    }

    public long getUpdatedAt() {
        return DateTimeUtil.toEpochMilli(updateTime);
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
    protected Object toSubUpdateData() {
        var data = new LinkedHashMap<>();
        var changedFields = this.changedFields;
        if (changedFields.get(0)) {
            data.put("uid", uid);
        }
        if (changedFields.get(1)) {
            data.put("basicInfo", basicInfo.toUpdateData());
        }
        if (changedFields.get(2)) {
            data.put("wallet", wallet.toUpdateData());
        }
        if (changedFields.get(3)) {
            data.put("equipments", equipments.toUpdateData());
        }
        if (changedFields.get(4)) {
            data.put("items", items.toUpdateData());
        }
        if (changedFields.get(6)) {
            data.put("createdAt", getCreatedAt());
        }
        if (changedFields.get(7)) {
            data.put("updatedAt", getUpdatedAt());
        }
        return data;
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
    public void load(BsonDocument src) {
        resetStates();
        uid = BsonUtil.intValue(src, BNAME_UID).orElseThrow();
        BsonUtil.documentValue(src, BNAME_BASIC_INFO).ifPresentOrElse(basicInfo::load, basicInfo::clean);
        BsonUtil.documentValue(src, BNAME_WALLET).ifPresentOrElse(wallet::load, wallet::clean);
        BsonUtil.documentValue(src, BNAME_EQUIPMENTS).ifPresentOrElse(equipments::load, equipments::clean);
        BsonUtil.documentValue(src, BNAME_ITEMS).ifPresentOrElse(items::load, items::clean);
        updateVersion = BsonUtil.intValue(src, BNAME_UPDATE_VERSION).orElse(0);
        createTime = BsonUtil.dateTimeValue(src, BNAME_CREATE_TIME).orElseThrow();
        updateTime = BsonUtil.dateTimeValue(src, BNAME_UPDATE_TIME).orElseThrow();
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
    public Object toDeletedData() {
        var data = new LinkedHashMap<>();
        var changedFields = this.changedFields;
        var basicInfo = this.basicInfo;
        if (changedFields.get(1) && basicInfo.anyDeleted()) {
            data.put("basicInfo", basicInfo.toDeletedData());
        }
        var wallet = this.wallet;
        if (changedFields.get(2) && wallet.anyDeleted()) {
            data.put("wallet", wallet.toDeletedData());
        }
        var equipments = this.equipments;
        if (changedFields.get(3) && equipments.anyDeleted()) {
            data.put("equipments", equipments.toDeletedData());
        }
        var items = this.items;
        if (changedFields.get(3) && items.anyDeleted()) {
            data.put("items", items.toDeletedData());
        }
        return data;
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

}
