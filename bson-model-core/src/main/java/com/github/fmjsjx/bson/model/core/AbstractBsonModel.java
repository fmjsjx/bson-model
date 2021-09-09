package com.github.fmjsjx.bson.model.core;

import java.util.Map;

/**
 * The abstract implementation of {@link BsonModel}.
 */
abstract class AbstractBsonModel implements BsonModel {

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

    @Override
    public abstract Map<Object, Object> toDelete();

    /**
     * Returns the number of the deleted field size on this model.
     * 
     * @return the number of the deleted field size on this model
     */
    protected abstract int deletedSize();
    
    @Override
    public boolean deleted() {
        return deletedSize() > 0;
    }

}
