package com.github.fmjsjx.bson.model2.core;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import org.bson.*;

import java.util.function.Function;

/**
 * Constants of {@link SingleValueType}s.
 *
 * @author MJ Fang
 * @see SingleValueType
 * @since 2.0
 */
public class SingleValueTypes {

    /**
     * Type for {@link Integer}.
     */
    public static final SingleValueType<Integer> INTEGER = new SimpleSingleValueType<>(Integer.class, v -> v.asNumber().intValue(), BsonInt32::new, JsonNode::intValue, IntNode::valueOf);
    /**
     * Type for {@link Long}.
     */
    public static final SingleValueType<Long> LONG = new SimpleSingleValueType<>(Long.class, v -> v.asNumber().longValue(), BsonInt64::new, JsonNode::longValue, LongNode::valueOf);
    /**
     * Type for {@link Double}.
     */
    public static final SingleValueType<Double> DOUBLE = new SimpleSingleValueType<>(Double.class, v -> v.asNumber().doubleValue(), BsonDouble::new, JsonNode::doubleValue, DoubleNode::valueOf);
    /**
     * Type for {@link String}.
     */
    public static final SingleValueType<String> STRING = new SimpleSingleValueType<>(String.class, v -> v.asString().getValue(), BsonString::new, JsonNode::textValue, TextNode::valueOf);

    private record SimpleSingleValueType<T>(Class<T> type, Function<BsonValue, T> bsonValueDecoder,
                                            Function<T, BsonValue> bsonValueEncoder,
                                            Function<JsonNode, T> jsonNodeDecoder,
                                            Function<T, JsonNode> jsonNodeEncoder
    ) implements SingleValueType<T> {

        @Override
        public T parse(BsonValue value) {
            if (value == null || value.isNull()) {
                return null;
            }
            return bsonValueDecoder.apply(value);
        }

        @Override
        public T parse(JsonNode value) {
            if (value == null || value.isNull()) {
                return null;
            }
            return jsonNodeDecoder.apply(value);
        }

        @Override
        public BsonValue toBsonValue(T value) {
            if (value == null) {
                return BsonNull.VALUE;
            }
            return bsonValueEncoder.apply(value);
        }

        @Override
        public JsonNode toJsonNode(T value) {
            if (value == null) {
                return NullNode.getInstance();
            }
            return jsonNodeEncoder.apply(value);
        }

    }

    private SingleValueTypes() {
    }

}
