package com.github.fmjsjx.bson.model.core;

import java.util.ArrayList;
import java.util.List;

import org.bson.conversions.Bson;

/**
 * The abstract root implementation of Object BSON Model.
 * 
 * @param <Self> the type of the implementation class
 * 
 * @since 2.0
 */
public abstract class RootModel<Self extends RootModel<Self>> extends ObjectModel<Self> {

    @Override
    public BsonModel parent() {
        throw new UnsupportedOperationException("method parent() cannot be called for root model");
    }

    /**
     * Creates and returns a new list of updates for this model.
     * 
     * @return the list of updates
     */
    public List<Bson> toUpdates() {
        var updates = new ArrayList<Bson>();
        appendUpdates(updates);
        return updates;
    }

    @Override
    public final DotNotation xpath() {
        return DotNotation.root();
    }

}
