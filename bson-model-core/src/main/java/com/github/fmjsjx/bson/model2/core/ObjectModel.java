package com.github.fmjsjx.bson.model2.core;

import com.mongodb.client.model.Updates;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import java.util.BitSet;
import java.util.List;

/**
 * The abstract object implementation of {@link BsonModel}.
 *
 * @param <Self> the type of the implementation class
 */
public abstract class ObjectModel<Self extends ObjectModel<Self>> extends AbstractBsonModel<BsonDocument, Self> {

    protected final BitSet changedFields = new BitSet();

    @Override
    public int appendUpdates(List<Bson> updates) {
        var base = updates.size();
        if (isFullyUpdate()) {
            appendFullUpdate(updates);
        } else {
            appendFieldUpdates(updates);
        }
        return updates.size() - base;
    }

    /**
     * Append the full update of this model into the given list.
     *
     * @param updates the list of updates
     */
    protected void appendFullUpdate(List<Bson> updates) {
        updates.add(Updates.set(path().value(), toBson()));
    }

    /**
     * Append the updates of changed fields on this model into the given list.
     *
     * @param updates the list of updates
     */
    protected abstract void appendFieldUpdates(List<Bson> updates);

    /**
     * Set changed of the field at the index.
     *
     * @param index the field index, begin with {@code 1}
     * @return this model
     */
    @SuppressWarnings("unchecked")
    protected Self fieldChanged(int index) {
        changedFields.set(index);
        triggerChanged();
        return (Self) this;
    }

    @Override
    protected void resetStates() {
        changedFields.clear();
        super.resetStates();
    }
}
