package com.github.fmjsjx.bson.model2.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.mongodb.client.model.Updates;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonNull;
import org.bson.BsonType;
import org.bson.conversions.Bson;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
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
    public BsonArray toBson() {
        var list = this.list;
        var bson = new BsonArray(list.size());
        if (!list.isEmpty()) {
            for (var v : list) {
                if (v == null) {
                    bson.add(BsonNull.VALUE);
                } else {
                    bson.add(v.toBson());
                }
            }
        }
        return bson;
    }

    @Override
    public DefaultListModel<E> load(BsonArray src) {
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
        return this;
    }

    @Override
    public JsonNode toJsonNode() {
        var list = this.list;
        var jsonNode = JsonNodeFactory.instance.arrayNode(list.size());
        if (!list.isEmpty()) {
            for (var v : list) {
                if (v == null) {
                    jsonNode.add(NullNode.getInstance());
                } else {
                    jsonNode.add(v.toJsonNode());
                }
            }
        }
        return jsonNode;
    }

    @Override
    protected void loadArrayNode(ArrayNode src) {
        clean();
        var valueFactory = this.valueFactory;
        var list = this.list;
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
    }

    @Override
    public E set(int index, E value) {
        if (value == null) {
            return remove(index);
        }
        value.mustUnbound();
        var list = this.list;
        var original = list.set(index, value.parent(this).index(index).fullyUpdate(true));
        if (original != null) {
            original.unbind();
        }
        triggerChanged(index);
        return original;
    }

    @Override
    public E remove(int index) {
        var list = this.list;
        var original = list.remove(index);
        if (original != null) {
            original.unbind();
            triggerChanged(index);
        }
        return original;
    }

    @Override
    public DefaultListModel<E> append(E value) {
        var list = this.list;
        var index = list.size();
        if (value == null) {
            list.add(null);
        } else {
            value.mustUnbound();
            list.add(value.parent(this).index(index).fullyUpdate(true));
        }
        triggerChanged(index);
        return this;
    }

    @Override
    protected Object toSubUpdateData() {
        var changedIndexes = this.changedIndexes;
        if (changedIndexes.isEmpty()) {
            return null;
        }
        var data = new LinkedHashMap<Integer, Object>(Math.max(8, changedIndexes.size() << 1));
        changedIndexes.intStream().mapToObj(list::get).filter(Objects::nonNull).forEach(v -> data.put(v.index, v.toUpdateData()));
        return data.isEmpty() ? null : data;
    }

    @Override
    public Object toData() {
        var list = this.list;
        if (list.isEmpty()) {
            return List.of();
        }
        return list.stream().map(e -> e == null ? null : e.toData()).toList();
    }

    @Override
    protected void resetChildren() {
        var list = this.list;
        if (isFullyUpdate()) {
            list.stream().filter(Objects::nonNull).forEach(E::reset);
        } else {
            var changedIndexes = this.changedIndexes;
            if (changedIndexes.size() > 0) {
                changedIndexes.intStream().mapToObj(list::get).filter(Objects::nonNull).forEach(E::reset);
            }
        }
    }

    @Override
    public int appendUpdates(List<Bson> updates) {
        var original = updates.size();
        if (isFullyUpdate()) {
            updates.add(Updates.set(path().value(), toBson()));
        } else {
            var changedIndexes = this.changedIndexes;
            if (changedIndexes.size() > 0) {
                changedIndexes.intStream().forEach(index -> {
                    var value = list.get(index);
                    if (value == null) {
                        updates.add(Updates.unset(path().resolve(index).value()));
                    } else {
                        value.appendUpdates(updates);
                    }
                });
            }
        }
        return updates.size() - original;
    }

    @Override
    protected void clearList() {
        var list = this.list;
        if (list.size() > 0) {
            for (var value : list) {
                if (value != null) {
                    value.unbind();
                }
            }
            list.clear();
        }
    }

    @Override
    public DefaultListModel<E> deepCopy() {
        var copy = new DefaultListModel<>(valueFactory);
        deepCopyTo(copy, false);
        return copy;
    }

    @Override
    protected void deepCopyFrom(DefaultListModel<E> src) {
        var list = this.list;
        for (var value : src.list) {
            if (value == null) {
                list.add(null);
            } else {
                list.add(value.deepCopy().parent(this).index(index));
            }
        }
    }

}
