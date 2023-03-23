package com.github.fmjsjx.bson.model2.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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
        var doc = new BsonDocument(Math.max(8, map.size() << 1));
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
    public Object toData() {
        var map = this.map;
        if (map.isEmpty()) {
            return Map.of();
        }
        var data = new LinkedHashMap<>(Math.max(8, map.size() << 1));
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
    public void load(BsonDocument src) {
        clean();
        var map = this.map;
        var valueType = this.valueType;
        for (var e : src.entrySet()) {
            var value = valueType.parse(e.getValue());
            if (value != null) {
                map.put(parseKey(e.getKey()), value);
            }
        }
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
    public void load(JsonNode src) {
        clean();
        var map = this.map;
        var valueType = this.valueType;
        if (src.isObject()) {
            for (var iter = src.fields(); iter.hasNext(); ) {
                var entry = iter.next();
                var value = valueType.parse(entry.getValue());
                if (value != null) {
                    map.put(parseKey(entry.getKey()), value);
                }
            }
        } else {
            throw new IllegalArgumentException("src expected be an OBJECT but was " + src.getNodeType());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object toUpdateData() {
        if (isFullyUpdate()) {
            return toData();
        }
        var changedKeys = this.changedKeys;
        if (changedKeys.isEmpty()) {
            return Map.of();
        }
        var data = new LinkedHashMap<>(Math.max(8, changedKeys.size() << 1));
        var valueType = this.valueType;
        for (var key : changedKeys) {
            var value = get((K) key);
            if (value != null) {
                data.put(key, valueType.toData(value));
            }
        }
        return data;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object toDeletedData() {
        var changedKeys = this.changedKeys;
        if (changedKeys.isEmpty()) {
            return Map.of();
        }
        var data = new LinkedHashMap<>(Math.max(8, changedKeys.size() << 1));
        var valueType = this.valueType;
        for (var key : changedKeys) {
            var value = get((K) key);
            if (value == null) {
                data.put(key, 1);
            } else {
                data.put(key, valueType.toData(value));
            }
        }
        return data;
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
        return map.remove(key);
    }

    @Override
    public boolean remove(K key, V value) {
        return map.remove(key, value);
    }

    @Override
    protected void appendUpdates(List<Bson> updates, Object key, V value) {
        updates.add(Updates.set(path().resolve(key.toString()).value(), valueType.toBsonValue(value)));
    }

}
