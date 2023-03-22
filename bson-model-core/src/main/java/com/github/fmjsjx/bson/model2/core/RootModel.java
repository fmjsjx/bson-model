package com.github.fmjsjx.bson.model2.core;

import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

/**
 * The abstract root implementation of {@link ObjectModel}.
 *
 * @param <Self> the type of the implementation class
 * @author MJ Fang
 * @see ObjectModel
 * @since 2.0
 */
public abstract class RootModel<Self extends RootModel<Self>> extends ObjectModel<Self> {

    @Override
    public BsonModel<?> parent() {
        return null;
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
    public DotNotationPath path() {
        return DotNotationPath.root();
    }

    @Override
    protected void emitChanged() {
        // do nothing for root model
    }

    @Override
    protected void triggerChanged() {
        // do nothing for root model
    }

}
