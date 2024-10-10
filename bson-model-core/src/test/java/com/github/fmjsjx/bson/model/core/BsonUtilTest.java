package com.github.fmjsjx.bson.model.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.github.fmjsjx.bson.model2.core.SingleValueTypes;
import org.bson.*;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BsonUtilTest {

    @Test
    public void testEmbedded() {
        var json = "{\"a\":{\"b\":{\"c1\":{\"d1\":1,\"d2\":\"2\",\"d3\":[1,2,3]},\"c2\":[{\"i\":0},{\"i\":1},{\"i\":2}]}}}";
        var document = Document.parse(json);
        try {
            assertTrue(BsonUtil.embedded(document, "a").isPresent());
            assertTrue(BsonUtil.embedded(document, "a", "b").isPresent());
            assertTrue(BsonUtil.embedded(document, "a", "b", "c1").isPresent());
            assertTrue(BsonUtil.embedded(document, "a", "b", "c2").isPresent());
            Optional<Integer> d1 = BsonUtil.embedded(document, "a", "b", "c1", "d1");
            assertTrue(d1.isPresent());
            assertEquals(1, d1.get().intValue());
            Optional<String> d2 = BsonUtil.embedded(document, "a", "b", "c1", "d2");
            assertTrue(d2.isPresent());
            assertEquals("2", d2.get());
            Optional<List<Integer>> d3 = BsonUtil.embedded(document, "a", "b", "c1", "d3");
            assertTrue(d3.isPresent());
            assertArrayEquals(new int[]{1, 2, 3}, d3.get().stream().mapToInt(Integer::intValue).toArray());
            Optional<Integer> c20 = BsonUtil.embedded(document, "a", "b", "c2", 0, "i");
            assertTrue(c20.isPresent());
            assertEquals(0, c20.get().intValue());
            Optional<Integer> c21 = BsonUtil.embedded(document, "a", "b", "c2", 1, "i");
            assertTrue(c21.isPresent());
            assertEquals(1, c21.get().intValue());
            Optional<Integer> c22 = BsonUtil.embedded(document, "a", "b", "c2", 2, "i");
            assertTrue(c22.isPresent());
            assertEquals(2, c22.get().intValue());
            assertFalse(BsonUtil.embedded(document, "a", "b", "c").isPresent());
            assertFalse(BsonUtil.embedded(document, "a2").isPresent());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testEmbeddedInt() {
        var json = "{\"a\":{\"b\":{\"c1\":{\"d1\":1,\"d2\":\"2\",\"d3\":[1,2,3]},\"c2\":[{\"i\":0},{\"i\":1},{\"i\":2}]}}}";
        var document = Document.parse(json);
        try {
            var d1 = BsonUtil.embeddedInt(document, "a", "b", "c1", "d1");
            assertTrue(d1.isPresent());
            assertEquals(1, d1.getAsInt());
            var d30 = BsonUtil.embeddedInt(document, "a", "b", "c1", "d3", 0);
            assertTrue(d30.isPresent());
            assertEquals(1, d30.getAsInt());
            var d31 = BsonUtil.embeddedInt(document, "a", "b", "c1", "d3", 1);
            assertTrue(d31.isPresent());
            assertEquals(2, d31.getAsInt());
            var d32 = BsonUtil.embeddedInt(document, "a", "b", "c1", "d3", 2);
            assertTrue(d32.isPresent());
            assertEquals(3, d32.getAsInt());
            var c20 = BsonUtil.embeddedInt(document, "a", "b", "c2", 0, "i");
            assertTrue(c20.isPresent());
            assertEquals(0, c20.getAsInt());
            var c21 = BsonUtil.embeddedInt(document, "a", "b", "c2", 1, "i");
            assertTrue(c21.isPresent());
            assertEquals(1, c21.getAsInt());
            var c22 = BsonUtil.embeddedInt(document, "a", "b", "c2", 2, "i");
            assertTrue(c22.isPresent());
            assertEquals(2, c22.getAsInt());
            assertFalse(BsonUtil.embeddedInt(document, "a", "b", "c").isPresent());
            assertFalse(BsonUtil.embeddedInt(document, "a2").isPresent());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testEmbeddedLong() {
        var json = "{\"a\":{\"b\":{\"c1\":{\"d1\":1,\"d2\":\"2\",\"d3\":[1,2,3]},\"c2\":[{\"i\":0},{\"i\":1},{\"i\":2}]}}}";
        var document = Document.parse(json);
        try {
            var d1 = BsonUtil.embeddedLong(document, "a", "b", "c1", "d1");
            assertTrue(d1.isPresent());
            assertEquals(1, d1.getAsLong());
            var d30 = BsonUtil.embeddedLong(document, "a", "b", "c1", "d3", 0);
            assertTrue(d30.isPresent());
            assertEquals(1, d30.getAsLong());
            var d31 = BsonUtil.embeddedLong(document, "a", "b", "c1", "d3", 1);
            assertTrue(d31.isPresent());
            assertEquals(2, d31.getAsLong());
            var d32 = BsonUtil.embeddedLong(document, "a", "b", "c1", "d3", 2);
            assertTrue(d32.isPresent());
            assertEquals(3, d32.getAsLong());
            var c20 = BsonUtil.embeddedLong(document, "a", "b", "c2", 0, "i");
            assertTrue(c20.isPresent());
            assertEquals(0, c20.getAsLong());
            var c21 = BsonUtil.embeddedLong(document, "a", "b", "c2", 1, "i");
            assertTrue(c21.isPresent());
            assertEquals(1, c21.getAsLong());
            var c22 = BsonUtil.embeddedLong(document, "a", "b", "c2", 2, "i");
            assertTrue(c22.isPresent());
            assertEquals(2, c22.getAsLong());
            assertFalse(BsonUtil.embeddedLong(document, "a", "b", "c").isPresent());
            assertFalse(BsonUtil.embeddedLong(document, "a2").isPresent());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testEmbeddedDouble() {
        var json = "{\"a\":{\"b\":{\"c1\":{\"d1\":1,\"d2\":\"2\",\"d3\":[1,2,3]},\"c2\":[{\"i\":0},{\"i\":1},{\"i\":2}]}}}";
        var document = Document.parse(json);
        try {
            var d1 = BsonUtil.embeddedDouble(document, "a", "b", "c1", "d1");
            assertTrue(d1.isPresent());
            assertEquals(1, d1.getAsDouble());
            var d30 = BsonUtil.embeddedDouble(document, "a", "b", "c1", "d3", 0);
            assertTrue(d30.isPresent());
            assertEquals(1, d30.getAsDouble());
            var d31 = BsonUtil.embeddedDouble(document, "a", "b", "c1", "d3", 1);
            assertTrue(d31.isPresent());
            assertEquals(2, d31.getAsDouble());
            var d32 = BsonUtil.embeddedDouble(document, "a", "b", "c1", "d3", 2);
            assertTrue(d32.isPresent());
            assertEquals(3, d32.getAsDouble());
            var c20 = BsonUtil.embeddedDouble(document, "a", "b", "c2", 0, "i");
            assertTrue(c20.isPresent());
            assertEquals(0, c20.getAsDouble());
            var c21 = BsonUtil.embeddedDouble(document, "a", "b", "c2", 1, "i");
            assertTrue(c21.isPresent());
            assertEquals(1, c21.getAsDouble());
            var c22 = BsonUtil.embeddedDouble(document, "a", "b", "c2", 2, "i");
            assertTrue(c22.isPresent());
            assertEquals(2, c22.getAsDouble());
            assertFalse(BsonUtil.embeddedDouble(document, "a", "b", "c").isPresent());
            assertFalse(BsonUtil.embeddedDouble(document, "a2").isPresent());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testEmbeddedDateTime() {
        var json = "{\"a\":{\"b\":{\"c1\":{\"d1\":ISODate(\"2021-04-19T10:37:10.128Z\"),\"d2\":\"2\",\"d3\":[ISODate(\"2021-04-19T10:37:10.128Z\"),2,3]},\"c2\":[{\"i\":ISODate(\"2021-04-19T10:37:10.128Z\")},{\"i\":ISODate(\"2021-04-19T10:37:10.128Z\")},{\"i\":ISODate(\"2021-04-19T10:37:10.128Z\")}]}}}";
        var document = Document.parse(json);
        try {
            var zone = ZoneId.systemDefault();
            var time = ZonedDateTime.parse("2021-04-19T10:37:10.128Z").withZoneSameInstant(zone).toLocalDateTime();
            var d1 = BsonUtil.embeddedDateTime(document, "a", "b", "c1", "d1");
            assertTrue(d1.isPresent());
            assertEquals(time, d1.get());
            var d30 = BsonUtil.embeddedDateTime(document, "a", "b", "c1", "d3", 0);
            assertTrue(d30.isPresent());
            var c20 = BsonUtil.embeddedDateTime(document, "a", "b", "c2", 0, "i");
            assertTrue(c20.isPresent());
            assertEquals(time, c20.get());
            var c21 = BsonUtil.embeddedDateTime(document, "a", "b", "c2", 1, "i");
            assertTrue(c21.isPresent());
            assertEquals(time, c21.get());
            var c22 = BsonUtil.embeddedDateTime(document, "a", "b", "c2", 2, "i");
            assertTrue(c22.isPresent());
            assertEquals(time, c22.get());
            assertFalse(BsonUtil.embeddedDateTime(document, "a", "b", "c").isPresent());
            assertFalse(BsonUtil.embeddedDateTime(document, "a2").isPresent());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testEmbeddedZonedDateTime() {
        var json = "{\"a\":{\"b\":{\"c1\":{\"d1\":ISODate(\"2021-04-19T10:37:10.128Z\"),\"d2\":\"2\",\"d3\":[ISODate(\"2021-04-19T10:37:10.128Z\"),2,3]},\"c2\":[{\"i\":ISODate(\"2021-04-19T10:37:10.128Z\")},{\"i\":ISODate(\"2021-04-19T10:37:10.128Z\")},{\"i\":ISODate(\"2021-04-19T10:37:10.128Z\")}]}}}";
        var document = Document.parse(json);
        try {
            var zone = ZoneId.of("Z");
            var time = ZonedDateTime.parse("2021-04-19T10:37:10.128Z");
            var d1 = BsonUtil.embeddedDateTime(zone, document, "a", "b", "c1", "d1");
            assertTrue(d1.isPresent());
            assertEquals(time, d1.get());
            var d30 = BsonUtil.embeddedDateTime(zone, document, "a", "b", "c1", "d3", 0);
            assertTrue(d30.isPresent());
            var c20 = BsonUtil.embeddedDateTime(zone, document, "a", "b", "c2", 0, "i");
            assertTrue(c20.isPresent());
            assertEquals(time, c20.get());
            var c21 = BsonUtil.embeddedDateTime(zone, document, "a", "b", "c2", 1, "i");
            assertTrue(c21.isPresent());
            assertEquals(time, c21.get());
            var c22 = BsonUtil.embeddedDateTime(zone, document, "a", "b", "c2", 2, "i");
            assertTrue(c22.isPresent());
            assertEquals(time, c22.get());
            assertFalse(BsonUtil.embeddedDateTime(zone, document, "a", "b", "c").isPresent());
            assertFalse(BsonUtil.embeddedDateTime(zone, document, "a2").isPresent());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDocumentValue() {
        // {"a":{},"b":{"n":"b"}}
        var json = "{\"a\":{},\"b\":{\"n\":\"b\"}}";
        var document = Document.parse(json);
        try {
            var value = BsonUtil.documentValue(document, "a");
            assertTrue(value.isPresent());
            assertEquals(0, value.get().size());
            value = BsonUtil.documentValue(document, "b");
            assertTrue(value.isPresent());
            assertEquals(1, value.get().size());
            assertEquals("b", value.get().getString("n"));
            value = BsonUtil.documentValue(document, "c");
            assertTrue(value.isEmpty());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testEmbeddedBsonDocument() {
        var json = "{\"a\":{\"b\":{\"c1\":{\"d1\":1,\"d2\":\"2\",\"d3\":[1,2,3]},\"c2\":[{\"i\":0},{\"i\":1},{\"i\":2}]}}}";
        var document = BsonDocument.parse(json);
        try {
            assertTrue(BsonUtil.embedded(document, "a").isPresent());
            assertTrue(BsonUtil.embedded(document, "a", "b").isPresent());
            assertTrue(BsonUtil.embedded(document, "a", "b", "c1").isPresent());
            assertTrue(BsonUtil.embedded(document, "a", "b", "c2").isPresent());
            Optional<BsonInt32> d1 = BsonUtil.embedded(document, "a", "b", "c1", "d1");
            assertTrue(d1.isPresent());
            assertEquals(1, d1.get().intValue());
            Optional<BsonString> d2 = BsonUtil.embedded(document, "a", "b", "c1", "d2");
            assertTrue(d2.isPresent());
            assertEquals("2", d2.get().getValue());
            Optional<BsonArray> d3 = BsonUtil.embedded(document, "a", "b", "c1", "d3");
            assertTrue(d3.isPresent());
            assertArrayEquals(new int[]{1, 2, 3}, d3.get().stream().mapToInt(v -> v.asInt32().getValue()).toArray());
            Optional<BsonInt32> c20 = BsonUtil.embedded(document, "a", "b", "c2", 0, "i");
            assertTrue(c20.isPresent());
            assertEquals(0, c20.get().intValue());
            Optional<BsonInt32> c21 = BsonUtil.embedded(document, "a", "b", "c2", 1, "i");
            assertTrue(c21.isPresent());
            assertEquals(1, c21.get().intValue());
            Optional<BsonInt32> c22 = BsonUtil.embedded(document, "a", "b", "c2", 2, "i");
            assertTrue(c22.isPresent());
            assertEquals(2, c22.get().intValue());
            assertFalse(BsonUtil.embedded(document, "a", "b", "c").isPresent());
            assertFalse(BsonUtil.embedded(document, "a2").isPresent());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testEmbeddedIntBsonDocument() {
        var json = "{\"a\":{\"b\":{\"c1\":{\"d1\":1,\"d2\":\"2\",\"d3\":[1,2,3]},\"c2\":[{\"i\":0},{\"i\":1},{\"i\":2}]}}}";
        var document = BsonDocument.parse(json);
        try {
            var d1 = BsonUtil.embeddedInt(document, "a", "b", "c1", "d1");
            assertTrue(d1.isPresent());
            assertEquals(1, d1.getAsInt());
            var d30 = BsonUtil.embeddedInt(document, "a", "b", "c1", "d3", 0);
            assertTrue(d30.isPresent());
            assertEquals(1, d30.getAsInt());
            var d31 = BsonUtil.embeddedInt(document, "a", "b", "c1", "d3", 1);
            assertTrue(d31.isPresent());
            assertEquals(2, d31.getAsInt());
            var d32 = BsonUtil.embeddedInt(document, "a", "b", "c1", "d3", 2);
            assertTrue(d32.isPresent());
            assertEquals(3, d32.getAsInt());
            var c20 = BsonUtil.embeddedInt(document, "a", "b", "c2", 0, "i");
            assertTrue(c20.isPresent());
            assertEquals(0, c20.getAsInt());
            var c21 = BsonUtil.embeddedInt(document, "a", "b", "c2", 1, "i");
            assertTrue(c21.isPresent());
            assertEquals(1, c21.getAsInt());
            var c22 = BsonUtil.embeddedInt(document, "a", "b", "c2", 2, "i");
            assertTrue(c22.isPresent());
            assertEquals(2, c22.getAsInt());
            assertFalse(BsonUtil.embeddedInt(document, "a", "b", "c").isPresent());
            assertFalse(BsonUtil.embeddedInt(document, "a2").isPresent());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testEmbeddedLongBsonDocument() {
        var json = "{\"a\":{\"b\":{\"c1\":{\"d1\":1,\"d2\":\"2\",\"d3\":[1,2,3]},\"c2\":[{\"i\":0},{\"i\":1},{\"i\":2}]}}}";
        var document = BsonDocument.parse(json);
        try {
            var d1 = BsonUtil.embeddedLong(document, "a", "b", "c1", "d1");
            assertTrue(d1.isPresent());
            assertEquals(1, d1.getAsLong());
            var d30 = BsonUtil.embeddedLong(document, "a", "b", "c1", "d3", 0);
            assertTrue(d30.isPresent());
            assertEquals(1, d30.getAsLong());
            var d31 = BsonUtil.embeddedLong(document, "a", "b", "c1", "d3", 1);
            assertTrue(d31.isPresent());
            assertEquals(2, d31.getAsLong());
            var d32 = BsonUtil.embeddedLong(document, "a", "b", "c1", "d3", 2);
            assertTrue(d32.isPresent());
            assertEquals(3, d32.getAsLong());
            var c20 = BsonUtil.embeddedLong(document, "a", "b", "c2", 0, "i");
            assertTrue(c20.isPresent());
            assertEquals(0, c20.getAsLong());
            var c21 = BsonUtil.embeddedLong(document, "a", "b", "c2", 1, "i");
            assertTrue(c21.isPresent());
            assertEquals(1, c21.getAsLong());
            var c22 = BsonUtil.embeddedLong(document, "a", "b", "c2", 2, "i");
            assertTrue(c22.isPresent());
            assertEquals(2, c22.getAsLong());
            assertFalse(BsonUtil.embeddedLong(document, "a", "b", "c").isPresent());
            assertFalse(BsonUtil.embeddedLong(document, "a2").isPresent());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testEmbeddedDoubleBsonDocument() {
        var json = "{\"a\":{\"b\":{\"c1\":{\"d1\":1,\"d2\":\"2\",\"d3\":[1,2,3]},\"c2\":[{\"i\":0},{\"i\":1},{\"i\":2}]}}}";
        var document = BsonDocument.parse(json);
        try {
            var d1 = BsonUtil.embeddedDouble(document, "a", "b", "c1", "d1");
            assertTrue(d1.isPresent());
            assertEquals(1, d1.getAsDouble());
            var d30 = BsonUtil.embeddedDouble(document, "a", "b", "c1", "d3", 0);
            assertTrue(d30.isPresent());
            assertEquals(1, d30.getAsDouble());
            var d31 = BsonUtil.embeddedDouble(document, "a", "b", "c1", "d3", 1);
            assertTrue(d31.isPresent());
            assertEquals(2, d31.getAsDouble());
            var d32 = BsonUtil.embeddedDouble(document, "a", "b", "c1", "d3", 2);
            assertTrue(d32.isPresent());
            assertEquals(3, d32.getAsDouble());
            var c20 = BsonUtil.embeddedDouble(document, "a", "b", "c2", 0, "i");
            assertTrue(c20.isPresent());
            assertEquals(0, c20.getAsDouble());
            var c21 = BsonUtil.embeddedDouble(document, "a", "b", "c2", 1, "i");
            assertTrue(c21.isPresent());
            assertEquals(1, c21.getAsDouble());
            var c22 = BsonUtil.embeddedDouble(document, "a", "b", "c2", 2, "i");
            assertTrue(c22.isPresent());
            assertEquals(2, c22.getAsDouble());
            assertFalse(BsonUtil.embeddedDouble(document, "a", "b", "c").isPresent());
            assertFalse(BsonUtil.embeddedDouble(document, "a2").isPresent());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testEmbeddedDateTimeBsonDocument() {
        var json = "{\"a\":{\"b\":{\"c1\":{\"d1\":ISODate(\"2021-04-19T10:37:10.128Z\"),\"d2\":\"2\",\"d3\":[ISODate(\"2021-04-19T10:37:10.128Z\"),2,3]},\"c2\":[{\"i\":ISODate(\"2021-04-19T10:37:10.128Z\")},{\"i\":ISODate(\"2021-04-19T10:37:10.128Z\")},{\"i\":ISODate(\"2021-04-19T10:37:10.128Z\")}]}}}";
        var document = BsonDocument.parse(json);
        try {
            var zone = ZoneId.systemDefault();
            var time = ZonedDateTime.parse("2021-04-19T10:37:10.128Z").withZoneSameInstant(zone).toLocalDateTime();
            var d1 = BsonUtil.embeddedDateTime(document, "a", "b", "c1", "d1");
            assertTrue(d1.isPresent());
            assertEquals(time, d1.get());
            var d30 = BsonUtil.embeddedDateTime(document, "a", "b", "c1", "d3", 0);
            assertTrue(d30.isPresent());
            var c20 = BsonUtil.embeddedDateTime(document, "a", "b", "c2", 0, "i");
            assertTrue(c20.isPresent());
            assertEquals(time, c20.get());
            var c21 = BsonUtil.embeddedDateTime(document, "a", "b", "c2", 1, "i");
            assertTrue(c21.isPresent());
            assertEquals(time, c21.get());
            var c22 = BsonUtil.embeddedDateTime(document, "a", "b", "c2", 2, "i");
            assertTrue(c22.isPresent());
            assertEquals(time, c22.get());
            assertFalse(BsonUtil.embeddedDateTime(document, "a", "b", "c").isPresent());
            assertFalse(BsonUtil.embeddedDateTime(document, "a2").isPresent());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testEmbeddedZonedDateTimeBsonDocument() {
        var json = "{\"a\":{\"b\":{\"c1\":{\"d1\":ISODate(\"2021-04-19T10:37:10.128Z\"),\"d2\":\"2\",\"d3\":[ISODate(\"2021-04-19T10:37:10.128Z\"),2,3]},\"c2\":[{\"i\":ISODate(\"2021-04-19T10:37:10.128Z\")},{\"i\":ISODate(\"2021-04-19T10:37:10.128Z\")},{\"i\":ISODate(\"2021-04-19T10:37:10.128Z\")}]}}}";
        var document = BsonDocument.parse(json);
        try {
            var zone = ZoneId.of("Z");
            var time = ZonedDateTime.parse("2021-04-19T10:37:10.128Z");
            var d1 = BsonUtil.embeddedDateTime(zone, document, "a", "b", "c1", "d1");
            assertTrue(d1.isPresent());
            assertEquals(time, d1.get());
            var d30 = BsonUtil.embeddedDateTime(zone, document, "a", "b", "c1", "d3", 0);
            assertTrue(d30.isPresent());
            var c20 = BsonUtil.embeddedDateTime(zone, document, "a", "b", "c2", 0, "i");
            assertTrue(c20.isPresent());
            assertEquals(time, c20.get());
            var c21 = BsonUtil.embeddedDateTime(zone, document, "a", "b", "c2", 1, "i");
            assertTrue(c21.isPresent());
            assertEquals(time, c21.get());
            var c22 = BsonUtil.embeddedDateTime(zone, document, "a", "b", "c2", 2, "i");
            assertTrue(c22.isPresent());
            assertEquals(time, c22.get());
            assertFalse(BsonUtil.embeddedDateTime(zone, document, "a", "b", "c").isPresent());
            assertFalse(BsonUtil.embeddedDateTime(zone, document, "a2").isPresent());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testUuidValue() {
        var uuid = UUID.randomUUID();
        assertTrue(BsonUtil.uuidValue(new Document("uuid", uuid), "uuid").isPresent());
        assertEquals(uuid, BsonUtil.uuidValue(new Document("uuid", uuid), "uuid").orElseThrow());
        assertTrue(BsonUtil.uuidValue(new BsonDocument("uuid", new BsonBinary(uuid)), "uuid").isPresent());
        assertEquals(uuid, BsonUtil.uuidValue(new BsonDocument("uuid", new BsonBinary(uuid)), "uuid").orElseThrow());
        assertTrue(BsonUtil.uuidValue(new BsonDocument("uuid", new BsonBinary(uuid, UuidRepresentation.JAVA_LEGACY)), "uuid", UuidRepresentation.JAVA_LEGACY).isPresent());
        assertEquals(uuid, BsonUtil.uuidValue(new BsonDocument("uuid", new BsonBinary(uuid, UuidRepresentation.JAVA_LEGACY)), "uuid", UuidRepresentation.JAVA_LEGACY).orElseThrow());
    }

    @Test
    public void testUuidLegacyValue() {
        var uuid = UUID.randomUUID();
        assertTrue(BsonUtil.uuidLegacyValue(new BsonDocument("uuid", new BsonBinary(uuid, UuidRepresentation.JAVA_LEGACY)), "uuid").isPresent());
        assertEquals(uuid, BsonUtil.uuidLegacyValue(new BsonDocument("uuid", new BsonBinary(uuid, UuidRepresentation.JAVA_LEGACY)), "uuid").orElseThrow());
    }

    @Test
    public void testToBsonArray() {
        var list = List.of("a", "b", "c", "d", "e");
        var array = BsonUtil.toBsonArray(list, SingleValueTypes.STRING::toBsonValue);
        assertNotNull(array);
        assertEquals(5, array.size());
        assertEquals(new BsonString("a"), array.get(0));
        assertEquals(new BsonString("b"), array.get(1));
        assertEquals(new BsonString("c"), array.get(2));
        assertEquals(new BsonString("d"), array.get(3));
        assertEquals(new BsonString("e"), array.get(4));

        var list2 = new ArrayList<Integer>();
        list2.add(1);
        list2.add(null);
        list2.add(3);
        array = BsonUtil.toBsonArray(list2, SingleValueTypes.INTEGER::toBsonValue);
        assertNotNull(array);
        assertEquals(3, array.size());
        assertEquals(new BsonInt32(1), array.get(0));
        assertEquals(BsonNull.VALUE, array.get(1));
        assertEquals(new BsonInt32(3), array.get(2));

        array = BsonUtil.toBsonArray(new int[]{1, 2, 3});
        assertNotNull(array);
        assertEquals(3, array.size());
        assertEquals(new BsonInt32(1), array.get(0));
        assertEquals(new BsonInt32(2), array.get(1));
        assertEquals(new BsonInt32(3), array.get(2));

        array = BsonUtil.toBsonArray(new long[]{1, 2, 3});
        assertNotNull(array);
        assertEquals(3, array.size());
        assertEquals(new BsonInt64(1), array.get(0));
        assertEquals(new BsonInt64(2), array.get(1));
        assertEquals(new BsonInt64(3), array.get(2));

        array = BsonUtil.toBsonArray(new double[]{1, 2, 3});
        assertNotNull(array);
        assertEquals(3, array.size());
        assertEquals(new BsonDouble(1), array.get(0));
        assertEquals(new BsonDouble(2), array.get(1));
        assertEquals(new BsonDouble(3), array.get(2));
    }

    @Test
    public void testToBsonBinary() {
        var uuid = UUID.randomUUID();
        assertEquals(new BsonBinary(uuid, UuidRepresentation.STANDARD), BsonUtil.toBsonBinary(uuid));
    }

    @Test
    public void testToBsonBinaryUuidLegacy() {
        var uuid = UUID.randomUUID();
        assertEquals(new BsonBinary(uuid, UuidRepresentation.JAVA_LEGACY), BsonUtil.toBsonBinaryUuidLegacy(uuid));
    }

    @Test
    public void testToObjectNode() {
        var currentTimeMillis = System.currentTimeMillis();
        var objectId = new ObjectId();
        var uuid = UUID.randomUUID();
        var document = new BsonDocument("str", new BsonString("str"))
                .append("int", new BsonInt32(123))
                .append("long", new BsonInt64(1234567890123L))
                .append("double", new BsonDouble(1.2))
                .append("decimal128", new BsonDecimal128(Decimal128.parse("1234567890.123456789")))
                .append("boolean", BsonBoolean.FALSE)
                .append("null", BsonNull.VALUE)
                .append("date-time", new BsonDateTime(currentTimeMillis))
                .append("timestamp", new BsonTimestamp((int) (currentTimeMillis / 1000), 0))
                .append("object-id", new BsonObjectId(objectId))
                .append("uuid", new BsonBinary(uuid))
                .append("array", new BsonArray())
                .append("sub-document", new BsonDocument());
        var objectNode = BsonUtil.toObjectNode(document);
        assertNotNull(objectNode);
        assertEquals(13, objectNode.size());
        assertEquals("str", objectNode.get("str").textValue());
        assertEquals(123, objectNode.get("int").intValue());
        assertEquals(1234567890123L, objectNode.get("long").longValue());
        assertEquals(1.2, objectNode.get("double").doubleValue());
        assertEquals(new BigDecimal("1234567890.123456789"), objectNode.get("decimal128").decimalValue());
        assertFalse(objectNode.get("boolean").booleanValue());
        assertEquals(NullNode.instance, objectNode.get("null"));
        assertEquals(currentTimeMillis, objectNode.get("date-time").longValue());
        assertEquals(currentTimeMillis / 1000 * 1000, objectNode.get("timestamp").longValue());
        assertEquals(objectId.toHexString(), objectNode.get("object-id").textValue());
        assertEquals(uuid.toString(), objectNode.get("uuid").textValue());
        assertNotNull(objectNode.get("array"));
        assertTrue(objectNode.get("array").isArray());
        assertEquals(0, objectNode.get("array").size());
        assertNotNull(objectNode.get("sub-document"));
        assertTrue(objectNode.get("sub-document").isObject());
        assertEquals(0, objectNode.get("sub-document").size());
    }

    @Test
    public void testToArrayNode() {
        var currentTimeMillis = System.currentTimeMillis();
        var objectId = new ObjectId();
        var uuid = UUID.randomUUID();
        var array = new BsonArray();
        array.add(new BsonString("str"));
        array.add(new BsonInt32(123));
        array.add(new BsonInt64(1234567890123L));
        array.add(new BsonDouble(1.2));
        array.add(new BsonDecimal128(Decimal128.parse("1234567890.123456789")));
        array.add(BsonBoolean.FALSE);
        array.add(BsonNull.VALUE);
        array.add(new BsonDateTime(currentTimeMillis));
        array.add(new BsonTimestamp((int) (currentTimeMillis / 1000), 0));
        array.add(new BsonObjectId(objectId));
        array.add(new BsonBinary(uuid));
        array.add(new BsonArray());
        array.add(new BsonDocument());
        var arranNode = BsonUtil.toArrayNode(array);
        assertNotNull(arranNode);
        assertEquals(13, arranNode.size());
        assertEquals("str", arranNode.get(0).textValue());
        assertEquals(123, arranNode.get(1).intValue());
        assertEquals(1234567890123L, arranNode.get(2).longValue());
        assertEquals(1.2, arranNode.get(3).doubleValue());
        assertEquals(new BigDecimal("1234567890.123456789"), arranNode.get(4).decimalValue());
        assertFalse(arranNode.get(5).booleanValue());
        assertEquals(NullNode.instance, arranNode.get(6));
        assertEquals(currentTimeMillis, arranNode.get(7).longValue());
        assertEquals(currentTimeMillis / 1000 * 1000, arranNode.get(8).longValue());
        assertEquals(objectId.toHexString(), arranNode.get(9).textValue());
        assertEquals(uuid.toString(), arranNode.get(10).textValue());
        assertNotNull(arranNode.get(11));
        assertTrue(arranNode.get(11).isArray());
        assertEquals(0, arranNode.get(11).size());
        assertNotNull(arranNode.get(12));
        assertTrue(arranNode.get(12).isObject());
        assertEquals(0, arranNode.get(12).size());
    }

    @Test
    public void testToMap() {
        var currentTimeMillis = System.currentTimeMillis();
        var objectId = new ObjectId();
        var uuid = UUID.randomUUID();
        var document = new BsonDocument("str", new BsonString("str"))
                .append("int", new BsonInt32(123))
                .append("long", new BsonInt64(1234567890123L))
                .append("double", new BsonDouble(1.2))
                .append("decimal128", new BsonDecimal128(Decimal128.parse("1234567890.123456789")))
                .append("boolean", BsonBoolean.FALSE)
                .append("null", BsonNull.VALUE)
                .append("date-time", new BsonDateTime(currentTimeMillis))
                .append("timestamp", new BsonTimestamp((int) (currentTimeMillis / 1000), 0))
                .append("object-id", new BsonObjectId(objectId))
                .append("uuid", new BsonBinary(uuid))
                .append("array", new BsonArray())
                .append("sub-document", new BsonDocument());
        var map = BsonUtil.toMap(document);
        assertNotNull(map);
        assertEquals(13, map.size());
        assertEquals("str", map.get("str"));
        assertEquals(123, map.get("int"));
        assertEquals(1234567890123L, map.get("long"));
        assertEquals(1.2, map.get("double"));
        assertEquals(new BigDecimal("1234567890.123456789"), map.get("decimal128"));
        assertEquals(false, map.get("boolean"));
        assertTrue(map.containsKey("null"));
        assertNull(map.get("null"));
        assertEquals(currentTimeMillis, map.get("date-time"));
        assertEquals(currentTimeMillis / 1000 * 1000, map.get("timestamp"));
        assertEquals(objectId.toHexString(), map.get("object-id"));
        assertEquals(uuid.toString(), map.get("uuid"));
        assertNotNull(map.get("array"));
        assertInstanceOf(List.class, map.get("array"));
        assertEquals(0, ((List<?>) map.get("array")).size());
        assertNotNull(map.get("sub-document"));
        assertInstanceOf(Map.class, map.get("sub-document"));
        assertEquals(0, ((Map<?, ?>) map.get("sub-document")).size());
    }

    @Test
    public void testToList() {
        var currentTimeMillis = System.currentTimeMillis();
        var objectId = new ObjectId();
        var uuid = UUID.randomUUID();
        var array = new BsonArray();
        array.add(new BsonString("str"));
        array.add(new BsonInt32(123));
        array.add(new BsonInt64(1234567890123L));
        array.add(new BsonDouble(1.2));
        array.add(new BsonDecimal128(Decimal128.parse("1234567890.123456789")));
        array.add(BsonBoolean.FALSE);
        array.add(BsonNull.VALUE);
        array.add(new BsonDateTime(currentTimeMillis));
        array.add(new BsonTimestamp((int) (currentTimeMillis / 1000), 0));
        array.add(new BsonObjectId(objectId));
        array.add(new BsonBinary(uuid));
        array.add(new BsonArray());
        array.add(new BsonDocument());
        var list = BsonUtil.toList(array);
        assertNotNull(list);
        assertEquals(13, list.size());
        assertEquals("str", list.get(0));
        assertEquals(123, list.get(1));
        assertEquals(1234567890123L, list.get(2));
        assertEquals(1.2, list.get(3));
        assertEquals(new BigDecimal("1234567890.123456789"), list.get(4));
        assertEquals(false, list.get(5));
        assertNull(list.get(6));
        assertEquals(currentTimeMillis, list.get(7));
        assertEquals(currentTimeMillis / 1000 * 1000, list.get(8));
        assertEquals(objectId.toHexString(), list.get(9));
        assertEquals(uuid.toString(), list.get(10));
        assertNotNull(list.get(11));
        assertInstanceOf(List.class, list.get(11));
        assertEquals(0, ((List<?>) list.get(11)).size());
        assertNotNull(list.get(12));
        assertInstanceOf(Map.class, list.get(12));
        assertEquals(0, ((Map<?, ?>) list.get(12)).size());
    }

    @Test
    public void testJsonNodeToBsonDocument() {
        var jsonNode = JsonNodeFactory.instance.objectNode()
                .put("str", "str")
                .put("int", 123)
                .put("long", 1234567890123L)
                .put("double", new BigDecimal("1.2"))
                .put("decimal", new BigInteger("12345678901234567890"))
                .put("boolean", false)
                .putNull("null");
        jsonNode.putArray("array");
        jsonNode.putObject("object");
        var document = BsonUtil.toBsonDocument(jsonNode);
        assertEquals(9, document.size());
        assertEquals("str", document.getString("str").getValue());
        assertEquals(123, document.getInt32("int").getValue());
        assertEquals(1234567890123L, document.getInt64("long").getValue());
        assertEquals(1.2, document.getDouble("double").getValue());
        assertEquals(
                new BigDecimal(new BigInteger("12345678901234567890")),
                document.getDecimal128("decimal").getValue().bigDecimalValue()
        );
        assertFalse(document.getBoolean("boolean").getValue());
        assertEquals(BsonNull.VALUE, document.get("null"));
        assertEquals(0, document.getArray("array").size());
        assertEquals(0, document.getDocument("object").size());
    }

    @Test
    public void testJsonNodeToBsonArray() {
        var jsonNode = JsonNodeFactory.instance.arrayNode()
                .add("str")
                .add(123)
                .add(1234567890123L)
                .add(new BigDecimal("1.2"))
                .add(new BigInteger("12345678901234567890"))
                .add(false)
                .addNull();
        jsonNode.addArray();
        jsonNode.addObject();
        var array = BsonUtil.toBsonArray(jsonNode);
        assertEquals(9, array.size());
        assertEquals("str", array.get(0).asString().getValue());
        assertEquals(123, array.get(1).asInt32().getValue());
        assertEquals(1234567890123L, array.get(2).asInt64().getValue());
        assertEquals(1.2, array.get(3).asDouble().getValue());
        assertEquals(
                new BigDecimal(new BigInteger("12345678901234567890")),
                array.get(4).asDecimal128().getValue().bigDecimalValue()
        );
        assertFalse(array.get(5).asBoolean().getValue());
        assertEquals(BsonNull.VALUE, array.get(6));
        assertEquals(0, array.get(7).asArray().size());
        assertEquals(0, array.get(8).asDocument().size());
    }

}
