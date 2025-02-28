package com.github.fmjsjx.bson.model2.generator.model;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.fmjsjx.bson.model.core.BsonUtil;
import com.github.fmjsjx.bson.model2.core.*;
import com.mongodb.client.model.Updates;
import org.bson.*;
import org.bson.conversions.Bson;

import java.util.*;

public class Wallet extends ObjectModel<Wallet> {

    public static final String BNAME_COIN_TOTAL = "ct";
    public static final String BNAME_COIN_USED = "cu";
    public static final String BNAME_DIAMOND = "d";
    public static final String BNAME_AD = "ad";

    private long coinTotal;
    private long coinUsed;
    private long diamond;
    private long ad;

    public long getCoinTotal() {
        return coinTotal;
    }

    public void setCoinTotal(long coinTotal) {
        if (coinTotal != this.coinTotal) {
            this.coinTotal = coinTotal;
            fieldsChanged(0, 2);
        }
    }

    public long addCoinTotal(long coinTotal) {
        coinTotal = this.coinTotal += coinTotal;
        fieldsChanged(0, 2);
        return coinTotal;
    }

    public long getCoinUsed() {
        return coinUsed;
    }

    public void setCoinUsed(long coinUsed) {
        if (coinUsed != this.coinUsed) {
            this.coinUsed = coinUsed;
            fieldsChanged(1, 2);
        }
    }

    public long addCoinUsed(long coinUsed) {
        coinUsed = this.coinUsed += coinUsed;
        fieldsChanged(1, 2);
        return coinUsed;
    }

    public long getCoin() {
        return coinTotal - coinUsed;
    }

    public long getDiamond() {
        return diamond;
    }

    public void setDiamond(long diamond) {
        if (diamond != this.diamond) {
            this.diamond = diamond;
            fieldChanged(3);
        }
    }

    public long getAd() {
        return ad;
    }

    public void setAd(long ad) {
        if (ad != this.ad) {
            this.ad = ad;
            fieldChanged(4);
        }
    }

    public long increaseAd() {
        fieldChanged(4);
        return ++ad;
    }

    public boolean coinTotalChanged() {
        return changedFields.get(0);
    }

    public boolean coinUsedChanged() {
        return changedFields.get(1);
    }

    public boolean coinChanged() {
        return changedFields.get(2);
    }

    public boolean diamondChanged() {
        return changedFields.get(3);
    }

    public boolean adChanged() {
        return changedFields.get(4);
    }

    @Override
    public BsonDocument toBson() {
        var bson = new BsonDocument();
        bson.append(BNAME_COIN_TOTAL, new BsonInt64(coinTotal));
        bson.append(BNAME_COIN_USED, new BsonInt64(coinUsed));
        bson.append(BNAME_DIAMOND, new BsonInt64(diamond));
        bson.append(BNAME_AD, new BsonInt64(ad));
        return bson;
    }

    @Override
    public Wallet load(BsonDocument src) {
        resetStates();
        coinTotal = BsonUtil.longValue(src, BNAME_COIN_TOTAL).orElse(0);
        coinUsed = BsonUtil.longValue(src, BNAME_COIN_USED).orElse(0);
        diamond = BsonUtil.longValue(src, BNAME_DIAMOND).orElse(0);
        ad = BsonUtil.longValue(src, BNAME_AD).orElse(0);
        return this;
    }

    @Override
    public JsonNode toJsonNode() {
        var jsonNode = JsonNodeFactory.instance.objectNode();
        jsonNode.put(BNAME_COIN_TOTAL, coinTotal);
        jsonNode.put(BNAME_COIN_USED, coinUsed);
        jsonNode.put(BNAME_DIAMOND, diamond);
        jsonNode.put(BNAME_AD, ad);
        return jsonNode;
    }

    @Override
    public JSONObject toFastjson2Node() {
        var jsonObject = new JSONObject();
        jsonObject.put(BNAME_COIN_TOTAL, coinTotal);
        jsonObject.put(BNAME_COIN_USED, coinUsed);
        jsonObject.put(BNAME_DIAMOND, diamond);
        jsonObject.put(BNAME_AD, ad);
        return jsonObject;
    }

    @Override
    public Map<Object, Object> toData() {
        var data = new LinkedHashMap<>();
        data.put("coinTotal", coinTotal);
        data.put("coin", getCoin());
        data.put("diamond", diamond);
        data.put("ad", ad);
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
    public Wallet clean() {
        coinTotal = 0;
        coinUsed = 0;
        diamond = 0;
        ad = 0;
        resetStates();
        return this;
    }

    @Override
    public Wallet deepCopy() {
        var copy = new Wallet();
        deepCopyTo(copy, false);
        return copy;
    }

    @Override
    public void deepCopyFrom(Wallet src) {
        coinTotal = src.coinTotal;
        coinUsed = src.coinUsed;
        diamond = src.diamond;
        ad = src.ad;
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {
        var changedFields = this.changedFields;
        if (changedFields.isEmpty()) {
            return;
        }
        if (changedFields.get(0)) {
            updates.add(Updates.set(path().resolve(BNAME_COIN_TOTAL).value(), coinTotal));
        }
        if (changedFields.get(1)) {
            updates.add(Updates.set(path().resolve(BNAME_COIN_USED).value(), coinUsed));
        }
        if (changedFields.get(3)) {
            updates.add(Updates.set(path().resolve(BNAME_DIAMOND).value(), diamond));
        }
        if (changedFields.get(4)) {
            updates.add(Updates.set(path().resolve(BNAME_AD).value(), ad));
        }
    }

    @Override
    protected void loadObjectNode(JsonNode src) {
        resetStates();
        coinTotal = BsonUtil.longValue(src, BNAME_COIN_TOTAL).orElse(0);
        coinUsed = BsonUtil.longValue(src, BNAME_COIN_USED).orElse(0);
        diamond = BsonUtil.longValue(src, BNAME_DIAMOND).orElse(0);
        ad = BsonUtil.longValue(src, BNAME_AD).orElse(0);
    }

    @Override
    protected void loadJSONObject(JSONObject src) {
        resetStates();
        coinTotal = BsonUtil.longValue(src, BNAME_COIN_TOTAL).orElse(0);
        coinUsed = BsonUtil.longValue(src, BNAME_COIN_USED).orElse(0);
        diamond = BsonUtil.longValue(src, BNAME_DIAMOND).orElse(0);
        ad = BsonUtil.longValue(src, BNAME_AD).orElse(0);
    }

    @Override
    protected void appendUpdateData(Map<Object, Object> data) {
        var changedFields = this.changedFields;
        if (changedFields.isEmpty()) {
            return;
        }
        if (changedFields.get(0)) {
            data.put("coinTotal", coinTotal);
        }
        if (changedFields.get(2)) {
            data.put("coin", getCoin());
        }
        if (changedFields.get(3)) {
            data.put("diamond", diamond);
        }
        if (changedFields.get(4)) {
            data.put("ad", ad);
        }
    }

    @Override
    public Map<Object, Object> toDeletedData() {
        return null;
    }

    @Override
    protected void appendDeletedData(Map<Object, Object> data) {
    }

    @Override
    public String toString() {
        return "Wallet(" + "coinTotal=" + coinTotal +
                ", coinUsed=" + coinUsed +
                ", diamond=" + diamond +
                ", ad=" + ad +
                ")";
    }

}
