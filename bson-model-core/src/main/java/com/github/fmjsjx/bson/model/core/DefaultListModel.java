package com.github.fmjsjx.bson.model.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.bson.BsonArray;
import org.bson.BsonNull;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fmjsjx.libcommon.collection.IntHashSet;
import com.github.fmjsjx.libcommon.collection.IntSet;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.mongodb.client.model.Updates;

/**
 * The default implementation of list model.
 *
 * @param <E> the type of the elements in this list
 * @param <P> the type of the parent
 * 
 * @since 1.1
 */
public final class DefaultListModel<E extends DefaultListValueModel<E>, P extends BsonModel>
        extends ListModel<E, P, DefaultListModel<E, P>> {

    final IntSet updateIndexes = new IntHashSet();

    private final Supplier<E> valueFactory;
    private boolean fullyUpdate;

    /**
     * Constructs a new {@link DefaultListModel} instance with the specified
     * components.
     * <p>
     * The values will be {@code null}.
     * 
     * @param parent       the parent model
     * @param name         the field name of this list in document
     * @param valueFactory the factory creates value instances
     */
    public DefaultListModel(P parent, String name, Supplier<E> valueFactory) {
        super(parent, name);
        this.valueFactory = valueFactory;
    }

    /**
     * Constructs a new {@link DefaultListModel} instance with the specified
     * components.
     * <p>
     * The values will be initialized with specified size(all values will be fill as
     * {@code null}).
     * 
     * @param parent       the parent model
     * @param name         the field name of this list in document
     * @param valueFactory the factory creates value instances
     * @param size         the size of the list
     */
    public DefaultListModel(P parent, String name, Supplier<E> valueFactory, int size) {
        this(parent, name, valueFactory);
        var list = new ArrayList<E>(size);
        for (var i = 0; i < size; i++) {
            list.add(null);
        }
        super.list = list;
    }

    @Override
    public BsonValue toBson() {
        var list = this.list;
        if (list == null) {
            return BsonNull.VALUE;
        }
        var bson = new BsonArray();
        for (var e : list) {
            if (e == null) {
                bson.add(BsonNull.VALUE);
            } else {
                bson.add(e.toBson());
            }
        }
        return bson;
    }

    @Override
    public void load(Any src) {
        clear0();
        if (src.valueType() == ValueType.ARRAY) {
            var len = src.size();
            var list = new ArrayList<E>(len);
            for (int index = 0; index < len; index++) {
                var esrc = src.get(index);
                if (esrc.valueType() == ValueType.OBJECT) {
                    var value = generateValueModel(index);
                    value.load(esrc);
                    list.add(value);
                } else {
                    list.add(null);
                }
            }
            super.list = list;
        } else {
            list = null;
        }
    }

    private E generateValueModel(int index) {
        return valueFactory.get().parent(this).index(index);
    }

    @Override
    public DefaultListModel<E, P> clear() {
        fullyUpdate = true;
        updateIndexes.clear();
        clear0();
        return this;
    }

    /**
     * Clean this model. This method is very similar to {@link #clear()}. The
     * difference is the list value will be set {@code null}.
     * 
     * @return this model
     */
    public DefaultListModel<E, P> clean() {
        clear();
        this.list = null;
        return this;
    }

    private void clear0() {
        var list = super.list;
        if (list != null) {
            for (var value : list) {
                if (value != null) {
                    value.unbind();
                }
            }
            list.clear();
        }
    }

    @Override
    public void load(JsonNode src) {
        clear0();
        if (src.isArray()) {
            var len = src.size();
            var list = new ArrayList<E>(len);
            for (int index = 0; index < len; index++) {
                var esrc = src.get(index);
                if (esrc.isObject()) {
                    var value = generateValueModel(index);
                    value.load(esrc);
                    list.add(value);
                } else {
                    list.add(null);
                }
            }
            super.list = list;
        } else {
            list = null;
        }
    }

    @Override
    public int appendUpdates(List<Bson> updates) {
        if (fullyUpdate()) {
            if (nil()) {
                updates.add(Updates.unset(xpath().value()));
            } else {
                updates.add(Updates.set(xpath().value(), toBson()));
            }
            return 1;
        }
        var size = updateIndexes.size();
        if (size > 0) {
            updateIndexes.intStream().sorted().forEach(index -> {
                var value = list.get(index);
                if (value == null) {
                    updates.add(Updates.unset(xpath().resolve(index).value()));
                } else {
                    value.appendUpdates(updates);
                }
            });
        }
        return size;
    }

    @Override
    public boolean updated() {
        return fullyUpdate() || updateIndexes.size() > 0;
    }

    /**
     * Returns if this model will be fully updated or not.
     * 
     * @return {@code true} if this model will be fully updated, {@code false}
     *         otherwise
     */
    public boolean fullyUpdate() {
        return fullyUpdate;
    }

    @Override
    public Object toUpdate() {
        if (fullyUpdate()) {
            return this;
        }
        var updateIndexes = this.updateIndexes;
        if (updateIndexes.isEmpty()) {
            return Map.of();
        }
        var update = new LinkedHashMap<Object, Object>();
        updateIndexes.intStream().sorted().mapToObj(list::get).filter(Objects::nonNull).forEach(v -> {
            update.put(v.index(), v.toUpdate());
        });
        return update;
    }

    @Override
    public void load(BsonArray src) {
        clear0();
        var len = src.size();
        var list = new ArrayList<E>(len);
        for (int index = 0; index < len; index++) {
            var bson = src.get(index);
            if (bson != null && bson.isDocument()) {
                var value = generateValueModel(index);
                value.load(bson.asDocument());
                list.add(value);
            } else {
                list.add(null);
            }
        }
        this.list = list;
    }

    @Override
    public void load(List<Object> src) {
        clear0();
        var len = src.size();
        var list = new ArrayList<E>(len);
        for (int index = 0; index < len; index++) {
            var obj = src.get(index);
            if (obj != null && obj instanceof Document) {
                var value = generateValueModel(index);
                value.load((Document) obj);
                list.add(value);
            } else {
                list.add(null);
            }
        }
        this.list = list;
    }

    /**
     * Convert values of this model to {@link Document} type.
     * 
     * @return a {@code Document} list
     */
    public List<Document> toDocuments() {
        var list = this.list;
        if (list == null) {
            return null;
        }
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        var data = new ArrayList<Document>(list.size());
        for (var value : list) {
            if (value == null) {
                data.add(null);
            } else {
                data.add(value.toDocument());
            }
        }
        return data;
    }

    @Override
    public List<?> toData() {
        var list = this.list;
        if (list == null) {
            return null;
        }
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        var data = new ArrayList<>(list.size());
        for (var value : list) {
            if (value == null) {
                data.add(null);
            } else {
                data.add(value.toData());
            }
        }
        return data;
    }

    @Override
    protected void resetChildren() {
        var list = this.list;
        if (fullyUpdate()) {
            if (list != null) {
                list.stream().filter(Objects::nonNull).forEach(E::reset);
            }
        } else {
            var updateIndexes = this.updateIndexes;
            if (updateIndexes.size() > 0) {
                updateIndexes.intStream().sorted().mapToObj(list::get).filter(Objects::nonNull).forEach(E::reset);
            }
        }
    }

    @Override
    protected void resetStates() {
        fullyUpdate = false;
        var updateIndexes = this.updateIndexes;
        if (updateIndexes.size() > 0) {
            updateIndexes.clear();
        }
    }

    @Override
    public Map<Object, Object> toDelete() {
        var updateIndexes = this.updateIndexes;
        if (updateIndexes.isEmpty()) {
            return Map.of();
        }
        var delete = new LinkedHashMap<Object, Object>();
        updateIndexes.intStream().sorted().forEach(index -> {
            if (list.get(index) == null) {
                delete.put(index, 1);
            }
        });
        return delete;
    }

    @Override
    public int deletedSize() {
        var updateIndexes = this.updateIndexes;
        if (updateIndexes.isEmpty()) {
            return 0;
        }
        return updateIndexes.intStream().sorted().mapToObj(list::get).filter(Objects::isNull).mapToInt(e -> 1).sum();
    }

    @Override
    public Optional<List<E>> values() {
        var list = this.list;
        if (list == null) {
            return Optional.empty();
        }
        if (list.isEmpty()) {
            return Optional.of(List.of());
        }
        return Optional.of(Collections.unmodifiableList(list));
    }

    public Optional<E> value(int index) {
        var list = this.list;
        if (list == null) {
            throw new IllegalArgumentException("the values of this list model is null");
        }
        return Optional.ofNullable(list.get(index));
    }

    public DefaultListModel<E, P> values(List<E> values) {
        clear();
        var len = values.size();
        if (len == 0) {
            list = new ArrayList<>();
        } else {
            var list = new ArrayList<E>(len);
            for (int index = 0; index < len; index++) {
                var value = values.get(index);
                if (value.bound()) {
                    throw new IllegalArgumentException("some value has already been bound");
                }
                list.add(bindValue(index, value));
            }
            this.list = list;
        }
        return this;
    }

    /**
     * Replaces the element at the specified position in this list with the
     * specified element (optional operation).
     * 
     * @param index the index
     * @param value the element value
     * @return the element previously at the specified position
     */
    public Optional<E> value(int index, E value) {
        var list = this.list;
        if (list == null) {
            if (value != null) {
                fullyUpdate = true;
                this.list = list = new ArrayList<E>(index + 1);
                list.set(index, bindValue(index, value));
            }
            return Optional.empty();
        }
        if (value == null) {
            var old = setValue(index, null);
            return Optional.ofNullable(old);
        }
        if (value.bound()) {
            throw new IllegalArgumentException("the value has already been bound");
        }
        var old = setValue(index, bindValue(index, value));
        return Optional.ofNullable(old);
    }

    private E bindValue(int index, E value) {
        return value.parent(this).index(index);
    }

    private E setValue(int index, E value) {
        var old = list.set(index, value);
        if (old != null) {
            old.unbind();
        }
        markUpdated(index);
        return old;
    }

    private void markUpdated(int index) {
        if (!fullyUpdate()) {
            updateIndexes.add(index);
            emitUpdated();
        }
    }

    /**
     * Removes the element at the specified position in this list.
     * <p>
     * This method is equivalent to:
     * 
     * <pre>
     * {@code
     * return value(index, null);
     * }
     * </pre>
     * 
     * @param index the index
     * @return the element previously at the specified position
     */
    public Optional<E> remove(int index) {
        return value(index, null);
    }

    /**
     * Appends the specified element to the end of this list.
     * 
     * @param value the element value to be appended to this list
     * @return this model
     */
    public DefaultListModel<E, P> append(E value) {
        if (value == null) {
            var list = super.list;
            if (list == null) {
                super.list = list = new ArrayList<>();
                fullyUpdate = true;
            }
            var index = list.size();
            list.add(null);
            markUpdated(index);
        } else {
            if (value.bound()) {
                throw new IllegalArgumentException("the value has already been bound");
            }
            var list = super.list;
            if (list == null) {
                super.list = list = new ArrayList<>();
                fullyUpdate = true;
            }
            var index = list.size();
            list.add(bindValue(index, value).fullyUpdate(true));
            markUpdated(index);
        }
        return this;
    }

}
