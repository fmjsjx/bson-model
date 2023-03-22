package com.github.fmjsjx.bson.model2.core;

import com.fasterxml.jackson.databind.JsonNode;
import org.bson.BsonDocument;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The default implementation of {@link MapModel}.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @author MJ Fang
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
    public void load(BsonDocument src) {
        clean();
        var valueFactory = this.valueFactory;
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
    }

    @Override
    public void load(JsonNode src) {
        clean();
        var valueFactory = this.valueFactory;
        if (src.isObject()) {
            for (var iter = src.fields(); iter.hasNext(); ) {
                var entry = iter.next();
                var key = parseKey(entry.getKey());
                var value = valueFactory.get();
                value.load(entry.getValue());
                map.put(key, value.parent(this).key(key));
            }
        } else {
            throw new IllegalArgumentException("src expected be an OBJECT but was " + src.getNodeType());
        }
    }

}
