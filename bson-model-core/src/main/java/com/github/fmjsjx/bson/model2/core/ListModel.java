package com.github.fmjsjx.bson.model2.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.fmjsjx.libcommon.collection.IntHashSet;
import com.github.fmjsjx.libcommon.collection.IntSet;
import org.bson.BsonArray;

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
public abstract class ListModel<E, Self extends ListModel<E, Self>>
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

    @SuppressWarnings("unchecked")
    @Override
    public Self load(JsonNode src) {
        if (!src.isArray()) {
            throw new IllegalArgumentException("src expected be an ARRAY but was " + src.getNodeType());
        }
        loadArrayNode((ArrayNode) src);
        return (Self) this;
    }

    /**
     * Load data from the source data {@link ArrayNode}.
     *
     * @param src the source data {@code ArrayNode}
     */
    protected abstract void loadArrayNode(ArrayNode src);

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
     * @throws IndexOutOfBoundsException if the index is out of range ({@code index < 0 || index >= size()})
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
    public abstract E set(int index, E value);

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
    public abstract E remove(int index);

    /**
     * Appends the specified element to the end of this list.
     *
     * @param value the element value to be appended to this list
     * @return this model
     */
    public abstract Self append(E value);

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
        return isFullyUpdate() || !changedIndexes.isEmpty();
    }

    @Override
    public boolean anyUpdated() {
        if (isFullyUpdate()) {
            return true;
        }
        var changedIndexes = this.changedIndexes;
        if (changedIndexes.isEmpty()) {
            return false;
        }
        var list = this.list;
        return changedIndexes.intStream().mapToObj(list::get).anyMatch(Objects::nonNull);
    }

    @Override
    protected int deletedSize() {
        var changedIndexes = this.changedIndexes;
        if (changedIndexes.isEmpty()) {
            return 0;
        }
        var list = this.list;
        return (int) changedIndexes.intStream().mapToObj(list::get).filter(Objects::isNull).count();
    }

    @Override
    public boolean anyDeleted() {
        var changedIndexes = this.changedIndexes;
        if (changedIndexes.isEmpty()) {
            return false;
        }
        var list = this.list;
        return changedIndexes.intStream().mapToObj(list::get).anyMatch(Objects::isNull);
    }

    @Override
    public Object toDeletedData() {
        var changedIndexes = this.changedIndexes;
        if (changedIndexes.isEmpty()) {
            return null;
        }
        var data = new LinkedHashMap<>(Math.max(8, changedIndexes.size() << 1));
        var list = this.list;
        changedIndexes.intStream().forEach(index -> {
            if (list.get(index) == null) {
                data.put(index, 1);
            }
        });
        return data.isEmpty() ? null : data;
    }

    @Override
    protected void resetStates() {
        var changedIndexes = this.changedIndexes;
        if (changedIndexes.size() > 0) {
            changedIndexes.clear();
        }
        super.resetStates();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Self clear() {
        fullyUpdate(true);
        changedIndexes.clear();
        clearList();
        return (Self) this;
    }

    protected abstract void clearList();

    @SuppressWarnings("unchecked")
    @Override
    public Self clean() {
        clearList();
        resetStates();
        return (Self) this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + list;
    }
}
