package com.github.fmjsjx.bson.model2.core;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import org.bson.BsonValue;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.Map;

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
     * @param <P> the type of the parent model
     * @return the parent model
     */
    <P extends BsonModel<?>> P parent();

    /**
     * Returns the path of this model.
     *
     * @return the path of this model
     */
    DotNotationPath path();

    /**
     * Convert this model to a {@link BsonValue}.
     *
     * @return a {@code BsonValue}
     */
    T toBson();

    /**
     * Load data from the source {@link BsonValue}.
     *
     * @param src the source {@code BsonValue}
     * @return this model
     */
    BsonModel<?> load(T src);

    /**
     * Convert this model to a {@link JsonNode}.
     *
     * @return a {@code JsonNode}
     */
    JsonNode toJsonNode();

    /**
     * Load data from the source data {@link JsonNode}.
     *
     * @param src the source data {@code JsonNode}
     * @return this model
     */
    BsonModel<?> load(JsonNode src);

    /**
     * Convert this model to a {@link JSONObject} or a {@link JSONArray}.
     *
     * @return a {@code JSONObject} or a {@code JSONArray}
     * @since 2.2
     */
    default Object toFastjson2Node() {
        throw new UnsupportedOperationException("fastjson2 not supported");
    }

    /**
     * Load data from the source that is a {@link JSONObject} or a {@link JSONArray}.
     *
     * @param src the source data that is a {@code JSONObject} or a {@code JSONArray}
     * @return this model
     * @since 2.2
     */
    default BsonModel<?> loadFastjson2Node(Object src) {
        throw new UnsupportedOperationException("fastjson2 not supported");
    }

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
     * Creates and returns a new data object for this model.
     * <p>
     * Type of the returned object may be Map or List.
     *
     * @return a new data object for this model
     */
    Object toData();

    /**
     * Returns {@code true} if any value of this model has been updated in context, {@code false} otherwise.
     *
     * @return {@code true} if any value of this model has been updated in context, {@code false} otherwise
     */
    boolean anyUpdated();

    /**
     * Creates and returns a new update data object for this model.
     *
     * @return a new update data object for this model
     */
    Map<Object, Object> toUpdateData();

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
    Map<Object, Object> toDeletedData();

    /**
     * Deep copy.
     *
     * @return a new model
     */
    BsonModel<T> deepCopy();

}
