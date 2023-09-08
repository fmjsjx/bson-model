package com.github.fmjsjx.bson.model2.core;

import com.fasterxml.jackson.databind.JsonNode;
import org.bson.BsonValue;

/**
 * The abstract implementation of {@link BsonModel}.
 *
 * @param <T>    the type of the BsonValue that this model mapping with
 * @param <Self> the type of the implementation class
 * @author MJ Fang
 * @since 2.0
 */
public abstract class AbstractBsonModel<T extends BsonValue, Self extends AbstractBsonModel<T, Self>> implements BsonModel<T> {

    protected BsonModel<?> parent;
    protected int index = -1;
    protected Object key;

    protected boolean fullyUpdate;
    protected boolean changedTriggered;

    protected DotNotationPath cachedPath;

    @SuppressWarnings("unchecked")
    @Override
    public <P extends BsonModel<?>> P parent() {
        return (P) parent;
    }

    /**
     * Sets the parent {@link BsonModel}.
     *
     * @param parent the parent
     * @return this model
     */
    @SuppressWarnings("unchecked")
    public Self parent(BsonModel<?> parent) {
        this.parent = parent;
        return (Self) this;
    }

    /**
     * Set the index of this model.
     *
     * @param index the index
     * @return this model
     */
    @SuppressWarnings("unchecked")
    public Self index(int index) {
        this.index = index;
        return (Self) this;
    }

    /**
     * Set the key of this model.
     *
     * @param key the key
     * @return this model
     */
    @SuppressWarnings("unchecked")
    public Self key(Object key) {
        this.key = key;
        return (Self) this;
    }

    protected boolean bound() {
        return parent != null;
    }

    /**
     * This model must be unbound.
     *
     * @throws IllegalArgumentException if this model has been already bound
     */
    public void mustUnbound() {
        if (bound()) {
            throw new IllegalArgumentException("the model has been already bound");
        }
    }

    /**
     * Unbind this model.
     *
     * @return this model
     */
    @SuppressWarnings("unchecked")
    public Self unbind() {
        parent = null;
        index = -1;
        key = null;
        cachedPath = null;
        return (Self) this;
    }

    @Override
    public abstract Self load(T src);

    @Override
    public abstract Self load(JsonNode src);

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
    protected void resetStates() {
        fullyUpdate = false;
        changedTriggered = false;
    }

    /**
     * Returns the number of the deleted values on this model.
     *
     * @return the number of the deleted values on this model
     */
    protected abstract int deletedSize();

    @Override
    public boolean anyDeleted() {
        return deletedSize() > 0;
    }

    protected void triggerChanged() {
        if (!changedTriggered) {
            emitChanged();
            changedTriggered = true;
        }
    }

    /**
     * Emit updated event of this model.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void emitChanged() {
        var parent = parent();
        if (parent != null) {
            if (parent instanceof MapModel model && key != null) {
                model.triggerChanged(key);
            } else if (parent instanceof ObjectModel<?> model && key != null && index >= 0) {
                model.triggerChanged(index);
            } else if (parent instanceof ListModel<?, ?> model && index >= 0) {
                model.triggerChanged(index);
            } else if (parent instanceof AbstractBsonModel<?, ?> model) {
                model.triggerChanged();
            }
        }
    }

    @Override
    public DotNotationPath path() {
        var cachedPath = this.cachedPath;
        if (cachedPath == null) {
            var parent = parent();
            if (parent == null) {
                return DotNotationPath.root();
            }
            if (parent instanceof ListModel & index >= 0) {
                this.cachedPath = cachedPath = parent.path().resolve(index);
            } else if (key != null) {
                this.cachedPath = cachedPath = parent.path().resolve(key);
            } else {
                throw new IllegalStateException("parent exists without key or index");
            }
        }
        return cachedPath;
    }

    /**
     * Sets if the model should fully update or not.
     *
     * @param fullyUpdate {@code true} if the model should fully update, {@code false} otherwise
     * @return this model
     */
    @SuppressWarnings("unchecked")
    public final Self fullyUpdate(boolean fullyUpdate) {
        if (fullyUpdate != isFullyUpdate()) {
            this.fullyUpdate = fullyUpdate;
            if (fullyUpdate) {
                triggerChanged();
            }
        }
        return (Self) this;
    }

    /**
     * Returns if the model should fully update or not.
     *
     * @return {@code true} if the model should fully update, {@code false} otherwise
     */
    public final boolean isFullyUpdate() {
        return fullyUpdate;
    }

    /**
     * Clean this model.
     *
     * @return this model
     */
    public abstract Self clean();

    @Override
    public abstract Self deepCopy();

    /**
     * Deep copy values from this model to the {@code dest} model.
     *
     * @param dest the destination model
     * @return this model
     */
    public Self deepCopyTo(Self dest) {
        return deepCopyTo(dest, true);
    }

    /**
     * Deep copy values from this model to the {@code dest} model.
     *
     * @param dest  the destination model
     * @param clean whether the destination model should be clean or not
     * @return this model
     */
    @SuppressWarnings("unchecked")
    public Self deepCopyTo(Self dest, boolean clean) {
        if (clean) {
            dest.clean();
        }
        var self = (Self) this;
        dest.deepCopyFrom(self);
        return self;
    }

    /**
     * Deep copy values from the {@code src} model.
     *
     * @param src the source model
     */
    protected abstract void deepCopyFrom(Self src);

}
