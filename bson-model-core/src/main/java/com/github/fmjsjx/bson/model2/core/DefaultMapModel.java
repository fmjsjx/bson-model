package com.github.fmjsjx.bson.model2.core;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The default implementation of {@link MapModel}.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @author MJ Fang
 * @see MapModel
 * @see SingleValueMapModel
 * @since 2.0
 */
public final class DefaultMapModel<K, V extends AbstractBsonModel<BsonDocument, V>> extends MapModel<K, V, DefaultMapModel<K, V>> {

    /**
     * Constructs a new {@link DefaultMapModel} instance with integer keys and the specified components.
     *
     * @param valueFactory the factory creates value instances
     * @param <T>          the type of mapped values
     * @return a new {@code DefaultMapModel<Integer, T>} instance with integer keys and the specified components
     */
    public static final <T extends AbstractBsonModel<BsonDocument, T>> DefaultMapModel<Integer, T> integerKeysMap(Supplier<T> valueFactory) {
        return new DefaultMapModel<>(Integer::parseInt, valueFactory);
    }

    /**
     * Constructs a new {@link DefaultMapModel} instance with long keys and the specified components.
     *
     * @param valueFactory the factory creates value instances
     * @param <T>          the type of mapped values
     * @return a new {@code DefaultMapModel<Integer, T>} instance with long keys and the specified components
     */
    public static final <T extends AbstractBsonModel<BsonDocument, T>> DefaultMapModel<Long, T> longKeysMap(Supplier<T> valueFactory) {
        return new DefaultMapModel<>(Long::parseLong, valueFactory);
    }

    /**
     * Constructs a new {@link DefaultMapModel} instance with string keys and the specified components.
     *
     * @param valueFactory the factory creates value instances
     * @param <T>          the type of mapped values
     * @return a new {@code DefaultMapModel<Integer, T>} instance with string keys and the specified components
     */
    public static final <T extends AbstractBsonModel<BsonDocument, T>> DefaultMapModel<String, T> stringKeysMap(Supplier<T> valueFactory) {
        return new DefaultMapModel<>(Function.identity(), valueFactory);
    }

    private final Supplier<V> valueFactory;

    /**
     * Constructs a new {@link DefaultMapModel} instance with the specified components.
     *
     * @param keyParser    the parser to parse keys
     * @param valueFactory the factory creates value instances
     */
    public DefaultMapModel(Function<String, K> keyParser, Supplier<V> valueFactory) {
        super(keyParser);
        this.valueFactory = valueFactory;
    }

    @Override
    public BsonDocument toBson() {
        var map = this.map;
        var bson = new BsonDocument(Math.max(8, map.size()));
        if (!map.isEmpty()) {
            for (var e : map.entrySet()) {
                bson.append(e.getKey().toString(), e.getValue().toBson());
            }
        }
        return bson;
    }

    @Override
    public DefaultMapModel<K, V> load(BsonDocument src) {
        clean();
        var valueFactory = this.valueFactory;
        var map = this.map;
        for (var e : src.entrySet()) {
            var v = e.getValue();
            if (v instanceof BsonDocument doc) {
                var key = parseKey(e.getKey());
                var value = valueFactory.get();
                value.load(doc);
                map.put(key, value.parent(this).key(key));
            } else {
                throw new IllegalArgumentException("bson value expected be an DOCUMENT but was " + v.getBsonType());
            }
        }
        return this;
    }

    @Override
    public JsonNode toJsonNode() {
        var map = this.map;
        var jsonNode = JsonNodeFactory.instance.objectNode();
        if (!map.isEmpty()) {
            for (var e : map.entrySet()) {
                jsonNode.set(e.getKey().toString(), e.getValue().toJsonNode());
            }
        }
        return jsonNode;
    }

    @Override
    protected void loadObjectNode(ObjectNode src) {
        clean();
        var valueFactory = this.valueFactory;
        var map = this.map;
        for (var iter = src.fields(); iter.hasNext(); ) {
            var entry = iter.next();
            var key = parseKey(entry.getKey());
            var value = valueFactory.get();
            value.load(entry.getValue());
            map.put(key, value.parent(this).key(key));
        }
    }

