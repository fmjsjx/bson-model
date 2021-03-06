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
import com.github.fmjsjx.bson.model.core.DefaultListModel;
import com.github.fmjsjx.bson.model.core.DefaultMapModel;
import com.github.fmjsjx.bson.model.core.RootModel;
import com.github.fmjsjx.bson.model.core.SimpleMapModel;
import com.github.fmjsjx.bson.model.core.SimpleValueTypes;
import com.github.fmjsjx.libcommon.util.DateTimeUtil;
import com.github.fmjsjx.libcommon.util.ObjectUtil;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.mongodb.client.model.Updates;

public class Player extends RootModel<Player> {

    public static final String BNAME_UID = "_id";
    public static final String BNAME_WALLET = "wt";
    public static final String BNAME_EQUIPMENTS = "eqm";
    public static final String BNAME_ITEMS = "itm";
    public static final String BNAME_CASH = "cs";
    public static final String BNAME_GIFTS = "gfs";
    public static final String BNAME_UPDATE_VERSION = "_uv";
    public static final String BNAME_CREATE_TIME = "_ct";
    public static final String BNAME_UPDATE_TIME = "_ut";
    public static final String BNAME_TEST_SUB1 = "ts1";

    private int uid;
    private final Wallet wallet = new Wallet(this);
    private final DefaultMapModel<String, Equipment, Player> equipments = DefaultMapModel.stringKeys(this, "eqm", Equipment::new);
    private final SimpleMapModel<Integer, Integer, Player> items = SimpleMapModel.integerKeys(this, "itm", SimpleValueTypes.INTEGER);
    private final CashInfo cash = new CashInfo(this);
    private final DefaultListModel<GiftInfo, Player> gifts = new DefaultListModel<>(this, "gfs", GiftInfo::new);
    @JsonIgnore
    private int updateVersion;
    @JsonIgnore
    private LocalDateTime createTime;
    @JsonIgnore
    private LocalDateTime updateTime;
    private final DefaultMapModel<Integer, TestSub1, Player> testSub1 = DefaultMapModel.integerKeys(this, "ts1", TestSub1::new);

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        if (this.uid != uid) {
            this.uid = uid;
            fieldUpdated(1);
        }
    }

    public Wallet getWallet() {
        return wallet;
    }

    public DefaultMapModel<String, Equipment, Player> getEquipments() {
        return equipments;
    }

    public SimpleMapModel<Integer, Integer, Player> getItems() {
        return items;
    }

    public CashInfo getCash() {
        return cash;
    }

    public DefaultListModel<GiftInfo, Player> getGifts() {
        return gifts;
    }

    @JsonIgnore
    public int getUpdateVersion() {
        return updateVersion;
    }

    public void setUpdateVersion(int updateVersion) {
        if (this.updateVersion != updateVersion) {
            this.updateVersion = updateVersion;
            fieldUpdated(7);
        }
    }

    public int increaseUpdateVersion() {
        var updateVersion = this.updateVersion += 1;
        fieldUpdated(7);
        return updateVersion;
    }

    @JsonIgnore
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        if (ObjectUtil.isNotEquals(this.createTime, createTime)) {
            this.createTime = createTime;
            fieldUpdated(8);
        }
    }

    @JsonIgnore
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        if (ObjectUtil.isNotEquals(this.updateTime, updateTime)) {
            this.updateTime = updateTime;
            fieldUpdated(9);
        }
    }

    public DefaultMapModel<Integer, TestSub1, Player> getTestSub1() {
        return testSub1;
    }

    @Override
    public boolean updated() {
        if (wallet.updated() || equipments.updated() || items.updated() || cash.updated() || gifts.updated() || testSub1.updated()) {
            return true;
        }
        return super.updated();
    }

    @Override
    public BsonDocument toBson() {
        var bson = new BsonDocument();
        bson.append("_id", new BsonInt32(uid));
        bson.append("wt", wallet.toBson());
        bson.append("eqm", equipments.toBson());
        bson.append("itm", items.toBson());
        bson.append("cs", cash.toBson());
        var gifts = this.gifts;
        if (!gifts.nil()) {
            bson.append("gfs", gifts.toBson());
        }
        bson.append("_uv", new BsonInt32(updateVersion));
        bson.append("_ct", BsonUtil.toBsonDateTime(createTime));
        if (updateTime != null) {
            bson.append("_ut", BsonUtil.toBsonDateTime(updateTime));
        }
        bson.append("ts1", testSub1.toBson());
        return bson;
    }

    @Override
    public Document toDocument() {
        var doc = new Document();
        doc.append("_id", uid);
        doc.append("wt", wallet.toDocument());
        doc.append("eqm", equipments.toDocument());
        doc.append("itm", items.toDocument());
        doc.append("cs", cash.toDocument());
        var gifts = this.gifts;
        if (!gifts.nil()) {
            doc.append("gfs", gifts.toDocuments());
        }
        doc.append("_uv", updateVersion);
        doc.append("_ct", DateTimeUtil.toLegacyDate(createTime));
        if (updateTime != null) {
            doc.append("_ut", DateTimeUtil.toLegacyDate(updateTime));
        }
        doc.append("ts1", testSub1.toDocument());
        return doc;
    }

    @Override
    public Map<String, ?> toData() {
        var data = new LinkedHashMap<String, Object>();
        data.put("_id", uid);
        data.put("wt", wallet.toData());
        data.put("eqm", equipments.toData());
        data.put("itm", items.toData());
        data.put("cs", cash.toData());
        data.put("gfs", gifts.toData());
        data.put("_uv", updateVersion);
        data.put("_ct", DateTimeUtil.toEpochMilli(createTime));
        if (updateTime != null) {
            data.put("_ut", DateTimeUtil.toEpochMilli(updateTime));
        }
        data.put("ts1", testSub1.toData());
        return data;
    }

    @Override
    public void load(Document src) {
        uid = BsonUtil.intValue(src, "_id").getAsInt();
        BsonUtil.documentValue(src, "wt").ifPresentOrElse(wallet::load, wallet::reset);
        BsonUtil.documentValue(src, "eqm").ifPresentOrElse(equipments::load, equipments::clear);
        BsonUtil.documentValue(src, "itm").ifPresentOrElse(items::load, items::clear);
        BsonUtil.documentValue(src, "cs").ifPresentOrElse(cash::load, cash::reset);
        BsonUtil.listValue(src, "gfs").ifPresentOrElse(gifts::load, gifts::clean);
        updateVersion = BsonUtil.intValue(src, "_uv").orElse(0);
        createTime = BsonUtil.dateTimeValue(src, "_ct").get();
        updateTime = BsonUtil.dateTimeValue(src, "_ut").orElseGet(LocalDateTime::now);
        BsonUtil.documentValue(src, "ts1").ifPresentOrElse(testSub1::load, testSub1::clear);
        reset();
    }

    @Override
    public void load(BsonDocument src) {
        uid = BsonUtil.intValue(src, "_id").getAsInt();
        BsonUtil.documentValue(src, "wt").ifPresentOrElse(wallet::load, wallet::reset);
        BsonUtil.documentValue(src, "eqm").ifPresentOrElse(equipments::load, equipments::clear);
        BsonUtil.documentValue(src, "itm").ifPresentOrElse(items::load, items::clear);
        BsonUtil.documentValue(src, "cs").ifPresentOrElse(cash::load, cash::reset);
        BsonUtil.arrayValue(src, "gfs").ifPresentOrElse(gifts::load, gifts::clean);
        updateVersion = BsonUtil.intValue(src, "_uv").orElse(0);
        createTime = BsonUtil.dateTimeValue(src, "_ct").get();
        updateTime = BsonUtil.dateTimeValue(src, "_ut").orElseGet(LocalDateTime::now);
        BsonUtil.documentValue(src, "ts1").ifPresentOrElse(testSub1::load, testSub1::clear);
        reset();
    }

    @Override
    public void load(Any src) {
        if (src.valueType() != ValueType.OBJECT) {
            reset();
            return;
        }
        uid = BsonUtil.intValue(src, "_id").getAsInt();
        BsonUtil.objectValue(src, "wt").ifPresentOrElse(wallet::load, wallet::reset);
        BsonUtil.objectValue(src, "eqm").ifPresentOrElse(equipments::load, equipments::clear);
        BsonUtil.objectValue(src, "itm").ifPresentOrElse(items::load, items::clear);
        BsonUtil.objectValue(src, "cs").ifPresentOrElse(cash::load, cash::reset);
        BsonUtil.arrayValue(src, "gfs").ifPresentOrElse(gifts::load, gifts::clean);
        updateVersion = BsonUtil.intValue(src, "_uv").orElse(0);
        createTime = BsonUtil.dateTimeValue(src, "_ct").get();
        updateTime = BsonUtil.dateTimeValue(src, "_ut").orElseGet(LocalDateTime::now);
        BsonUtil.objectValue(src, "ts1").ifPresentOrElse(testSub1::load, testSub1::clear);
        reset();
    }

    @Override
    public void load(JsonNode src) {
        if (!src.isObject()) {
            reset();
            return;
        }
        uid = BsonUtil.intValue(src, "_id").getAsInt();
        BsonUtil.objectValue(src, "wt").ifPresentOrElse(wallet::load, wallet::reset);
        BsonUtil.objectValue(src, "eqm").ifPresentOrElse(equipments::load, equipments::clear);
        BsonUtil.objectValue(src, "itm").ifPresentOrElse(items::load, items::clear);
        BsonUtil.objectValue(src, "cs").ifPresentOrElse(cash::load, cash::reset);
        BsonUtil.arrayValue(src, "gfs").ifPresentOrElse(gifts::load, gifts::clean);
        updateVersion = BsonUtil.intValue(src, "_uv").orElse(0);
        createTime = BsonUtil.dateTimeValue(src, "_ct").get();
        updateTime = BsonUtil.dateTimeValue(src, "_ut").orElseGet(LocalDateTime::now);
        BsonUtil.objectValue(src, "ts1").ifPresentOrElse(testSub1::load, testSub1::clear);
        reset();
    }

    public boolean uidUpdated() {
        return updatedFields.get(1);
    }

    public boolean walletUpdated() {
        return wallet.updated();
    }

    public boolean equipmentsUpdated() {
        return equipments.updated();
    }

    public boolean itemsUpdated() {
        return items.updated();
    }

    public boolean cashUpdated() {
        return cash.updated();
    }

    public boolean giftsUpdated() {
        return gifts.updated();
    }

    public boolean updateVersionUpdated() {
        return updatedFields.get(7);
    }

    public boolean createTimeUpdated() {
        return updatedFields.get(8);
    }

    public boolean updateTimeUpdated() {
        return updatedFields.get(9);
    }

    public boolean testSub1Updated() {
        return testSub1.updated();
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {
        var updatedFields = this.updatedFields;
        if (updatedFields.get(1)) {
            updates.add(Updates.set("_id", uid));
        }
        var wallet = this.wallet;
        if (wallet.updated()) {
            wallet.appendUpdates(updates);
        }
        var equipments = this.equipments;
        if (equipments.updated()) {
            equipments.appendUpdates(updates);
        }
        var items = this.items;
        if (items.updated()) {
            items.appendUpdates(updates);
        }
        var cash = this.cash;
        if (cash.updated()) {
            cash.appendUpdates(updates);
        }
        var gifts = this.gifts;
        if (gifts.updated()) {
            gifts.appendUpdates(updates);
        }
        if (updatedFields.get(7)) {
            updates.add(Updates.set("_uv", updateVersion));
        }
        if (updatedFields.get(8)) {
            updates.add(Updates.set("_ct", BsonUtil.toBsonDateTime(createTime)));
        }
        if (updatedFields.get(9)) {
            updates.add(Updates.set("_ut", BsonUtil.toBsonDateTime(updateTime)));
        }
        var testSub1 = this.testSub1;
        if (testSub1.updated()) {
            testSub1.appendUpdates(updates);
        }
    }

    @Override
    protected void resetChildren() {
        wallet.reset();
        equipments.reset();
        items.reset();
        cash.reset();
        gifts.reset();
        testSub1.reset();
    }

    @Override
    public Object toSubUpdate() {
        var update = new LinkedHashMap<>();
        var updatedFields = this.updatedFields;
        if (updatedFields.get(1)) {
            update.put("uid", uid);
        }
        if (wallet.updated()) {
            update.put("wallet", wallet.toUpdate());
        }
        if (equipments.updated()) {
            update.put("equipments", equipments.toUpdate());
        }
        if (items.updated()) {
            update.put("items", items.toUpdate());
        }
        if (cash.updated()) {
            update.put("cash", cash.toUpdate());
        }
        if (gifts.updated()) {
            update.put("gifts", gifts.toUpdate());
        }
        if (testSub1.updated()) {
            update.put("testSub1", testSub1.toUpdate());
        }
        return update;
    }

    @Override
    public Map<Object, Object> toDelete() {
        var delete = new LinkedHashMap<>();
        var wallet = this.wallet;
        if (wallet.deletedSize() > 0) {
            delete.put("wallet", wallet.toDelete());
        }
        var equipments = this.equipments;
        if (equipments.deletedSize() > 0) {
            delete.put("equipments", equipments.toDelete());
        }
        var items = this.items;
        if (items.deletedSize() > 0) {
            delete.put("items", items.toDelete());
        }
        var cash = this.cash;
        if (cash.deletedSize() > 0) {
            delete.put("cash", cash.toDelete());
        }
        var gifts = this.gifts;
        if (gifts.deletedSize() > 0) {
            delete.put("gifts", gifts.toDelete());
        }
        var testSub1 = this.testSub1;
        if (testSub1.deletedSize() > 0) {
            delete.put("testSub1", testSub1.toDelete());
        }
        return delete;
    }

    @Override
    protected int deletedSize() {
        var n = 0;
        if (wallet.deletedSize() > 0) {
            n++;
        }
        if (equipments.deletedSize() > 0) {
            n++;
        }
        if (items.deletedSize() > 0) {
            n++;
        }
        if (cash.deletedSize() > 0) {
            n++;
        }
        if (gifts.deletedSize() > 0) {
            n++;
        }
        if (testSub1.deletedSize() > 0) {
            n++;
        }
        return n;
    }

    @Override
    public String toString() {
        return "Player(" + "uid=" + uid + ", " + "wallet=" + wallet + ", " + "equipments=" + equipments + ", " + "items=" + items + ", " + "cash=" + cash + ", " + "gifts=" + gifts + ", " + "updateVersion=" + updateVersion + ", " + "createTime=" + createTime + ", " + "updateTime=" + updateTime + ", " + "testSub1=" + testSub1 + ")";
    }

}
