package com.github.fmjsjx.bson.model2.core;

import com.fasterxml.jackson.databind.JsonNode;
import org.bson.BsonValue;

/**
 * The interface defines methods for types of single values.
 *
 * @param <T> the type of the single value
 * @author MJ Fang
 * @see SingleValueTypes
 * @see SingleValueMapModel
 * @since 2.0
 */
public interface SingleValueType<T> {

    /**
     * Returns the class of the value type.
     *
     * @return the class of the value type
     */
    Class<T> type();

    /**
     * Parse the value from {@link BsonValue} to java type.
     *
     * @param value the value as {@code BsonType}
     * @return the value in java type
     */
    T parse(BsonValue value);

    /**
     * Parse the value from {@link JsonNode} to java type.
     *
     * @param value the value as {@code JsonNode}
     * @return the value in java type
     */
    T parse(JsonNode value);

    /**
     * Converts the value from java type to {@link BsonValue}.
     *
     * @param value the value in java type
     * @return a {@code BsonValue}
     */
    BsonValue toBsonValue(T value);

    /**
     * Converts value from java type a {@link JsonNode}.
     *
     * @param value the value
     * @return a {@code JsonNode}
     */
    JsonNode toJsonNode(T value);

    /**
     * Converts value from model type to data type.
     *
     * @param value the value
     * @return the converted value
     */
    default Object toData(T value) {
        return value;
    }

    /**
     * Parse the value to the model type.
     *
     * @param value the JSON value
     * @return the value in model type
     * @since 2.2
     */
    T parseData(Object value);

}
