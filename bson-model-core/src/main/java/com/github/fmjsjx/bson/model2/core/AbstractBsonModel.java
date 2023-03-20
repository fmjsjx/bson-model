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
    protected int listIndex = -1;
    protected Object mapKey;

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
    protected Self listIndex(int listIndex) {
        this.listIndex = listIndex;
        return (Self) this;
    }

    @SuppressWarnings("unchecked")
    protected Self mapKey(Object mapKey) {
        this.mapKey = mapKey;
        return (Self) this;
    }

    @SuppressWarnings("unchecked")
    protected Self unbind() {
        this.parent = null;
        this.listIndex = -1;
        this.mapKey = null;
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
    protected void emitChanged() {
        var parent = parent();
        if (parent != null) {
            // TODO if (parent instanceOf MapModel mapModel && mapKey != null) {
            // TODO     mapModel.changedKey(mapKey);
            // TODO } else if (parent instanceOf ListModel listModel && listIndex >= 0) {
            // TODO     listModel.changedIndex(listIndex);
            // TODO }
            parent.emitChanged();
        }
    }

}
