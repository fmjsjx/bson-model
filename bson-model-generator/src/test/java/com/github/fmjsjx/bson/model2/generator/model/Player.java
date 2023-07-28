package com.github.fmjsjx.bson.model2.generator.model;

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

public class Player extends RootModel<Player> {

    public static final int ONLINE = 1;
    public static final int OFFLINE = 0;
    public static final String ROLE_GM = "GM";
    public static final LocalDateTime DOOMSDAY = LocalDateTime.of(2038, 9, 7, 11, 38, 59);

    public static final String BNAME_ID = "_id";
    public static final String BNAME_BASIC_INFO = "bi";
    public static final String BNAME_WALLET = "w";
    public static final String BNAME_EQUIPMENTS = "e";
    public static final String BNAME_ITEMS = "i";
    public static final String BNAME_UPDATE_VERSION = "_uv";
    public static final String BNAME_CREATE_TIME = "_ct";
    public static final String BNAME_UPDATE_TIME = "_ut";
    public static final String BNAME_FRIENDS = "f";

    private int id;
    private final BasicInfo basicInfo = new BasicInfo().parent(this).key(BNAME_BASIC_INFO).index(1);
    private final Wallet wallet = new Wallet().parent(this).key(BNAME_WALLET).index(2);
    private final DefaultMapModel<String, Equipment> equipments = DefaultMapModel.stringKeysMap(Equipment::new).parent(this).key(BNAME_EQUIPMENTS).index(3);
    private final SingleValueMapModel<Integer, Integer> items = SingleValueMapModel.integerKeysMap(SingleValueTypes.INTEGER).parent(this).key(BNAME_ITEMS).index(4);
    private int updateVersion;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<Player> friends;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id != this.id) {
            this.id = id;
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
        if (updateVersion != this.updateVersion) {
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
        if (!createTime.equals(this.createTime)) {
            this.createTime = createTime;
            fieldsChanged(6, 8);
        }
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        Objects.requireNonNull(updateTime, "updateTime must not be null");
        if (!updateTime.equals(this.updateTime)) {
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

    public boolean idChanged() {
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
        bson.append(BNAME_ID, new BsonInt32(id));
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
        id = BsonUtil.intValue(src, BNAME_ID).orElseThrow();
        BsonUtil.documentValue(src, BNAME_BASIC_INFO).ifPresentOrElse(basicInfo::load, basicInfo::clean);
        BsonUtil.documentValue(src, BNAME_WALLET).ifPresentOrElse(wallet::load, wallet::clean);
        BsonUtil.documentValue(src, BNAME_EQUIPMENTS).ifPresentOrElse(equipments::load, equipments::clean);
        BsonUtil.documentValue(src, BNAME_ITEMS).ifPresentOrElse(items::load, items::clean);
        updateVersion = BsonUtil.intValue(src, BNAME_UPDATE_VERSION).orElse(0);
        createTime = BsonUtil.dateTimeValue(src, BNAME_CREATE_TIME).orElseThrow();
        updateTime = BsonUtil.dateTimeValue(src, BNAME_UPDATE_TIME).orElseThrow();
        friends = BsonUtil.arrayValue(src, BNAME_FRIENDS, (BsonDocument v) -> new Player().load(v)).orElse(null);
        return this;
    }

    @Override
    public JsonNode toJsonNode() {
        var jsonNode = JsonNodeFactory.instance.objectNode();
        jsonNode.put(BNAME_ID, id);
        jsonNode.set(BNAME_BASIC_INFO, basicInfo.toJsonNode());
        jsonNode.set(BNAME_WALLET, wallet.toJsonNode());
        jsonNode.set(BNAME_EQUIPMENTS, equipments.toJsonNode());
        jsonNode.set(BNAME_ITEMS, items.toJsonNode());
        jsonNode.put(BNAME_UPDATE_VERSION, updateVersion);
        jsonNode.put(BNAME_CREATE_TIME, DateTimeUtil.toEpochMilli(createTime));
        jsonNode.put(BNAME_UPDATE_TIME, DateTimeUtil.toEpochMilli(updateTime));
        var friends = this.friends;
        if (friends != null) {
            var friendsArrayNode = jsonNode.arrayNode(friends.size());
            friends.stream().map(Player::toJsonNode).forEach(friendsArrayNode::add);
            jsonNode.set(BNAME_FRIENDS, friendsArrayNode);
        }
        return jsonNode;
    }

    @Override
    public Object toData() {
        var data = new LinkedHashMap<>();
        data.put("uid", id);
        data.put("basicInfo", basicInfo.toData());
        data.put("wallet", wallet.toData());
        data.put("equipments", equipments.toData());
        data.put("items", items.toData());
        data.put("createdAt", getCreatedAt());
        data.put("updatedAt", getUpdatedAt());
        var friends = this.friends;
        if (friends != null) {
            data.put("friends", friends.stream().map(Player::toData).toList());
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
        if (changedFields.get(4) && items.anyDeleted()) {
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
        id = 0;
        basicInfo.clean();
        wallet.clean();
        equipments.clean();
        items.clean();
        updateVersion = 0;
        createTime = null;
        updateTime = null;
        friends = null;
        resetStates();
        return this;
    }

    @Override
    public Player deepCopy() {
        var copy = new Player();
        deepCopyTo(copy, false);
        return copy;
    }

    @Override
    public void deepCopyFrom(Player src) {
        id = src.id;
        src.basicInfo.deepCopyTo(basicInfo, false);
        src.wallet.deepCopyTo(wallet, false);
        src.equipments.deepCopyTo(equipments, false);
        src.items.deepCopyTo(items, false);
        updateVersion = src.updateVersion;
        createTime = src.createTime;
        updateTime = src.updateTime;
        var friends = src.friends;
        if (friends != null) {
            var friendsCopy = new ArrayList<Player>(friends.size());
            for (var friendsCopyValue : friends) {
                if (friendsCopyValue == null) {
                    friendsCopy.add(null);
                } else {
                    friendsCopy.add(friendsCopyValue.deepCopy());
                }
            }
            this.friends = friendsCopy;
        } else {
            this.friends = null;
        }
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
            basicInfo.appendUpdates(updates);
        }
        if (changedFields.get(2)) {
            wallet.appendUpdates(updates);
        }
        if (changedFields.get(3)) {
            equipments.appendUpdates(updates);
        }
        if (changedFields.get(4)) {
            items.appendUpdates(updates);
        }
        if (changedFields.get(5)) {
            updates.add(Updates.set(path().resolve(BNAME_UPDATE_VERSION).value(), updateVersion));
        }
        if (changedFields.get(6)) {
            updates.add(Updates.set(path().resolve(BNAME_CREATE_TIME).value(), BsonUtil.toBsonDateTime(createTime)));
        }
        if (changedFields.get(7)) {
            updates.add(Updates.set(path().resolve(BNAME_UPDATE_TIME).value(), BsonUtil.toBsonDateTime(updateTime)));
        }
    }

    @Override
    protected void loadObjectNode(JsonNode src) {
        resetStates();
        id = BsonUtil.intValue(src, BNAME_ID).orElseThrow();
        BsonUtil.objectValue(src, BNAME_BASIC_INFO).ifPresentOrElse(basicInfo::load, basicInfo::clean);
        BsonUtil.objectValue(src, BNAME_WALLET).ifPresentOrElse(wallet::load, wallet::clean);
        BsonUtil.objectValue(src, BNAME_EQUIPMENTS).ifPresentOrElse(equipments::load, equipments::clean);
        BsonUtil.objectValue(src, BNAME_ITEMS).ifPresentOrElse(items::load, items::clean);
        updateVersion = BsonUtil.intValue(src, BNAME_UPDATE_VERSION).orElse(0);
        createTime = BsonUtil.dateTimeValue(src, BNAME_CREATE_TIME).orElseThrow();
        updateTime = BsonUtil.dateTimeValue(src, BNAME_UPDATE_TIME).orElseThrow();
        friends = BsonUtil.listValue(src, BNAME_FRIENDS, v -> new Player().load(v)).orElse(null);
    }

    @Override
    protected void appendUpdateData(Map<Object, Object> data) {
        var changedFields = this.changedFields;
        if (changedFields.isEmpty()) {
            return;
        }
        if (changedFields.get(0)) {
            data.put("uid", id);
        }
        if (changedFields.get(1)) {
            var basicInfoUpdateData = basicInfo.toUpdateData();
            if (basicInfoUpdateData != null) {
                data.put("basicInfo", basicInfoUpdateData);
            }
        }
        if (changedFields.get(2)) {
            var walletUpdateData = wallet.toUpdateData();
            if (walletUpdateData != null) {
                data.put("wallet", walletUpdateData);
            }
        }
        if (changedFields.get(3)) {
            var equipmentsUpdateData = equipments.toUpdateData();
            if (equipmentsUpdateData != null) {
                data.put("equipments", equipmentsUpdateData);
            }
        }
        if (changedFields.get(4)) {
            var itemsUpdateData = items.toUpdateData();
            if (itemsUpdateData != null) {
                data.put("items", itemsUpdateData);
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
            var basicInfoDeletedData = basicInfo.toDeletedData();
            if (basicInfoDeletedData != null) {
                data.put("basicInfo", basicInfoDeletedData);
            }
        }
        if (changedFields.get(2)) {
            var walletDeletedData = wallet.toDeletedData();
            if (walletDeletedData != null) {
                data.put("wallet", walletDeletedData);
            }
        }
        if (changedFields.get(3)) {
            var equipmentsDeletedData = equipments.toDeletedData();
            if (equipmentsDeletedData != null) {
                data.put("equipments", equipmentsDeletedData);
            }
        }
        if (changedFields.get(4)) {
            var itemsDeletedData = items.toDeletedData();
            if (itemsDeletedData != null) {
                data.put("items", itemsDeletedData);
            }
        }
    }

    @Override
    public String toString() {
        return "Player(" + "id=" + id +
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
