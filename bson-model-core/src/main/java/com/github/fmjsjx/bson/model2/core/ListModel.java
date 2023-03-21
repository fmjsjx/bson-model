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

/**
 * The abstract list implementation of {@link AbstractContainerModel}.
 *
 * @param <E>    the type of elements in this list
 * @param <Self> the type of the implementation class
 * @author MJ Fang
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
     * @return an {@code Optional<List<E>>}
     */
    public List<E> values() {
        return Collections.unmodifiableList(list);
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
            return list.stream().map(E::toData).toList();
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
        var size = list.size();
        if (size == 0) {
            return List.of();
        }
        var data = new ArrayList<>(size);
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
        fullyUpdate(false);
        var changedIndexes = this.changedIndexes;
        if (changedIndexes.size() > 0) {
            changedIndexes.clear();
        }
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

    /**
     * Removes all the values from this list.
     *
     * @return this list
     */
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

}
