package com.github.fmjsjx.bson.model2.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fmjsjx.bson.model2.core.RootModel;
import com.github.fmjsjx.bson.model2.core.SingleValueMapModel;
import com.github.fmjsjx.bson.model2.core.SingleValueTypes;
import com.github.fmjsjx.libcommon.util.DateTimeUtil;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import java.time.LocalDateTime;
import java.util.List;

public class Player extends RootModel<Player> {

    public static final String BNAME_UID = "_id";
    public static final String BNAME_BASIC_INFO = "bi";
    public static final String BNAME_WALLET = "w";
    public static final String BNAME_ITEMS = "i";
    public static final String BNAME_UPDATE_VERSION = "_uv";
    public static final String BNAME_CREATE_TIME = "_ct";
    public static final String BNAME_UPDATE_TIME = "_ut";

    private int uid;
    private final BasicInfo basicInfo = new BasicInfo().parent(this).key(BNAME_BASIC_INFO).index(1);
    private final Wallet wallet = new Wallet().parent(this).key(BNAME_WALLET).index(2);
    private final SingleValueMapModel<Integer, Integer> items = SingleValueMapModel.integerKeysMap(SingleValueTypes.INTEGER).parent(this).key(BNAME_ITEMS).index(3);
    @JsonIgnore
    private int updateVersion;
    @JsonIgnore
    private LocalDateTime createTime;
    @JsonIgnore
    private LocalDateTime updateTime;

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

    public SingleValueMapModel<Integer, Integer> getItems() {
        return items;
    }

    public int getUpdateVersion() {
        return updateVersion;
    }

    public void setUpdateVersion(int updateVersion) {
        if (this.updateVersion != updateVersion) {
            this.updateVersion = updateVersion;
            fieldChanged(4);
        }
    }

    public int increaseUpdateVersion() {
        fieldChanged(4);
        return ++updateVersion;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        if (!this.createTime.equals(createTime)) {
            this.createTime = createTime;
            fieldChanged(5);
        }
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        if (!this.updateTime.equals(updateTime)) {
            this.updateTime = updateTime;
            fieldChanged(6);
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
        items.reset();
    }

    @Override
    protected int deletedSize() {
        var n = 0;
        if (basicInfo.anyDeleted()) {
            n++;
        }
        if (wallet.anyDeleted()) {
            n++;
        }
        if (items.anyChanged()) {
            n++;
        }
        return n;
    }

    @Override
    protected Object toSubUpdateData() {
        return null;
    }

    @Override
    public BsonDocument toBson() {
        return null;
    }

    @Override
    public void load(BsonDocument src) {

    }

    @Override
    public JsonNode toJsonNode() {
        return null;
    }

    @Override
    public Object toData() {
        return null;
    }

    @Override
    public Object toDeletedData() {
        return null;
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {

    }

    @Override
    protected void loadObjectNode(ObjectNode src) {

    }
}
