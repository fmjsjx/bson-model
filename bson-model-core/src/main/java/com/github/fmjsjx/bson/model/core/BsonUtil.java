package com.github.fmjsjx.bson.model.core;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bson.*;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fmjsjx.libcommon.json.JsonException;
import com.github.fmjsjx.libcommon.util.DateTimeUtil;
import com.jsoniter.JsonIterator;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;

/**
 * Utility class for BSON.
 */
public class BsonUtil {

    /**
     * Gets the value in an embedded document.
     *
     * @param <V>      the type of the return value
     * @param document the source document
     * @param keys     the array of keys
     * @return an {@code Optional<V>}
     */
    public static final <V> Optional<V> embedded(Document document, Object... keys) {
        return embedded(document, Arrays.asList(keys));
    }

    /**
     * Gets the value in an embedded document.
     *
     * @param <V>      the type of the return value
     * @param document the source document
     * @param keys     the list of keys
     * @return an {@code Optional<V>}
     */
    @SuppressWarnings("unchecked")
    public static final <V> Optional<V> embedded(Document document, List<Object> keys) {
        Object value = Objects.requireNonNull(document, "document must not be null");
        var i = 0;
        for (var key : keys) {
            value = nextValue(keys, value, i, key);
            if (value == null) {
                return Optional.empty();
            }
            i++;
        }
        return Optional.of((V) value);
    }

    private static final Object nextValue(List<Object> keys, Object value, int i, Object key) {
        if (value instanceof Document doc) {
            value = doc.get(key);
        } else if (value instanceof List<?> list) {
            var index = key instanceof Number num ? num.intValue() : Integer.parseInt(key.toString());
            if (index < list.size()) {
                value = list.get(index);
            } else {
                value = null;
            }
        } else {
            var path = keys.stream().limit(i).map(String::valueOf).collect(Collectors.joining("."));
            throw new ClassCastException(
                    String.format("At dot notation \"%s\", the value is not a Document or a List (%s)", path,
                            value.getClass().getName()));
        }
        return value;
    }

    /**
     * Gets the {@code Document} value in an embedded document.
     *
     * @param document the source document
     * @param keys     the array of keys
     * @return an {@code Optional<Document>}
     */
    public static final Optional<Document> embeddedDocument(Document document, Object... keys) {
        return embedded(document, keys);
    }

    /**
     * Gets the {@code Document} value in an embedded document.
     *
     * @param document the source document
     * @param keys     the list of keys
     * @return an {@code Optional<Document>}
     */
    public static final Optional<Document> embeddedDocument(Document document, List<Object> keys) {
        return embedded(document, keys);
    }

    /**
     * Gets the {@code int} value in an embedded document.
     *
     * @param document the source document
     * @param keys     the array of keys
     * @return an {@code OptionalInt}
     */
    public static final OptionalInt embeddedInt(Document document, Object... keys) {
        return embeddedInt(document, Arrays.asList(keys));
    }

    /**
     * Gets the {@code int} value in an embedded document.
     *
     * @param document the source document
     * @param keys     the list of keys
     * @return an {@code OptionalInt}
     */
    public static final OptionalInt embeddedInt(Document document, List<Object> keys) {
        Object value = Objects.requireNonNull(document, "document must not be null");
        var i = 0;
        for (var key : keys) {
            value = nextValue(keys, value, i, key);
            if (value == null) {
                return OptionalInt.empty();
            }
            i++;
        }
        if (value instanceof Number num) {
            return OptionalInt.of(num.intValue());
        }
        throw new ClassCastException(String.format("The value is not a Number (%s)", value.getClass().getName()));
    }

    /**
     * Gets the {@code long} value in an embedded document.
     *
     * @param document the source document
     * @param keys     the array of keys
     * @return an {@code OptionalLong}
     */
    public static final OptionalLong embeddedLong(Document document, Object... keys) {
        return embeddedLong(document, Arrays.asList(keys));
    }

    /**
     * Gets the {@code long} value in an embedded document.
     *
     * @param document the source document
     * @param keys     the list of keys
     * @return an {@code OptionalInt}
     */
    public static final OptionalLong embeddedLong(Document document, List<Object> keys) {
        Object value = Objects.requireNonNull(document, "document must not be null");
        var i = 0;
        for (var key : keys) {
            value = nextValue(keys, value, i, key);
            if (value == null) {
                return OptionalLong.empty();
            }
            i++;
        }
        if (value instanceof Number) {
            return OptionalLong.of(((Number) value).longValue());
        }
        throw new ClassCastException(String.format("The value is not a Number (%s)", value.getClass().getName()));
    }

    /**
     * Gets the {@code double} value in an embedded document.
     *
     * @param document the source document
     * @param keys     the array of keys
     * @return an {@code OptionalDouble}
     */
    public static final OptionalDouble embeddedDouble(Document document, Object... keys) {
        return embeddedDouble(document, Arrays.asList(keys));
    }

    /**
     * Gets the {@code double} value in an embedded document.
     *
     * @param document the source document
     * @param keys     the list of keys
     * @return an {@code OptionalDouble}
     */
    public static final OptionalDouble embeddedDouble(Document document, List<Object> keys) {
        Object value = Objects.requireNonNull(document, "document must not be null");
        var i = 0;
        for (var key : keys) {
            value = nextValue(keys, value, i, key);
            if (value == null) {
                return OptionalDouble.empty();
            }
            i++;
        }
        if (value instanceof Number) {
            return OptionalDouble.of(((Number) value).doubleValue());
        }
        throw new ClassCastException(String.format("The value is not a Number (%s)", value.getClass().getName()));
    }

    /**
     * Gets the {@code LocalDateTime} value in an embedded document.
     *
     * @param document the source document
     * @param keys     the array of keys
     * @return an {@code Optional<LocalDateTime>}
     */
    public static final Optional<LocalDateTime> embeddedDateTime(Document document, Object... keys) {
        return embeddedDateTime(document, Arrays.asList(keys));
    }

    /**
     * Gets the {@code LocalDateTime} value in an embedded document.
     *
     * @param document the source document
     * @param keys     the list of keys
     * @return an {@code Optional<LocalDateTime>}
     */
    public static final Optional<LocalDateTime> embeddedDateTime(Document document, List<Object> keys) {
        Object value = Objects.requireNonNull(document, "document must not be null");
        var i = 0;
        for (var key : keys) {
            value = nextValue(keys, value, i, key);
            if (value == null) {
                return Optional.empty();
            }
            i++;
        }
        if (value instanceof Date) {
            return Optional.of(LocalDateTime.ofInstant(((Date) value).toInstant(), ZoneId.systemDefault()));
        }
        throw new ClassCastException(String.format("The value is not a Date (%s)", value.getClass().getName()));
    }

    /**
     * Gets the {@code ZonedDateTime} value in an embedded document.
     *
     * @param zone     the zone to combine with, not null
     * @param document the source document
     * @param keys     the array of keys
     * @return an {@code Optional<ZonedDateTime>}
     */
    public static final Optional<ZonedDateTime> embeddedDateTime(ZoneId zone, Document document, Object... keys) {
        return embeddedDateTime(zone, document, Arrays.asList(keys));
    }

    /**
     * Gets the {@code ZonedDateTime} value in an embedded document.
     *
     * @param zone     the zone to combine with, not null
     * @param document the source document
     * @param keys     the list of keys
     * @return an {@code Optional<ZonedDateTime>}
     */
    public static final Optional<ZonedDateTime> embeddedDateTime(ZoneId zone, Document document, List<Object> keys) {
        Object value = Objects.requireNonNull(document, "document must not be null");
        var i = 0;
        for (var key : keys) {
            value = nextValue(keys, value, i, key);
            if (value == null) {
                return Optional.empty();
            }
            i++;
        }
        if (value instanceof Date) {
            return Optional.of(((Date) value).toInstant().atZone(zone));
        }
        throw new ClassCastException(String.format("The value is not a Date (%s)", value.getClass().getName()));
    }

    /**
     * Gets the {@code string} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code Optional<String>}
     */
    public static final Optional<String> stringValue(Document document, String key) {
        return Optional.ofNullable(document.getString(key));
    }

