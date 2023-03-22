package com.github.fmjsjx.bson.model2.core;

import com.github.fmjsjx.libcommon.collection.IntHashSet;
import com.github.fmjsjx.libcommon.collection.IntSet;
import com.mongodb.client.model.Updates;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonNull;
import org.bson.conversions.Bson;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * The abstract list implementation of {@link AbstractContainerModel}.
 *
 * @param <E>    the type of elements in this list
 * @param <Self> the type of the implementation class
 * @author MJ Fang
 * @see DefaultListModel
 * @since 2.x
 */
public abstract class ListModel<E extends AbstractBsonModel<BsonDocument, E>, Self extends ListModel<E, Self>>
        extends AbstractContainerModel<BsonArray, Self> {

    protected final List<E> list;
    protected final IntSet changedIndexes = new IntHashSet();

    /**
     * Constructs a new {@link ListModel} using {@link ArrayList}.
     */
    protected ListModel() {
        this(ArrayList::new);
    }

    /**
     * Constructs a new {@link ListModel} with the specified list.
     *
     * @param listFactory the factory to create {@link List}s
     */
    protected ListModel(Supplier<List<E>> listFactory) {
        this(listFactory.get());
    }

    /**
     * Constructs a new {@link ListModel} with the specified list.
     *
     * @param list a {@link List}
     */
    protected ListModel(List<E> list) {
        this.list = list;
    }

    /**
     * Returns the values of this model.
     *
     * @return the values of this model
     */
    public List<E> values() {
        return Collections.unmodifiableList(list);
    }

    /**
     * Returns a sequential {@code Stream} over the elements in this model.
     *
     * @return a sequential {@code Stream} over the elements in this model
     */
    public Stream<E> stream() {
        return list.stream();
    }

    /**
     * Returns the element at the specified position in this list
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException – if the index is out of range (index < 0 || index >= size())
     */
    public E get(int index) {
        return list.get(index);
    }

    /**
     * Returns the element at the specified position in this list
     *
     * @param index index of the element to return
     * @return an {@code Optional<E>}
     */
    public Optional<E> value(int index) {
        return Optional.ofNullable(get(index));
    }

    /**
     * Replaces the element at the specified position in this list with the
     * specified element (optional operation).
     *
     * @param index the index
     * @param value the element value
     * @return the element previously at the specified position
     */
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

    /**
     * Replaces the element at the specified position in this list with the
     * specified element (optional operation).
     *
     * @param index the index
     * @param value the element value
     * @return the element previously at the specified position
     */
    public Optional<E> replace(int index, E value) {
        return Optional.ofNullable(set(index, value));
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
    public E remove(int index) {
        var list = this.list;
        var original = list.remove(index);
        if (original != null) {
            original.unbind();
            triggerChanged(index);
        }
        return original;
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param value the element value to be appended to this list
     * @return this model
     */
    @SuppressWarnings("unchecked")
    public Self append(E value) {
        var list = this.list;
        var index = list.size();
        if (value == null) {
            list.add(null);
        } else {
            value.mustUnbound();
            list.add(value.parent(this).index(index).fullyUpdate(true));
        }
        triggerChanged(index);
        return (Self) this;
    }

    protected final void triggerChanged(int index) {
        changedIndexes.add(index);
        triggerChanged();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean anyChanged() {
        return isFullyUpdate() || changedIndexes.size() > 0;
    }

    @Override
    protected int deletedSize() {
        var changedIndexes = this.changedIndexes;
        if (changedIndexes.isEmpty()) {
            return 0;
        }
        return (int) changedIndexes.intStream().mapToObj(list::get).filter(Objects::isNull).count();
    }

    @Override
    public Object toUpdateData() {
        if (isFullyUpdate()) {
            return toData();
        }
        var changedIndexes = this.changedIndexes;
        if (changedIndexes.isEmpty()) {
            return Map.of();
        }
        var data = new LinkedHashMap<Integer, Object>();
        changedIndexes.intStream().mapToObj(list::get).filter(Objects::nonNull).forEach(v -> data.put(v.index, v.toUpdateData()));
        return data;
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
    public Object toDeletedData() {
        var changedIndexes = this.changedIndexes;
        if (changedIndexes.isEmpty()) {
            return Map.of();
        }
        var data = new LinkedHashMap<>();
        changedIndexes.intStream().forEach(index -> {
            if (list.get(index) == null) {
                data.put(index, 1);
            }
        });
        return data;
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
    protected void resetStates() {
        var changedIndexes = this.changedIndexes;
        if (changedIndexes.size() > 0) {
            changedIndexes.clear();
        }
        super.resetStates();
    }

    @Override
    public BsonArray toBson() {
        var list = this.list;
        var size = list.size();
        var bson = new BsonArray(size);
        for (var v : list) {
            if (v == null) {
                bson.add(BsonNull.VALUE);
            } else {
                bson.add(v.toBson());
            }
        }
        return bson;
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

    @SuppressWarnings("unchecked")
    @Override
    public Self clear() {
        fullyUpdate(true);
        changedIndexes.clear();
        clearList();
        return (Self) this;
    }

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

    @SuppressWarnings("unchecked")
    @Override
    public Self clean() {
        clearList();
        resetStates();
        return (Self) this;
    }

}
