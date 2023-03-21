package com.github.fmjsjx.bson.model2.core;

import org.bson.BsonValue;

/**
 * The abstract implementation of container model.
 *
 * @param <T>    the type of the {@link BsonValue} that this model mapping with
 * @param <Self> the type of the implementation class
 * @author MJ Fang
 * @see ListModel
 * @see MapModel
 * @see DefaultMapModel
 * @since 2.0
 */
abstract class AbstractContainerModel<T extends BsonValue, Self extends AbstractContainerModel<T, Self>> extends AbstractBsonModel<T, Self> {

    protected boolean fullyUpdate;

    @Override
    public DotNotationPath path() {
        var parent = parent();
        if (parent == null) {
            return DotNotationPath.root();
        }
        if (key != null) {
            return parent.path().resolve(key.toString());
        } else if (index >= 0) {
            return parent.path().resolve(index);
        }
        return null;
    }

    /**
     * Returns the number of elements in this container.
     *
     * @return the number of elements in this container
     */
    public abstract int size();

    /**
     * Return if this container is empty or not.
     *
     * @return {@code true} if this container is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean isFullyUpdate() {
        return fullyUpdate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Self fullyUpdate(boolean fullyUpdate) {
        this.fullyUpdate = fullyUpdate;
        return (Self) this;
    }

    /**
     * Remove all data.
     *
     * @return this model
     */
    public abstract Self clear();
}
