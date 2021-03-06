package com.github.fmjsjx.bson.model.core;

import org.bson.BsonDocument;

/***
 * The abstract class for values of {@link DefaultMapModel}.
 *
 * @param <K>    the type of key
 * @param <Self> the type of implementation
 * 
 * @see DefaultMapModel
 * @see ObjectModel
 */
public abstract class DefaultMapValueModel<K, Self extends DefaultMapValueModel<K, Self>> extends ObjectModel<Self> {

    protected K key;
    protected DefaultMapModel<K, Self, ?> parent;

    /**
     * Returns the key.
     * 
     * @return the key
     */
    public K key() {
        return key;
    }

    @Override
    public abstract BsonDocument toBson();

    @SuppressWarnings("unchecked")
    protected Self key(K key) {
        this.key = key;
        return (Self) this;
    }

    @Override
    public DefaultMapModel<K, Self, ?> parent() {
        return parent;
    }

    @SuppressWarnings("unchecked")
    protected Self parent(DefaultMapModel<K, Self, ?> parent) {
        this.parent = parent;
        return (Self) this;
    }

    protected Self unbind() {
        return key(null).parent(null);
    }

    /**
     * Returns if this DefaultMapValueModel is bound or not.
     * 
     * @return {@code true} if this DefaultMapValueModel is already bound,
     *         {@code false} otherwise
     */
    public boolean bound() {
        return key != null && parent != null;
    }

    protected void emitUpdated() {
        var key = this.key;
        var parent = this.parent;
        if (key != null && parent != null) {
            parent.updatedKeys.add(key);
        }
        super.emitUpdated();
    }

    @Override
    public DotNotation xpath() {
        if (!bound()) {
            throw new IllegalStateException("this DefaultMapValueModel is not binding");
        }
        return parent().xpath().resolve(String.valueOf(key()));
    }

}
