package com.github.fmjsjx.bson.model2.core;

import com.fasterxml.jackson.databind.JsonNode;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonType;

import java.util.function.Supplier;

/**
 * The default implementation of {@link ListModel}.
 *
 * @param <E> the type of the elements in this list
 * @author MJ Fang
 * @since 2.0
 */
public final class DefaultListModel<E extends AbstractBsonModel<BsonDocument, E>> extends ListModel<E, DefaultListModel<E>> {

    private final Supplier<E> valueFactory;

    /**
     * Constructs a new {@link DefaultListModel} instance with the specified components.
     *
     * @param valueFactory the factory creates value instances
     */
    public DefaultListModel(Supplier<E> valueFactory) {
        this.valueFactory = valueFactory;
    }

    @Override
    public void load(BsonArray src) {
        clean();
        for (var v : src) {
            if (v != null && v.getBsonType() != BsonType.NULL) {
                if (v instanceof BsonDocument doc) {
                    var value = valueFactory.get();
                    value.load(doc);
                } else {
                    throw new IllegalArgumentException("bson value expected be an DOCUMENT but was " + v.getBsonType());
                }
            } else {
                list.add(null);
            }
        }
    }

    @Override
    public void load(JsonNode src) {
        clean();
        var valueFactory = this.valueFactory;
        if (src.isArray()) {
            var len = src.size();
            for (var i = 0; i < len; i++) {
                var v = src.get(i);
                if (v != null && !v.isNull()) {
                    var value = valueFactory.get();
                    value.load(v);
                    list.add(value.parent(this).index(i));
                } else {
                    list.add(null);
                }
            }
        } else {
            throw new IllegalArgumentException("src expected be an ARRAY but was " + src.getNodeType());
        }
    }

}
