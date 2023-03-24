package com.github.fmjsjx.bson.model2.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fmjsjx.bson.model.core.BsonUtil;
import com.github.fmjsjx.bson.model2.core.ObjectModel;
import com.mongodb.client.model.Updates;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.conversions.Bson;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Wallet extends ObjectModel<Wallet> {

    public static final String BNAME_COIN_TOTAL = "ct";
    public static final String BNAME_COIN_USED = "cu";
    public static final String BNAME_DIAMOND = "d";
    public static final String BNAME_AD = "ad";

    private long coinTotal;
    private long coinUsed;
    private long diamond;
    private int ad;

    public long getCoinTotal() {
        return coinTotal;
    }

    public void setCoinTotal(long coinTotal) {
        if (this.coinTotal != coinTotal) {
            this.coinTotal = coinTotal;
            fieldChanged(0);
            fieldChanged(2);
        }
    }

    public long addCoinTotal(long n) {
        var coinTotal = this.coinTotal += n;
        fieldChanged(0);
        fieldChanged(2);
        return coinTotal;
    }

    public long getCoinUsed() {
        return coinUsed;
    }

    public void setCoinUsed(long coinUsed) {
        if (this.coinUsed != coinUsed) {
            this.coinUsed = coinUsed;
            fieldChanged(1);
            fieldChanged(2);
        }
    }

    public long addCoinUsed(long n) {
        var coinUsed = this.coinUsed += n;
        fieldChanged(1);
        fieldChanged(2);
        return coinUsed;
    }

    public long getCoin() {
        return coinTotal - coinUsed;
    }

    public long getDiamond() {
        return diamond;
    }

    public void setDiamond(long diamond) {
        if (this.diamond != diamond) {
            this.diamond = diamond;
            fieldChanged(3);
        }
    }

    public int getAd() {
        return ad;
    }

    public void setAd(int ad) {
        if (this.ad != ad) {
            this.ad = ad;
            fieldChanged(4);
        }
    }

    public int increaseAd() {
        var ad = this.ad += 1;
        fieldChanged(4);
        return ad;
    }

    @Override
    protected void resetChildren() {
    }

    @Override
    protected int deletedSize() {
        return 0;
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
    public BsonDocument toBson() {
        var bson = new BsonDocument();
        bson.append(BNAME_COIN_TOTAL, new BsonInt64(coinTotal));
        bson.append(BNAME_COIN_USED, new BsonInt64(coinUsed));
        bson.append(BNAME_DIAMOND, new BsonInt64(diamond));
        bson.append(BNAME_AD, new BsonInt32(ad));
        return bson;
    }

    @Override
    public void load(BsonDocument src) {
        resetStates();
        coinTotal = BsonUtil.longValue(src, BNAME_COIN_TOTAL).orElse(0);
        coinUsed = BsonUtil.longValue(src, BNAME_COIN_USED).orElse(0);
        diamond = BsonUtil.longValue(src, BNAME_DIAMOND).orElse(0);
        ad = BsonUtil.intValue(src, BNAME_AD).orElse(0);
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
    protected void loadObjectNode(ObjectNode src) {
        coinTotal = BsonUtil.longValue(src, BNAME_COIN_TOTAL).orElse(0);
        coinUsed = BsonUtil.longValue(src, BNAME_COIN_USED).orElse(0);
        diamond = BsonUtil.longValue(src, BNAME_DIAMOND).orElse(0);
        ad = BsonUtil.intValue(src, BNAME_AD).orElse(0);
    }

    public boolean coinTotalUpdated() {
        return changedFields.get(0);
    }

    public boolean coinUsedUpdated() {
        return changedFields.get(1);
    }

    public boolean coinUpdated() {
        return changedFields.get(2);
    }

    public boolean diamondUpdated() {
        return changedFields.get(3);
    }

    public boolean adUpdated() {
        return changedFields.get(4);
    }

    @Override
    public Object toData() {
        var data = new LinkedHashMap<>();
        data.put("coinTotal", coinTotal);
        data.put("coin", getCoin());
        data.put("diamond", diamond);
        data.put("ad", ad);
        return data;
    }

    @Override
    protected Object toSubUpdateData() {
        var data = new LinkedHashMap<>();
        var changedFields = this.changedFields;
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
        return data;
    }

    @Override
    public Object toDeletedData() {
        return Map.of();
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {
        var changedFields = this.changedFields;
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
    public String toString() {
        return "Wallet(" + "coinTotal=" + coinTotal + ", " + "coinUsed=" + coinUsed + ", " + "diamond=" + diamond + ", " + "ad=" + ad + ")";
    }

}