    /**
     * Gets the {@code Document} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code Optional<Document>}
     */
    public static final Optional<Document> documentValue(Document document, String key) {
        var value = document.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof Document) {
            return Optional.of((Document) value);
        }
        throw new ClassCastException(String.format("The value is not a Document (%s)", value.getClass().getName()));
    }

    /**
     * Gets the {@code int} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code OptionalInt}
     */
    public static final OptionalInt intValue(Document document, String key) {
        var value = document.get(key);
        if (value == null) {
            return OptionalInt.empty();
        }
        if (value instanceof Number) {
            return OptionalInt.of(((Number) value).intValue());
        }
        throw new ClassCastException(String.format("The value is not a Number (%s)", value.getClass().getName()));
    }

    /**
     * Gets the {@code long} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code OptionalLong}
     */
    public static final OptionalLong longValue(Document document, String key) {
        var value = document.get(key);
        if (value == null) {
            return OptionalLong.empty();
        }
        if (value instanceof Number) {
            return OptionalLong.of(((Number) value).longValue());
        }
        throw new ClassCastException(String.format("The value is not a Number (%s)", value.getClass().getName()));
    }

    /**
     * Gets the {@code double} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code OptionalDouble}
     */
    public static final OptionalDouble doubleValue(Document document, String key) {
        var value = document.get(key);
        if (value == null) {
            return OptionalDouble.empty();
        }
        if (value instanceof Number) {
            return OptionalDouble.of(((Number) value).doubleValue());
        }
        throw new ClassCastException(String.format("The value is not a Number (%s)", value.getClass().getName()));
    }

    /**
     * Gets the {@code LocalDateTime} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code Optional<LocalDateTime>}
     */
    public static final Optional<LocalDateTime> dateTimeValue(Document document, String key) {
        var value = document.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof Date) {
            return Optional.of(DateTimeUtil.local((Date) value));
        }
        throw new ClassCastException(String.format("The value is not a Date (%s)", value.getClass().getName()));
    }

    /**
     * Gets the {@code ZonedDateTime} value in a document.
     *
     * @param zone     the zone to combine with, not null
     * @param document the source document
     * @param key      the key
     * @return an {@code Optional<ZonedDateTime>}
     */
    public static final Optional<ZonedDateTime> dateTimeValue(ZoneId zone, Document document, String key) {
        var value = document.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof Date) {
            return Optional.of(DateTimeUtil.zoned((Date) value, zone));
        }
        throw new ClassCastException(String.format("The value is not a Date (%s)", value.getClass().getName()));
    }

    /**
     * Convert the specified {@link LocalDateTime} to {@link BsonDateTime}.
     *
     * @param time the {@code LocalDateTime} to be converted
     * @return a {@code BsonDateTime}
     */
    public static final BsonDateTime toBsonDateTime(LocalDateTime time) {
        return toBsonDateTime(time.atZone(ZoneId.systemDefault()));
    }

    /**
     * Convert the specified {@link ZonedDateTime} to {@link BsonDateTime}.
     *
     * @param time the {@code ZonedDateTime} to be converted
     * @return a {@code BsonDateTime}
     */
    public static final BsonDateTime toBsonDateTime(ZonedDateTime time) {
        return new BsonDateTime(time.toInstant().toEpochMilli());
    }

    /**
     * Gets the {@link BsonValue} in an embedded document.
     *
     * @param <V>      the type of the return value
     * @param document the source document
     * @param keys     the array of keys
     * @return an {@code Optional<V>}
     */
    public static final <V extends BsonValue> Optional<V> embedded(BsonDocument document, Object... keys) {
        return embedded(document, Arrays.asList(keys));
    }

    /**
     * Gets the {@link BsonValue} in an embedded document.
     *
     * @param <V>      the type of the return value
     * @param document the source document
     * @param keys     the list of keys
     * @return an {@code Optional<V>}
     */
    @SuppressWarnings("unchecked")
    public static final <V extends BsonValue> Optional<V> embedded(BsonDocument document, List<Object> keys) {
        BsonValue value = Objects.requireNonNull(document, "document must not be null");
        var i = 0;
        for (var key : keys) {
            value = nextValue(keys, value, i, key);
            if (value == null) {
                return Optional.empty();
            }
            i++;
        }
        return Optional.of((V) value);
    }

    private static final BsonValue nextValue(List<Object> keys, BsonValue value, int i, Object key) {
        if (value instanceof BsonNull) {
            value = null;
        } else if (value instanceof BsonDocument doc) {
            value = doc.get(key);
        } else if (value instanceof BsonArray list) {
            var index = key instanceof Number ? ((Number) key).intValue() : Integer.parseInt(key.toString());
            if (index < list.size()) {
                value = list.get(index);
            } else {
                value = null;
            }
        } else {
            var path = keys.stream().limit(i).map(String::valueOf).collect(Collectors.joining("."));
            throw new ClassCastException(
                    String.format("At dot notation \"%s\", the value is not a Document or a List (%s)", path,
                            value.getClass().getName()));
        }
        return value;
    }

    /**
     * Gets the {@code BsonDocument} value in an embedded document.
     *
     * @param document the source document
     * @param keys     the array of keys
     * @return an {@code Optional<BsonDocument>}
     */
    public static final Optional<BsonDocument> embeddedDocument(BsonDocument document, Object... keys) {
        return embedded(document, keys);
    }

    /**
     * Gets the {@code BsonDocument} value in an embedded document.
     *
     * @param document the source document
     * @param keys     the list of keys
     * @return an {@code Optional<BsonDocument>}
     */
    public static final Optional<BsonDocument> embeddedDocument(BsonDocument document, List<Object> keys) {
        return embedded(document, keys);
    }

    /**
     * Gets the {@code int} value in an embedded document.
     *
     * @param document the source document
     * @param keys     the array of keys
     * @return an {@code OptionalInt}
     */
    public static final OptionalInt embeddedInt(BsonDocument document, Object... keys) {
        return embeddedInt(document, Arrays.asList(keys));
    }

    /**
     * Gets the {@code int} value in an embedded document.
     *
     * @param document the source document
     * @param keys     the list of keys
     * @return an {@code OptionalInt}
     */
    public static final OptionalInt embeddedInt(BsonDocument document, List<Object> keys) {
        Optional<BsonNumber> value = embedded(document, keys);
        return value.map(bsonNumber -> OptionalInt.of(bsonNumber.intValue())).orElseGet(OptionalInt::empty);
    }

    /**
     * Gets the {@code long} value in an embedded document.
     *
     * @param document the source document
     * @param keys     the array of keys
     * @return an {@code OptionalLong}
     */
    public static final OptionalLong embeddedLong(BsonDocument document, Object... keys) {
        return embeddedLong(document, Arrays.asList(keys));
    }

    /**
     * Gets the {@code long} value in an embedded document.
     *
     * @param document the source document
     * @param keys     the list of keys
     * @return an {@code OptionalLong}
     */
    public static final OptionalLong embeddedLong(BsonDocument document, List<Object> keys) {
        Optional<BsonNumber> value = embedded(document, keys);
        return value.map(bsonNumber -> OptionalLong.of(bsonNumber.longValue())).orElseGet(OptionalLong::empty);
    }

    /**
     * Gets the {@code double} value in an embedded document.
     *
     * @param document the source document
     * @param keys     the array of keys
     * @return an {@code OptionalDouble}
     */
    public static final OptionalDouble embeddedDouble(BsonDocument document, Object... keys) {
        return embeddedDouble(document, Arrays.asList(keys));
    }

    /**
     * Gets the {@code double} value in an embedded document.
     *
     * @param document the source document
     * @param keys     the list of keys
     * @return an {@code OptionalDouble}
     */
    public static final OptionalDouble embeddedDouble(BsonDocument document, List<Object> keys) {
        Optional<BsonNumber> value = embedded(document, keys);
        return value.map(bsonNumber -> OptionalDouble.of(bsonNumber.doubleValue())).orElseGet(OptionalDouble::empty);
    }

    /**
     * Gets the {@code LocalDateTime} value in an embedded document.
     *
     * @param document the source document
     * @param keys     the array of keys
     * @return an {@code Optional<LocalDateTime>}
     */
    public static final Optional<LocalDateTime> embeddedDateTime(BsonDocument document, Object... keys) {
        return embeddedDateTime(document, Arrays.asList(keys));
    }

    /**
     * Gets the {@code LocalDateTime} value in an embedded document.
     *
     * @param document the source document
     * @param keys     the list of keys
     * @return an {@code Optional<LocalDateTime>}
     */
    public static final Optional<LocalDateTime> embeddedDateTime(BsonDocument document, List<Object> keys) {
        return embedded(document, keys).map(BsonUtil::toLocalDateTime);
    }

    /**
     * Gets the {@code ZonedDateTime} value in an embedded document.
     *
     * @param zone     the zone to combine with, not null
     * @param document the source document
     * @param keys     the array of keys
     * @return an {@code Optional<ZonedDateTime>}
     */
    public static final Optional<ZonedDateTime> embeddedDateTime(ZoneId zone, BsonDocument document, Object... keys) {
        return embeddedDateTime(zone, document, Arrays.asList(keys));
    }

    /**
     * Gets the {@code ZonedDateTime} value in an embedded document.
     *
     * @param zone     the zone to combine with, not null
     * @param document the source document
     * @param keys     the list of keys
     * @return an {@code Optional<ZonedDateTime>}
     */
    public static final Optional<ZonedDateTime> embeddedDateTime(ZoneId zone, BsonDocument document,
                                                                 List<Object> keys) {
        return embedded(document, keys).map(value -> toZonedDateTime(value, zone));
    }

    /**
     * Gets the {@code string} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code Optional<String>}
     */
    public static final Optional<String> stringValue(BsonDocument document, String key) {
        var value = document.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        return Optional.of(value.asString().getValue());
    }

    /**
     * Gets the {@code BsonDocument} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code Optional<BsonDocument>}
     */
    public static final Optional<BsonDocument> documentValue(BsonDocument document, String key) {
        var value = document.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        return Optional.of(value.asDocument());
    }

    /**
     * Gets the {@code int} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code OptionalInt}
     */
    public static final OptionalInt intValue(BsonDocument document, String key) {
        var value = document.get(key);
        if (value == null || value.isNull()) {
            return OptionalInt.empty();
        }
        if (value instanceof BsonNumber) {
            return OptionalInt.of(((BsonNumber) value).intValue());
        }
        throw new ClassCastException(String.format("The value is not a BsonNumber (%s)", value.getClass().getName()));
    }

    /**
     * Gets the {@code long} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code OptionalLong}
     */
    public static final OptionalLong longValue(BsonDocument document, String key) {
        var value = document.get(key);
        if (value == null || value.isNull()) {
            return OptionalLong.empty();
        }
        if (value instanceof BsonNumber) {
            return OptionalLong.of(((BsonNumber) value).longValue());
        }
        throw new ClassCastException(String.format("The value is not a BsonNumber (%s)", value.getClass().getName()));
    }

    /**
     * Gets the {@code double} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code OptionalDouble}
     */
    public static final OptionalDouble doubleValue(BsonDocument document, String key) {
        var value = document.get(key);
        if (value == null || value.isNull()) {
            return OptionalDouble.empty();
        }
        if (value instanceof BsonNumber) {
            return OptionalDouble.of(((BsonNumber) value).doubleValue());
        }
        throw new ClassCastException(String.format("The value is not a BsonNumber (%s)", value.getClass().getName()));
    }

    /**
     * Gets the {@code LocalDateTime} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code Optional<LocalDateTime>}
     */
    public static final Optional<LocalDateTime> dateTimeValue(BsonDocument document, String key) {
        var value = document.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        return Optional.of(BsonUtil.toLocalDateTime(value));
    }

    /**
     * Gets the {@code ZonedDateTime} value in a document.
     *
     * @param zone     the zone to combine with, not null
     * @param document the source document
     * @param key      the key
     * @return an {@code Optional<ZonedDateTime>}
     */
    public static final Optional<ZonedDateTime> dateTimeValue(ZoneId zone, BsonDocument document, String key) {
        var value = document.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        if (value.isDateTime()) {
            return Optional.of(ZonedDateTime.ofInstant(Instant.ofEpochMilli(((BsonDateTime) value).getValue()), zone));
        } else if (value.isTimestamp()) {
            return Optional.of(DateTimeUtil.zoned(((BsonTimestamp) value).getTime(), zone));
        }
        throw new ClassCastException(
                String.format("The value is not a BsonDateTime or BsonTimestamp (%s)", value.getClass().getName()));
    }

    /**
     * Gets the {@code ObjectId} value in a document with key {@code "_id"}.
     *
     * @param document the source document
     * @return an {@code Optional<ObjectId>}
     */
    public static final Optional<ObjectId> objectIdValue(Document document) {
        return objectIdValue(document, "_id");
    }

    /**
     * Gets the {@code ObjectId} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code Optional<ObjectId>}
     */
    public static final Optional<ObjectId> objectIdValue(Document document, String key) {
        return Optional.ofNullable(document.getObjectId(key));
    }

    /**
     * Gets the {@code ObjectId} value in a document with key {@code "_id"}.
     *
     * @param document the source document
     * @return an {@code Optional<ObjectId>}
     */
    public static final Optional<ObjectId> objectIdValue(BsonDocument document) {
        return objectIdValue(document, "_id");
    }

    /**
     * Gets the {@code ObjectId} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code Optional<ObjectId>}
     */
    public static final Optional<ObjectId> objectIdValue(BsonDocument document, String key) {
        var value = document.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        return Optional.of(value.asObjectId().getValue());
    }

    /**
     * Parse the JSON value to {@link Document}.
     *
     * @param json the source JSON string
     * @return a {@code Document}
     */
    public static final Document parseToDocument(String json) {
        return parseToDocument(json.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Parse the JSON value to {@link Document}.
     *
     * @param json the source byte array stores JSON value
     * @return a {@code Document}
     */
    public static final Document parseToDocument(byte[] json) {
        var iterator = JsonIterator.parse(json);
        try {
            var type = iterator.whatIsNext();
            if (type == ValueType.OBJECT) {
                return convertToDocument(iterator);
            }
            throw new IllegalArgumentException("the given json expected an OBJECT but was " + type);
        } catch (Exception e) {
            throw new JsonException(e);
        }
    }

    private static final Document convertToDocument(JsonIterator iterator) throws IOException {
        var doc = new Document();
        for (var field = iterator.readObject(); field != null; field = iterator.readObject()) {
            var type = iterator.whatIsNext();
            if (type == ValueType.OBJECT) {
                doc.put(field, convertToDocument(iterator));
            } else if (type == ValueType.ARRAY) {
                doc.put(field, convertToList(iterator));
            } else {
                doc.put(field, convertToValue(type, iterator));
            }
        }
        return doc;
    }

    private static final List<Object> convertToList(JsonIterator iterator) throws IOException {
        var list = new ArrayList<>();
        for (; iterator.readArray(); ) {
            var type = iterator.whatIsNext();
            if (type == ValueType.OBJECT) {
                list.add(convertToDocument(iterator));
            } else if (type == ValueType.ARRAY) {
                list.add(convertToList(iterator));
            } else {
                list.add(convertToValue(type, iterator));
            }
        }
        return list;
    }

    private static final Object convertToValue(ValueType type, JsonIterator iterator) throws IOException {
        if (type == ValueType.STRING) {
            return iterator.readString();
        } else if (type == ValueType.NUMBER) {
            var str = iterator.readNumberAsString();
            var dotIndex = str.indexOf('.');
            if (dotIndex == -1) {
                try {
                    long value = Long.parseLong(str);
                    if (value <= Integer.MAX_VALUE && value >= Integer.MIN_VALUE) {
                        return (int) value;
                    }
                    return value;
                } catch (NumberFormatException e) {
                    return Decimal128.parse(str);
                }
            } else {
                if (str.length() > 15) {
                    return Decimal128.parse(str);
                }
                return Double.valueOf(str);
            }
        } else if (type == ValueType.NULL) {
            iterator.readNull();
            return null;
        } else if (type == ValueType.BOOLEAN) {
            return iterator.readBoolean();
        }
        iterator.skip();
        return null;
    }

    /**
     * Parse the JSON value to {@link BsonValue}.
     *
     * @param <T>  the type of the return value
     * @param json the source JSON string
     * @return a {@code BsonValue}
     */
    public static final <T extends BsonValue> T parseToBson(String json) {
        return parseToBson(json.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Parse the JSON value to {@link BsonValue}.
     *
     * @param <T>  the type of the return value
     * @param json the source byte array stores JSON value
     * @return a {@code BsonValue}
     */
    @SuppressWarnings("unchecked")
    public static final <T extends BsonValue> T parseToBson(byte[] json) {
        var iterator = JsonIterator.parse(json);
        try {
            return (T) convertToBson(iterator);
        } catch (Exception e) {
            throw new JsonException(e);
        }
    }

    private static final BsonValue convertToBson(JsonIterator iterator) throws IOException {
        var type = iterator.whatIsNext();
        if (type == ValueType.OBJECT) {
            var doc = new BsonDocument();
            for (var field = iterator.readObject(); field != null; field = iterator.readObject()) {
                doc.put(field, convertToBson(iterator));
            }
            return doc;
        } else if (type == ValueType.ARRAY) {
            BsonArray array = new BsonArray();
            for (; iterator.readArray(); ) {
                array.add(convertToBson(iterator));
            }
            return array;
        } else {
            if (type == ValueType.STRING) {
                return new BsonString(iterator.readString());
            } else if (type == ValueType.NUMBER) {
                var str = iterator.readNumberAsString();
                var dotIndex = str.indexOf('.');
                if (dotIndex == -1) {
                    try {
                        long value = Long.parseLong(str);
                        if (value <= Integer.MAX_VALUE && value >= Integer.MIN_VALUE) {
                            return new BsonInt32((int) value);
                        }
                        return new BsonInt64(value);
                    } catch (NumberFormatException e) {
                        return new BsonDecimal128(Decimal128.parse(str));
                    }
                } else {
                    if (str.length() > 15) {
                        return new BsonDecimal128(Decimal128.parse(str));
                    }
                    double value = Double.parseDouble(str);
                    return new BsonDouble(value);
                }
            } else if (type == ValueType.NULL) {
                iterator.readNull();
                return BsonNull.VALUE;
            } else if (type == ValueType.BOOLEAN) {
                return BsonBoolean.valueOf(iterator.readBoolean());
            }
            iterator.skip();
            // no other type
            return BsonNull.VALUE;
        }
    }

    /**
     * Gets the list value in an embedded document.
     *
     * @param <E>      the type of the element in list
     * @param document the source document
     * @param keys     the array of keys
     * @return an {@code Optional<List<E>>}
     */
    public static final <E> Optional<List<E>> embeddedList(Document document, Object... keys) {
        return embedded(document, keys);
    }

    /**
     * Gets the list value in an embedded document.
     *
     * @param <E>      the type of the element in list
     * @param document the source document
     * @param keys     the list of keys
     * @return an {@code Optional<List<E>>}
     */
    public static final <E> Optional<List<E>> embeddedList(Document document, List<Object> keys) {
        return embedded(document, keys);
    }

    /**
     * Gets the {@code Document} value in a document.
     *
     * @param <E>      the type of the element in list
     * @param document the source document
     * @param key      the key
     * @return an {@code Optional<List<E>>}
     */
    @SuppressWarnings("unchecked")
    public static final <E> Optional<List<E>> listValue(Document document, String key) {
        var value = document.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof List) {
            return Optional.of((List<E>) value);
        }
        throw new ClassCastException(String.format("The value is not a List (%s)", value.getClass().getName()));
    }

    /**
     * Gets the {@code BsonArray} value in an embedded document.
     *
     * @param document the source document
     * @param keys     the array of keys
     * @return an {@code Optional<BsonArray>}
     */
    public static final Optional<BsonArray> embeddedArray(BsonDocument document, Object... keys) {
        return embedded(document, keys);
    }

    /**
     * Gets the {@code BsonArray} value in an embedded document.
     *
     * @param document the source document
     * @param keys     the list of keys
     * @return an {@code Optional<BsonArray>}
     */
    public static final Optional<BsonArray> embeddedArray(BsonDocument document, List<Object> keys) {
        return embedded(document, keys);
    }

    /**
     * Gets the {@code BsonArray} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code Optional<BsonArray>}
     */
    public static final Optional<BsonArray> arrayValue(BsonDocument document, String key) {
        var value = document.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        return Optional.of(value.asArray());
    }

    /**
     * Converts the specified {@link BsonValue} to {@link LocalDateTime} with the
     * system default time-zone.
     *
     * @param value the {@code BsonValue}
     * @return a {@code LocalDateTime}
     */
    public static final LocalDateTime toLocalDateTime(BsonValue value) {
        return toLocalDateTime(value, ZoneId.systemDefault());
    }

    /**
     * Converts the specified {@link BsonValue} to {@link LocalDateTime}.
     *
     * @param value the {@code BsonValue}
     * @param zone  the time zone
     * @return a {@code LocalDateTime}
     */
    public static final LocalDateTime toLocalDateTime(BsonValue value, ZoneId zone) {
        if (value.isDateTime()) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(((BsonDateTime) value).getValue()), zone);
        } else if (value.isTimestamp()) {
            return DateTimeUtil.local(((BsonTimestamp) value).getTime());
        }
        throw new ClassCastException(
                String.format("The value is not a BsonDateTime or BsonTimestamp (%s)", value.getClass().getName()));
    }

    /**
     * Converts the specified {@link BsonValue} to {@link ZonedDateTime} with the
     * system default time-zone.
     *
     * @param value the {@code BsonValue}
     * @return a {@code ZonedDateTime}
     */
    public static final ZonedDateTime toZonedDateTime(BsonValue value) {
        return toZonedDateTime(value, ZoneId.systemDefault());
    }

    /**
     * Converts the specified {@link BsonValue} to {@link ZonedDateTime}.
     *
     * @param value the {@code BsonValue}
     * @param zone  the time zone
     * @return a {@code ZonedDateTime}
     */
    public static final ZonedDateTime toZonedDateTime(BsonValue value, ZoneId zone) {
        if (value.isDateTime()) {
            return ZonedDateTime.ofInstant(Instant.ofEpochMilli(((BsonDateTime) value).getValue()), zone);
        } else if (value.isTimestamp()) {
            return DateTimeUtil.zoned(((BsonTimestamp) value).getTime(), zone);
        }
        throw new ClassCastException(
                String.format("The value is not a BsonDateTime or BsonTimestamp (%s)", value.getClass().getName()));
    }

    /**
     * Converts the specified {@link BsonValue} to {@link OffsetDateTime} with the
     * system default time-zone.
     *
     * @param value the {@code BsonValue}
     * @return a {@code OffsetDateTime}
     */
    public static final OffsetDateTime toOffsetDateTime(BsonValue value) {
        return toOffsetDateTime(value, ZoneId.systemDefault());
    }

    /**
     * Converts the specified {@link BsonValue} to {@link OffsetDateTime}.
     *
     * @param value the {@code BsonValue}
     * @param zone  the time zone
     * @return a {@code OffsetDateTime}
     */
    public static final OffsetDateTime toOffsetDateTime(BsonValue value, ZoneId zone) {
        if (value.isDateTime()) {
            return OffsetDateTime.ofInstant(Instant.ofEpochMilli(((BsonDateTime) value).getValue()), zone);
        } else if (value.isTimestamp()) {
            return DateTimeUtil.offset(((BsonTimestamp) value).getTime(), zone);
        }
        throw new ClassCastException(
                String.format("The value is not a BsonDateTime or BsonTimestamp (%s)", value.getClass().getName()));
    }

    /**
     * Gets the object value in an {@link Any}.
     *
     * @param any the source {@link Any}
     * @param key the key
     * @return an {@code Optional<Any>}
     */
    public static final Optional<Any> objectValue(Any any, String key) {
        var value = any.get(key);
        if (value == null || value.valueType() == ValueType.INVALID || value.valueType() == ValueType.NULL) {
            return Optional.empty();
        }
        if (value.valueType() == ValueType.OBJECT) {
            return Optional.of(value);
        }
        throw new ClassCastException(String.format("The value is not a OBJECT (%s)", value.valueType().name()));
    }

    /**
     * Gets the boolean value in an {@link Any}.
     *
     * @param any the source {@link Any}
     * @param key the key
     * @return an {@code Optional<Boolean>}
     */
    public static final Optional<Boolean> booleanValue(Any any, String key) {
        var value = any.get(key);
        if (value == null || value.valueType() == ValueType.INVALID || value.valueType() == ValueType.NULL) {
            return Optional.empty();
        }
        if (value.valueType() == ValueType.BOOLEAN) {
            return Optional.of(value.toBoolean());
        }
        throw new ClassCastException(String.format("The value is not a BOOLEAN (%s)", value.valueType().name()));
    }

    /**
     * Gets the boolean value in an {@link Any}.
     *
     * @param any the source {@link Any}
     * @param key the key
     * @return an {@code Optional<String>}
     */
    public static final Optional<String> stringValue(Any any, String key) {
        var value = any.get(key);
        if (value == null || value.valueType() == ValueType.INVALID || value.valueType() == ValueType.NULL) {
            return Optional.empty();
        }
        if (value.valueType() == ValueType.STRING) {
            return Optional.of(value.toString());
        }
        throw new ClassCastException(String.format("The value is not a STRING (%s)", value.valueType().name()));
    }

    /**
     * Gets the array value in an {@link Any}.
     *
     * @param any the source {@link Any}
     * @param key the key
     * @return an {@code Optional<Any>}
     */
    public static final Optional<Any> arrayValue(Any any, String key) {
        var value = any.get(key);
        if (value == null || value.valueType() == ValueType.INVALID || value.valueType() == ValueType.NULL) {
            return Optional.empty();
        }
        if (value.valueType() == ValueType.ARRAY) {
            return Optional.of(value);
        }
        throw new ClassCastException(String.format("The value is not a ARRAY (%s)", value.valueType().name()));
    }

    /**
     * Gets the {@code int} value in an {@link Any}.
     *
     * @param any the source {@link Any}
     * @param key the key
     * @return an {@code OptionalInt}
     */
    public static final OptionalInt intValue(Any any, String key) {
        var value = any.get(key);
        if (value == null || value.valueType() == ValueType.INVALID || value.valueType() == ValueType.NULL) {
            return OptionalInt.empty();
        }
        if (value.valueType() == ValueType.NUMBER) {
            return OptionalInt.of(value.toInt());
        }
        throw new ClassCastException(String.format("The value is not a NUMBER (%s)", value.valueType().name()));
    }

    /**
     * Gets the {@code long} value in an {@link Any}.
     *
     * @param any the source {@link Any}
     * @param key the key
     * @return an {@code OptionalLong}
     */
    public static final OptionalLong longValue(Any any, String key) {
        var value = any.get(key);
        if (value == null || value.valueType() == ValueType.INVALID || value.valueType() == ValueType.NULL) {
            return OptionalLong.empty();
        }
        if (value.valueType() == ValueType.NUMBER) {
            return OptionalLong.of(value.toLong());
        }
        throw new ClassCastException(String.format("The value is not a NUMBER (%s)", value.valueType().name()));
    }

    /**
     * Gets the {@code double} value in an {@link Any}.
     *
     * @param any the source {@link Any}
     * @param key the key
     * @return an {@code OptionalDouble}
     */
    public static final OptionalDouble doubleValue(Any any, String key) {
        var value = any.get(key);
        if (value == null || value.valueType() == ValueType.INVALID || value.valueType() == ValueType.NULL) {
            return OptionalDouble.empty();
        }
        if (value.valueType() == ValueType.NUMBER) {
            return OptionalDouble.of(value.toDouble());
        }
        throw new ClassCastException(String.format("The value is not a NUMBER (%s)", value.valueType().name()));
    }

    /**
     * Gets the {@code LocalDateTime} value in an {@link Any}.
     *
     * @param any the source {@link Any}
     * @param key the key
     * @return an {@code Optional<LocalDateTime>}
     */
    public static final Optional<LocalDateTime> dateTimeValue(Any any, String key) {
        var value = any.get(key);
        if (value == null || value.valueType() == ValueType.INVALID || value.valueType() == ValueType.NULL) {
            return Optional.empty();
        }
        if (value.valueType() == ValueType.NUMBER) {
            return Optional.of(DateTimeUtil.ofEpochMilli(value.toLong()));
        }
        throw new ClassCastException(String.format("The value is not a NUMBER (%s)", value.valueType().name()));
    }

    /**
     * Gets the {@code ZonedDateTime} value in an {@link Any}.
     *
     * @param zone the zone to combine with, not null
     * @param any  the source {@link Any}
     * @param key  the key
     * @return an {@code Optional<ZonedDateTime>}
     */
    public static final Optional<ZonedDateTime> dateTimeValue(ZoneId zone, Any any, String key) {
        var value = any.get(key);
        if (value == null || value.valueType() == ValueType.INVALID || value.valueType() == ValueType.NULL) {
            return Optional.empty();
        }
        if (value.valueType() == ValueType.NUMBER) {
            return Optional.of(DateTimeUtil.ofEpochMilli(value.toLong(), zone));
        }
        throw new ClassCastException(String.format("The value is not a NUMBER (%s)", value.valueType().name()));
    }

    /**
     * Gets the {@code ObjectId} value in an {@link Any} with key {@code "_id"}.
     *
     * @param any the source {@link Any}
     * @return an {@code Optional<ObjectId>}
     */
    public static final Optional<ObjectId> objectIdValue(Any any) {
        return objectIdValue(any, "_id");
    }

    /**
     * Gets the {@code ObjectId} value in an {@link Any}.
     *
     * @param any the source {@link Any}
     * @param key the key
     * @return an {@code Optional<ObjectId>}
     */
    public static final Optional<ObjectId> objectIdValue(Any any, String key) {
        var value = any.get(key);
        if (value == null || value.valueType() == ValueType.INVALID || value.valueType() == ValueType.NULL) {
            return Optional.empty();
        }
        if (value.valueType() == ValueType.STRING) {
            return Optional.of(new ObjectId(value.toString()));
        }
        throw new ClassCastException(String.format("The value is not a STRING (%s)", value.valueType().name()));
    }

    /**
     * Gets the object value in an {@link JsonNode}.
     *
     * @param node the source {@link JsonNode}
     * @param key  the key
     * @return an {@code Optional<JsonNode>}
     */
    public static final Optional<JsonNode> objectValue(JsonNode node, String key) {
        var value = node.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        if (value.isObject()) {
            return Optional.of(value);
        }
        throw new ClassCastException(String.format("The value is not a OBJECT (%s)", value.getNodeType().name()));
    }

    /**
     * Gets the boolean value in an {@link JsonNode}.
     *
     * @param node the source {@link JsonNode}
     * @param key  the key
     * @return an {@code Optional<Boolean>}
     */
    public static final Optional<Boolean> booleanValue(JsonNode node, String key) {
        var value = node.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        if (value.isBoolean()) {
            return Optional.of(value.booleanValue());
        }
        throw new ClassCastException(String.format("The value is not a BOOLEAN (%s)", value.getNodeType().name()));
    }

    /**
     * Gets the array value in an {@link JsonNode}.
     *
     * @param node the source {@link JsonNode}
     * @param key  the key
     * @return an {@code Optional<JsonNode>}
     */
    public static final Optional<JsonNode> arrayValue(JsonNode node, String key) {
        var value = node.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        if (value.isArray()) {
            return Optional.of(value);
        }
        throw new ClassCastException(String.format("The value is not a ARRAY (%s)", value.getNodeType().name()));
    }

    /**
     * Gets the string value in an {@link JsonNode}.
     *
     * @param node the source {@link JsonNode}
     * @param key  the key
     * @return an {@code Optional<String>}
     */
    public static final Optional<String> stringValue(JsonNode node, String key) {
        var value = node.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        if (value.isTextual()) {
            return Optional.of(value.textValue());
        }
        throw new ClassCastException(String.format("The value is not a STRING (%s)", value.getNodeType().name()));
    }

    /**
     * Gets the {@code int} value in an {@link JsonNode}.
     *
     * @param node the source {@link JsonNode}
     * @param key  the key
     * @return an {@code OptionalInt}
     */
    public static final OptionalInt intValue(JsonNode node, String key) {
        var value = node.get(key);
        if (value == null || value.isNull()) {
            return OptionalInt.empty();
        }
        if (value.isNumber()) {
            return OptionalInt.of(value.intValue());
        }
        throw new ClassCastException(String.format("The value is not a NUMBER (%s)", value.getNodeType().name()));
    }

    /**
     * Gets the {@code long} value in an {@link JsonNode}.
     *
     * @param node the source {@link JsonNode}
     * @param key  the key
     * @return an {@code OptionalLong}
     */
    public static final OptionalLong longValue(JsonNode node, String key) {
        var value = node.get(key);
        if (value == null || value.isNull()) {
            return OptionalLong.empty();
        }
        if (value.isNumber()) {
            return OptionalLong.of(value.longValue());
        }
        throw new ClassCastException(String.format("The value is not a NUMBER (%s)", value.getNodeType().name()));
    }

    /**
     * Gets the {@code double} value in an {@link JsonNode}.
     *
     * @param node the source {@link JsonNode}
     * @param key  the key
     * @return an {@code OptionalDouble}
     */
    public static final OptionalDouble doubleValue(JsonNode node, String key) {
        var value = node.get(key);
        if (value == null || value.isNull()) {
            return OptionalDouble.empty();
        }
        if (value.isNumber()) {
            return OptionalDouble.of(value.doubleValue());
        }
        throw new ClassCastException(String.format("The value is not a NUMBER (%s)", value.getNodeType().name()));
    }

    /**
     * Gets the {@code LocalDateTime} value in an {@link JsonNode}.
     *
     * @param node the source {@link JsonNode}
     * @param key  the key
     * @return an {@code Optional<LocalDateTime>}
     */
    public static final Optional<LocalDateTime> dateTimeValue(JsonNode node, String key) {
        var value = node.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        if (value.isNumber()) {
            return Optional.of(DateTimeUtil.ofEpochMilli(value.longValue()));
        }
        throw new ClassCastException(String.format("The value is not a NUMBER (%s)", value.getNodeType().name()));
    }

    /**
     * Gets the {@code ZonedDateTime} value in an {@link JsonNode}.
     *
     * @param zone the zone to combine with, not null
     * @param node the source {@link JsonNode}
     * @param key  the key
     * @return an {@code Optional<ZonedDateTime>}
     */
    public static final Optional<ZonedDateTime> dateTimeValue(ZoneId zone, JsonNode node, String key) {
        var value = node.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        if (value.isNumber()) {
            return Optional.of(DateTimeUtil.ofEpochMilli(value.longValue(), zone));
        }
        throw new ClassCastException(String.format("The value is not a NUMBER (%s)", value.getNodeType().name()));
    }

    /**
     * Gets the {@code ObjectId} value in an {@link JsonNode} with key
     * {@code "_id"}.
     *
     * @param node the source {@link JsonNode}
     * @return an {@code Optional<ObjectId>}
     */
    public static final Optional<ObjectId> objectIdValue(JsonNode node) {
        return objectIdValue(node, "_id");
    }

    /**
     * Gets the {@code ObjectId} value in an {@link JsonNode}.
     *
     * @param node the source {@link JsonNode}
     * @param key  the key
     * @return an {@code Optional<ObjectId>}
     */
    public static final Optional<ObjectId> objectIdValue(JsonNode node, String key) {
        var value = node.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        if (value.isTextual()) {
            return Optional.of(new ObjectId(value.textValue()));
        }
        throw new ClassCastException(String.format("The value is not a STRING (%s)", value.getNodeType().name()));
    }

    /**
     * Gets the {@link UUID} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code Optional<String>}
     * @since 2.0
     */
    public static final Optional<UUID> uuidValue(Document document, String key) {
        return Optional.ofNullable(document.get(key, UUID.class));
    }

    /**
     * Gets the {@link UUID} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code Optional<UUID>}
     * @since 2.0
     */
    public static final Optional<UUID> uuidValue(BsonDocument document, String key) {
        return uuidValue(document, key, UuidRepresentation.STANDARD);
    }

    /**
     * Gets the {@link UUID} value in a document.
     *
     * @param document           the source document
     * @param key                the key
     * @param uuidRepresentation the representation to use when converting a UUID to a BSON binary value
     * @return an {@code Optional<UUID>}
     * @since 2.0
     */
    public static final Optional<UUID> uuidValue(BsonDocument document, String key,
                                                 UuidRepresentation uuidRepresentation) {
        var value = document.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        return Optional.of(value.asBinary().asUuid(uuidRepresentation));
    }

    /**
     * Gets the {@code Integer} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code Optional<Integer>}
     * @since 2.0
     */
    public static final Optional<Integer> integerValue(BsonDocument document, String key) {
        var value = document.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        if (value instanceof BsonNumber) {
            return Optional.of(((BsonNumber) value).intValue());
        }
        throw new ClassCastException(String.format("The value is not a BsonNumber (%s)", value.getClass().getName()));
    }

    /**
     * Gets the {@code Integer} value in an {@link JsonNode}.
     *
     * @param node the source {@link JsonNode}
     * @param key  the key
     * @return an {@code Optional<Integer>}
     * @since 2.0
     */
    public static final Optional<Integer> integerValue(JsonNode node, String key) {
        var value = node.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        if (value.isNumber()) {
            return Optional.of(value.intValue());
        }
        throw new ClassCastException(String.format("The value is not a NUMBER (%s)", value.getNodeType().name()));
    }

    /**
     * Gets the {@code Long} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code Optional<Long>}
     * @since 2.0
     */
    public static final Optional<Long> boxedLongValue(BsonDocument document, String key) {
        var value = document.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        if (value instanceof BsonNumber) {
            return Optional.of(((BsonNumber) value).longValue());
        }
        throw new ClassCastException(String.format("The value is not a BsonNumber (%s)", value.getClass().getName()));
    }

    /**
     * Gets the {@code Long} value in an {@link JsonNode}.
     *
     * @param node the source {@link JsonNode}
     * @param key  the key
     * @return an {@code Optional<Long>}
     * @since 2.0
     */
    public static final Optional<Long> boxedLongValue(JsonNode node, String key) {
        var value = node.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        if (value.isNumber()) {
            return Optional.of(value.longValue());
        }
        throw new ClassCastException(String.format("The value is not a NUMBER (%s)", value.getNodeType().name()));
    }

    /**
     * Gets the {@code Double} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code Optional<Double>}
     * @since 2.0
     */
    public static final Optional<Double> boxedDoubleValue(BsonDocument document, String key) {
        var value = document.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        if (value instanceof BsonNumber) {
            return Optional.of(((BsonNumber) value).doubleValue());
        }
        throw new ClassCastException(String.format("The value is not a BsonNumber (%s)", value.getClass().getName()));
    }

    /**
     * Gets the {@code Double} value in an {@link JsonNode}.
     *
     * @param node the source {@link JsonNode}
     * @param key  the key
     * @return an {@code Optional<Double>}
     * @since 2.0
     */
    public static final Optional<Double> boxedDoubleValue(JsonNode node, String key) {
        var value = node.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        if (value.isNumber()) {
            return Optional.of(value.doubleValue());
        }
        throw new ClassCastException(String.format("The value is not a NUMBER (%s)", value.getNodeType().name()));
    }

    /**
     * Gets the {@code Boolean} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code Optional<Boolean>}
     * @since 2.0
     */
    public static final Optional<Boolean> boxedBooleanValue(BsonDocument document, String key) {
        var value = document.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        if (value instanceof BsonBoolean) {
            return Optional.of(((BsonBoolean) value).getValue());
        }
        throw new ClassCastException(String.format("The value is not a BsonBoolean (%s)", value.getClass().getName()));
    }

    /**
     * Gets the {@code Boolean} value in an {@link JsonNode}.
     *
     * @param node the source {@link JsonNode}
     * @param key  the key
     * @return an {@code Optional<Boolean>}
     * @since 2.0
     */
    public static final Optional<Boolean> boxedBooleanValue(JsonNode node, String key) {
        var value = node.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        if (value.isBoolean()) {
            return Optional.of(value.booleanValue());
        }
        throw new ClassCastException(String.format("The value is not a BOOLEAN (%s)", value.getNodeType().name()));
    }

    /**
     * Gets the {@code BsonArray} value in a document, and then convert each element.
     *
     * @param document the source document
     * @param key      the key
     * @param mapper   the value mapper for each element
     * @param <T>      the type for each element in the {@link BsonArray}
     * @param <R>      the target type for each element to be converted to
     * @return an {@code Optional<List<R>>}
     * @since 2.0
     */
    @SuppressWarnings("unchecked")
    public static final <T extends BsonValue, R> Optional<List<R>> arrayValue(
            BsonDocument document, String key, Function<T, R> mapper) {
        var value = document.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        var array = value.asArray();
        var list = new ArrayList<R>(array.size());
        for (var v : array) {
            if (v.isNull()) {
                list.add(null);
            } else {
                list.add(mapper.apply((T) v));
            }
        }
        return Optional.of(list);
    }

    /**
     * Gets the {@code Document} value in a document, and then convert each entry.
     *
     * @param document    the source document
     * @param key         the key
     * @param keyMapper   the mapper for each key
     * @param valueMapper the mapper for each value
     * @param <T>         the type for each value in the {@link BsonDocument}
     * @param <K>         the target type for each key to be converted to
     * @param <V>         the target type for each value to be converted to
     * @return an {@code Optional<Map<K, V>>}
     * @since 2.0
     */
    @SuppressWarnings("unchecked")
    public static final <T extends BsonValue, K, V> Optional<Map<K, V>> documentValue(
            BsonDocument document, String key, Function<String, K> keyMapper, Function<T, V> valueMapper) {
        var value = document.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        var doc = value.asDocument();
        var map = new LinkedHashMap<K, V>(Math.max(8, doc.size()));
        for (var e : doc.entrySet()) {
            var v = e.getValue();
            if (v != null) {
                map.put(keyMapper.apply(e.getKey()), valueMapper.apply((T) v));
            }
        }
        return Optional.of(map);
    }

    /**
     * Gets the {@code Document} value in a document, and then convert each entry.
     *
     * @param document    the source document
     * @param key         the key
     * @param valueMapper the mapper for each value
     * @param <T>         the type for each value in the {@link BsonDocument}
     * @param <V>         the target type for each value to be converted to
     * @return an {@code Optional<Map<String, V>>}
     * @since 2.0
     */
    public static final <T extends BsonValue, V> Optional<Map<String, V>> documentValue(
            BsonDocument document, String key, Function<T, V> valueMapper) {
        return documentValue(document, key, Function.identity(), valueMapper);
    }

    /**
     * Convert the specified {@link Iterable} to {@link BsonArray}.
     *
     * @param iterable the iterable
     * @param mapper   the mapper for each value
     * @param <T>      the type of the element
     * @return a {@code BsonArray}
     * @since 2.0
     */
    public static final <T> BsonArray toBsonArray(Iterable<T> iterable, Function<T, BsonValue> mapper) {
        var bsonArray = new BsonArray(iterable instanceof Collection<T> collection ? collection.size() : 10);
        for (var e : iterable) {
            if (e == null) {
                bsonArray.add(BsonNull.VALUE);
            } else {
                bsonArray.add(mapper.apply(e));
            }
        }
        return bsonArray;
    }

    /**
     * Convert the specified {@code int} array to {@link BsonArray}.
     *
     * @param array the array
     * @return a {@code BsonArray}
     * @since 2.0
     */
    public static final BsonArray toBsonArray(int[] array) {
        var bsonArray = new BsonArray(array.length);
        for (var i : array) {
            bsonArray.add(new BsonInt32(i));
        }
        return bsonArray;
    }

    /**
     * Convert the specified {@code long} array to {@link BsonArray}.
     *
     * @param array the array
     * @return a {@code BsonArray}
     * @since 2.0
     */
    public static final BsonArray toBsonArray(long[] array) {
        var bsonArray = new BsonArray(array.length);
        for (var i : array) {
            bsonArray.add(new BsonInt64(i));
        }
        return bsonArray;
    }

    /**
     * Convert the specified {@code double} array to {@link BsonArray}.
     *
     * @param array the array
     * @return a {@code BsonArray}
     * @since 2.0
     */
    public static final BsonArray toBsonArray(double[] array) {
        var bsonArray = new BsonArray(array.length);
        for (var i : array) {
            bsonArray.add(new BsonDouble(i));
        }
        return bsonArray;
    }

    /**
     * Gets the {@code int array} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code Optional<int[]>}
     * @since 2.0
     */
    public static final Optional<int[]> intArrayValue(BsonDocument document, String key) {
        var value = document.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        var array = value.asArray();
        var values = new int[array.size()];
        for (var i = 0; i < values.length; i++) {
            var v = array.get(i);
            if (v instanceof BsonNumber bsonNumber) {
                values[i] = bsonNumber.intValue();
            } else {
                throw new ClassCastException(String.format("The value is not a BsonNumber (%s)", v.getBsonType()));
            }
        }
        return Optional.of(values);
    }

    /**
     * Gets the {@code int array} value in a {@link JsonNode}.
     *
     * @param src the source {@code JsonNode}
     * @param key the key
     * @return an {@code Optional<int[]>}
     * @since 2.0
     */
    public static final Optional<int[]> intArrayValue(JsonNode src, String key) {
        var value = src.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        if (value.isArray()) {
            var values = new int[value.size()];
            for (var i = 0; i < values.length; i++) {
                values[i] = value.get(i).intValue();
            }
            return Optional.of(values);
        }
        throw new ClassCastException(String.format("The value is not a ARRAY (%s)", value.getNodeType().name()));
    }

    /**
     * Gets the {@code long array} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code Optional<long[]>}
     * @since 2.0
     */
    public static final Optional<long[]> longArrayValue(BsonDocument document, String key) {
        var value = document.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        var array = value.asArray();
        var values = new long[array.size()];
        for (var i = 0; i < values.length; i++) {
            var v = array.get(i);
            if (v instanceof BsonNumber bsonNumber) {
                values[i] = bsonNumber.longValue();
            } else {
                throw new ClassCastException(String.format("The value is not a BsonNumber (%s)", v.getBsonType()));
            }
        }
        return Optional.of(values);
    }

    /**
     * Gets the {@code long array} value in a {@link JsonNode}.
     *
     * @param src the source {@code JsonNode}
     * @param key the key
     * @return an {@code Optional<long[]>}
     * @since 2.0
     */
    public static final Optional<long[]> longArrayValue(JsonNode src, String key) {
        var value = src.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        if (value.isArray()) {
            var values = new long[value.size()];
            for (var i = 0; i < values.length; i++) {
                values[i] = value.get(i).longValue();
            }
            return Optional.of(values);
        }
        throw new ClassCastException(String.format("The value is not a ARRAY (%s)", value.getNodeType().name()));
    }

    /**
     * Gets the {@code double array} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code Optional<double[]>}
     * @since 2.0
     */
    public static final Optional<double[]> doubleArrayValue(BsonDocument document, String key) {
        var value = document.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        var array = value.asArray();
        var values = new double[array.size()];
        for (var i = 0; i < values.length; i++) {
            var v = array.get(i);
            if (v instanceof BsonNumber bsonNumber) {
                values[i] = bsonNumber.doubleValue();
            } else {
                throw new ClassCastException(String.format("The value is not a BsonNumber (%s)", v.getBsonType()));
            }
        }
        return Optional.of(values);
    }

    /**
     * Gets the {@code double array} value in a {@link JsonNode}.
     *
     * @param src the source {@code JsonNode}
     * @param key the key
     * @return an {@code Optional<double[]>}
     * @since 2.0
     */
    public static final Optional<double[]> doubleArrayValue(JsonNode src, String key) {
        var value = src.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        if (value.isArray()) {
            var values = new double[value.size()];
            for (var i = 0; i < values.length; i++) {
                values[i] = value.get(i).doubleValue();
            }
            return Optional.of(values);
        }
        throw new ClassCastException(String.format("The value is not a ARRAY (%s)", value.getNodeType().name()));
    }

    /**
     * Gets the list value from a {@link JsonNode}.
     *
     * @param node   the source {@link JsonNode}
     * @param key    the key
     * @param mapper the mapper
     * @param <T>    the type of the return element
     * @return an {@code Optional<List<T>>}
     * @since 2.0
     */
    public static final <T> Optional<List<T>> listValue(JsonNode node, String key, Function<JsonNode, T> mapper) {
        var value = node.get(key);
        if (value == null || value.isNull()) {
            return Optional.empty();
        }
        if (value.isArray()) {
            var list = new ArrayList<T>(value.size());
            for (var v : value) {
                if (v == null || v.isNull()) {
                    list.add(null);
                } else {
                    list.add(mapper.apply(v));
                }
            }
            return Optional.of(list);
        }
        throw new ClassCastException(String.format("The value is not a ARRAY (%s)", value.getNodeType().name()));
    }

    /**
     * Convert the specified {@link UUID} to {@link BsonBinary}.
     *
     * @param uuid the {@code UUID} to be converted
     * @return a {@code BsonBinary}
     * @since 2.0
     */
    public static final BsonBinary toBsonBinary(UUID uuid) {
        return new BsonBinary(uuid, UuidRepresentation.STANDARD);
    }

    /**
     * Convert the specified {@link UUID} to {@link BsonBinary} with binary subtype {@code 3} .
     *
     * @param uuid the {@code UUID} to be converted
     * @return a {@code BsonBinary}
     * @since 2.0
     */
    public static final BsonBinary toBsonBinaryUuidLegacy(UUID uuid) {
        return new BsonBinary(uuid, UuidRepresentation.JAVA_LEGACY);
    }

    /**
     * Gets the {@link UUID} value in a document.
     *
     * @param document the source document
     * @param key      the key
     * @return an {@code Optional<UUID>}
     * @since 2.0
     */
    public static final Optional<UUID> uuidLegacyValue(BsonDocument document, String key) {
        return uuidValue(document, key, UuidRepresentation.JAVA_LEGACY);
    }

    /**
     * Convert the specified {@link BsonDocument} to {@link ObjectNode}.
     *
     * @param document the {@code BsonDocument}
     * @return an {@code ObjectNode}
     * @since 2.1
     */
    public static final ObjectNode toObjectNode(BsonDocument document) {
        var objectNode = JsonNodeFactory.instance.objectNode();
        document.forEach((key, value) -> {
            if (value instanceof BsonString v) {
                objectNode.put(key, v.getValue());
            } else if (value instanceof BsonNumber) {
                if (value instanceof BsonInt32 v) {
                    objectNode.put(key, v.getValue());
                } else if (value instanceof BsonInt64 v) {
                    objectNode.put(key, v.getValue());
                } else if (value instanceof BsonDouble v) {
                    objectNode.put(key, v.getValue());
                } else if (value instanceof BsonDecimal128 v) {
                    objectNode.put(key, v.getValue().bigDecimalValue());
                }
            } else if (value instanceof BsonBoolean v) {
                objectNode.put(key, v.getValue());
            } else if (value instanceof BsonNull) {
                objectNode.putNull(key);
            } else if (value instanceof BsonDateTime v) {
                objectNode.put(key, v.getValue());
            } else if (value instanceof BsonTimestamp v) {
                objectNode.put(key, v.getTime() * 1000L);
            } else if (value instanceof BsonObjectId v) {
                objectNode.put(key, v.getValue().toHexString());
            } else if (value instanceof BsonBinary v) {
                // only support standard UUID
                objectNode.put(key, v.asUuid().toString());
            } else if (value instanceof BsonArray array) {
                objectNode.set(key, toArrayNode(array));
            } else if (value instanceof BsonDocument v) {
                objectNode.set(key, toObjectNode(v));
            }
            // ignore other BSON type (e.g. symbol)
        });
        return objectNode;
    }

    /**
     * Convert the specified {@link BsonArray} to {@link ArrayNode}.
     *
     * @param array the {@code BsonArray}
     * @return an {@code ArrayNode}
     * @since 2.1
     */
    public static final ArrayNode toArrayNode(BsonArray array) {
        var arrayNode = JsonNodeFactory.instance.arrayNode(array.size());
        for (var value : array) {
            if (value instanceof BsonString v) {
                arrayNode.add(v.getValue());
            } else if (value instanceof BsonNumber) {
                if (value instanceof BsonInt32 v) {
                    arrayNode.add(v.getValue());
                } else if (value instanceof BsonInt64 v) {
                    arrayNode.add(v.getValue());
                } else if (value instanceof BsonDouble v) {
                    arrayNode.add(v.getValue());
                } else if (value instanceof BsonDecimal128 v) {
                    arrayNode.add(v.getValue().bigDecimalValue());
                }
            } else if (value instanceof BsonBoolean v) {
                arrayNode.add(v.getValue());
            } else if (value instanceof BsonNull) {
                arrayNode.addNull();
            } else if (value instanceof BsonDateTime v) {
                arrayNode.add(v.getValue());
            } else if (value instanceof BsonTimestamp v) {
                arrayNode.add(v.getTime() * 1000L);
            } else if (value instanceof BsonObjectId v) {
                arrayNode.add(v.getValue().toHexString());
            } else if (value instanceof BsonBinary v) {
                // only support standard UUID
                arrayNode.add(v.asUuid().toString());
            } else if (value instanceof BsonArray v) {
                arrayNode.add(toArrayNode(v));
            } else if (value instanceof BsonDocument v) {
                arrayNode.add(toObjectNode(v));
            }
            // ignore other BSON type (e.g. symbol)
        }
        return arrayNode;
    }

    /**
     * Convert the specified {@link BsonDocument} to {@link Map} data.
     *
     * @param document the {@code BsonDocument}
     * @return an {@code Map<String, Object>}
     * @since 2.1
     */
    public static final Map<String, Object> toMap(BsonDocument document) {
        var map = new LinkedHashMap<String, Object>();
        document.forEach((key, value) -> {
            if (value instanceof BsonString v) {
                map.put(key, v.getValue());
            } else if (value instanceof BsonNumber) {
                if (value instanceof BsonInt32 v) {
                    map.put(key, v.getValue());
                } else if (value instanceof BsonInt64 v) {
                    map.put(key, v.getValue());
                } else if (value instanceof BsonDouble v) {
                    map.put(key, v.getValue());
                } else if (value instanceof BsonDecimal128 v) {
                    map.put(key, v.getValue().bigDecimalValue());
                }
            } else if (value instanceof BsonBoolean v) {
                map.put(key, v.getValue());
            } else if (value instanceof BsonNull) {
                map.put(key, null);
            } else if (value instanceof BsonDateTime v) {
                map.put(key, v.getValue());
            } else if (value instanceof BsonTimestamp v) {
                map.put(key, v.getTime() * 1000L);
            } else if (value instanceof BsonObjectId v) {
                map.put(key, v.getValue().toHexString());
            } else if (value instanceof BsonBinary v) {
                // only support standard UUID
                map.put(key, v.asUuid().toString());
            } else if (value instanceof BsonArray array) {
                map.put(key, toList(array));
            } else if (value instanceof BsonDocument v) {
                map.put(key, toMap(v));
            }
            // ignore other BSON type (e.g. symbol)
        });
        return map;
    }

    /**
     * Convert the specified {@link BsonArray} to {@link List}.
     *
     * @param array the {@code BsonArray}
     * @return an {@code List<Object>}
     * @since 2.1
     */
    public static final List<Object> toList(BsonArray array) {
        var list = new ArrayList<>(array.size());
        for (var value : array) {
            if (value instanceof BsonString v) {
                list.add(v.getValue());
            } else if (value instanceof BsonNumber) {
                if (value instanceof BsonInt32 v) {
                    list.add(v.getValue());
                } else if (value instanceof BsonInt64 v) {
                    list.add(v.getValue());
                } else if (value instanceof BsonDouble v) {
                    list.add(v.getValue());
                } else if (value instanceof BsonDecimal128 v) {
                    list.add(v.getValue().bigDecimalValue());
                }
            } else if (value instanceof BsonBoolean v) {
                list.add(v.getValue());
            } else if (value instanceof BsonNull) {
                list.add(null);
            } else if (value instanceof BsonDateTime v) {
                list.add(v.getValue());
            } else if (value instanceof BsonTimestamp v) {
                list.add(v.getTime() * 1000L);
            } else if (value instanceof BsonObjectId v) {
                list.add(v.getValue().toHexString());
            } else if (value instanceof BsonBinary v) {
                // only support standard UUID
                list.add(v.asUuid().toString());
            } else if (value instanceof BsonArray v) {
                list.add(toList(v));
            } else if (value instanceof BsonDocument v) {
                list.add(toMap(v));
            }
            // ignore other BSON type (e.g. symbol)
        }
        return list;
    }

    /**
     * Convert the specified {@link JsonNode} to {@link BsonDocument}.
     *
     * @param jsonNode the source {@code JsonNode}
     * @return a {@code BsonDocument}
     * @since 2.1
     */
    public static final BsonDocument toBsonDocument(JsonNode jsonNode) {
        if (!jsonNode.isObject()) {
            throw new IllegalArgumentException("the source jsonNode expected <OBJECT> but was <" + jsonNode.getNodeType() + ">");
        }
        var document = new BsonDocument();
        for (var iter = jsonNode.fields(); iter.hasNext(); ) {
            var entry = iter.next();
            var key = entry.getKey();
            var value = entry.getValue();
            if (value == null || value.isNull()) {
                document.append(key, BsonNull.VALUE);
            } else if (value.isTextual()) {
                document.append(key, new BsonString(value.textValue()));
            } else if (value.isNumber()) {
                if (value.isInt()) {
                    document.append(key, new BsonInt32(value.intValue()));
                } else if (value.isLong()) {
                    document.append(key, new BsonInt64(value.longValue()));
                } else if (value.isDouble() || value.isBigDecimal()) {
                    document.append(key, new BsonDouble(value.doubleValue()));
                } else if (value.isBigInteger()) {
                    document.append(key, new BsonDecimal128(new Decimal128(value.decimalValue())));
                }
            } else if (value.isBoolean()) {
                document.append(key, BsonBoolean.valueOf(value.booleanValue()));
            } else if (value.isArray()) {
                document.append(key, toBsonArray(value));
            } else if (value.isObject()) {
                document.append(key, toBsonDocument(value));
            }
        }
        return document;
    }

    /**
     * Convert the specified {@link JsonNode} to {@link BsonArray}.
     *
     * @param jsonNode the source {@code JsonNode}
     * @return a {@code BsonArray}
     * @since 2.1
     */
    public static final BsonArray toBsonArray(JsonNode jsonNode) {
        if (!jsonNode.isArray()) {
            throw new IllegalArgumentException("the source jsonNode expected <ARRAY> but was <" + jsonNode.getNodeType() + ">");
        }
        var array = new BsonArray(jsonNode.size());
        for (var value : jsonNode) {
            if (value == null || value.isNull()) {
                array.add(BsonNull.VALUE);
            } else if (value.isTextual()) {
                array.add(new BsonString(value.textValue()));
            } else if (value.isNumber()) {
                if (value.isInt()) {
                    array.add(new BsonInt32(value.intValue()));
                } else if (value.isLong()) {
                    array.add(new BsonInt64(value.longValue()));
                } else if (value.isDouble() || value.isBigDecimal()) {
                    array.add(new BsonDouble(value.doubleValue()));
                } else if (value.isBigInteger()) {
                    array.add(new BsonDecimal128(new Decimal128(value.decimalValue())));
                }
            } else if (value.isBoolean()) {
                array.add(BsonBoolean.valueOf(value.booleanValue()));
            } else if (value.isArray()) {
                array.add(toBsonArray(value));
            } else if (value.isObject()) {
                array.add(toBsonDocument(value));
            }
        }
        return array;
    }

    /**
     * Gets the object value in a {@link JSONObject}.
     *
     * @param jsonObject the source {@link JSONObject}
     * @param key        the key
     * @return an {@code Optional<JSONObject>}
     * @since 2.2
     */
    public static final Optional<JSONObject> objectValue(JSONObject jsonObject, String key) {
        var value = jsonObject.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof JSONObject v) {
            return Optional.of(v);
        }
        throw new ClassCastException(String.format("The value is not a JSONObject (%s)", value.getClass().getSimpleName()));
    }

    /**
     * Gets the {@code boolean} value in a {@link JSONObject}.
     *
     * @param jsonObject the source {@link JSONObject}
     * @param key        the key
     * @return an {@code Optional<Boolean>}
     * @since 2.2
     */
    public static final Optional<Boolean> booleanValue(JSONObject jsonObject, String key) {
        var value = jsonObject.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof Boolean v) {
            return Optional.of(v);
        }
        throw new ClassCastException(String.format("The value is not a Boolean (%s)", value.getClass().getSimpleName()));
    }

    /**
     * Gets the array value in a {@link JSONObject}.
     *
     * @param jsonObject the source {@link JSONObject}
     * @param key        the key
     * @return an {@code Optional<JSONArray>}
     * @since 2.2
     */
    public static final Optional<JSONArray> arrayValue(JSONObject jsonObject, String key) {
        var value = jsonObject.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof JSONArray v) {
            return Optional.of(v);
        }
        throw new ClassCastException(String.format("The value is not a JSONArray (%s)", value.getClass().getSimpleName()));
    }

    /**
     * Gets the string value in a {@link JSONObject}.
     *
     * @param jsonObject the source {@link JSONObject}
     * @param key        the key
     * @return an {@code Optional<String>}
     * @since 2.2
     */
    public static final Optional<String> stringValue(JSONObject jsonObject, String key) {
        var value = jsonObject.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof String v) {
            return Optional.of(v);
        }
        throw new ClassCastException(String.format("The value is not a String (%s)", value.getClass().getSimpleName()));
    }

    /**
     * Gets the {@code int} value in a {@link JSONObject}.
     *
     * @param jsonObject the source {@link JSONObject}
     * @param key        the key
     * @return an {@code OptionalInt}
     * @since 2.2
     */
    public static final OptionalInt intValue(JSONObject jsonObject, String key) {
        var value = jsonObject.get(key);
        if (value == null) {
            return OptionalInt.empty();
        }
        if (value instanceof Number v) {
            return OptionalInt.of(v.intValue());
        }
        throw new ClassCastException(String.format("The value is not a Number (%s)", value.getClass().getSimpleName()));
    }

    /**
     * Gets the {@code long} value in a {@link JSONObject}.
     *
     * @param jsonObject the source {@link JSONObject}
     * @param key        the key
     * @return an {@code OptionalLong}
     * @since 2.2
     */
    public static final OptionalLong longValue(JSONObject jsonObject, String key) {
        var value = jsonObject.get(key);
        if (value == null) {
            return OptionalLong.empty();
        }
        if (value instanceof Number v) {
            return OptionalLong.of(v.longValue());
        }
        throw new ClassCastException(String.format("The value is not a Number (%s)", value.getClass().getSimpleName()));
    }

    /**
     * Gets the {@code double} value in a {@link JSONObject}.
     *
     * @param jsonObject the source {@link JSONObject}
     * @param key        the key
     * @return an {@code OptionalDouble}
     * @since 2.2
     */
    public static final OptionalDouble doubleValue(JSONObject jsonObject, String key) {
        var value = jsonObject.get(key);
        if (value == null) {
            return OptionalDouble.empty();
        }
        if (value instanceof Number v) {
            return OptionalDouble.of(v.doubleValue());
        }
        throw new ClassCastException(String.format("The value is not a Number (%s)", value.getClass().getSimpleName()));
    }

    /**
     * Gets the {@code LocalDateTime} value in a {@link JSONObject}.
     *
     * @param jsonObject the source {@link JSONObject}
     * @param key        the key
     * @return an {@code Optional<LocalDateTime>}
     * @since 2.2
     */
    public static final Optional<LocalDateTime> dateTimeValue(JSONObject jsonObject, String key) {
        var value = jsonObject.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof Number v) {
            return Optional.of(DateTimeUtil.ofEpochMilli(v.longValue()));
        }
        throw new ClassCastException(String.format("The value is not a Number (%s)", value.getClass().getSimpleName()));
    }

    /**
     * Gets the {@code ZonedDateTime} value in a {@link JSONObject}.
     *
     * @param zone       the zone to combine with, not null
     * @param jsonObject the source {@link JSONObject}
     * @param key        the key
     * @return an {@code Optional<ZonedDateTime>}
     * @since 2.2
     */
    public static final Optional<ZonedDateTime> dateTimeValue(ZoneId zone, JSONObject jsonObject, String key) {
        var value = jsonObject.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof Number v) {
            return Optional.of(DateTimeUtil.ofEpochMilli(v.longValue(), zone));
        }
        throw new ClassCastException(String.format("The value is not a Number (%s)", value.getClass().getSimpleName()));
    }

    /**
     * Gets the {@code ObjectId} value in an {@link JSONObject} with key
     * {@code "_id"}.
     *
     * @param jsonObject the source {@link JSONObject}
     * @return an {@code Optional<ObjectId>}
     * @since 2.2
     */
    public static final Optional<ObjectId> objectIdValue(JSONObject jsonObject) {
        return objectIdValue(jsonObject, "_id");
    }

    /**
     * Gets the {@code ObjectId} value in a {@link JSONObject}.
     *
     * @param jsonObject the source {@link JSONObject}
     * @param key        the key
     * @return an {@code Optional<ObjectId>}
     * @since 2.2
     */
    public static final Optional<ObjectId> objectIdValue(JSONObject jsonObject, String key) {
        var value = jsonObject.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof String v) {
            return Optional.of(new ObjectId(v));
        }
        throw new ClassCastException(String.format("The value is not a String (%s)", value.getClass().getSimpleName()));
    }

    /**
     * Gets the {@code Integer} value in a {@link JSONObject}.
     *
     * @param jsonObject the source {@link JSONObject}
     * @param key        the key
     * @return an {@code Optional<Integer>}
     * @since 2.2
     */
    public static final Optional<Integer> integerValue(JSONObject jsonObject, String key) {
        var value = jsonObject.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof Number v) {
            return Optional.of(v.intValue());
        }
        throw new ClassCastException(String.format("The value is not a Number (%s)", value.getClass().getSimpleName()));
    }

    /**
     * Gets the {@code Long} value in a {@link JSONObject}.
     *
     * @param jsonObject the source {@link JSONObject}
     * @param key        the key
     * @return an {@code Optional<Long>}
     * @since 2.2
     */
    public static final Optional<Long> boxedLongValue(JSONObject jsonObject, String key) {
        var value = jsonObject.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof Number v) {
            return Optional.of(v.longValue());
        }
        throw new ClassCastException(String.format("The value is not a Number (%s)", value.getClass().getSimpleName()));
    }

    /**
     * Gets the {@code Double} value in a {@link JSONObject}.
     *
     * @param jsonObject the source {@link JSONObject}
     * @param key        the key
     * @return an {@code Optional<Double>}
     * @since 2.2
     */
    public static final Optional<Double> boxedDoubleValue(JSONObject jsonObject, String key) {
        var value = jsonObject.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof Number v) {
            return Optional.of(v.doubleValue());
        }
        throw new ClassCastException(String.format("The value is not a Number (%s)", value.getClass().getSimpleName()));
    }

    /**
     * Gets the {@code Boolean} value in a {@link JSONObject}.
     *
     * @param jsonObject the source {@link JSONObject}
     * @param key        the key
     * @return an {@code Optional<Boolean>}
     * @since 2.2
     */
    public static final Optional<Boolean> boxedBooleanValue(JSONObject jsonObject, String key) {
        var value = jsonObject.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof Boolean v) {
            return Optional.of(v);
        }
        throw new ClassCastException(String.format("The value is not a Number (%s)", value.getClass().getSimpleName()));
    }

    /**
     * Gets the {@code int array} value in a {@link JSONObject}.
     *
     * @param jsonObject the source {@link JSONObject}
     * @param key        the key
     * @return an {@code Optional<int[]>}
     * @since 2.2
     */
    public static final Optional<int[]> intArrayValue(JSONObject jsonObject, String key) {
        var value = jsonObject.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof JSONArray v) {
            var values = new int[v.size()];
            for (var i = 0; i < values.length; i++) {
                values[i] = v.getIntValue(i);
            }
            return Optional.of(values);
        }
        throw new ClassCastException(String.format("The value is not a JSONArray (%s)", value.getClass().getSimpleName()));
    }

    /**
     * Gets the {@code long array} value in a {@link JSONObject}.
     *
     * @param jsonObject the source {@link JSONObject}
     * @param key        the key
     * @return an {@code Optional<long[]>}
     * @since 2.2
     */
    public static final Optional<long[]> longArrayValue(JSONObject jsonObject, String key) {
        var value = jsonObject.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof JSONArray v) {
            var values = new long[v.size()];
            for (var i = 0; i < values.length; i++) {
                values[i] = v.getLongValue(i);
            }
            return Optional.of(values);
        }
        throw new ClassCastException(String.format("The value is not a JSONArray (%s)", value.getClass().getSimpleName()));
    }

    /**
     * Gets the {@code double array} value in a {@link JSONObject}.
     *
     * @param jsonObject the source {@link JSONObject}
     * @param key        the key
     * @return an {@code Optional<double[]>}
     * @since 2.2
     */
    public static final Optional<double[]> doubleArrayValue(JSONObject jsonObject, String key) {
        var value = jsonObject.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof JSONArray v) {
            var values = new double[v.size()];
            for (var i = 0; i < values.length; i++) {
                values[i] = v.getDoubleValue(i);
            }
            return Optional.of(values);
        }
        throw new ClassCastException(String.format("The value is not a JSONArray (%s)", value.getClass().getSimpleName()));
    }

    /**
     * Gets the list value in a {@link JSONObject}.
     *
     * @param jsonObject the source {@link JSONObject}
     * @param key        the key
     * @param mapper     the mapper
     * @return an {@code Optional<double[]>}
     * @since 2.2
     */
    public static final <T> Optional<List<T>> listValue(JSONObject jsonObject, String key, Function<Object, T> mapper) {
        var value = jsonObject.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof JSONArray values) {
            var list = new ArrayList<T>(values.size());
            for (var v : values) {
                if (v == null) {
                    list.add(null);
                } else {
                    list.add(mapper.apply(v));
                }
            }
            return Optional.of(list);
        }
        throw new ClassCastException(String.format("The value is not a JSONArray (%s)", value.getClass().getSimpleName()));
    }

    /**
     * Convert the specified {@link JSONObject} to {@link BsonDocument}.
     *
     * @param jsonObject the source {@code JSONObject}
     * @return a {@code BsonDocument}
     * @since 2.2
     */
    public static final BsonDocument toBsonDocument(JSONObject jsonObject) {
        var document = new BsonDocument();
        for (var entry : jsonObject.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            if (value == null) {
                document.append(key, BsonNull.VALUE);
            } else if (value instanceof String v) {
                document.append(key, new BsonString(v));
            } else if (value instanceof Number) {
                if (value instanceof Integer v) {
                    document.append(key, new BsonInt32(v));
                } else if (value instanceof Long v) {
                    document.append(key, new BsonInt64(v));
                } else if (value instanceof Double v) {
                    document.append(key, new BsonDouble(v));
                } else if (value instanceof BigDecimal v) {
                    document.append(key, new BsonDouble(v.doubleValue()));
                } else if (value instanceof BigInteger v) {
                    document.append(key, new BsonDecimal128(new Decimal128(new BigDecimal(v))));
                }
            } else if (value instanceof Boolean v) {
                document.append(key, BsonBoolean.valueOf(v));
            } else if (value instanceof JSONArray v) {
                document.append(key, toBsonArray(v));
            } else if (value instanceof JSONObject v) {
                document.append(key, toBsonDocument(v));
            }
        }
        return document;
    }

    /**
     * Convert the specified {@link JSONArray} to {@link BsonArray}.
     *
     * @param jsonArray the source {@code JSONArray}
     * @return a {@code BsonArray}
     * @since 2.2
     */
    public static final BsonArray toBsonArray(JSONArray jsonArray) {
        var array = new BsonArray(jsonArray.size());
        for (var value : jsonArray) {
            if (value == null) {
                array.add(BsonNull.VALUE);
            } else if (value instanceof String v) {
                array.add(new BsonString(v));
            } else if (value instanceof Number) {
                if (value instanceof Integer v) {
                    array.add(new BsonInt32(v));
                } else if (value instanceof Long v) {
                    array.add(new BsonInt64(v));
                } else if (value instanceof Double v) {
                    array.add(new BsonDouble(v));
                } else if (value instanceof BigDecimal v) {
                    array.add(new BsonDouble(v.doubleValue()));
                } else if (value instanceof BigInteger v) {
                    array.add(new BsonDecimal128(new Decimal128(new BigDecimal(v))));
                }
            } else if (value instanceof Boolean v) {
                array.add(BsonBoolean.valueOf(v));
            } else if (value instanceof JSONArray v) {
                array.add(toBsonArray(v));
            } else if (value instanceof JSONObject v) {
                array.add(toBsonDocument(v));
            }
        }
        return array;
    }

    /**
     * Convert the specified {@link BsonDocument} to {@link JSONObject}.
     *
     * @param document the {@code BsonDocument}
     * @return an {@code JSONObject}
     * @since 2.2
     */
    public static final JSONObject toJSONObject(BsonDocument document) {
        var jsonObject = new JSONObject();
        for (var entry : document.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            if (value instanceof BsonString v) {
                jsonObject.put(key, v.getValue());
            } else if (value instanceof BsonNumber) {
                if (value instanceof BsonInt32 v) {
                    jsonObject.put(key, v.getValue());
                } else if (value instanceof BsonInt64 v) {
                    jsonObject.put(key, v.getValue());
                } else if (value instanceof BsonDouble v) {
                    jsonObject.put(key, v.getValue());
                } else if (value instanceof BsonDecimal128 v) {
                    jsonObject.put(key, v.getValue().bigDecimalValue());
                }
            } else if (value instanceof BsonBoolean v) {
                jsonObject.put(key, v.getValue());
            } else if (value instanceof BsonNull) {
                jsonObject.put(key, null);
            } else if (value instanceof BsonDateTime v) {
                jsonObject.put(key, v.getValue());
            } else if (value instanceof BsonTimestamp v) {
                jsonObject.put(key, v.getTime() * 1000L);
            } else if (value instanceof BsonObjectId v) {
                jsonObject.put(key, v.getValue().toHexString());
            } else if (value instanceof BsonBinary v) {
                // only support standard UUID
                jsonObject.put(key, v.asUuid().toString());
            } else if (value instanceof BsonArray array) {
                jsonObject.put(key, toJSONArray(array));
            } else if (value instanceof BsonDocument v) {
                jsonObject.put(key, toJSONObject(v));
            }
            // ignore other BSON type (e.g. symbol)
        }
        return jsonObject;
    }

    /**
     * Convert the specified {@link BsonArray} to {@link JSONArray}.
     *
     * @param array the {@code BsonArray}
     * @return an {@code JSONArray}
     * @since 2.2
     */
    public static final JSONArray toJSONArray(BsonArray array) {
        var jsonArray = new JSONArray(array.size());
        for (var value : array) {
            if (value instanceof BsonString v) {
                jsonArray.add(v.getValue());
            } else if (value instanceof BsonNumber) {
                if (value instanceof BsonInt32 v) {
                    jsonArray.add(v.getValue());
                } else if (value instanceof BsonInt64 v) {
                    jsonArray.add(v.getValue());
                } else if (value instanceof BsonDouble v) {
                    jsonArray.add(v.getValue());
                } else if (value instanceof BsonDecimal128 v) {
                    jsonArray.add(v.getValue().bigDecimalValue());
                }
            } else if (value instanceof BsonBoolean v) {
                jsonArray.add(v.getValue());
            } else if (value instanceof BsonNull) {
                jsonArray.add(null);
            } else if (value instanceof BsonDateTime v) {
                jsonArray.add(v.getValue());
            } else if (value instanceof BsonTimestamp v) {
                jsonArray.add(v.getTime() * 1000L);
            } else if (value instanceof BsonObjectId v) {
                jsonArray.add(v.getValue().toHexString());
            } else if (value instanceof BsonBinary v) {
                // only support standard UUID
                jsonArray.add(v.asUuid().toString());
            } else if (value instanceof BsonArray v) {
                jsonArray.add(toJSONArray(v));
            } else if (value instanceof BsonDocument v) {
                jsonArray.add(toJSONObject(v));
            }
            // ignore other BSON type (e.g. symbol)
        }
        return jsonArray;
    }

    private BsonUtil() {
    }

}
