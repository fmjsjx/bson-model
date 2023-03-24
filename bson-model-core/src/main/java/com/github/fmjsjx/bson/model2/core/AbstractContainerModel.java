package com.github.fmjsjx.bson.model2.core;

import org.bson.BsonValue;

/**
 * The abstract implementation of container model.
 *
 * @param <T>    the type of the {@link BsonValue} that this model mapping with
 * @param <Self> the type of the implementation class
 * @author MJ Fang
 * @see ListModel
 * @see DefaultListModel
 * @see MapModel
 * @see DefaultMapModel
 * @since 2.0
 */
abstract class AbstractContainerModel<T extends BsonValue, Self extends AbstractContainerModel<T, Self>> extends AbstractBsonModel<T, Self> {

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

    /**
     * Remove all data.
     *
     * @return this model
     */
    public abstract Self clear();

    /**
     * Clean this model.
     * <p>
     * This method is very similar to {@link #clear()}. The difference is the
     * states will be reset and will never trigger changed.
     *
     * @return this model
     */
    @Override
    public abstract Self clean();

}
