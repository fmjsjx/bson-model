package com.github.fmjsjx.bson.model.generator.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fmjsjx.bson.model.core.BsonUtil;
import com.github.fmjsjx.bson.model.core.DotNotation;
import com.github.fmjsjx.bson.model.core.ObjectModel;
import com.github.fmjsjx.bson.model.core.SimpleMapModel;
import com.github.fmjsjx.bson.model.core.SimpleValueTypes;
import com.github.fmjsjx.libcommon.util.DateTimeUtil;
import com.github.fmjsjx.libcommon.util.ObjectUtil;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.mongodb.client.model.Updates;
import org.bson.BsonArray;

public class CashInfo extends ObjectModel<CashInfo> {

    public static final String BNAME_STAGES = "stg";
    public static final String BNAME_CARDS = "cs";
    public static final String BNAME_ORDER_IDS = "ois";
    public static final String BNAME_TEST_DATE = "tsd";
    public static final String BNAME_TEST_DATE_MAP = "tdm";

    private static final DotNotation XPATH = DotNotation.of("cs");

    private final Player parent;

    private final SimpleMapModel<Integer, Integer, CashInfo> stages = SimpleMapModel.integerKeys(this, "stg", SimpleValueTypes.INTEGER);
    private List<Integer> cards;
    @JsonIgnore
    private List<Integer> orderIds;
    @JsonIgnore
    private LocalDate testDate;
    @JsonIgnore
    private final SimpleMapModel<Integer, LocalDate, CashInfo> testDateMap = SimpleMapModel.integerKeys(this, "tdm", SimpleValueTypes.DATE);

    public CashInfo(Player parent) {
        this.parent = parent;
    }

    public SimpleMapModel<Integer, Integer, CashInfo> getStages() {
        return stages;
    }

    public List<Integer> getCards() {
        return cards;
    }

    public void setCards(List<Integer> cards) {
        if (cards == null) {
            this.cards = null;
        } else {
            this.cards = List.copyOf(cards);
        }
        updatedFields.set(2);
    }

    @JsonIgnore
    public List<Integer> getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(List<Integer> orderIds) {
        if (orderIds == null) {
            this.orderIds = null;
        } else {
            this.orderIds = List.copyOf(orderIds);
        }
        updatedFields.set(3);
    }

    @JsonIgnore
    public LocalDate getTestDate() {
        return testDate;
    }

    public void setTestDate(LocalDate testDate) {
        if (ObjectUtil.isNotEquals(this.testDate, testDate)) {
            this.testDate = testDate;
            updatedFields.set(4);
        }
    }

    @JsonIgnore
    public SimpleMapModel<Integer, LocalDate, CashInfo> getTestDateMap() {
        return testDateMap;
    }

    @Override
    public Player parent() {
        return parent;
    }

    @Override
    public DotNotation xpath() {
        return XPATH;
    }

    @Override
    public boolean updated() {
        if (stages.updated() || testDateMap.updated()) {
            return true;
        }
        return super.updated();
    }

    @Override
    public BsonDocument toBson() {
        var bson = new BsonDocument();
        bson.append("stg", stages.toBson());
        var cards = this.cards;
        if (cards != null) {
            var cardsArray = new BsonArray(cards.size());
            cards.stream().map(SimpleValueTypes.INTEGER::toBson).forEach(cardsArray::add);
            bson.append("cs", cardsArray);
        }
        var orderIds = this.orderIds;
        if (orderIds != null) {
            var orderIdsArray = new BsonArray(orderIds.size());
            orderIds.stream().map(SimpleValueTypes.INTEGER::toBson).forEach(orderIdsArray::add);
            bson.append("ois", orderIdsArray);
        }
        if (testDate != null) {
            bson.append("tsd", new BsonInt32(DateTimeUtil.toNumber(testDate)));
        }
        bson.append("tdm", testDateMap.toBson());
        return bson;
    }

    @Override
    public Document toDocument() {
        var doc = new Document();
        doc.append("stg", stages.toDocument());
        var cards = this.cards;
        if (cards != null) {
            doc.append("cs", cards);
        } else {
            doc.append("cs", null);
        }
        var orderIds = this.orderIds;
        if (orderIds != null) {
            doc.append("ois", orderIds);
        } else {
            doc.append("ois", null);
        }
        if (testDate != null) {
            doc.append("tsd", DateTimeUtil.toNumber(testDate));
        }
        doc.append("tdm", testDateMap.toDocument());
        return doc;
    }

