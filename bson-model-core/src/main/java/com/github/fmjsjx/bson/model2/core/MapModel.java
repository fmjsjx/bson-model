package com.github.fmjsjx.bson.model2.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.model.Updates;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * The abstract map implementation of {@link AbstractContainerModel}.
 *
 * @param <K>    the type of keys maintained by this map
 * @param <V>    the type of mapped values
 * @param <Self> the type of the implementation class
 * @author MJ Fang
 * @see DefaultMapModel
 * @see SingleValueMapModel
 * @since 2.0
 */
public abstract class MapModel<K, V, Self extends MapModel<K, V, Self>>
        extends AbstractContainerModel<BsonDocument, Self> {

    protected final Map<K, V> map;
    protected final Function<String, K> keyParser;
    protected final Set<Object> changedKeys = new LinkedHashSet<>();

    /**
     * Constructs a new {@link MapModel} using {@link LinkedHashMap}.
     *
     * @param keyParser the parser parses keys
     */
    protected MapModel(Function<String, K> keyParser) {
        this(keyParser, LinkedHashMap::new);
    }

    /**
     * Constructs a new {@link MapModel} with the specified map given.
     *
     * @param map       a {@link Map}
     * @param keyParser the parser parses keys
     */
    protected MapModel(Map<K, V> map, Function<String, K> keyParser) {
        this.map = map;
        this.keyParser = keyParser;
    }

    /**
     * Constructs a new {@link MapModel} with the specified map given.
     *
     * @param keyParser  the parser parses keys
     * @param mapFactory the factory to create {@link Map}s
     */
    protected MapModel(Function<String, K> keyParser, Supplier<Map<K, V>> mapFactory) {
        this(mapFactory.get(), keyParser);
    }

    @Override
    public void load(JsonNode src) {
        if (!src.isObject()) {
            throw new IllegalArgumentException("src expected be an OBJECT but was " + src.getNodeType());
        }
        loadObjectNode((ObjectNode) src);
    }

    /**
     * Load data from the source data {@link ObjectNode}.
     *
     * @param src the source data {@code ObjectNode}
     */
    protected abstract void loadObjectNode(ObjectNode src);

    /**
     * Parse the specified key with the {@code keyParser}.
     *
     * @param key the key to be parsed
     * @return the parsed key
     */
    protected K parseKey(String key) {
        return keyParser.apply(key);
    }

    @Override
    public boolean anyChanged() {
        if (isFullyUpdate()) {
            return true;
        }
        var changedKeys = this.changedKeys;
        if (changedKeys.isEmpty()) {
            return false;
        }
        var map = this.map;
        for (var key : changedKeys) {
            if (map.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean anyDeleted() {
        var changedKeys = this.changedKeys;
        if (changedKeys.isEmpty()) {
            return false;
        }
        var map = this.map;
        for (var key : changedKeys) {
            if (!map.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the value to which the specified key is mapped.
     *
     * @param key the key
     * @return an {@code Optional<V>}
     */
    public Optional<V> take(K key) {
        return Optional.ofNullable(get(key));
    }

    /**
     * Returns the value to which the specified key is mapped.
     *
     * @param key the key
     * @return the value, or {@code null} if absent
     */
    public V get(K key) {
        return map.get(key);
    }

    /**
     * Associates the specified value with the specified key in this map.
     *
     * @param key   the key
     * @param value the value
     * @return the previous value associated with the key
     */
    public abstract V put(K key, V value);

    /**
     * Associates the specified value with the specified key in this map.
     *
     * @param key   the key
     * @param value the value
     * @return the previous value associated with the key
     */
    public Optional<V> replace(K key, V value) {
        return Optional.ofNullable(put(key, value));
    }

    /**
     * Copies all the mappings from the specified map to this map.
     *
     * @param map mappings to be stored in this map
     * @return this map
     */
    @SuppressWarnings("unchecked")
    public Self putAll(Map<K, V> map) {
        map.forEach(this::put);
        return (Self) this;
    }


    /**
     * If the specified key is not already associated with a value, attempts to
     * compute its value using the given mapping function and enters it into this
     * map.
     *
     * @param key             key with which the specified value is to be associated
     * @param mappingFunction the mapping function to compute a value (must not be
     *                        {@code null})
     * @return the current (existing or computed) value associated with the
     * specified key
     * @throws NullPointerException   if the specified key is {@code null} and this
     *                                map does not support null keys, or the
     *                                mappingFunction is {@code null}
     * @throws NoSuchElementException if the computed value is {@code null}
     */
    public V putIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) throws NoSuchElementException {
        Objects.requireNonNull(mappingFunction);
        var v = get(key);
        if (v == null) {
            var value = mappingFunction.apply(key);
            if (value == null) {
                throw new NoSuchElementException("the value computed by the mappingFunction must not be null");
            }
            put(key, value);
            return value;
        }
        return v;
    }

    /**
     * If the specified key is not already associated with a value (or is mapped to
     * {@code null}), attempts to compute its value using the given mapping function
     * and enters it into this map unless {@code null}.
     *
     * @param key             key with which the specified value is to be associated
     * @param mappingFunction the mapping function to compute a value
     * @return the current (existing or computed) value associated with the
     * specified key, or {@code null} if the computed value is {@code null}
     * @throws NullPointerException if the specified key is {@code null} and this
     *                              map does not support null keys, or the
     *                              mappingFunction is {@code null}
     */
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        var v = get(key);
        if (v == null) {
            var value = mappingFunction.apply(key);
            if (value != null) {
                put(key, value);
                return value;
            }
        }
        return v;
    }

    /**
     * Removes the mapping for a key from this map if it is present.
     *
     * @param key the key
     * @return the previous value associated with the key
     */
    public Optional<V> delete(K key) {
        return Optional.ofNullable(remove(key));
    }

    /**
     * Removes the mapping for a key from this map if it is present.
     *
     * @param key the key
     * @return the previous value associated with the key
     */
    public abstract V remove(K key);

    protected final void triggerChanged(K key) {
        changedKeys.add(key);
        triggerChanged();
    }

    /**
     * Removes the entry for the specified key only if it is currently mapped to the
     * specified value.
     *
     * @param key   the key
     * @param value the value
     * @return {@code true} if the value was removed
     */
    public abstract boolean remove(K key, V value);

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return number of key-value mappings in this map;
     */
    @Override
    public int size() {
        return map.size();
    }

    /**
     * Returns if this map is empty or not.
     *
     * @return {@code true} if this map is empty, {@code false} otherwise
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns an unmodifiable list containing all the keys contained in this map.
     *
     * @return the unmodifiable list containing all the keys contained in this map.
     */
    public List<K> keys() {
        return List.copyOf(map.keySet());
    }

    /**
     * Returns a sequential {@code Stream} with the keys contained in this map.
     *
     * @return a sequential {@code Stream} with the keys contained in this map
     */
    public Stream<K> keyStream() {
        return map.keySet().stream();
    }

    /**
     * Returns an unmodifiable list containing all the values contained in this map.
     *
     * @return the unmodifiable list containing all the values contained in this map.
     */
    public List<V> values() {
        return List.copyOf(map.values());
    }

    /**
     * Returns a sequential {@code Stream} with the values contained in this map.
     *
     * @return a sequential {@code Stream} with the values contained in this map
     */
    public Stream<V> valueStream() {
        return map.values().stream();
    }

    /**
     * Performs the given action for each entry in this map until all entries have
     * been processed or the action throws an exception.
     *
     * @param action the action to be performed for each entry
     */
    public void forEach(BiConsumer<K, V> action) {
        map.forEach(action);
    }

    /**
     * Returns an unmodifiable list containing all the mappings contained in this map.
     *
     * @return the unmodifiable list containing all the mappings contained in this map.
     */
    public List<Map.Entry<K, V>> entries() {
        return List.copyOf(map.entrySet());
    }

    /**
     * Returns a sequential {@code Stream} with the mappings contained in this map.
     *
     * @return a sequential {@code Stream} with the mappings contained in this map
     */
    public Stream<Map.Entry<K, V>> entryStream() {
        return map.entrySet().stream();
    }

    /**
     * Returns if this map contains a mapping for the specified key or not.
     *
     * @param key the key
     * @return {@code true} if this map contains a mapping for the specified key, {@code false} otherwise
     */
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    @Override
    protected void resetStates() {
        changedKeys.clear();
        super.resetStates();
    }

    @SuppressWarnings("unchecked")
    @Override
    public int appendUpdates(List<Bson> updates) {
        var original = updates.size();
        if (isFullyUpdate()) {
            updates.add(Updates.set(path().value(), toBson()));
        } else {
            for (var key : changedKeys) {
                var value = get((K) key);
                if (value == null) {
                    updates.add(Updates.unset(path().resolve(key.toString()).value()));
                } else {
                    appendUpdates(updates, key, value);
                }
            }
        }
        return updates.size() - original;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Self clear() {
        changedKeys.addAll(map.keySet());
        clearMap();
        triggerChanged();
        return (Self) this;
    }

    protected abstract void clearMap();

    @SuppressWarnings("unchecked")
    @Override
    public Self clean() {
        clearMap();
        resetStates();
        return (Self) this;
    }

    /**
     * Appends the updates of specified value into the given list.
     *
     * @param updates updates the list of updates
     * @param key     the key
     * @param value   the value
     */
    protected abstract void appendUpdates(List<Bson> updates, Object key, V value);

    @Override
    public String toString() {
        return getClass().getSimpleName() + map;
    }

}
