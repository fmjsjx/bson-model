package com.github.fmjsjx.bson.model.core;

import org.bson.BsonDocument;

/**
 * The abstract class for values of {@link DefaultListModel}.
 * 
 * @param <Self> the type of implementation
 * 
 * @see DefaultListModel
 * @see ObjectModel
 * 
 * @since 1.1
 */
public abstract class DefaultListValueModel<Self extends DefaultListValueModel<Self>> extends ObjectModel<Self> {

    protected int index = -1;
    protected DefaultListModel<Self, ?> parent;

    /**
     * Returns the index.
     * 
     * @return the index
     */
    public int index() {
        return index;
    }

    @Override
    public abstract BsonDocument toBson();

    @SuppressWarnings("unchecked")
    protected Self index(int index) {
        this.index = index;
        return (Self) this;
    }

    @Override
    public DefaultListModel<Self, ?> parent() {
        return parent;
    }

    @SuppressWarnings("unchecked")
    protected Self parent(DefaultListModel<Self, ?> parent) {
        this.parent = parent;
        return (Self) this;
    }

    protected Self unbind() {
        return index(-1).parent(null);
    }

    /**
     * Returns if this DefaultListValueModel is bound or not.
     * 
     * @return {@code true} if this DefaultListValueModel is already bound,
     *         {@code false} otherwise
     */
    public boolean bound() {
        return index >= 0 & parent != null;
    }

    @SuppressWarnings("unchecked")
    protected Self emitUpdated() {
        var index = this.index;
        var parent = this.parent;
        if (index >= 0 && parent != null) {
            parent.updateIndexes.add(index);
        }
        return (Self) this;
    }

    @Override
    public DotNotation xpath() {
        if (!bound()) {
            throw new IllegalStateException("this DefaultListValueModel is not binding");
        }
        return parent().xpath().resolve(index());
    }

}
