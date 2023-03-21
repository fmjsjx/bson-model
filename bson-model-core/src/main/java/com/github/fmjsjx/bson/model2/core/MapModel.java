package com.github.fmjsjx.bson.model2.core;

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
 * @since 2.0
 */
public abstract class MapModel<K, V extends AbstractBsonModel<BsonDocument, V>, Self extends MapModel<K, V, Self>>
        extends AbstractContainerModel<BsonDocument, Self> {

    protected final Map<K, V> map;
    protected final Function<String, K> keyParser;
    protected final Set<K> changedKeys = new LinkedHashSet<>();
    protected final Set<K> deletedKeys = new LinkedHashSet<>();

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
        return isFullyUpdate() || changedKeys.size() > 0 || anyDeleted();
    }

    @Override
    protected int deletedSize() {
        var n = deletedKeys.size();
        for (var key : changedKeys) {
            var value = map.get(key);
            if (value != null && value.anyDeleted()) {
                n++;
            }
        }
        return n;
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
    public Optional<V> replace(K key, V value) {
        return Optional.ofNullable(put(key, value));
    }

    /**
     * Associates the specified value with the specified key in this map.
     *
     * @param key   the key
     * @param value the value
     * @return the previous value associated with the key
     */
    public V put(K key, V value) {
        if (value == null) {
            return remove(key);
        }
        var original = map.put(key, value);
        if (original != null) {
            if (original == value) {
                return value;
            }
            original.unbind();
        }
        value.key(key).parent(this);
        changedKeys.add(key);
        deletedKeys.remove(key);
        emitChanged();
        return original;
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
    public V remove(K key) {
        var value = map.remove(key);
        if (value != null) {
            unbind(key, value);
        }
        return value;
    }

    private void unbind(K key, V value) {
        value.unbind();
        changedKeys.remove(key);
        deletedKeys.add(key);
        emitChanged();
    }

    /**
     * Removes the entry for the specified key only if it is currently mapped to the
     * specified value.
     *
     * @param key   the key
     * @param value the value
     * @return {@code true} if the value was removed
     */
    public boolean remove(K key, V value) {
        if (map.remove(key, value)) {
            unbind(key, value);
            return true;
        }
        return false;
    }

    /**
     * Removes all the mappings from this map.
     *
     * @return this map
     */
    @SuppressWarnings("unchecked")
    @Override
    public Self clear() {
        changedKeys.clear();
        deletedKeys.addAll(map.keySet());
        clearMap();
        return (Self) this;
    }

    protected void clearMap() {
        var map = this.map;
        if (map.size() > 0) {
            map.values().forEach(V::unbind);
            map.clear();
        }
    }

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
        deletedKeys.clear();
        fullyUpdate(false);
    }

    @Override
    public BsonDocument toBson() {
        var bson = new BsonDocument(map.size());
        for (var e : map.entrySet()) {
            bson.append(e.getKey().toString(), e.getValue().toBson());
        }
        return bson;
    }

    @Override
    protected void resetChildren() {
        for (var key : changedKeys) {
            take(key).ifPresent(V::reset);
        }
    }

    @Override
    public Object toData() {
        var map = this.map;
        if (map.isEmpty()) {
            return Map.of();
        }
        var data = new LinkedHashMap<>();
        for (var e : map.entrySet()) {
            var value = e.getValue();
            if (value != null) {
                data.put(e.getKey(), value.toData());
            }
        }
        return data;
    }

    @Override
    public Object toUpdateData() {
        if (isFullyUpdate()) {
            var data = new LinkedHashMap<>();
            for (var e : map.entrySet()) {
                var value = e.getValue();
                if (value != null) {
                    data.put(e.getKey(), value.toUpdateData());
                }
            }
            return data;
        }
        var changedKeys = this.changedKeys;
        if (changedKeys.isEmpty()) {
            return Map.of();
        }
        var data = new LinkedHashMap<>();
        for (var key : changedKeys) {
            var value = get(key);
            if (value != null) {
                data.put(key, value.toUpdateData());
            }
        }
        return data;
    }

    @Override
    public Object toDeletedData() {
        var data = new LinkedHashMap<>();
        for (var key : changedKeys) {
            var value = get(key);
            if (value != null && value.anyDeleted()) {
                data.put(key, value.toDeletedData());
            }
        }
        return data;
    }

    @Override
    public int appendUpdates(List<Bson> updates) {
        var original = updates.size();
        if (isFullyUpdate()) {
            updates.add(Updates.set(path().value(), toBson()));
        } else {
            for (var key : changedKeys) {
                var value = get(key);
                if (value != null) {
                    value.appendUpdates(updates);
                }
            }
            for (var key : deletedKeys) {
                updates.add(Updates.unset(path().resolve(key.toString()).value()));
            }
        }
        return updates.size() - original;
    }

}