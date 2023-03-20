package com.github.fmjsjx.bson.model2.core;

import org.bson.BsonValue;
import org.bson.conversions.Bson;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * The top interface for BSON model.
 *
 * @param <T> the type of the {@link BsonValue} that this model mapping with
 * @author MJ Fang
 * @since 2.0
 */
public interface BsonModel<T extends BsonValue> {

    /**
     * Returns the parent model.
     *
     * @return the parent model
     */
    BsonModel<?> parent();

    /**
     * Returns the path of this model.
     *
     * @return the path of this model
     */
    String path();

    /**
     * Convert this model to a {@link BsonValue}.
     *
     * @return a {@code BsonValue}
     */
    T toBson();

    /**
     * Creates and returns a new data object for this model.
     * <p>
     * Type of the returned object may be Map or List.
     *
     * @return a new data object for this model
     */
    Object toData();

    /**
     * Load data from the source {@link BsonValue}.
     *
     * @param src the source {@code BsonValue}
     */
    void load(T src);

    /**
     * Load data from the JSON string source data.
     *
     * @param src the JSON string source data
     */
    default void loadJsonString(String src) {
        loadJsonBytes(Objects.requireNonNull(src, "src must not be null").getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Load data from the JSON bytes source data.
     *
     * @param src the JSON bytes source data
     */
    void loadJsonBytes(byte[] src);

    /**
     * Appends the updates of this model into the given list.
     *
     * @param updates the list of original updates
     * @return the number of the updates added
     */
    int appendUpdates(List<Bson> updates);

    /**
     * Reset states of this model.
     */
    void reset();

    /**
     * Returns {@code true} if any value of this model has been changed in context, {@code false} otherwise.
     *
     * @return {@code true} if any value of this model has been changed in context, {@code false} otherwise
     */
    boolean anyChanged();

    /**
     * Creates and returns a new update data object for this model.
     *
     * @return a new update data object for this model
     */
    Object toUpdateData();

    /**
     * Returns {@code true} if any value of this model has been deleted in context, {@code false} otherwise.
     *
     * @return {@code true} if any value of this model has been deleted in context, {@code false} otherwise
     */
    boolean anyDeleted();

    /**
     * Creates and returns a new deleted data object for this model.
     *
     * @return a new deleted data object for this model
     */
    Object toDeletedData();

}