    @Override
    public JSONObject toFastjson2Node() {
        var map = this.map;
        var jsonObject = new JSONObject();
        if (!map.isEmpty()) {
            for (var e : map.entrySet()) {
                jsonObject.put(e.getKey().toString(), e.getValue().toFastjson2Node());
            }
        }
        return jsonObject;
    }

    @Override
    protected void loadJSONObject(JSONObject src) {
        clean();
        var valueFactory = this.valueFactory;
        var map = this.map;
        for (var e : src.entrySet()) {
            var key = parseKey(e.getKey());
            var value = valueFactory.get();
            value.loadFastjson2Node(e.getValue());
            map.put(key, value.parent(this).key(key));
        }
    }

    @Override
    protected int deletedSize() {
        var map = this.map;
        var n = 0;
        for (var key : changedKeys) {
            var value = map.get(key);
            if (value == null || value.anyDeleted()) {
                n++;
            }
        }
        return n;
    }

    @Override
    public V put(K key, V value) {
        if (value == null) {
            return remove(key);
        }
        value.mustUnbound();
        var original = map.put(key, value.key(key).parent(this).fullyUpdate(true));
        if (original != null) {
            original.unbind();
        }
        triggerChanged(key);
        return original;
    }

    @Override
    public V remove(K key) {
        var value = map.remove(key);
        if (value != null) {
            value.unbind();
            triggerChanged(key);
        }
        return value;
    }

    public boolean remove(K key, V value) {
        if (map.remove(key, value)) {
            value.unbind();
            triggerChanged(key);
            return true;
        }
        return false;
    }

    @Override
    protected void clearMap() {
        var map = this.map;
        if (map.size() > 0) {
            map.values().forEach(V::unbind);
            map.clear();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void resetChildren() {
        for (var key : changedKeys) {
            take((K) key).ifPresent(V::reset);
        }
    }

    @Override
    public Map<Object, Object> toData() {
        var map = this.map;
        if (map.isEmpty()) {
            return Map.of();
        }
        var data = new LinkedHashMap<>(Math.max(8, map.size()));
        for (var e : map.entrySet()) {
            var value = e.getValue();
            if (value != null) {
                data.put(e.getKey(), value.toData());
            }
        }
        return data;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Object, Object> toSubUpdateData() {
        var changedKeys = this.changedKeys;
        if (changedKeys.isEmpty()) {
            return null;
        }
        var data = new LinkedHashMap<>(Math.max(8, changedKeys.size() << 1));
        for (var key : changedKeys) {
            var value = get((K) key);
            if (value != null) {
                data.put(key, value.toUpdateData());
            }
        }
        return data.isEmpty() ? null : data;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Object, Object> toDeletedData() {
        var changedKeys = this.changedKeys;
        if (changedKeys.isEmpty()) {
            return null;
        }
        var data = new LinkedHashMap<>(Math.max(8, changedKeys.size() << 1));
        for (var key : changedKeys) {
            var value = get((K) key);
            if (value == null) {
                data.put(key, 1);
            } else {
                var subData = value.toDeletedData();
                if (subData != null) {
                    data.put(key, subData);
                }
            }
        }
        return data.isEmpty() ? null : data;
    }

    @Override
    protected void appendUpdates(List<Bson> updates, Object key, V value) {
        value.appendUpdates(updates);
    }

    @Override
    public DefaultMapModel<K, V> deepCopy() {
        var copy = new DefaultMapModel<>(keyParser, valueFactory);
        deepCopyTo(copy, false);
        return copy;
    }

    @Override
    protected void deepCopyFrom(DefaultMapModel<K, V> src) {
        var map = this.map;
        for (var entry : src.map.entrySet()) {
            var key = entry.getKey();
            map.put(key, entry.getValue().deepCopy().parent(this).key(key));
        }
    }

}
