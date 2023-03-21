package com.github.fmjsjx.bson.model2.core;

import org.bson.BsonValue;

/**
 * The abstract implementation of {@link BsonModel}.
 *
 * @param <T>    the type of the BsonValue that this model mapping with
 * @param <Self> the type of the implementation class
 * @author MJ Fang
 * @since 2.0
 */
abstract class AbstractBsonModel<T extends BsonValue, Self extends AbstractBsonModel<T, Self>> implements BsonModel<T> {

    protected AbstractBsonModel<?, ?> parent;
    protected int index = -1;
    protected Object key;

    @Override
    public AbstractBsonModel<?, ?> parent() {
        return parent;
    }

    @SuppressWarnings("unchecked")
    protected Self parent(AbstractBsonModel<?, ?> parent) {
        this.parent = parent;
        return (Self) this;
    }

    @SuppressWarnings("unchecked")
    protected Self index(int index) {
        this.index = index;
        return (Self) this;
    }

    @SuppressWarnings("unchecked")
    protected Self key(Object mapKey) {
        this.key = mapKey;
        return (Self) this;
    }

    @SuppressWarnings("unchecked")
    protected Self unbind() {
        this.parent = null;
        this.index = -1;
        this.key = null;
        return (Self) this;
    }

    @Override
    public void reset() {
        resetChildren();
        resetStates();
    }

    /**
     * Reset children of this model.
     */
    protected abstract void resetChildren();

    /**
     * Reset states of this model.
     */
    protected abstract void resetStates();

    /**
     * Returns the number of the deleted values on this model.
     *
     * @return the number of the deleted values on this model
     */
    protected abstract int deletedSize();

    @Override
    public boolean anyDeleted() {
        return deletedSize() > 0;
    }

    /**
     * Emit updated event of this model.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void emitChanged() {
        var parent = parent();
        if (parent != null) {
            if (parent instanceof MapModel model && key != null) {
                model.changedKeys.add(key);
            } else if (parent instanceof ObjectModel<?> model && key != null && index > 0) {
                model.changedFields.set(index);
            } else if (parent instanceof ListModel model && index >= 0) {
                model.changedIndexes.add(index);
            }
            parent.emitChanged();
        }
    }

    @Override
    public DotNotationPath path() {
        var parent = parent();
        if (parent != null) {
            if (key != null) {
                return parent.path().resolve(key);
            } else if (index >= 0 && parent instanceof ListModel) {
                return parent.path().resolve(index);
            }
            throw new IllegalStateException("parent exists without key or index");
        }
        return DotNotationPath.root();
    }

    public abstract Self fullyUpdate(boolean fullyUpdate);

    public abstract boolean isFullyUpdate();
}
