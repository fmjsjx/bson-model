package com.github.fmjsjx.bson.model2.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.model.Updates;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
            if (!changedFields.isEmpty()) {
                appendFieldUpdates(updates);
            }
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
     * @param index the field index
     * @return this model
     */
    @SuppressWarnings("unchecked")
    protected Self fieldChanged(int index) {
        changedFields.set(index);
        triggerChanged();
        return (Self) this;
    }

    /**
     * Set changed of the fields at the specified indexes.
     *
     * @param indexes the index array of the fields
     * @return this model
     */
    @SuppressWarnings("unchecked")
    protected Self fieldsChanged(int... indexes) {
        var changedFields = this.changedFields;
        for (var index : indexes) {
            changedFields.set(index);
        }
        triggerChanged();
        return (Self) this;
    }

    @Override
    protected void resetStates() {
        changedFields.clear();
        super.resetStates();
    }

    @Override
    public boolean anyChanged() {
        return isFullyUpdate() || changedFields.length() > 0;
    }

    @Override
    public void load(JsonNode src) {
        if (!src.isObject()) {
            throw new IllegalArgumentException("src expected be an OBJECT but was " + src.getNodeType());
        }
        loadObjectNode((ObjectNode) src);
    }

    /**
     * Load data from the source data {@link ObjectNode}.
     *
     * @param src the source data {@code ObjectNode}
     */
    protected abstract void loadObjectNode(ObjectNode src);

    @Override
    protected Object toSubUpdateData() {
        if (changedFields.isEmpty()) {
            return null;
        }
        var data = new LinkedHashMap<>();
        appendUpdateData(data);
        return data.isEmpty() ? null : data;
    }

    /**
     * Append update data.
     *
     * @param data the target data
     */
    protected abstract void appendUpdateData(Map<Object, Object> data);

    @Override
    public Object toDeletedData() {
        if (changedFields.isEmpty()) {
            return null;
        }
        var data = new LinkedHashMap<>();
        appendDeletedData(data);
        return data.isEmpty() ? null : data;
    }

    /**
     * Append deleted data.
     *
     * @param data the target data
     */
    protected abstract void appendDeletedData(Map<Object, Object> data);

}