    @Override
    public Map<String, ?> toData() {
        var data = new LinkedHashMap<String, Object>();
        data.put("stg", stages.toData());
        var cards = this.cards;
        if (cards != null) {
            data.put("cs", cards);
        }
        var orderIds = this.orderIds;
        if (orderIds != null) {
            data.put("ois", orderIds);
        }
        if (testDate != null) {
            data.put("tsd", DateTimeUtil.toNumber(testDate));
        }
        data.put("tdm", testDateMap.toData());
        return data;
    }

    @Override
    public void load(Document src) {
        BsonUtil.documentValue(src, "stg").ifPresentOrElse(stages::load, stages::clear);
        cards = BsonUtil.listValue(src, "cs").map(cardsList -> {
            return cardsList.stream().map(SimpleValueTypes.INTEGER::cast).collect(Collectors.toUnmodifiableList());
        }).orElse(null);
        orderIds = BsonUtil.listValue(src, "ois").map(orderIdsList -> {
            return orderIdsList.stream().map(SimpleValueTypes.INTEGER::cast).collect(Collectors.toUnmodifiableList());
        }).orElse(null);
        var testDateOptionalInt = BsonUtil.intValue(src, "tsd");
        testDate = testDateOptionalInt.isEmpty() ? null : DateTimeUtil.toDate(testDateOptionalInt.getAsInt());
        BsonUtil.documentValue(src, "tdm").ifPresentOrElse(testDateMap::load, testDateMap::clear);
    }

    @Override
    public void load(BsonDocument src) {
        BsonUtil.documentValue(src, "stg").ifPresentOrElse(stages::load, stages::clear);
        cards = BsonUtil.arrayValue(src, "cs").map(cardsArray -> {
            return cardsArray.stream().map(SimpleValueTypes.INTEGER::parse).collect(Collectors.toUnmodifiableList());
        }).orElse(null);
        orderIds = BsonUtil.arrayValue(src, "ois").map(orderIdsArray -> {
            return orderIdsArray.stream().map(SimpleValueTypes.INTEGER::parse).collect(Collectors.toUnmodifiableList());
        }).orElse(null);
        var testDateOptionalInt = BsonUtil.intValue(src, "tsd");
        testDate = testDateOptionalInt.isEmpty() ? null : DateTimeUtil.toDate(testDateOptionalInt.getAsInt());
        BsonUtil.documentValue(src, "tdm").ifPresentOrElse(testDateMap::load, testDateMap::clear);
    }

    @Override
    public void load(Any src) {
        if (src.valueType() != ValueType.OBJECT) {
            reset();
            return;
        }
        BsonUtil.objectValue(src, "stg").ifPresentOrElse(stages::load, stages::clear);
        cards = BsonUtil.arrayValue(src, "cs").filter(cardsAny -> cardsAny.valueType() == ValueType.ARRAY).map(cardsAny -> {
            var cards = new ArrayList<Integer>(cardsAny.size());
            for (var cardsAnyElement : cardsAny) {
                cards.add(SimpleValueTypes.INTEGER.parse(cardsAnyElement));
            }
            return List.copyOf(cards);
        }).orElse(null);
        orderIds = BsonUtil.arrayValue(src, "ois").filter(orderIdsAny -> orderIdsAny.valueType() == ValueType.ARRAY).map(orderIdsAny -> {
            var orderIds = new ArrayList<Integer>(orderIdsAny.size());
            for (var orderIdsAnyElement : orderIdsAny) {
                orderIds.add(SimpleValueTypes.INTEGER.parse(orderIdsAnyElement));
            }
            return List.copyOf(orderIds);
        }).orElse(null);
        var testDateOptionalInt = BsonUtil.intValue(src, "tsd");
        testDate = testDateOptionalInt.isEmpty() ? null : DateTimeUtil.toDate(testDateOptionalInt.getAsInt());
        BsonUtil.objectValue(src, "tdm").ifPresentOrElse(testDateMap::load, testDateMap::clear);
    }

