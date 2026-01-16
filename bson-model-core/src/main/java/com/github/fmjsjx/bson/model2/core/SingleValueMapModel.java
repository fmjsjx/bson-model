package com.github.fmjsjx.bson.model2.core;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.model.Updates;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The single value implementation of {@link MapModel}.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @author MJ Fang
 * @see MapModel
 * @see DefaultMapModel
 * @since 2.0
 */
public final class SingleValueMapModel<K, V> extends MapModel<K, V, SingleValueMapModel<K, V>> {

    /**
     * Constructs a new {@link SingleValueMapModel} instance with integer keys
     * and the specified components.
     *
     * @param <V>       the type of the mapped values
     * @param valueType the value type
     * @return a new {@code SingleValueMapModel<Integer, T>} instance with
     * integer keys and the specified components
     */
    public static final <V> SingleValueMapModel<Integer, V> integerKeysMap(SingleValueType<V> valueType) {
        return new SingleValueMapModel<>(Integer::parseInt, valueType);
    }

    /**
     * Constructs a new {@link SingleValueMapModel} instance with long keys and
     * the specified components.
     *
     * @param <V>       the type of the mapped values
     * @param valueType the value type
     * @return a new {@code SingleValueMapModel<Long, T>} instance with long
     * keys and the specified components
     */
    public static final <V> SingleValueMapModel<Long, V> longKeysMap(SingleValueType<V> valueType) {
        return new SingleValueMapModel<>(Long::parseLong, valueType);
    }

    /**
     * Constructs a new {@link SingleValueMapModel} instance with string keys
     * and the specified components.
     *
     * @param <V>       the type of the mapped values
     * @param valueType the value type
     * @return a new {@code SingleValueMapModel<String, T>} instance with string
     * keys and the specified components
     */
    public static final <V> SingleValueMapModel<String, V> stringKeysMap(SingleValueType<V> valueType) {
        return new SingleValueMapModel<>(Function.identity(), valueType);
    }

    private final SingleValueType<V> valueType;

    /**
     * Constructs a new {@link SingleValueMapModel} instance with the specified components.
     *
     * @param keyParser the parser to parse keys
     * @param valueType the value type
     */
    public SingleValueMapModel(Function<String, K> keyParser, SingleValueType<V> valueType) {
        super(keyParser);
        this.valueType = valueType;
    }

    @Override
    protected void resetChildren() {
    }

    @Override
    protected int deletedSize() {
        var changedKeys = this.changedKeys;
        if (changedKeys.isEmpty()) {
            return 0;
        }
        var map = this.map;
        var n = 0;
        for (var key : changedKeys) {
            if (!map.containsKey(key)) {
                n++;
            }
        }
        return n;
    }

    @Override
    protected void clearMap() {
        map.clear();
    }

    @Override
    public BsonDocument toBson() {
        var map = this.map;
        var doc = new BsonDocument(Math.max(8, map.size()));
        var valueType = this.valueType;
        for (var e : map.entrySet()) {
            var v = e.getValue();
            if (v != null) {
                doc.append(e.getKey().toString(), valueType.toBsonValue(v));
            }
        }
        return doc;
    }

    @Override
    public Map<Object, Object> toData() {
        var map = this.map;
        if (map.isEmpty()) {
            return Map.of();
        }
        var data = new LinkedHashMap<>(Math.max(8, map.size()));
        var valueType = this.valueType;
        for (var e : map.entrySet()) {
            var v = e.getValue();
            if (v != null) {
                data.put(e.getKey(), valueType.toData(v));
            }
        }
        return data;
    }

    @Override
    public SingleValueMapModel<K, V> load(BsonDocument src) {
        clean();
        var map = this.map;
        var valueType = this.valueType;
        for (var e : src.entrySet()) {
            var value = valueType.parse(e.getValue());
            if (value != null) {
                map.put(parseKey(e.getKey()), value);
            }
        }
        return this;
    }

    @Override
    public JsonNode toJsonNode() {
        var map = this.map;
        var jsonNode = JsonNodeFactory.instance.objectNode();
        if (!map.isEmpty()) {
            var valueType = this.valueType;
            for (var e : map.entrySet()) {
                var v = e.getValue();
                if (v != null) {
                    jsonNode.set(e.getKey().toString(), valueType.toJsonNode(v));
                }
            }
        }
        return jsonNode;
    }

    @Override
    protected void loadObjectNode(ObjectNode src) {
        clean();
        var map = this.map;
        var valueType = this.valueType;
        for (var entry : src.properties()) {
            var value = valueType.parse(entry.getValue());
            if (value != null) {
                map.put(parseKey(entry.getKey()), value);
            }
        }
    }

    @Override
    public JSONObject toFastjson2Node() {
        var map = this.map;
        var valueType = this.valueType;
        var jsonObject = new JSONObject();
        if (!map.isEmpty()) {
            for (var e: map.entrySet()) {
                jsonObject.put(e.getKey().toString(), valueType.toData(e.getValue()));
            }
        }
        return jsonObject;
    }

    @Override
    protected void loadJSONObject(JSONObject src) {
        clean();
        var map = this.map;
        var valueType = this.valueType;
        for (var e: src.entrySet()) {
            var value = valueType.parseData(e.getValue());
            if (value != null) {
                map.put(parseKey(e.getKey()), value);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Object, Object> toSubUpdateData() {
        var changedKeys = this.changedKeys;
        if (changedKeys.isEmpty()) {
            return null;
        }
        var data = new LinkedHashMap<>(Math.max(8, changedKeys.size() << 1));
        var valueType = this.valueType;
        for (var key : changedKeys) {
            var value = get((K) key);
            if (value != null) {
                data.put(key, valueType.toData(value));
            }
        }
        return data.isEmpty() ? null : data;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Object, Object> toDeletedData() {
        var changedKeys = this.changedKeys;
        if (changedKeys.isEmpty()) {
            return Map.of();
        }
        var data = new LinkedHashMap<>(Math.max(8, changedKeys.size() << 1));
        for (var key : changedKeys) {
            var value = get((K) key);
            if (value == null) {
                data.put(key, 1);
            }
        }
        return data.isEmpty() ? null : data;
    }

    @Override
    public V put(K key, V value) {
        if (value == null) {
            return remove(key);
        }
        var map = this.map;
        var original = map.put(key, value);
        triggerChanged(key);
        return original;
    }

    @Override
    public V remove(K key) {
        var original = map.remove(key);
        triggerChanged(key);
        return original;
    }

    @Override
    public boolean remove(K key, V value) {
        var removed = map.remove(key, value);
        if (removed) {
            triggerChanged(key);
        }
        return removed;
    }

    @Override
    protected void appendUpdates(List<Bson> updates, Object key, V value) {
        updates.add(Updates.set(path().resolve(key.toString()).value(), valueType.toBsonValue(value)));
    }

    @Override
    public SingleValueMapModel<K, V> deepCopy() {
        var copy = new SingleValueMapModel<>(keyParser, valueType);
        deepCopyTo(copy);
        return copy;
    }

    @Override
    protected void deepCopyFrom(SingleValueMapModel<K, V> src) {
        map.putAll(src.map);
    }

}
