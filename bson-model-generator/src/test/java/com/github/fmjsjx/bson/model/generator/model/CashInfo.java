package com.github.fmjsjx.bson.model.generator.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import com.github.fmjsjx.libcommon.collection.ListSet;
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
    public static final String BNAME_ORDER_DATES = "ods";
    public static final String BNAME_ORDER_TIMES = "ots";
    public static final String BNAME_TEST_DATE = "tsd";
    public static final String BNAME_TEST_DATE_MAP = "tdm";
    public static final String BNAME_TEST_SIMPLE_SET = "tss";
    public static final String BNAME_TEST_SIMPLE_SET2 = "tss2";
    public static final String BNAME_TEST_SIMPLE_SET3 = "tss3";
    public static final String BNAME_TEST_SIMPLE_SET4 = "tss4";
    public static final String BNAME_TEST_LIST_SET = "tls";

    private static final DotNotation XPATH = DotNotation.of("cs");

    private final Player parent;

    private final SimpleMapModel<Integer, Integer, CashInfo> stages = SimpleMapModel.integerKeys(this, "stg", SimpleValueTypes.INTEGER);
    private List<Integer> cards;
    @JsonIgnore
    private List<Integer> orderIds;
    @JsonIgnore
    private List<LocalDate> orderDates;
    @JsonIgnore
    private List<LocalDateTime> orderTimes;
    @JsonIgnore
    private LocalDate testDate;
    @JsonIgnore
    private final SimpleMapModel<Integer, LocalDate, CashInfo> testDateMap = SimpleMapModel.integerKeys(this, "tdm", SimpleValueTypes.DATE);
    private ListSet<Integer> testSimpleSet;
    private ListSet<String> testSimpleSet2;
    @JsonIgnore
    private ListSet<LocalDate> testSimpleSet3;
    @JsonIgnore
    private ListSet<LocalDateTime> testSimpleSet4;
    private ListSet<Integer> testListSet;

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
        fieldUpdated(2);
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
        fieldUpdated(3);
    }

    public Optional<List<Integer>> optionalOrderIds() {
        return Optional.ofNullable(orderIds);
    }

    public void setOrderIdsOf(Integer... orderIds) {
        if (orderIds.length == 0) {
            this.orderIds = List.of();
        } else {
            this.orderIds = List.of(orderIds);
        }
        fieldUpdated(3);
    }

    @JsonIgnore
    public List<LocalDate> getOrderDates() {
        return orderDates;
    }

    public void setOrderDates(List<LocalDate> orderDates) {
        if (orderDates == null) {
            this.orderDates = null;
        } else {
            this.orderDates = List.copyOf(orderDates);
        }
        fieldUpdated(4);
    }

    @JsonIgnore
    public List<LocalDateTime> getOrderTimes() {
        return orderTimes;
    }

    public void setOrderTimes(List<LocalDateTime> orderTimes) {
        if (orderTimes == null) {
            this.orderTimes = null;
        } else {
            this.orderTimes = List.copyOf(orderTimes);
        }
        fieldUpdated(5);
    }

    @JsonIgnore
    public LocalDate getTestDate() {
        return testDate;
    }

    public void setTestDate(LocalDate testDate) {
        if (ObjectUtil.isNotEquals(this.testDate, testDate)) {
            this.testDate = testDate;
            fieldUpdated(6);
        }
    }

    @JsonIgnore
    public SimpleMapModel<Integer, LocalDate, CashInfo> getTestDateMap() {
        return testDateMap;
    }

    public Set<Integer> getTestSimpleSet() {
        return testSimpleSet;
    }

    public void setTestSimpleSet(Set<Integer> testSimpleSet) {
        if (testSimpleSet == null) {
            this.testSimpleSet = null;
        } else {
            this.testSimpleSet = ListSet.copyOf(testSimpleSet);
        }
        fieldUpdated(8);
    }

    public Set<String> getTestSimpleSet2() {
        return testSimpleSet2;
    }

    public void setTestSimpleSet2(Set<String> testSimpleSet2) {
        if (testSimpleSet2 == null) {
            this.testSimpleSet2 = null;
        } else {
            this.testSimpleSet2 = ListSet.copyOf(testSimpleSet2);
        }
        fieldUpdated(9);
    }

    @JsonIgnore
    public Set<LocalDate> getTestSimpleSet3() {
        return testSimpleSet3;
    }

    public void setTestSimpleSet3(Set<LocalDate> testSimpleSet3) {
        if (testSimpleSet3 == null) {
            this.testSimpleSet3 = null;
        } else {
            this.testSimpleSet3 = ListSet.copyOf(testSimpleSet3);
        }
        fieldUpdated(10);
    }

    @JsonIgnore
    public Set<LocalDateTime> getTestSimpleSet4() {
        return testSimpleSet4;
    }

    public void setTestSimpleSet4(Set<LocalDateTime> testSimpleSet4) {
        if (testSimpleSet4 == null) {
            this.testSimpleSet4 = null;
        } else {
            this.testSimpleSet4 = ListSet.copyOf(testSimpleSet4);
        }
        fieldUpdated(11);
    }

    public ListSet<Integer> getTestListSet() {
        return testListSet;
    }

    public void setTestListSet(Collection<Integer> testListSet) {
        if (testListSet == null) {
            this.testListSet = null;
        } else {
            this.testListSet = ListSet.copyOf(testListSet);
        }
        fieldUpdated(12);
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
        var orderDates = this.orderDates;
        if (orderDates != null) {
            var orderDatesArray = new BsonArray(orderDates.size());
            orderDates.stream().map(SimpleValueTypes.DATE::toBson).forEach(orderDatesArray::add);
            bson.append("ods", orderDatesArray);
        }
        var orderTimes = this.orderTimes;
        if (orderTimes != null) {
            var orderTimesArray = new BsonArray(orderTimes.size());
            orderTimes.stream().map(SimpleValueTypes.DATETIME::toBson).forEach(orderTimesArray::add);
            bson.append("ots", orderTimesArray);
        }
        if (testDate != null) {
            bson.append("tsd", new BsonInt32(DateTimeUtil.toNumber(testDate)));
        }
        bson.append("tdm", testDateMap.toBson());
        var testSimpleSet = this.testSimpleSet;
        if (testSimpleSet != null) {
            var testSimpleSetArray = new BsonArray(testSimpleSet.size());
            testSimpleSet.stream().map(SimpleValueTypes.INTEGER::toBson).forEach(testSimpleSetArray::add);
            bson.append("tss", testSimpleSetArray);
        }
        var testSimpleSet2 = this.testSimpleSet2;
        if (testSimpleSet2 != null) {
            var testSimpleSet2Array = new BsonArray(testSimpleSet2.size());
            testSimpleSet2.stream().map(SimpleValueTypes.STRING::toBson).forEach(testSimpleSet2Array::add);
            bson.append("tss2", testSimpleSet2Array);
        }
        var testSimpleSet3 = this.testSimpleSet3;
        if (testSimpleSet3 != null) {
            var testSimpleSet3Array = new BsonArray(testSimpleSet3.size());
            testSimpleSet3.stream().map(SimpleValueTypes.DATE::toBson).forEach(testSimpleSet3Array::add);
            bson.append("tss3", testSimpleSet3Array);
        }
        var testSimpleSet4 = this.testSimpleSet4;
        if (testSimpleSet4 != null) {
            var testSimpleSet4Array = new BsonArray(testSimpleSet4.size());
            testSimpleSet4.stream().map(SimpleValueTypes.DATETIME::toBson).forEach(testSimpleSet4Array::add);
            bson.append("tss4", testSimpleSet4Array);
        }
        var testListSet = this.testListSet;
        if (testListSet != null) {
            var testListSetArray = new BsonArray(testListSet.size());
            testListSet.stream().map(SimpleValueTypes.INTEGER::toBson).forEach(testListSetArray::add);
            bson.append("tls", testListSetArray);
        }
        return bson;
    }

    @Override
    public Document toDocument() {
        var doc = new Document();
        doc.append("stg", stages.toDocument());
        var cards = this.cards;
        if (cards != null) {
            doc.append("cs", cards);
        }
        var orderIds = this.orderIds;
        if (orderIds != null) {
            doc.append("ois", orderIds);
        }
        var orderDates = this.orderDates;
        if (orderDates != null) {
            doc.append("ods", orderDates.stream().map(SimpleValueTypes.DATE::toStorage).collect(Collectors.toList()));
        }
        var orderTimes = this.orderTimes;
        if (orderTimes != null) {
            doc.append("ots", orderTimes.stream().map(SimpleValueTypes.DATETIME::toStorage).collect(Collectors.toList()));
        }
        if (testDate != null) {
            doc.append("tsd", DateTimeUtil.toNumber(testDate));
        }
        doc.append("tdm", testDateMap.toDocument());
        var testSimpleSet = this.testSimpleSet;
        if (testSimpleSet != null) {
            doc.append("tss", testSimpleSet.internalList());
        }
        var testSimpleSet2 = this.testSimpleSet2;
        if (testSimpleSet2 != null) {
            doc.append("tss2", testSimpleSet2.internalList());
        }
        var testSimpleSet3 = this.testSimpleSet3;
        if (testSimpleSet3 != null) {
            doc.append("tss3", testSimpleSet3.stream().map(SimpleValueTypes.DATE::toStorage).collect(Collectors.toList()));
        }
        var testSimpleSet4 = this.testSimpleSet4;
        if (testSimpleSet4 != null) {
            doc.append("tss4", testSimpleSet4.stream().map(SimpleValueTypes.DATETIME::toStorage).collect(Collectors.toList()));
        }
        var testListSet = this.testListSet;
        if (testListSet != null) {
            doc.append("tls", testListSet.internalList());
        }
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
        var orderDates = this.orderDates;
        if (orderDates != null) {
            data.put("ods", orderDates.stream().map(SimpleValueTypes.DATE::toData).collect(Collectors.toList()));
        }
        var orderTimes = this.orderTimes;
        if (orderTimes != null) {
            data.put("ots", orderTimes.stream().map(SimpleValueTypes.DATETIME::toData).collect(Collectors.toList()));
        }
        if (testDate != null) {
            data.put("tsd", DateTimeUtil.toNumber(testDate));
        }
        data.put("tdm", testDateMap.toData());
        var testSimpleSet = this.testSimpleSet;
        if (testSimpleSet != null) {
            data.put("tss", testSimpleSet);
        }
        var testSimpleSet2 = this.testSimpleSet2;
        if (testSimpleSet2 != null) {
            data.put("tss2", testSimpleSet2);
        }
        var testSimpleSet3 = this.testSimpleSet3;
        if (testSimpleSet3 != null) {
            data.put("tss3", testSimpleSet3.stream().map(SimpleValueTypes.DATE::toData).collect(Collectors.toList()));
        }
        var testSimpleSet4 = this.testSimpleSet4;
        if (testSimpleSet4 != null) {
            data.put("tss4", testSimpleSet4.stream().map(SimpleValueTypes.DATETIME::toData).collect(Collectors.toList()));
        }
        var testListSet = this.testListSet;
        if (testListSet != null) {
            data.put("tls", testListSet);
        }
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
        orderDates = BsonUtil.listValue(src, "ods").map(orderDatesList -> {
            return orderDatesList.stream().map(SimpleValueTypes.DATE::cast).collect(Collectors.toUnmodifiableList());
        }).orElse(null);
        orderTimes = BsonUtil.listValue(src, "ots").map(orderTimesList -> {
            return orderTimesList.stream().map(SimpleValueTypes.DATETIME::cast).collect(Collectors.toUnmodifiableList());
        }).orElse(null);
        var testDateOptionalInt = BsonUtil.intValue(src, "tsd");
        testDate = testDateOptionalInt.isEmpty() ? null : DateTimeUtil.toDate(testDateOptionalInt.getAsInt());
        BsonUtil.documentValue(src, "tdm").ifPresentOrElse(testDateMap::load, testDateMap::clear);
        testSimpleSet = BsonUtil.listValue(src, "tss").map(testSimpleSetList -> {
            return ListSet.copyOf(testSimpleSetList.stream().map(SimpleValueTypes.INTEGER::cast).collect(Collectors.toList()));
        }).orElse(null);
        testSimpleSet2 = BsonUtil.listValue(src, "tss2").map(testSimpleSet2List -> {
            return ListSet.copyOf(testSimpleSet2List.stream().map(SimpleValueTypes.STRING::cast).collect(Collectors.toList()));
        }).orElse(null);
        testSimpleSet3 = BsonUtil.listValue(src, "tss3").map(testSimpleSet3List -> {
            return ListSet.copyOf(testSimpleSet3List.stream().map(SimpleValueTypes.DATE::cast).collect(Collectors.toList()));
        }).orElse(null);
        testSimpleSet4 = BsonUtil.listValue(src, "tss4").map(testSimpleSet4List -> {
            return ListSet.copyOf(testSimpleSet4List.stream().map(SimpleValueTypes.DATETIME::cast).collect(Collectors.toList()));
        }).orElse(null);
        testListSet = BsonUtil.listValue(src, "tls").map(testListSetList -> {
            return ListSet.copyOf(testListSetList.stream().map(SimpleValueTypes.INTEGER::cast).collect(Collectors.toList()));
        }).orElse(null);
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
        orderDates = BsonUtil.arrayValue(src, "ods").map(orderDatesArray -> {
            return orderDatesArray.stream().map(SimpleValueTypes.DATE::parse).collect(Collectors.toUnmodifiableList());
        }).orElse(null);
        orderTimes = BsonUtil.arrayValue(src, "ots").map(orderTimesArray -> {
            return orderTimesArray.stream().map(SimpleValueTypes.DATETIME::parse).collect(Collectors.toUnmodifiableList());
        }).orElse(null);
        var testDateOptionalInt = BsonUtil.intValue(src, "tsd");
        testDate = testDateOptionalInt.isEmpty() ? null : DateTimeUtil.toDate(testDateOptionalInt.getAsInt());
        BsonUtil.documentValue(src, "tdm").ifPresentOrElse(testDateMap::load, testDateMap::clear);
        testSimpleSet = BsonUtil.arrayValue(src, "tss").map(testSimpleSetArray -> {
            var testSimpleSetList = testSimpleSetArray.stream().map(SimpleValueTypes.INTEGER::parse).collect(Collectors.toList());
            return ListSet.copyOf(testSimpleSetList);
        }).orElse(null);
        testSimpleSet2 = BsonUtil.arrayValue(src, "tss2").map(testSimpleSet2Array -> {
            var testSimpleSet2List = testSimpleSet2Array.stream().map(SimpleValueTypes.STRING::parse).collect(Collectors.toList());
            return ListSet.copyOf(testSimpleSet2List);
        }).orElse(null);
        testSimpleSet3 = BsonUtil.arrayValue(src, "tss3").map(testSimpleSet3Array -> {
            var testSimpleSet3List = testSimpleSet3Array.stream().map(SimpleValueTypes.DATE::parse).collect(Collectors.toList());
            return ListSet.copyOf(testSimpleSet3List);
        }).orElse(null);
        testSimpleSet4 = BsonUtil.arrayValue(src, "tss4").map(testSimpleSet4Array -> {
            var testSimpleSet4List = testSimpleSet4Array.stream().map(SimpleValueTypes.DATETIME::parse).collect(Collectors.toList());
            return ListSet.copyOf(testSimpleSet4List);
        }).orElse(null);
        testListSet = BsonUtil.arrayValue(src, "tls").map(testListSetArray -> {
            var testListSetList = testListSetArray.stream().map(SimpleValueTypes.INTEGER::parse).collect(Collectors.toList());
            return ListSet.copyOf(testListSetList);
        }).orElse(null);
    }

    @Override
    public void load(Any src) {
        if (src.valueType() != ValueType.OBJECT) {
            reset();
            return;
        }
        BsonUtil.objectValue(src, "stg").ifPresentOrElse(stages::load, stages::clear);
        cards = BsonUtil.arrayValue(src, "cs").filter(cardsAny -> cardsAny.valueType() == ValueType.ARRAY).map(cardsAny -> {
            var cardsList = new ArrayList<Integer>(cardsAny.size());
            for (var cardsAnyElement : cardsAny) {
                cardsList.add(SimpleValueTypes.INTEGER.parse(cardsAnyElement));
            }
            return List.copyOf(cardsList);
        }).orElse(null);
        orderIds = BsonUtil.arrayValue(src, "ois").filter(orderIdsAny -> orderIdsAny.valueType() == ValueType.ARRAY).map(orderIdsAny -> {
            var orderIdsList = new ArrayList<Integer>(orderIdsAny.size());
            for (var orderIdsAnyElement : orderIdsAny) {
                orderIdsList.add(SimpleValueTypes.INTEGER.parse(orderIdsAnyElement));
            }
            return List.copyOf(orderIdsList);
        }).orElse(null);
        orderDates = BsonUtil.arrayValue(src, "ods").filter(orderDatesAny -> orderDatesAny.valueType() == ValueType.ARRAY).map(orderDatesAny -> {
            var orderDatesList = new ArrayList<LocalDate>(orderDatesAny.size());
            for (var orderDatesAnyElement : orderDatesAny) {
                orderDatesList.add(SimpleValueTypes.DATE.parse(orderDatesAnyElement));
            }
            return List.copyOf(orderDatesList);
        }).orElse(null);
        orderTimes = BsonUtil.arrayValue(src, "ots").filter(orderTimesAny -> orderTimesAny.valueType() == ValueType.ARRAY).map(orderTimesAny -> {
            var orderTimesList = new ArrayList<LocalDateTime>(orderTimesAny.size());
            for (var orderTimesAnyElement : orderTimesAny) {
                orderTimesList.add(SimpleValueTypes.DATETIME.parse(orderTimesAnyElement));
            }
            return List.copyOf(orderTimesList);
        }).orElse(null);
        var testDateOptionalInt = BsonUtil.intValue(src, "tsd");
        testDate = testDateOptionalInt.isEmpty() ? null : DateTimeUtil.toDate(testDateOptionalInt.getAsInt());
        BsonUtil.objectValue(src, "tdm").ifPresentOrElse(testDateMap::load, testDateMap::clear);
        testSimpleSet = BsonUtil.arrayValue(src, "tss").filter(testSimpleSetAny -> testSimpleSetAny.valueType() == ValueType.ARRAY).map(testSimpleSetAny -> {
            var testSimpleSetList = new ArrayList<Integer>(testSimpleSetAny.size());
            for (var testSimpleSetAnyElement : testSimpleSetAny) {
                testSimpleSetList.add(SimpleValueTypes.INTEGER.parse(testSimpleSetAnyElement));
            }
            return ListSet.copyOf(testSimpleSetList);
        }).orElse(null);
        testSimpleSet2 = BsonUtil.arrayValue(src, "tss2").filter(testSimpleSet2Any -> testSimpleSet2Any.valueType() == ValueType.ARRAY).map(testSimpleSet2Any -> {
            var testSimpleSet2List = new ArrayList<String>(testSimpleSet2Any.size());
            for (var testSimpleSet2AnyElement : testSimpleSet2Any) {
                testSimpleSet2List.add(SimpleValueTypes.STRING.parse(testSimpleSet2AnyElement));
            }
            return ListSet.copyOf(testSimpleSet2List);
        }).orElse(null);
        testSimpleSet3 = BsonUtil.arrayValue(src, "tss3").filter(testSimpleSet3Any -> testSimpleSet3Any.valueType() == ValueType.ARRAY).map(testSimpleSet3Any -> {
            var testSimpleSet3List = new ArrayList<LocalDate>(testSimpleSet3Any.size());
            for (var testSimpleSet3AnyElement : testSimpleSet3Any) {
                testSimpleSet3List.add(SimpleValueTypes.DATE.parse(testSimpleSet3AnyElement));
            }
            return ListSet.copyOf(testSimpleSet3List);
        }).orElse(null);
        testSimpleSet4 = BsonUtil.arrayValue(src, "tss4").filter(testSimpleSet4Any -> testSimpleSet4Any.valueType() == ValueType.ARRAY).map(testSimpleSet4Any -> {
            var testSimpleSet4List = new ArrayList<LocalDateTime>(testSimpleSet4Any.size());
            for (var testSimpleSet4AnyElement : testSimpleSet4Any) {
                testSimpleSet4List.add(SimpleValueTypes.DATETIME.parse(testSimpleSet4AnyElement));
            }
            return ListSet.copyOf(testSimpleSet4List);
        }).orElse(null);
        testListSet = BsonUtil.arrayValue(src, "tls").filter(testListSetAny -> testListSetAny.valueType() == ValueType.ARRAY).map(testListSetAny -> {
            var testListSetList = new ArrayList<Integer>(testListSetAny.size());
            for (var testListSetAnyElement : testListSetAny) {
                testListSetList.add(SimpleValueTypes.INTEGER.parse(testListSetAnyElement));
            }
            return ListSet.copyOf(testListSetList);
        }).orElse(null);
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
        orderDates = BsonUtil.arrayValue(src, "ods").filter(JsonNode::isArray).map(orderDatesNode -> {
            var orderDates = new ArrayList<LocalDate>(orderDatesNode.size());
            for (var orderDatesNodeElement : orderDatesNode) {
                orderDates.add(SimpleValueTypes.DATE.parse(orderDatesNodeElement));
            }
            return List.copyOf(orderDates);
        }).orElse(null);
        orderTimes = BsonUtil.arrayValue(src, "ots").filter(JsonNode::isArray).map(orderTimesNode -> {
            var orderTimes = new ArrayList<LocalDateTime>(orderTimesNode.size());
            for (var orderTimesNodeElement : orderTimesNode) {
                orderTimes.add(SimpleValueTypes.DATETIME.parse(orderTimesNodeElement));
            }
            return List.copyOf(orderTimes);
        }).orElse(null);
        var testDateOptionalInt = BsonUtil.intValue(src, "tsd");
        testDate = testDateOptionalInt.isEmpty() ? null : DateTimeUtil.toDate(testDateOptionalInt.getAsInt());
        BsonUtil.objectValue(src, "tdm").ifPresentOrElse(testDateMap::load, testDateMap::clear);
        testSimpleSet = BsonUtil.arrayValue(src, "tss").filter(JsonNode::isArray).map(testSimpleSetNode -> {
            var testSimpleSetList = new ArrayList<Integer>(testSimpleSetNode.size());
            for (var testSimpleSetNodeElement : testSimpleSetNode) {
                testSimpleSetList.add(SimpleValueTypes.INTEGER.parse(testSimpleSetNodeElement));
            }
            return ListSet.copyOf(testSimpleSetList);
        }).orElse(null);
        testSimpleSet2 = BsonUtil.arrayValue(src, "tss2").filter(JsonNode::isArray).map(testSimpleSet2Node -> {
            var testSimpleSet2List = new ArrayList<String>(testSimpleSet2Node.size());
            for (var testSimpleSet2NodeElement : testSimpleSet2Node) {
                testSimpleSet2List.add(SimpleValueTypes.STRING.parse(testSimpleSet2NodeElement));
            }
            return ListSet.copyOf(testSimpleSet2List);
        }).orElse(null);
        testSimpleSet3 = BsonUtil.arrayValue(src, "tss3").filter(JsonNode::isArray).map(testSimpleSet3Node -> {
            var testSimpleSet3List = new ArrayList<LocalDate>(testSimpleSet3Node.size());
            for (var testSimpleSet3NodeElement : testSimpleSet3Node) {
                testSimpleSet3List.add(SimpleValueTypes.DATE.parse(testSimpleSet3NodeElement));
            }
            return ListSet.copyOf(testSimpleSet3List);
        }).orElse(null);
        testSimpleSet4 = BsonUtil.arrayValue(src, "tss4").filter(JsonNode::isArray).map(testSimpleSet4Node -> {
            var testSimpleSet4List = new ArrayList<LocalDateTime>(testSimpleSet4Node.size());
            for (var testSimpleSet4NodeElement : testSimpleSet4Node) {
                testSimpleSet4List.add(SimpleValueTypes.DATETIME.parse(testSimpleSet4NodeElement));
            }
            return ListSet.copyOf(testSimpleSet4List);
        }).orElse(null);
        testListSet = BsonUtil.arrayValue(src, "tls").filter(JsonNode::isArray).map(testListSetNode -> {
            var testListSetList = new ArrayList<Integer>(testListSetNode.size());
            for (var testListSetNodeElement : testListSetNode) {
                testListSetList.add(SimpleValueTypes.INTEGER.parse(testListSetNodeElement));
            }
            return ListSet.copyOf(testListSetList);
        }).orElse(null);
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

    public boolean orderDatesUpdated() {
        return updatedFields.get(4);
    }

    public boolean orderTimesUpdated() {
        return updatedFields.get(5);
    }

    public boolean testDateUpdated() {
        return updatedFields.get(6);
    }

    public boolean testDateMapUpdated() {
        return testDateMap.updated();
    }

    public boolean testSimpleSetUpdated() {
        return updatedFields.get(8);
    }

    public boolean testSimpleSet2Updated() {
        return updatedFields.get(9);
    }

    public boolean testSimpleSet3Updated() {
        return updatedFields.get(10);
    }

    public boolean testSimpleSet4Updated() {
        return updatedFields.get(11);
    }

    public boolean testListSetUpdated() {
        return updatedFields.get(12);
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
            var orderDates = this.orderDates;
            if (orderDates == null) {
                updates.add(Updates.unset(xpath().resolve("ods").value()));
            } else {
                var orderDatesArray = new BsonArray(orderDates.size());
                orderDates.stream().map(SimpleValueTypes.DATE::toBson).forEach(orderDatesArray::add);
                updates.add(Updates.set(xpath().resolve("ods").value(), orderDatesArray));
            }
        }
        if (updatedFields.get(5)) {
            var orderTimes = this.orderTimes;
            if (orderTimes == null) {
                updates.add(Updates.unset(xpath().resolve("ots").value()));
            } else {
                var orderTimesArray = new BsonArray(orderTimes.size());
                orderTimes.stream().map(SimpleValueTypes.DATETIME::toBson).forEach(orderTimesArray::add);
                updates.add(Updates.set(xpath().resolve("ots").value(), orderTimesArray));
            }
        }
        if (updatedFields.get(6)) {
            updates.add(Updates.set(xpath().resolve("tsd").value(), DateTimeUtil.toNumber(testDate)));
        }
        var testDateMap = this.testDateMap;
        if (testDateMap.updated()) {
            testDateMap.appendUpdates(updates);
        }
        if (updatedFields.get(8)) {
            var testSimpleSet = this.testSimpleSet;
            if (testSimpleSet == null) {
                updates.add(Updates.unset(xpath().resolve("tss").value()));
            } else {
                var testSimpleSetArray = new BsonArray(testSimpleSet.size());
                testSimpleSet.stream().map(SimpleValueTypes.INTEGER::toBson).forEach(testSimpleSetArray::add);
                updates.add(Updates.set(xpath().resolve("tss").value(), testSimpleSetArray));
            }
        }
        if (updatedFields.get(9)) {
            var testSimpleSet2 = this.testSimpleSet2;
            if (testSimpleSet2 == null) {
                updates.add(Updates.unset(xpath().resolve("tss2").value()));
            } else {
                var testSimpleSet2Array = new BsonArray(testSimpleSet2.size());
                testSimpleSet2.stream().map(SimpleValueTypes.STRING::toBson).forEach(testSimpleSet2Array::add);
                updates.add(Updates.set(xpath().resolve("tss2").value(), testSimpleSet2Array));
            }
        }
        if (updatedFields.get(10)) {
            var testSimpleSet3 = this.testSimpleSet3;
            if (testSimpleSet3 == null) {
                updates.add(Updates.unset(xpath().resolve("tss3").value()));
            } else {
                var testSimpleSet3Array = new BsonArray(testSimpleSet3.size());
                testSimpleSet3.stream().map(SimpleValueTypes.DATE::toBson).forEach(testSimpleSet3Array::add);
                updates.add(Updates.set(xpath().resolve("tss3").value(), testSimpleSet3Array));
            }
        }
        if (updatedFields.get(11)) {
            var testSimpleSet4 = this.testSimpleSet4;
            if (testSimpleSet4 == null) {
                updates.add(Updates.unset(xpath().resolve("tss4").value()));
            } else {
                var testSimpleSet4Array = new BsonArray(testSimpleSet4.size());
                testSimpleSet4.stream().map(SimpleValueTypes.DATETIME::toBson).forEach(testSimpleSet4Array::add);
                updates.add(Updates.set(xpath().resolve("tss4").value(), testSimpleSet4Array));
            }
        }
        if (updatedFields.get(12)) {
            var testListSet = this.testListSet;
            if (testListSet == null) {
                updates.add(Updates.unset(xpath().resolve("tls").value()));
            } else {
                var testListSetArray = new BsonArray(testListSet.size());
                testListSet.stream().map(SimpleValueTypes.INTEGER::toBson).forEach(testListSetArray::add);
                updates.add(Updates.set(xpath().resolve("tls").value(), testListSetArray));
            }
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
        if (updatedFields.get(2) && cards != null) {
            update.put("cards", cards);
        }
        if (updatedFields.get(8) && testSimpleSet != null) {
            update.put("testSimpleSet", testSimpleSet);
        }
        if (updatedFields.get(9) && testSimpleSet2 != null) {
            update.put("testSimpleSet2", testSimpleSet2);
        }
        if (updatedFields.get(12) && testListSet != null) {
            update.put("testListSet", testListSet);
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
        if (updatedFields.get(2) && cards == null) {
            delete.put("cards", 1);
        }
        if (updatedFields.get(8) && testSimpleSet == null) {
            delete.put("testSimpleSet", 1);
        }
        if (updatedFields.get(9) && testSimpleSet2 == null) {
            delete.put("testSimpleSet2", 1);
        }
        if (updatedFields.get(12) && testListSet == null) {
            delete.put("testListSet", 1);
        }
        return delete;
    }

    @Override
    protected int deletedSize() {
        var n = 0;
        if (stages.deletedSize() > 0) {
            n++;
        }
        if (updatedFields.get(2) && cards == null) {
            n++;
        }
        if (updatedFields.get(8) && testSimpleSet == null) {
            n++;
        }
        if (updatedFields.get(9) && testSimpleSet2 == null) {
            n++;
        }
        if (updatedFields.get(12) && testListSet == null) {
            n++;
        }
        return n;
    }

    @Override
    public String toString() {
        return "CashInfo(" + "stages=" + stages + ", " + "cards=" + cards + ", " + "orderIds=" + orderIds + ", " + "orderDates=" + orderDates + ", " + "orderTimes=" + orderTimes + ", " + "testDate=" + testDate + ", " + "testDateMap=" + testDateMap + ", " + "testSimpleSet=" + testSimpleSet + ", " + "testSimpleSet2=" + testSimpleSet2 + ", " + "testSimpleSet3=" + testSimpleSet3 + ", " + "testSimpleSet4=" + testSimpleSet4 + ", " + "testListSet=" + testListSet + ")";
    }

}
