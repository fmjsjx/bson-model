package com.github.fmjsjx.bson.model2.core;

import org.bson.BsonValue;

/**
 * The abstract implementation of container model.
 *
 * @param <T>    the type of the {@link BsonValue} that this model mapping with
 * @param <Self> the type of the implementation class
 * @author MJ Fang
 * @since 2.0
 */
abstract class AbstractContainerModel<T extends BsonValue, Self extends AbstractContainerModel<T, Self>> extends AbstractBsonModel<T, Self> {

    @Override
    public String path() {
        var parent = parent();
        if (parent == null) {
            return "";
        }
        if (mapKey != null) {
            return parent.path() + "." + mapKey;
        } else if (listIndex >= 0) {
            return parent.path() + "." + listIndex;
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
    public boolean empty() {
        return size() == 0;
    }

}