    @Override
    public void load(JsonNode src) {
        if (!src.isObject()) {
            reset();
            return;
        }
        BsonUtil.objectValue(src, "stg").ifPresentOrElse(stages::load, stages::clear);
        cards = BsonUtil.arrayValue(src, "cs").filter(JsonNode::isArray).map(cardsNode -> {
            var cards = new ArrayList<Integer>(cardsNode.size());
            for (var cardsNodeElement : cardsNode) {
                cards.add(SimpleValueTypes.INTEGER.parse(cardsNodeElement));
            }
            return List.copyOf(cards);
        }).orElse(null);
        orderIds = BsonUtil.arrayValue(src, "ois").filter(JsonNode::isArray).map(orderIdsNode -> {
            var orderIds = new ArrayList<Integer>(orderIdsNode.size());
            for (var orderIdsNodeElement : orderIdsNode) {
                orderIds.add(SimpleValueTypes.INTEGER.parse(orderIdsNodeElement));
            }
            return List.copyOf(orderIds);
        }).orElse(null);
        var testDateOptionalInt = BsonUtil.intValue(src, "tsd");
        testDate = testDateOptionalInt.isEmpty() ? null : DateTimeUtil.toDate(testDateOptionalInt.getAsInt());
        BsonUtil.objectValue(src, "tdm").ifPresentOrElse(testDateMap::load, testDateMap::clear);
    }

    public boolean stagesUpdated() {
        return stages.updated();
    }

    public boolean cardsUpdated() {
        return updatedFields.get(2);
    }

    public boolean orderIdsUpdated() {
        return updatedFields.get(3);
    }

    public boolean testDateUpdated() {
        return updatedFields.get(4);
    }

    public boolean testDateMapUpdated() {
        return testDateMap.updated();
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {
        var updatedFields = this.updatedFields;
        var stages = this.stages;
        if (stages.updated()) {
            stages.appendUpdates(updates);
        }
        if (updatedFields.get(2)) {
            var cards = this.cards;
            if (cards == null) {
                updates.add(Updates.unset(xpath().resolve("cs").value()));
            } else {
                var cardsArray = new BsonArray(cards.size());
                cards.stream().map(SimpleValueTypes.INTEGER::toBson).forEach(cardsArray::add);
                updates.add(Updates.set(xpath().resolve("cs").value(), cardsArray));
            }
        }
        if (updatedFields.get(3)) {
            var orderIds = this.orderIds;
            if (orderIds == null) {
                updates.add(Updates.unset(xpath().resolve("ois").value()));
            } else {
                var orderIdsArray = new BsonArray(orderIds.size());
                orderIds.stream().map(SimpleValueTypes.INTEGER::toBson).forEach(orderIdsArray::add);
                updates.add(Updates.set(xpath().resolve("ois").value(), orderIdsArray));
            }
        }
        if (updatedFields.get(4)) {
            updates.add(Updates.set(xpath().resolve("tsd").value(), DateTimeUtil.toNumber(testDate)));
        }
        var testDateMap = this.testDateMap;
        if (testDateMap.updated()) {
            testDateMap.appendUpdates(updates);
        }
    }

    @Override
    protected void resetChildren() {
        stages.reset();
        testDateMap.reset();
    }

    @Override
    public Object toSubUpdate() {
        var update = new LinkedHashMap<>();
        var updatedFields = this.updatedFields;
        if (stages.updated()) {
            update.put("stages", stages.toUpdate());
        }
        if (updatedFields.get(2)) {
            update.put("cards", cards);
        }
        return update;
    }

    @Override
    public Map<Object, Object> toDelete() {
        var delete = new LinkedHashMap<>();
        var stages = this.stages;
        if (stages.deletedSize() > 0) {
            delete.put("stages", stages.toDelete());
        }
        if (updatedFields.get(2)) {
            delete.put("cards", 1);
        }
        return delete;
    }

    @Override
    protected int deletedSize() {
        var n = 0;
        if (stages.deletedSize() > 0) {
            n++;
        }
        if (updatedFields.get(2)) {
            n++;
        }
        return n;
    }

    @Override
    public String toString() {
        return "CashInfo(" + "stages=" + stages + ", " + "cards=" + cards + ", " + "orderIds=" + orderIds + ", " + "testDate=" + testDate + ", " + "testDateMap=" + testDateMap + ")";
    }

}
