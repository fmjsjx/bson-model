package com.github.fmjsjx.bson.model.core;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.bson.conversions.Bson;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mongodb.client.model.Updates;

/**
 * The abstract map implementation of BSON model.
 * 
 * @param <K>      the type of keys maintained by this map
 * @param <V>      the type of mapped values
 * @param <Parent> the type of the parent
 * @param <Self>   the type of the implementation class
 */
@JsonSerialize(using = MapModelSerializer.class)
public abstract class MapModel<K, V, Parent extends BsonModel, Self extends MapModel<K, V, Parent, ?>>
        extends AbstractContainerModel<Parent> {

    protected final Map<K, V> map;

    protected final Set<K> updatedKeys = new LinkedHashSet<>();
    protected final Set<K> removedKeys = new LinkedHashSet<>();

    protected final Function<String, K> keyParser;

    /**
     * Constructs a new {@link MapModel} using {@link LinkedHashMap}.
     * 
     * @param parent    the parent model
     * @param name      the field name of this map in document
     * @param keyParser the parser parses keys
     */
    protected MapModel(Parent parent, String name, Function<String, K> keyParser) {
        this(parent, name, keyParser, LinkedHashMap::new);
    }

    /**
     * Constructs a new {@link MapModel} by the specified factory.
     * 
     * @param parent     the parent model
     * @param name       the field name of this map in document
     * @param keyParser  the parser parses keys
     * @param mapFactory the factory to create {@link Map}s
     */
    protected MapModel(Parent parent, String name, Function<String, K> keyParser, Supplier<Map<K, V>> mapFactory) {
        this(parent, name, keyParser, mapFactory.get());
    }

    /**
     * Constructs a new {@link MapModel} with the specified map given.
     * 
     * @param parent    the parent model
     * @param name      the field name of this map in document
     * @param keyParser the parser parses keys
     * @param map       a {@link Map}
     */
    protected MapModel(Parent parent, String name, Function<String, K> keyParser, Map<K, V> map) {
        super(parent, name);
        this.keyParser = keyParser;
        this.map = map;
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
    public boolean updated() {
        return updatedKeys.size() > 0 || deletedSize() > 0;
    }

    /**
     * Returns the size of removed entries.
     * 
     * @return the size of removed entries
     */
    @Override
    public int deletedSize() {
        return removedKeys.size();
    }

    /**
     * Returns the value to which the specified key is mapped.
     * 
     * @param key the key
     * @return an {@code Optional<V>}
     */
    public Optional<V> get(K key) {
        return Optional.ofNullable(map.get(key));
    }

    /**
     * Associates the specified value with the specified key in this map.
     * 
     * @param key   the key
     * @param value the value
     * @return the previous value associated with the key
     */
    public abstract Optional<V> put(K key, V value);

    /**
     * Copies all of the mappings from the specified map to this map.
     * 
     * @param map mappings to be stored in this map
     * @return this map
     * 
     * @since 1.2
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
     *         specified key
     * @throws NullPointerException   if the specified key is {@code null} and this
     *                                map does not support null keys, or the
     *                                mappingFunction is {@code null}
     * @throws NoSuchElementException if the computed value is {@code null}
     * @since 1.2
     */
    public V putIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) throws NoSuchElementException {
        Objects.requireNonNull(mappingFunction);
        var v = get(key);
        if (v.isEmpty()) {
            var value = mappingFunction.apply(key);
            if (value == null) {
                throw new NoSuchElementException("the value computed by the mappingFunction must not be null");
            }
            put(key, value);
            return value;
        }
        return v.get();
    }

    /**
     * If the specified key is not already associated with a value (or is mapped to
     * {@code null}), attempts to compute its value using the given mapping function
     * and enters it into this map unless {@code null}.
     * 
     * @param key             key with which the specified value is to be associated
     * @param mappingFunction the mapping function to compute a value
     * @return the current (existing or computed) value associated with the
     *         specified key, or {@code null} if the computed value is {@code null}
     * @throws NullPointerException if the specified key is {@code null} and this
     *                              map does not support null keys, or the
     *                              mappingFunction is {@code null}
     * @since 1.2
     */
    public Optional<V> computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        var v = get(key);
        if (v.isEmpty()) {
            var value = mappingFunction.apply(key);
            if (value != null) {
                put(key, value);
                return Optional.of(value);
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
    public abstract Optional<V> remove(K key);

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
     * Removes all of the mappings from this map.
     * 
     * @return this map
     */
    public abstract Self clear();

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
    public boolean empty() {
        return map.isEmpty();
    }

    /**
     * Returns a sequential {@code Stream} with the the keys contained in this map.
     * 
     * @return a sequential {@code Stream} with the the keys contained in this map
     */
    public Stream<K> keys() {
        return map.keySet().stream();
    }

    /**
     * Returns a sequential {@code Stream} with the the values contained in this
     * map.
     * 
     * @return a sequential {@code Stream} with the the values contained in this map
     */
    public Stream<V> values() {
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
     * Returns a sequential {@code Stream} with the mappings contained in this map.
     * 
     * @return a sequential {@code Stream} with the mappings contained in this map
     * 
     * @since 1.2
     */
    public Stream<Entry<K, V>> entries() {
        return map.entrySet().stream();
    }

    /**
     * Returns if this map contains a mapping for the specified key or not.
     * 
     * @param key the key
     * @return {@code true} if this map contains a mapping for the specified key,
     *         {@code false} otherwise
     */
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    @Override
    protected void resetStates() {
        updatedKeys.clear();
        removedKeys.clear();
    }

    @Override
    public int appendUpdates(List<Bson> updates) {
        var updatedKeys = this.updatedKeys;
        var removedKeys = this.removedKeys;
        for (var key : updatedKeys) {
            var value = map.get(key);
            appendUpdates(updates, key, value);
        }
        for (var key : removedKeys) {
            updates.add(Updates.unset(xpath().resolve(key.toString()).value()));
        }
        return updatedKeys.size() + removedKeys.size();
    }

    /**
     * Appends the updates of specified value into the given list.
     * 
     * @param updates updates the list of updates
     * @param key     the key
     * @param value   the value
     */
    protected abstract void appendUpdates(List<Bson> updates, K key, V value);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + map.toString() + ")";
    }

    @Override
    public abstract Map<K, ?> toData();

}
