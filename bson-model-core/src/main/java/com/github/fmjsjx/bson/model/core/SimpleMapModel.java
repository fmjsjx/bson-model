package com.github.fmjsjx.bson.model.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.mongodb.client.model.Updates;

/**
 * The simple implementation of map model.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @param <P> the type of the parent
 */
public final class SimpleMapModel<K, V, P extends BsonModel> extends MapModel<K, V, P, SimpleMapModel<K, V, P>> {

    private static final Logger logger = LoggerFactory.getLogger(SimpleMapModel.class);

    /**
     * Constructs a new {@link SimpleMapModel} instance with integer keys and the
     * specified components.
     * 
     * @param <T>       the type of mapped values
     * @param <U>       the type of the parent
     * @param parent    the parent model
     * @param name      the field name of this map in document
     * @param valueType the value type
     * @return a new {@code SimpleMapModel<Integer, T>} instance with integer keys
     *         and the specified components
     */
    public static final <T, U extends BsonModel> SimpleMapModel<Integer, T, U> integerKeys(U parent, String name,
            SimpleValueType<T> valueType) {
        return new SimpleMapModel<>(parent, name, Integer::parseInt, valueType);
    }

    /**
     * Constructs a new {@link SimpleMapModel} instance with long keys and the
     * specified components.
     * 
     * @param <T>       the type of mapped values
     * @param <U>       the type of the parent
     * @param parent    the parent model
     * @param name      the field name of this map in document
     * @param valueType the value type
     * @return a new {@code SimpleMapModel<Long, T>} instance with integer keys and
     *         the specified components
     */
    public static final <T, U extends BsonModel> SimpleMapModel<Long, T, U> longKeys(U parent, String name,
            SimpleValueType<T> valueType) {
        return new SimpleMapModel<>(parent, name, Long::parseLong, valueType);
    }

    /**
     * Constructs a new {@link SimpleMapModel} instance with string keys and the
     * specified components.
     * 
     * @param <T>       the type of mapped values
     * @param <U>       the type of the parent
     * @param parent    the parent model
     * @param name      the field name of this map in document
     * @param valueType the value type
     * @return a new {@code SimpleMapModel<String, T>} instance with integer keys
     *         and the specified components
     */
    public static final <T, U extends BsonModel> SimpleMapModel<String, T, U> stringKeys(U parent, String name,
            SimpleValueType<T> valueType) {
        return new SimpleMapModel<>(parent, name, Function.identity(), valueType);
    }

    private final SimpleValueType<V> valueType;

    /**
     * Constructs a new {@link SimpleMapModel} instance with the specified
     * components.
     * 
     * @param parent    the parent model
     * @param name      the field name of this map in document
     * @param keyParser the parser to parse keys
     * @param valueType the value type
     */
    public SimpleMapModel(P parent, String name, Function<String, K> keyParser, SimpleValueType<V> valueType) {
        super(parent, name, keyParser);
        this.valueType = valueType;
    }

    @Override
    public BsonDocument toBson() {
        var bson = new BsonDocument();
        var valueType = this.valueType;
        map.forEach((k, v) -> bson.append(k.toString(), valueType.toBson(v)));
        return bson;
    }

    @Override
    public Document toDocument() {
        var doc = new Document();
        var valueType = this.valueType;
        map.forEach((k, v) -> doc.append(k.toString(), valueType.toStorage(v)));
        return doc;
    }

    @Override
    public void load(BsonDocument src) {
        map.clear();
        src.forEach((k, v) -> {
            try {
                map.put(parseKey(k), valueType.parse(v));
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Loading data failed on {}: {}", xpath().resolve(k), v);
                }
                // skip unsupported type values
            }
        });
    }

    @Override
    public void load(Document src) {
        map.clear();
        src.forEach((k, v) -> {
            try {
                map.put(parseKey(k), valueType.cast(v));
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Loading data failed on {}: {}", xpath().resolve(k), v);
                }
                // skip unsupported type values
            }
        });
    }

    @Override
    public void load(Any src) {
        map.clear();
        if (src.valueType() == ValueType.OBJECT) {
            src.asMap().forEach((k, v) -> {
                try {
                    map.put(parseKey(k), valueType.parse(v));
                } catch (Exception e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Loading data failed on {}: {}", xpath().resolve(k), v);
                    }
                    // skip unsupported type values
                }
            });
        }
    }

    @Override
    public void load(JsonNode src) {
        map.clear();
        if (src.isObject()) {
            for (var iter = src.fields(); iter.hasNext();) {
                var entry = iter.next();
                var k = entry.getKey();
                var v = entry.getValue();
                try {
                    map.put(parseKey(k), valueType.parse(v));
                } catch (Exception e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Loading data failed on {}: {}", xpath().resolve(k), v);
                    }
                    // skip unsupported type values
                }
            }
        }
    }

    @Override
    protected void appendUpdates(List<Bson> updates, K key, V value) {
        updates.add(Updates.set(xpath().resolve(key.toString()).value(), valueType.toStorage(value)));
    }

    @Override
    public Optional<V> put(K key, V value) {
        if (value == null) {
            return remove(key);
        }
        var old = map.put(key, value);
        if (old != null) {
            if (old == value) {
                return Optional.of(value);
            }
        }
        removedKeys.remove(key);
        updatedKeys.add(key);
        return Optional.ofNullable(old);
    }

    @Override
    public Optional<V> remove(K key) {
        var value = map.remove(key);
        if (value != null) {
            updatedKeys.remove(key);
            removedKeys.add(key);
            return Optional.of(value);
        }
        return Optional.empty();
    }

    @Override
    public boolean remove(K key, V value) {
        if (map.remove(key, value)) {
            updatedKeys.remove(key);
            removedKeys.add(key);
            return true;
        }
        return false;
    }

    @Override
    public SimpleMapModel<K, V, P> clear() {
        updatedKeys.clear();
        removedKeys.addAll(map.keySet());
        map.clear();
        return this;
    }

    @Override
    protected void resetChildren() {
        // skip
    }

    @Override
    public Object toUpdate() {
        var updatedKeys = this.updatedKeys;
        if (updatedKeys.isEmpty()) {
            return Map.of();
        }
        var update = new LinkedHashMap<Object, Object>();
        for (var key : updatedKeys) {
            var value = map.get(key);
            update.put(key, value);
        }
        return update;
    }

    @Override
    public Map<Object, Object> toDelete() {
        var removedKeys = this.removedKeys;
        if (removedKeys.isEmpty()) {
            return Map.of();
        }
        var delete = new LinkedHashMap<Object, Object>();
        for (var key : removedKeys) {
            delete.put(key, 1);
        }
        return delete;
    }

    @Override
    public Map<K, ?> toData() {
        var map = super.map;
        if (map.isEmpty()) {
            return new LinkedHashMap<>();
        }
        var data = new LinkedHashMap<K, Object>(Math.max(8, map.size() << 1));
        var valueType = this.valueType;
        map.forEach((k, v) -> data.put(k, valueType.toData(v)));
        return data;
    }

}
