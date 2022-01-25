package com.github.fmjsjx.bson.model.generator;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.bson.BsonArray;
import org.bson.BsonDateTime;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonNull;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;

import com.github.fmjsjx.bson.model.core.BsonUtil;
import com.github.fmjsjx.bson.model.generator.model.Equipment;
import com.github.fmjsjx.bson.model.generator.model.GiftInfo;
import com.github.fmjsjx.bson.model.generator.model.Player;
import com.github.fmjsjx.libcommon.json.Jackson2Library;
import com.github.fmjsjx.libcommon.json.JsoniterLibrary;
import com.github.fmjsjx.libcommon.util.DateTimeUtil;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.mongodb.client.model.Updates;

public class TestModel {

    @Test
    public void testToBson() {
        try {
            var player = new Player();
            player.setUid(123);
            player.getWallet().setCoinTotal(5000);
            player.getWallet().setCoinUsed(200);
            player.getWallet().setDiamond(10);
            player.getEquipments().putIfAbsent("12345678-1234-5678-9abc-123456789abc", k -> {
                var equipment = new Equipment();
                equipment.setId(k);
                equipment.setRefId(1);
                equipment.setAtk(10);
                equipment.setDef(0);
                equipment.setHp(0);
                return equipment;
            });
            player.getItems().putAll(Map.of(2001, 5));
            player.getCash().setCards(List.of(1, 2, 3, 4));
            player.getCash().setOrderIds(List.of(0, 1, 2, 3, 4));
            var today = LocalDate.now();
            var now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
            player.getCash().setOrderDates(List.of(today.minusDays(1), today));
            player.getCash().setOrderTimes(List.of(now.minusDays(1), now));
            player.getCash().getTestDateMap().put(1, today);
            player.getCash().setTestSimpleSet(new LinkedHashSet<>(List.of(1, 2, 3, 4)));
            player.getCash().setTestSimpleSet2(new LinkedHashSet<>(List.of("a", "b", "c")));
            player.getCash().setTestSimpleSet3(new LinkedHashSet<>(List.of(today.minusDays(1), today)));
            player.getCash().setTestSimpleSet4(new LinkedHashSet<>(List.of(now.minusDays(1), now)));
            var g1 = new GiftInfo();
            g1.setId(1);
            g1.setPrice(100);
            g1.setCreateTime(now.minusDays(1));
            player.getGifts().append(g1);
            var g2 = new GiftInfo();
            g2.setId(2);
            g2.setPrice(200);
            g2.setCreateTime(now);
            player.getGifts().append(g2);
            player.setCreateTime(now);
            player.setUpdateTime(now);

            assertEquals(4800, player.getWallet().coin());
            assertEquals(LocalDate.now().minusDays(3), player.getWallet().ago(3));
            var znow = ZonedDateTime.now();
            assertEquals(znow, player.getWallet().testMethodCode(znow.toLocalDateTime(), ZoneId.systemDefault()));

            var bson = player.toBson();
            assertNotNull(bson);
            assertEquals(9, bson.size());
            assertEquals(123, bson.getInt32("_id").intValue());
            assertEquals(4, bson.getDocument("wt").size());
            assertEquals(5000, bson.getDocument("wt").getInt64("ct").intValue());
            assertEquals(200, bson.getDocument("wt").getInt64("cu").intValue());
            assertEquals(10, bson.getDocument("wt").getInt64("d").intValue());
            assertEquals(0, bson.getDocument("wt").getInt32("ad").intValue());
            assertEquals(1, bson.getDocument("eqm").size());
            var eqb = bson.getDocument("eqm").getDocument("12345678-1234-5678-9abc-123456789abc");
            assertNotNull(eqb);
            assertEquals(5, eqb.size());
            assertEquals("12345678-1234-5678-9abc-123456789abc", eqb.getString("id").getValue());
            assertEquals(1, eqb.getInt32("rid").getValue());
            assertEquals(10, eqb.getInt32("atk").getValue());
            assertEquals(0, eqb.getInt32("def").getValue());
            assertEquals(0, eqb.getInt32("hp").getValue());
            assertEquals(1, bson.getDocument("itm").size());
            assertEquals(5, bson.getDocument("itm").getInt32("2001").intValue());
            assertEquals(0, bson.getInt32("_uv").intValue());
            assertEquals(10, bson.getDocument("cs").size());
            assertEquals(0, bson.getDocument("cs").getDocument("stg").size());
            assertEquals(4, bson.getDocument("cs").getArray("cs").size());
            assertEquals(1, bson.getDocument("cs").getArray("cs").get(0).asInt32().getValue());
            assertEquals(2, bson.getDocument("cs").getArray("cs").get(1).asInt32().getValue());
            assertEquals(3, bson.getDocument("cs").getArray("cs").get(2).asInt32().getValue());
            assertEquals(4, bson.getDocument("cs").getArray("cs").get(3).asInt32().getValue());
            assertEquals(5, bson.getDocument("cs").getArray("ois").size());
            assertEquals(0, bson.getDocument("cs").getArray("ois").get(0).asInt32().getValue());
            assertEquals(1, bson.getDocument("cs").getArray("ois").get(1).asInt32().getValue());
            assertEquals(2, bson.getDocument("cs").getArray("ois").get(2).asInt32().getValue());
            assertEquals(3, bson.getDocument("cs").getArray("ois").get(3).asInt32().getValue());
            assertEquals(4, bson.getDocument("cs").getArray("ois").get(4).asInt32().getValue());
            assertEquals(2, bson.getDocument("cs").getArray("ods").size());
            assertEquals(DateTimeUtil.toNumber(today.minusDays(1)),
                    bson.getDocument("cs").getArray("ods").get(0).asInt32().getValue());
            assertEquals(DateTimeUtil.toNumber(today),
                    bson.getDocument("cs").getArray("ods").get(1).asInt32().getValue());
            assertEquals(2, bson.getDocument("cs").getArray("ots").size());
            assertEquals(DateTimeUtil.toEpochMilli(now.minusDays(1)),
                    bson.getDocument("cs").getArray("ots").get(0).asDateTime().getValue());
            assertEquals(DateTimeUtil.toEpochMilli(now),
                    bson.getDocument("cs").getArray("ots").get(1).asDateTime().getValue());
            assertEquals(1, bson.getDocument("cs").getDocument("tdm").size());
            assertEquals(DateTimeUtil.toNumber(today),
                    bson.getDocument("cs").getDocument("tdm").getInt32("1").getValue());
            assertEquals(4, bson.getDocument("cs").getArray("tss").size());
            assertEquals(1, bson.getDocument("cs").getArray("tss").get(0).asInt32().getValue());
            assertEquals(2, bson.getDocument("cs").getArray("tss").get(1).asInt32().getValue());
            assertEquals(3, bson.getDocument("cs").getArray("tss").get(2).asInt32().getValue());
            assertEquals(4, bson.getDocument("cs").getArray("tss").get(3).asInt32().getValue());
            assertEquals(3, bson.getDocument("cs").getArray("tss2").size());
            assertEquals("a", bson.getDocument("cs").getArray("tss2").get(0).asString().getValue());
            assertEquals("b", bson.getDocument("cs").getArray("tss2").get(1).asString().getValue());
            assertEquals("c", bson.getDocument("cs").getArray("tss2").get(2).asString().getValue());
            assertEquals(2, bson.getDocument("cs").getArray("tss3").size());
            assertEquals(DateTimeUtil.toNumber(today.minusDays(1)),
                    bson.getDocument("cs").getArray("tss3").get(0).asInt32().getValue());
            assertEquals(DateTimeUtil.toNumber(today),
                    bson.getDocument("cs").getArray("tss3").get(1).asInt32().getValue());
            assertEquals(2, bson.getDocument("cs").getArray("tss4").size());
            assertEquals(DateTimeUtil.toEpochMilli(now.minusDays(1)),
                    bson.getDocument("cs").getArray("tss4").get(0).asDateTime().getValue());
            assertEquals(DateTimeUtil.toEpochMilli(now),
                    bson.getDocument("cs").getArray("tss4").get(1).asDateTime().getValue());

            assertEquals(2, bson.getArray("gfs").size());
            assertEquals(1, bson.getArray("gfs").get(0).asDocument().getInt32("id").getValue());
            assertEquals(100, bson.getArray("gfs").get(0).asDocument().getInt32("prc").getValue());
            assertEquals(DateTimeUtil.toEpochMilli(now.minusDays(1)),
                    bson.getArray("gfs").get(0).asDocument().getDateTime("ct").getValue());
            assertEquals(2, bson.getArray("gfs").get(1).asDocument().getInt32("id").getValue());
            assertEquals(200, bson.getArray("gfs").get(1).asDocument().getInt32("prc").getValue());
            assertEquals(DateTimeUtil.toEpochMilli(now),
                    bson.getArray("gfs").get(1).asDocument().getDateTime("ct").getValue());
            var zone = ZoneId.systemDefault();
            assertEquals(now, LocalDateTime.ofInstant(Instant.ofEpochMilli(bson.getDateTime("_ct").getValue()), zone));
            assertEquals(now, LocalDateTime.ofInstant(Instant.ofEpochMilli(bson.getDateTime("_ut").getValue()), zone));

            player.getCash().setTestDate(today);
            player.getGifts().remove(0);

            bson = player.toBson();
            assertNotNull(bson);
            assertEquals(9, bson.size());
            assertEquals(11, bson.getDocument("cs").size());
            assertEquals(DateTimeUtil.toNumber(today), bson.getDocument("cs").getInt32("tsd").getValue());
            assertEquals(BsonNull.VALUE, bson.getArray("gfs").get(0));
            assertEquals(now, LocalDateTime.ofInstant(Instant.ofEpochMilli(bson.getDateTime("_ct").getValue()), zone));
            assertEquals(now, LocalDateTime.ofInstant(Instant.ofEpochMilli(bson.getDateTime("_ut").getValue()), zone));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testToDocument() {
        try {
            var player = new Player();
            player.setUid(123);
            player.getWallet().setCoinTotal(5000);
            player.getWallet().setCoinUsed(200);
            player.getWallet().setDiamond(10);
            player.getEquipments().putIfAbsent("12345678-1234-5678-9abc-123456789abc", k -> {
                var equipment = new Equipment();
                equipment.setId(k);
                equipment.setRefId(1);
                equipment.setAtk(10);
                equipment.setDef(0);
                equipment.setHp(0);
                return equipment;
            });
            player.getItems().putAll(Map.of(2001, 5));
            player.getCash().setCards(List.of(1, 2, 3, 4));
            player.getCash().setOrderIds(List.of(0, 1, 2, 3, 4));
            var today = LocalDate.now();
            var now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
            player.getCash().setOrderDates(List.of(today.minusDays(1), today));
            player.getCash().setOrderTimes(List.of(now.minusDays(1), now));
            player.getCash().getTestDateMap().put(1, today);
            player.getCash().setTestSimpleSet(new LinkedHashSet<>(List.of(1, 2, 3, 4)));
            player.getCash().setTestSimpleSet2(new LinkedHashSet<>(List.of("a", "b", "c")));
            player.getCash().setTestSimpleSet3(new LinkedHashSet<>(List.of(today.minusDays(1), today)));
            player.getCash().setTestSimpleSet4(new LinkedHashSet<>(List.of(now.minusDays(1), now)));
            var g1 = new GiftInfo();
            g1.setId(1);
            g1.setPrice(100);
            g1.setCreateTime(now.minusDays(1));
            player.getGifts().append(g1);
            var g2 = new GiftInfo();
            g2.setId(2);
            g2.setPrice(200);
            g2.setCreateTime(now);
            player.getGifts().append(g2);
            player.setCreateTime(now);
            player.setUpdateTime(now);

            var doc = player.toDocument();
            assertNotNull(doc);
            assertEquals(9, doc.size());
            assertEquals(123, BsonUtil.intValue(doc, "_id").getAsInt());
            assertEquals(4, BsonUtil.documentValue(doc, "wt").get().size());
            assertEquals(5000, BsonUtil.embeddedInt(doc, "wt", "ct").getAsInt());
            assertEquals(200, BsonUtil.embeddedInt(doc, "wt", "cu").getAsInt());
            assertEquals(10, BsonUtil.embeddedInt(doc, "wt", "d").getAsInt());
            assertEquals(0, BsonUtil.embeddedInt(doc, "wt", "ad").getAsInt());
            assertEquals(1, BsonUtil.documentValue(doc, "eqm").get().size());
            var eqb = BsonUtil.embeddedDocument(doc, "eqm", "12345678-1234-5678-9abc-123456789abc").get();
            assertNotNull(eqb);
            assertEquals(5, eqb.size());
            assertEquals("12345678-1234-5678-9abc-123456789abc", eqb.getString("id"));
            assertEquals(1, BsonUtil.intValue(eqb, "rid").getAsInt());
            assertEquals(10, BsonUtil.intValue(eqb, "atk").getAsInt());
            assertEquals(0, BsonUtil.intValue(eqb, "def").getAsInt());
            assertEquals(0, BsonUtil.intValue(eqb, "hp").getAsInt());
            assertEquals(1, BsonUtil.documentValue(doc, "itm").get().size());
            assertEquals(5, BsonUtil.embeddedInt(doc, "itm", "2001").getAsInt());
            assertEquals(0, BsonUtil.intValue(doc, "_uv").getAsInt());
            assertEquals(10, BsonUtil.embeddedDocument(doc, "cs").get().size());
            assertEquals(0, BsonUtil.embeddedDocument(doc, "cs", "stg").get().size());
            assertEquals(4, BsonUtil.embeddedList(doc, "cs", "cs").get().size());
            assertEquals(1, BsonUtil.embeddedInt(doc, "cs", "cs", 0).getAsInt());
            assertEquals(2, BsonUtil.embeddedInt(doc, "cs", "cs", 1).getAsInt());
            assertEquals(3, BsonUtil.embeddedInt(doc, "cs", "cs", 2).getAsInt());
            assertEquals(4, BsonUtil.embeddedInt(doc, "cs", "cs", 3).getAsInt());
            assertEquals(5, BsonUtil.embeddedList(doc, "cs", "ois").get().size());
            assertEquals(0, BsonUtil.embeddedInt(doc, "cs", "ois", 0).getAsInt());
            assertEquals(1, BsonUtil.embeddedInt(doc, "cs", "ois", 1).getAsInt());
            assertEquals(2, BsonUtil.embeddedInt(doc, "cs", "ois", 2).getAsInt());
            assertEquals(3, BsonUtil.embeddedInt(doc, "cs", "ois", 3).getAsInt());
            assertEquals(4, BsonUtil.embeddedInt(doc, "cs", "ois", 4).getAsInt());
            assertEquals(2, BsonUtil.embeddedList(doc, "cs", "ods").get().size());
            assertEquals(DateTimeUtil.toNumber(today.minusDays(1)),
                    BsonUtil.embeddedInt(doc, "cs", "ods", 0).getAsInt());
            assertEquals(DateTimeUtil.toNumber(today), BsonUtil.embeddedInt(doc, "cs", "ods", 1).getAsInt());
            assertEquals(2, BsonUtil.embeddedList(doc, "cs", "ots").get().size());
            assertEquals(now.minusDays(1), BsonUtil.embeddedDateTime(doc, "cs", "ots", 0).get());
            assertEquals(now, BsonUtil.embeddedDateTime(doc, "cs", "ots", 1).get());
            assertEquals(DateTimeUtil.toNumber(today), BsonUtil.embeddedInt(doc, "cs", "tdm", "1").getAsInt());
            assertEquals(4, BsonUtil.embeddedList(doc, "cs", "tss").get().size());
            assertEquals(1, BsonUtil.embeddedInt(doc, "cs", "tss", 0).getAsInt());
            assertEquals(2, BsonUtil.embeddedInt(doc, "cs", "tss", 1).getAsInt());
            assertEquals(3, BsonUtil.embeddedInt(doc, "cs", "tss", 2).getAsInt());
            assertEquals(4, BsonUtil.embeddedInt(doc, "cs", "tss", 3).getAsInt());
            assertEquals(3, BsonUtil.embeddedList(doc, "cs", "tss2").get().size());
            assertEquals("a", BsonUtil.embedded(doc, "cs", "tss2", 0).get());
            assertEquals("b", BsonUtil.embedded(doc, "cs", "tss2", 1).get());
            assertEquals("c", BsonUtil.embedded(doc, "cs", "tss2", 2).get());
            assertEquals(2, BsonUtil.embeddedList(doc, "cs", "tss3").get().size());
            assertEquals(DateTimeUtil.toNumber(today.minusDays(1)),
                    BsonUtil.embeddedInt(doc, "cs", "tss3", 0).getAsInt());
            assertEquals(DateTimeUtil.toNumber(today), BsonUtil.embeddedInt(doc, "cs", "tss3", 1).getAsInt());
            assertEquals(2, BsonUtil.embeddedList(doc, "cs", "tss4").get().size());
            assertEquals(now.minusDays(1), BsonUtil.embeddedDateTime(doc, "cs", "tss4", 0).get());
            assertEquals(now, BsonUtil.embeddedDateTime(doc, "cs", "tss4", 1).get());

            assertEquals(2, BsonUtil.embeddedList(doc, "gfs").get().size());
            assertEquals(1, BsonUtil.embeddedInt(doc, "gfs", 0, "id").getAsInt());
            assertEquals(100, BsonUtil.embeddedInt(doc, "gfs", 0, "prc").getAsInt());
            assertEquals(now.minusDays(1), BsonUtil.embeddedDateTime(doc, "gfs", 0, "ct").get());
            assertEquals(2, BsonUtil.embeddedInt(doc, "gfs", 1, "id").getAsInt());
            assertEquals(200, BsonUtil.embeddedInt(doc, "gfs", 1, "prc").getAsInt());
            assertEquals(now, BsonUtil.embeddedDateTime(doc, "gfs", 1, "ct").get());
            var zone = ZoneId.systemDefault();
            assertEquals(now, LocalDateTime.ofInstant(doc.getDate("_ct").toInstant(), zone));
            assertEquals(now, LocalDateTime.ofInstant(doc.getDate("_ut").toInstant(), zone));

            player.getCash().setTestDate(today);
            player.getGifts().remove(0);

            doc = player.toDocument();
            assertNotNull(doc);
            assertEquals(9, doc.size());
            assertEquals(11, BsonUtil.embeddedDocument(doc, "cs").get().size());
            assertEquals(DateTimeUtil.toNumber(today), BsonUtil.embeddedInt(doc, "cs", "tsd").getAsInt());
            assertNull(BsonUtil.embedded(doc, "gts", 0).orElse(null));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testToData() {
        try {
            var player = new Player();
            player.setUid(123);
            player.getWallet().setCoinTotal(5000);
            player.getWallet().setCoinUsed(200);
            player.getWallet().setDiamond(10);
            player.getEquipments().putIfAbsent("12345678-1234-5678-9abc-123456789abc", k -> {
                var equipment = new Equipment();
                equipment.setId(k);
                equipment.setRefId(1);
                equipment.setAtk(10);
                equipment.setDef(0);
                equipment.setHp(0);
                return equipment;
            });
            player.getItems().putAll(Map.of(2001, 5));
            player.getCash().setCards(List.of(1, 2, 3, 4));
            player.getCash().setOrderIds(List.of(0, 1, 2, 3, 4));
            var today = LocalDate.now();
            var now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
            player.getCash().setOrderDates(List.of(today.minusDays(1), today));
            player.getCash().setOrderTimes(List.of(now.minusDays(1), now));
            player.getCash().getTestDateMap().put(1, today);
            player.getCash().setTestSimpleSet(new LinkedHashSet<>(List.of(1, 2, 3, 4)));
            player.getCash().setTestSimpleSet2(new LinkedHashSet<>(List.of("a", "b", "c")));
            player.getCash().setTestSimpleSet3(new LinkedHashSet<>(List.of(today.minusDays(1), today)));
            player.getCash().setTestSimpleSet4(new LinkedHashSet<>(List.of(now.minusDays(1), now)));
            var g1 = new GiftInfo();
            g1.setId(1);
            g1.setPrice(100);
            g1.setCreateTime(now.minusDays(1));
            player.getGifts().append(g1);
            var g2 = new GiftInfo();
            g2.setId(2);
            g2.setPrice(200);
            g2.setCreateTime(now);
            player.getGifts().append(g2);
            player.setCreateTime(now);
            player.setUpdateTime(now);

            var data = player.toData();
            assertEquals(9, data.size());
            var json = Jackson2Library.getInstance().dumpsToString(data);
            var map = new LinkedHashMap<String, Object>();
            map.put("_id", 123);
            var wt = new LinkedHashMap<String, Object>();
            map.put("wt", wt);
            wt.put("ct", 5000);
            wt.put("cu", 200);
            wt.put("d", 10);
            wt.put("ad", 0);
            var eqm = new LinkedHashMap<String, Object>();
            map.put("eqm", eqm);
            var eq = new LinkedHashMap<String, Object>();
            eqm.put("12345678-1234-5678-9abc-123456789abc", eq);
            eq.put("id", "12345678-1234-5678-9abc-123456789abc");
            eq.put("rid", 1);
            eq.put("atk", 10);
            eq.put("def", 0);
            eq.put("hp", 0);
            var itm = new LinkedHashMap<Integer, Object>();
            map.put("itm", itm);
            itm.put(2001, 5);
            var cs = new LinkedHashMap<String, Object>();
            map.put("cs", cs);
            cs.put("stg", new LinkedHashMap<>());
            cs.put("cs", List.of(1, 2, 3, 4));
            cs.put("ois", List.of(0, 1, 2, 3, 4));
            cs.put("ods", List.of(DateTimeUtil.toNumber(today.minusDays(1)), DateTimeUtil.toNumber(today)));
            cs.put("ots", List.of(DateTimeUtil.toEpochMilli(now.minusDays(1)), DateTimeUtil.toEpochMilli(now)));
            var tdm = new LinkedHashMap<String, Object>();
            cs.put("tdm", tdm);
            tdm.put("1", DateTimeUtil.toNumber(today));
            cs.put("tss", List.of(1, 2, 3, 4));
            cs.put("tss2", List.of("a", "b", "c"));
            cs.put("tss3", List.of(DateTimeUtil.toNumber(today.minusDays(1)), DateTimeUtil.toNumber(today)));
            cs.put("tss4", List.of(DateTimeUtil.toEpochMilli(now.minusDays(1)), DateTimeUtil.toEpochMilli(now)));
            var gfs = new ArrayList<>();
            map.put("gfs", gfs);
            var gm0 = new LinkedHashMap<>();
            gm0.put("id", 1);
            gm0.put("prc", 100);
            gm0.put("ct", DateTimeUtil.toEpochMilli(now.minusDays(1)));
            var gm1 = new LinkedHashMap<>();
            gm1.put("id", 2);
            gm1.put("prc", 200);
            gm1.put("ct", DateTimeUtil.toEpochMilli(now));
            gfs.add(gm0);
            gfs.add(gm1);
            map.put("_uv", 0);
            map.put("_ct", DateTimeUtil.toEpochMilli(now));
            map.put("_ut", DateTimeUtil.toEpochMilli(now));
            var expected = Jackson2Library.getInstance().dumpsToString(map);
            assertEquals(expected, json);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testLoadDocument() {
        try {
            var date = new Date();
            var now = DateTimeUtil.local(date);
            var today = now.toLocalDate();
            var doc = new Document().append("_id", 123) // uid
                    .append("wt", new Document("ct", 5000).append("cu", 200).append("d", 10).append("ad", 2)) // wallet
                    .append("eqm", // equipments
                            new Document("12345678-1234-5678-9abc-123456789abc",
                                    new Document("id", "12345678-1234-5678-9abc-123456789abc").append("rid", 1)
                                            .append("atk", 12).append("def", 2).append("hp", 100)) // equipment end
                    ) // equipments end
                    .append("itm", new Document("2001", 10)) // items
                    .append("cs", // cash
                            new Document("stg", new Document()) // stages
                                    .append("cs", new ArrayList<>(List.of(1, 2, 3, 4))) // cards
                                    .append("ois", new ArrayList<>(List.of(0, 1, 2, 3, 4))) // orderIds
                                    .append("ods", // orderDates
                                            new ArrayList<>(List.of(DateTimeUtil.toNumber(today.minusDays(1)),
                                                    DateTimeUtil.toNumber(today))))
                                    .append("ots", // orderTimes
                                            new ArrayList<>(List.of(DateTimeUtil.toLegacyDate(now.minusDays(1)), date)))
                                    .append("tsd", 20210712) // testDate
                                    .append("tdm", new Document("1", 20210712)) // testDateMap
                                    .append("tss", new ArrayList<>(List.of(1, 2, 3, 4))) // testSimpleSet
                                    .append("tss2", new ArrayList<>(List.of("a", "b", "c"))) // testSimpleSet2
                                    .append("tss3", // testSimpleSet3
                                            new ArrayList<>(List.of(DateTimeUtil.toNumber(today.minusDays(1)),
                                                    DateTimeUtil.toNumber(today))))
                                    .append("tss4", // testSimpleSet4
                                            new ArrayList<>(List.of(DateTimeUtil.toLegacyDate(now.minusDays(1)), date))) //
                    ) // cash end
                    .append("gfs", // gifts
                            new ArrayList<>(Arrays.asList(null, // 0
                                    new Document("id", 2).append("prc", 200).append("ct",
                                            DateTimeUtil.toLegacyDate(now)) // 1
                            ))) // gifts end
                    .append("_uv", 1) // update version
                    .append("_ct", date) // create time
                    .append("_ut", date); // update time
            var player = new Player();
            player.load(doc);
            assertFalse(player.updated());
            assertEquals(123, player.getUid());
            assertEquals(player, player.getWallet().parent());
            assertEquals(5000, player.getWallet().getCoinTotal());
            assertEquals(200, player.getWallet().getCoinUsed());
            assertEquals(4800, player.getWallet().getCoin());
            assertEquals(10, player.getWallet().getDiamond());
            assertEquals(2, player.getWallet().getAd());
            assertEquals(1, player.getEquipments().size());
            assertEquals(player, player.getEquipments().parent());
            var eq = player.getEquipments().get("12345678-1234-5678-9abc-123456789abc");
            assertTrue(eq.isPresent());
            assertEquals(player.getEquipments(), eq.get().parent());
            assertEquals("12345678-1234-5678-9abc-123456789abc", eq.get().key());
            assertEquals("12345678-1234-5678-9abc-123456789abc", eq.get().getId());
            assertEquals(1, eq.get().getRefId());
            assertEquals(12, eq.get().getAtk());
            assertEquals(2, eq.get().getDef());
            assertEquals(100, eq.get().getHp());
            assertEquals(1, player.getItems().size());
            assertEquals(player, player.getItems().parent());
            assertEquals(10, player.getItems().get(2001).get());
            assertEquals(0, player.getCash().getStages().size());
            assertEquals("cs.stg.1", player.getCash().getStages().xpath().resolve("1").value());
            assertNotNull(player.getCash().getCards());
            assertEquals(4, player.getCash().getCards().size());
            assertArrayEquals(new int[] { 1, 2, 3, 4 },
                    player.getCash().getCards().stream().mapToInt(Integer::intValue).toArray());
            assertNotNull(player.getCash().getOrderIds());
            assertEquals(5, player.getCash().getOrderIds().size());
            assertArrayEquals(new int[] { 0, 1, 2, 3, 4 },
                    player.getCash().getOrderIds().stream().mapToInt(Integer::intValue).toArray());
            assertNotNull(player.getCash().getOrderDates());
            assertEquals(2, player.getCash().getOrderDates().size());
            assertEquals(today.minusDays(1), player.getCash().getOrderDates().get(0));
            assertEquals(today, player.getCash().getOrderDates().get(1));
            assertNotNull(player.getCash().getOrderTimes());
            assertEquals(2, player.getCash().getOrderTimes().size());
            assertEquals(now.minusDays(1), player.getCash().getOrderTimes().get(0));
            assertEquals(now, player.getCash().getOrderTimes().get(1));
            assertEquals(LocalDate.of(2021, 7, 12), player.getCash().getTestDate());
            assertEquals(1, player.getCash().getTestDateMap().size());
            assertEquals(LocalDate.of(2021, 7, 12), player.getCash().getTestDateMap().get(1).get());
            assertNotNull(player.getCash().getTestSimpleSet());
            assertEquals(4, player.getCash().getTestSimpleSet().size());
            assertArrayEquals(new int[] { 1, 2, 3, 4 },
                    player.getCash().getTestSimpleSet().stream().mapToInt(Integer::intValue).sorted().toArray());
            assertNotNull(player.getCash().getTestSimpleSet2());
            assertEquals(3, player.getCash().getTestSimpleSet2().size());
            assertArrayEquals(new String[] { "a", "b", "c" },
                    player.getCash().getTestSimpleSet2().stream().sorted().toArray(String[]::new));
            assertNotNull(player.getCash().getTestSimpleSet3());
            assertEquals(2, player.getCash().getTestSimpleSet3().size());
            assertArrayEquals(new Object[] { today.minusDays(1), today },
                    player.getCash().getTestSimpleSet3().stream().sorted().toArray());
            assertNotNull(player.getCash().getTestSimpleSet4());
            assertEquals(2, player.getCash().getTestSimpleSet4().size());
            assertArrayEquals(new Object[] { now.minusDays(1), now },
                    player.getCash().getTestSimpleSet4().stream().sorted().toArray());
            assertFalse(player.getGifts().nil());
            assertEquals(2, player.getGifts().size());
            assertTrue(player.getGifts().value(0).isEmpty());
            assertTrue(player.getGifts().value(1).isPresent());
            assertEquals(2, player.getGifts().value(1).get().getId());
            assertEquals(200, player.getGifts().value(1).get().getPrice());
            assertEquals(now, player.getGifts().value(1).get().getCreateTime());
            assertEquals(1, player.getUpdateVersion());

            var zone = ZoneId.systemDefault();
            assertEquals(date, Date.from(player.getCreateTime().atZone(zone).toInstant()));
            assertEquals(date, Date.from(player.getUpdateTime().atZone(zone).toInstant()));

            doc = new Document().append("_id", 125) // uid
                    .append("wt", new Document("ct", 5200).append("cu", 200).append("d", 10).append("ad", 2)) // wallet
                    .append("eqm", // equipments
                            new Document("87654321-1234-5678-9abc-123456789abc",
                                    new Document("id", "87654321-1234-5678-9abc-123456789abc").append("rid", 1)
                                            .append("atk", 16).append("def", 2).append("hp", 100)))
                    .append("itm", new Document("2001", 10)) // items
                    .append("cs", new Document("stg", new Document())) // cash
                    .append("_uv", 1) // update version
                    .append("_ct", date) // create time
                    .append("_ut", date); // update time

            player.load(doc);

            assertFalse(player.updated());
            assertEquals(125, player.getUid());
            assertEquals(player, player.getWallet().parent());
            assertEquals(5200, player.getWallet().getCoinTotal());
            assertEquals(200, player.getWallet().getCoinUsed());
            assertEquals(5000, player.getWallet().getCoin());
            assertEquals(10, player.getWallet().getDiamond());
            assertEquals(2, player.getWallet().getAd());
            assertEquals(1, player.getEquipments().size());
            assertEquals(player, player.getEquipments().parent());

            eq = player.getEquipments().get("87654321-1234-5678-9abc-123456789abc");
            assertTrue(eq.isPresent());
            assertEquals(player.getEquipments(), eq.get().parent());
            assertEquals("87654321-1234-5678-9abc-123456789abc", eq.get().key());
            assertEquals("87654321-1234-5678-9abc-123456789abc", eq.get().getId());
            assertEquals(1, eq.get().getRefId());
            assertEquals(16, eq.get().getAtk());
            assertEquals(2, eq.get().getDef());
            assertEquals(100, eq.get().getHp());
            assertEquals(1, player.getItems().size());
            assertEquals(player, player.getItems().parent());
            assertEquals(10, player.getItems().get(2001).get());
            assertEquals(0, player.getCash().getStages().size());
            assertEquals("cs.stg.1", player.getCash().getStages().xpath().resolve("1").value());
            assertNull(player.getCash().getCards());
            assertNull(player.getCash().getOrderIds());
            assertNull(player.getCash().getTestDate());
            assertNull(player.getCash().getOrderDates());
            assertNull(player.getCash().getOrderTimes());
            assertEquals(0, player.getCash().getTestDateMap().size());
            assertTrue(player.getGifts().nil());
            assertEquals(1, player.getUpdateVersion());

            assertEquals(date, Date.from(player.getCreateTime().atZone(zone).toInstant()));
            assertEquals(date, Date.from(player.getUpdateTime().atZone(zone).toInstant()));

        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testLoadBsonDocument() {
        try {
            var date = new Date();
            var now = DateTimeUtil.local(date);
            var today = now.toLocalDate();
            var doc = new BsonDocument().append("_id", new BsonInt32(123)) // uid
                    .append("wt",
                            new BsonDocument("ct", new BsonInt64(5000)).append("cu", new BsonInt64(200))
                                    .append("d", new BsonInt64(10)).append("ad", new BsonInt32(2))) // wallet
                    .append("eqm", // equipments
                            new BsonDocument("12345678-1234-5678-9abc-123456789abc",
                                    new BsonDocument("id", new BsonString("12345678-1234-5678-9abc-123456789abc"))
                                            .append("rid", new BsonInt32(1)).append("atk", new BsonInt32(12))
                                            .append("def", new BsonInt32(2)).append("hp", new BsonInt32(100))))
                    .append("itm", new BsonDocument("2001", new BsonInt32(10))) // items
                    .append("cs", // cash
                            new BsonDocument("stg", new BsonDocument()) // stages
                                    .append("cs", // cards
                                            new BsonArray(List.of(new BsonInt32(1), new BsonInt32(2), new BsonInt32(3),
                                                    new BsonInt32(4))))
                                    .append("ois", // orderIds
                                            new BsonArray(List.of(new BsonInt32(0), new BsonInt32(1), new BsonInt32(2),
                                                    new BsonInt32(3), new BsonInt32(4))))
                                    .append("ods", // orderDates
                                            new BsonArray(List.of(
                                                    new BsonInt32(DateTimeUtil.toNumber(today.minusDays(1))),
                                                    new BsonInt32(DateTimeUtil.toNumber(today)))))
                                    .append("ots", // orderTimes
                                            new BsonArray(List.of(
                                                    new BsonDateTime(DateTimeUtil.toEpochMilli(now.minusDays(1))),
                                                    new BsonDateTime(DateTimeUtil.toEpochMilli(now)))))
                                    .append("tsd", new BsonInt32(20210712)) // testDate
                                    .append("tdm", new BsonDocument("1", new BsonInt32(20210712))) // testDateMap
                                    .append("tss", // testSimpleSet
                                            new BsonArray(List.of(new BsonInt32(1), new BsonInt32(2), new BsonInt32(3),
                                                    new BsonInt32(4))))
                                    .append("tss2", // testSimpleSet2
                                            new BsonArray(List
                                                    .of(new BsonString("a"), new BsonString("b"), new BsonString("c"))))
                                    .append("tss3", // testSimpleSet3
                                            new BsonArray(
                                                    List.of(new BsonInt32(DateTimeUtil.toNumber(today.minusDays(1))),
                                                            new BsonInt32(DateTimeUtil.toNumber(today)))))
                                    .append("tss4", // testSimpleSet4
                                            new BsonArray(List.of(
                                                    new BsonDateTime(DateTimeUtil.toEpochMilli(now.minusDays(1))),
                                                    new BsonDateTime(DateTimeUtil.toEpochMilli(now))))) //
                    ) // cash end
                    .append("gfs", // gifts
                            new BsonArray(List.of(BsonNull.VALUE, // 0
                                    new BsonDocument("id", new BsonInt32(2)).append("prc", new BsonInt32(200))
                                            .append("ct", new BsonDateTime(DateTimeUtil.toEpochMilli(now))) // 1
                            ))) // gifts end
                    .append("_uv", new BsonInt32(1)) // update version
                    .append("_ct", new BsonDateTime(date.getTime())) // create time
                    .append("_ut", new BsonDateTime(date.getTime())); // update time
            var player = new Player();
            player.load(doc);

            assertFalse(player.updated());
            assertEquals(123, player.getUid());
            assertEquals(player, player.getWallet().parent());
            assertEquals(5000, player.getWallet().getCoinTotal());
            assertEquals(200, player.getWallet().getCoinUsed());
            assertEquals(4800, player.getWallet().getCoin());
            assertEquals(10, player.getWallet().getDiamond());
            assertEquals(2, player.getWallet().getAd());
            assertEquals(1, player.getEquipments().size());
            assertEquals(player, player.getEquipments().parent());
            var eq = player.getEquipments().get("12345678-1234-5678-9abc-123456789abc");
            assertTrue(eq.isPresent());
            assertEquals(player.getEquipments(), eq.get().parent());
            assertEquals("12345678-1234-5678-9abc-123456789abc", eq.get().key());
            assertEquals("12345678-1234-5678-9abc-123456789abc", eq.get().getId());
            assertEquals(1, eq.get().getRefId());
            assertEquals(12, eq.get().getAtk());
            assertEquals(2, eq.get().getDef());
            assertEquals(100, eq.get().getHp());
            assertEquals(1, player.getItems().size());
            assertEquals(player, player.getItems().parent());
            assertEquals(10, player.getItems().get(2001).get());
            assertEquals(0, player.getCash().getStages().size());
            assertEquals("cs.stg.1", player.getCash().getStages().xpath().resolve("1").value());
            assertNotNull(player.getCash().getCards());
            assertEquals(4, player.getCash().getCards().size());
            assertArrayEquals(new int[] { 1, 2, 3, 4 },
                    player.getCash().getCards().stream().mapToInt(Integer::intValue).toArray());
            assertNotNull(player.getCash().getOrderIds());
            assertEquals(5, player.getCash().getOrderIds().size());
            assertArrayEquals(new int[] { 0, 1, 2, 3, 4 },
                    player.getCash().getOrderIds().stream().mapToInt(Integer::intValue).toArray());
            assertNotNull(player.getCash().getOrderDates());
            assertEquals(2, player.getCash().getOrderDates().size());
            assertEquals(today.minusDays(1), player.getCash().getOrderDates().get(0));
            assertEquals(today, player.getCash().getOrderDates().get(1));
            assertNotNull(player.getCash().getOrderTimes());
            assertEquals(2, player.getCash().getOrderTimes().size());
            assertEquals(now.minusDays(1), player.getCash().getOrderTimes().get(0));
            assertEquals(now, player.getCash().getOrderTimes().get(1));
            assertEquals(LocalDate.of(2021, 7, 12), player.getCash().getTestDate());
            assertEquals(1, player.getCash().getTestDateMap().size());
            assertEquals(LocalDate.of(2021, 7, 12), player.getCash().getTestDateMap().get(1).get());
            assertNotNull(player.getCash().getTestSimpleSet());
            assertEquals(4, player.getCash().getTestSimpleSet().size());
            assertArrayEquals(new int[] { 1, 2, 3, 4 },
                    player.getCash().getTestSimpleSet().stream().mapToInt(Integer::intValue).sorted().toArray());
            assertNotNull(player.getCash().getTestSimpleSet2());
            assertEquals(3, player.getCash().getTestSimpleSet2().size());
            assertArrayEquals(new String[] { "a", "b", "c" },
                    player.getCash().getTestSimpleSet2().stream().sorted().toArray(String[]::new));
            assertNotNull(player.getCash().getTestSimpleSet3());
            assertEquals(2, player.getCash().getTestSimpleSet3().size());
            assertArrayEquals(new Object[] { today.minusDays(1), today },
                    player.getCash().getTestSimpleSet3().stream().sorted().toArray());
            assertNotNull(player.getCash().getTestSimpleSet4());
            assertEquals(2, player.getCash().getTestSimpleSet4().size());
            assertArrayEquals(new Object[] { now.minusDays(1), now },
                    player.getCash().getTestSimpleSet4().stream().sorted().toArray());
            assertFalse(player.getGifts().nil());
            assertEquals(2, player.getGifts().size());
            assertTrue(player.getGifts().value(0).isEmpty());
            assertTrue(player.getGifts().value(1).isPresent());
            assertEquals(2, player.getGifts().value(1).get().getId());
            assertEquals(200, player.getGifts().value(1).get().getPrice());
            assertEquals(now, player.getGifts().value(1).get().getCreateTime());
            assertEquals(1, player.getUpdateVersion());
            assertEquals(1, player.getUpdateVersion());

            var zone = ZoneId.systemDefault();
            assertEquals(date, Date.from(player.getCreateTime().atZone(zone).toInstant()));
            assertEquals(date, Date.from(player.getUpdateTime().atZone(zone).toInstant()));

            doc = new BsonDocument().append("_id", new BsonInt32(125)) // uid
                    .append("wt",
                            new BsonDocument("ct", new BsonInt64(5200)).append("cu", new BsonInt64(200))
                                    .append("d", new BsonInt64(10)).append("ad", new BsonInt32(2))) // wallet
                    .append("eqm", // equipments
                            new BsonDocument("87654321-1234-5678-9abc-123456789abc",
                                    new BsonDocument("id", new BsonString("87654321-1234-5678-9abc-123456789abc"))
                                            .append("rid", new BsonInt32(1)).append("atk", new BsonInt32(16))
                                            .append("def", new BsonInt32(2)).append("hp", new BsonInt32(100))))
                    .append("itm", new BsonDocument("2001", new BsonInt32(10))) // items
                    .append("cs", new BsonDocument("stg", new BsonDocument())) // cash
                    .append("_uv", new BsonInt32(1)) // update version
                    .append("_ct", new BsonDateTime(date.getTime())) // create time
                    .append("_ut", new BsonDateTime(date.getTime())); // update time

            player.load(doc);

            assertFalse(player.updated());
            assertEquals(125, player.getUid());
            assertEquals(player, player.getWallet().parent());
            assertEquals(5200, player.getWallet().getCoinTotal());
            assertEquals(200, player.getWallet().getCoinUsed());
            assertEquals(5000, player.getWallet().getCoin());
            assertEquals(10, player.getWallet().getDiamond());
            assertEquals(2, player.getWallet().getAd());
            assertEquals(1, player.getEquipments().size());
            assertEquals(player, player.getEquipments().parent());

            eq = player.getEquipments().get("87654321-1234-5678-9abc-123456789abc");
            assertTrue(eq.isPresent());
            assertEquals(player.getEquipments(), eq.get().parent());
            assertEquals("87654321-1234-5678-9abc-123456789abc", eq.get().key());
            assertEquals("87654321-1234-5678-9abc-123456789abc", eq.get().getId());
            assertEquals(1, eq.get().getRefId());
            assertEquals(16, eq.get().getAtk());
            assertEquals(2, eq.get().getDef());
            assertEquals(100, eq.get().getHp());
            assertEquals(1, player.getItems().size());
            assertEquals(player, player.getItems().parent());
            assertEquals(10, player.getItems().get(2001).get());
            assertEquals(0, player.getCash().getStages().size());
            assertEquals("cs.stg.1", player.getCash().getStages().xpath().resolve("1").value());
            assertNull(player.getCash().getCards());
            assertNull(player.getCash().getOrderIds());
            assertNull(player.getCash().getOrderDates());
            assertNull(player.getCash().getOrderTimes());
            assertNull(player.getCash().getTestDate());
            assertEquals(0, player.getCash().getTestDateMap().size());
            assertTrue(player.getGifts().nil());
            assertEquals(1, player.getUpdateVersion());

            assertEquals(date, Date.from(player.getCreateTime().atZone(zone).toInstant()));
            assertEquals(date, Date.from(player.getUpdateTime().atZone(zone).toInstant()));

        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testLoadAny() {
        var date = new Date();
        var now = DateTimeUtil.local(date);
        var today = now.toLocalDate();
        var map = new LinkedHashMap<String, Object>();
        map.put("_id", 123);
        var wt = new LinkedHashMap<String, Object>();
        map.put("wt", wt);
        wt.put("ct", 5000);
        wt.put("cu", 200);
        wt.put("d", 10);
        wt.put("ad", 2);
        var eqm = new LinkedHashMap<String, Object>();
        map.put("eqm", eqm);
        var eqData = new LinkedHashMap<String, Object>();
        eqm.put("12345678-1234-5678-9abc-123456789abc", eqData);
        eqData.put("id", "12345678-1234-5678-9abc-123456789abc");
        eqData.put("rid", 1);
        eqData.put("atk", 12);
        eqData.put("def", 2);
        eqData.put("hp", 100);
        var itm = new LinkedHashMap<Integer, Object>();
        map.put("itm", itm);
        itm.put(2001, 5);
        var cs = new LinkedHashMap<String, Object>();
        map.put("cs", cs);
        cs.put("stg", new LinkedHashMap<>());
        cs.put("cs", new ArrayList<>(List.of(1, 2, 3, 4)));
        cs.put("ois", new ArrayList<>(List.of(0, 1, 2, 3, 4)));
        cs.put("ods", List.of(DateTimeUtil.toNumber(today.minusDays(1)), DateTimeUtil.toNumber(today)));
        cs.put("ots", List.of(DateTimeUtil.toEpochMilli(now.minusDays(1)), DateTimeUtil.toEpochMilli(now)));
        cs.put("tsd", 20210712);
        var tdm = new LinkedHashMap<String, Object>();
        cs.put("tdm", tdm);
        cs.put("tss", new ArrayList<>(List.of(1, 2, 3, 4)));
        cs.put("tss2", new ArrayList<>(List.of("a", "b", "c")));
        cs.put("tss3", List.of(DateTimeUtil.toNumber(today.minusDays(1)), DateTimeUtil.toNumber(today)));
        cs.put("tss4", List.of(DateTimeUtil.toEpochMilli(now.minusDays(1)), DateTimeUtil.toEpochMilli(now)));
        var gfs = new ArrayList<>();
        map.put("gfs", gfs);
        var gm1 = new LinkedHashMap<>();
        gm1.put("id", 2);
        gm1.put("prc", 200);
        gm1.put("ct", DateTimeUtil.toEpochMilli(now));
        gfs.add(null);
        gfs.add(gm1);
        tdm.put("1", 20210712);
        map.put("_uv", 1);
        map.put("_ct", DateTimeUtil.toEpochMilli(now));
        map.put("_ut", DateTimeUtil.toEpochMilli(now));
        var json = Jackson2Library.getInstance().dumpsToString(map);
        var any = JsoniterLibrary.getInstance().loads(json);
        try {
            var player = new Player();
            player.load(any);
            assertFalse(player.updated());
            assertEquals(123, player.getUid());
            assertEquals(player, player.getWallet().parent());
            assertEquals(5000, player.getWallet().getCoinTotal());
            assertEquals(200, player.getWallet().getCoinUsed());
            assertEquals(4800, player.getWallet().getCoin());
            assertEquals(10, player.getWallet().getDiamond());
            assertEquals(2, player.getWallet().getAd());
            assertEquals(1, player.getEquipments().size());
            assertEquals(player, player.getEquipments().parent());
            var eq = player.getEquipments().get("12345678-1234-5678-9abc-123456789abc");
            assertTrue(eq.isPresent());
            assertEquals(player.getEquipments(), eq.get().parent());
            assertEquals("12345678-1234-5678-9abc-123456789abc", eq.get().key());
            assertEquals("12345678-1234-5678-9abc-123456789abc", eq.get().getId());
            assertEquals(1, eq.get().getRefId());
            assertEquals(12, eq.get().getAtk());
            assertEquals(2, eq.get().getDef());
            assertEquals(100, eq.get().getHp());
            assertEquals(1, player.getItems().size());
            assertEquals(player, player.getItems().parent());
            assertEquals(5, player.getItems().get(2001).get());
            assertEquals(0, player.getCash().getStages().size());
            assertEquals("cs.stg.1", player.getCash().getStages().xpath().resolve("1").value());
            assertNotNull(player.getCash().getCards());
            assertEquals(4, player.getCash().getCards().size());
            assertArrayEquals(new int[] { 1, 2, 3, 4 },
                    player.getCash().getCards().stream().mapToInt(Integer::intValue).toArray());
            assertNotNull(player.getCash().getOrderIds());
            assertEquals(5, player.getCash().getOrderIds().size());
            assertArrayEquals(new int[] { 0, 1, 2, 3, 4 },
                    player.getCash().getOrderIds().stream().mapToInt(Integer::intValue).toArray());
            assertNotNull(player.getCash().getOrderDates());
            assertEquals(2, player.getCash().getOrderDates().size());
            assertEquals(today.minusDays(1), player.getCash().getOrderDates().get(0));
            assertEquals(today, player.getCash().getOrderDates().get(1));
            assertNotNull(player.getCash().getOrderTimes());
            assertEquals(2, player.getCash().getOrderTimes().size());
            assertEquals(now.minusDays(1), player.getCash().getOrderTimes().get(0));
            assertEquals(now, player.getCash().getOrderTimes().get(1));
            assertEquals(LocalDate.of(2021, 7, 12), player.getCash().getTestDate());
            assertEquals(1, player.getCash().getTestDateMap().size());
            assertEquals(LocalDate.of(2021, 7, 12), player.getCash().getTestDateMap().get(1).get());
            assertNotNull(player.getCash().getTestSimpleSet());
            assertEquals(4, player.getCash().getTestSimpleSet().size());
            assertArrayEquals(new int[] { 1, 2, 3, 4 },
                    player.getCash().getTestSimpleSet().stream().mapToInt(Integer::intValue).sorted().toArray());
            assertNotNull(player.getCash().getTestSimpleSet2());
            assertEquals(3, player.getCash().getTestSimpleSet2().size());
            assertArrayEquals(new String[] { "a", "b", "c" },
                    player.getCash().getTestSimpleSet2().stream().sorted().toArray(String[]::new));
            assertNotNull(player.getCash().getTestSimpleSet3());
            assertEquals(2, player.getCash().getTestSimpleSet3().size());
            assertArrayEquals(new Object[] { today.minusDays(1), today },
                    player.getCash().getTestSimpleSet3().stream().sorted().toArray());
            assertNotNull(player.getCash().getTestSimpleSet4());
            assertEquals(2, player.getCash().getTestSimpleSet4().size());
            assertArrayEquals(new Object[] { now.minusDays(1), now },
                    player.getCash().getTestSimpleSet4().stream().sorted().toArray());
            assertFalse(player.getGifts().nil());
            assertEquals(2, player.getGifts().size());
            assertTrue(player.getGifts().value(0).isEmpty());
            assertTrue(player.getGifts().value(1).isPresent());
            assertEquals(2, player.getGifts().value(1).get().getId());
            assertEquals(200, player.getGifts().value(1).get().getPrice());
            assertEquals(now, player.getGifts().value(1).get().getCreateTime());
            assertEquals(1, player.getUpdateVersion());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testLoadJsonNode() {
        var now = DateTimeUtil.local(new Date());
        var today = now.toLocalDate();
        var map = new LinkedHashMap<String, Object>();
        map.put("_id", 123);
        var wt = new LinkedHashMap<String, Object>();
        map.put("wt", wt);
        wt.put("ct", 5000);
        wt.put("cu", 200);
        wt.put("d", 10);
        wt.put("ad", 2);
        var eqm = new LinkedHashMap<String, Object>();
        map.put("eqm", eqm);
        var eqData = new LinkedHashMap<String, Object>();
        eqm.put("12345678-1234-5678-9abc-123456789abc", eqData);
        eqData.put("id", "12345678-1234-5678-9abc-123456789abc");
        eqData.put("rid", 1);
        eqData.put("atk", 12);
        eqData.put("def", 2);
        eqData.put("hp", 100);
        var itm = new LinkedHashMap<Integer, Object>();
        map.put("itm", itm);
        itm.put(2001, 5);
        var cs = new LinkedHashMap<String, Object>();
        map.put("cs", cs);
        cs.put("stg", new LinkedHashMap<>());
        cs.put("cs", new ArrayList<>(List.of(1, 2, 3, 4)));
        cs.put("ois", new ArrayList<>(List.of(0, 1, 2, 3, 4)));
        cs.put("ods", List.of(DateTimeUtil.toNumber(today.minusDays(1)), DateTimeUtil.toNumber(today)));
        cs.put("ots", List.of(DateTimeUtil.toEpochMilli(now.minusDays(1)), DateTimeUtil.toEpochMilli(now)));
        cs.put("tsd", 20210712);
        var tdm = new LinkedHashMap<String, Object>();
        cs.put("tdm", tdm);
        cs.put("tss", new ArrayList<>(List.of(1, 2, 3, 4)));
        cs.put("tss2", new ArrayList<>(List.of("a", "b", "c")));
        cs.put("tss3", List.of(DateTimeUtil.toNumber(today.minusDays(1)), DateTimeUtil.toNumber(today)));
        cs.put("tss4", List.of(DateTimeUtil.toEpochMilli(now.minusDays(1)), DateTimeUtil.toEpochMilli(now)));
        var gfs = new ArrayList<>();
        map.put("gfs", gfs);
        var gm1 = new LinkedHashMap<>();
        gm1.put("id", 2);
        gm1.put("prc", 200);
        gm1.put("ct", DateTimeUtil.toEpochMilli(now));
        gfs.add(null);
        gfs.add(gm1);
        tdm.put("1", 20210712);
        map.put("_uv", 1);
        map.put("_ct", DateTimeUtil.toEpochMilli(now));
        map.put("_ut", DateTimeUtil.toEpochMilli(now));
        var json = Jackson2Library.getInstance().dumpsToString(map);
        var jsonNode = Jackson2Library.getInstance().loads(json);
        try {
            var player = new Player();
            player.load(jsonNode);
            assertFalse(player.updated());
            assertEquals(123, player.getUid());
            assertEquals(player, player.getWallet().parent());
            assertEquals(5000, player.getWallet().getCoinTotal());
            assertEquals(200, player.getWallet().getCoinUsed());
            assertEquals(4800, player.getWallet().getCoin());
            assertEquals(10, player.getWallet().getDiamond());
            assertEquals(2, player.getWallet().getAd());
            assertEquals(1, player.getEquipments().size());
            assertEquals(player, player.getEquipments().parent());
            var eq = player.getEquipments().get("12345678-1234-5678-9abc-123456789abc");
            assertTrue(eq.isPresent());
            assertEquals(player.getEquipments(), eq.get().parent());
            assertEquals("12345678-1234-5678-9abc-123456789abc", eq.get().key());
            assertEquals("12345678-1234-5678-9abc-123456789abc", eq.get().getId());
            assertEquals(1, eq.get().getRefId());
            assertEquals(12, eq.get().getAtk());
            assertEquals(2, eq.get().getDef());
            assertEquals(100, eq.get().getHp());
            assertEquals(1, player.getItems().size());
            assertEquals(player, player.getItems().parent());
            assertEquals(5, player.getItems().get(2001).get());
            assertEquals(0, player.getCash().getStages().size());
            assertEquals("cs.stg.1", player.getCash().getStages().xpath().resolve("1").value());
            assertNotNull(player.getCash().getCards());
            assertEquals(4, player.getCash().getCards().size());
            assertArrayEquals(new int[] { 1, 2, 3, 4 },
                    player.getCash().getCards().stream().mapToInt(Integer::intValue).toArray());
            assertNotNull(player.getCash().getOrderIds());
            assertEquals(5, player.getCash().getOrderIds().size());
            assertArrayEquals(new int[] { 0, 1, 2, 3, 4 },
                    player.getCash().getOrderIds().stream().mapToInt(Integer::intValue).toArray());
            assertNotNull(player.getCash().getOrderDates());
            assertEquals(2, player.getCash().getOrderDates().size());
            assertEquals(today.minusDays(1), player.getCash().getOrderDates().get(0));
            assertEquals(today, player.getCash().getOrderDates().get(1));
            assertNotNull(player.getCash().getOrderTimes());
            assertEquals(2, player.getCash().getOrderTimes().size());
            assertEquals(now.minusDays(1), player.getCash().getOrderTimes().get(0));
            assertEquals(now, player.getCash().getOrderTimes().get(1));
            assertEquals(LocalDate.of(2021, 7, 12), player.getCash().getTestDate());
            assertEquals(1, player.getCash().getTestDateMap().size());
            assertEquals(LocalDate.of(2021, 7, 12), player.getCash().getTestDateMap().get(1).get());
            assertNotNull(player.getCash().getTestSimpleSet());
            assertEquals(4, player.getCash().getTestSimpleSet().size());
            assertArrayEquals(new int[] { 1, 2, 3, 4 },
                    player.getCash().getTestSimpleSet().stream().mapToInt(Integer::intValue).sorted().toArray());
            assertNotNull(player.getCash().getTestSimpleSet2());
            assertEquals(3, player.getCash().getTestSimpleSet2().size());
            assertArrayEquals(new String[] { "a", "b", "c" },
                    player.getCash().getTestSimpleSet2().stream().sorted().toArray(String[]::new));
            assertNotNull(player.getCash().getTestSimpleSet3());
            assertEquals(2, player.getCash().getTestSimpleSet3().size());
            assertArrayEquals(new Object[] { today.minusDays(1), today },
                    player.getCash().getTestSimpleSet3().stream().sorted().toArray());
            assertNotNull(player.getCash().getTestSimpleSet4());
            assertEquals(2, player.getCash().getTestSimpleSet4().size());
            assertArrayEquals(new Object[] { now.minusDays(1), now },
                    player.getCash().getTestSimpleSet4().stream().sorted().toArray());
            assertFalse(player.getGifts().nil());
            assertEquals(2, player.getGifts().size());
            assertTrue(player.getGifts().value(0).isEmpty());
            assertTrue(player.getGifts().value(1).isPresent());
            assertEquals(2, player.getGifts().value(1).get().getId());
            assertEquals(200, player.getGifts().value(1).get().getPrice());
            assertEquals(now, player.getGifts().value(1).get().getCreateTime());
            assertEquals(1, player.getUpdateVersion());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testToJson() {
        try {
            var player = new Player();
            player.setUid(123);
            player.getWallet().setCoinTotal(5000);
            player.getWallet().setCoinUsed(200);
            player.getWallet().setDiamond(10);
            player.getEquipments().putIfAbsent("12345678-1234-5678-9abc-123456789abc", k -> {
                var equipment = new Equipment();
                equipment.setId(k);
                equipment.setRefId(1);
                equipment.setAtk(10);
                equipment.setDef(0);
                equipment.setHp(0);
                return equipment;
            });
            player.getItems().putAll(Map.of(2001, 5));
            player.getCash().getStages().put(1, 1);
            player.getCash().setCards(List.of(1, 2, 3, 4));
            player.getCash().setOrderIds(List.of(0, 1, 2, 3, 4));
            var now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
            var today = now.toLocalDate();
            player.getCash().setOrderDates(List.of(today.minusDays(1), today));
            player.getCash().setOrderTimes(List.of(now.minusDays(1), now));
            player.getCash().setTestSimpleSet(new LinkedHashSet<>(List.of(1, 2, 3, 4)));
            player.getCash().setTestSimpleSet2(new LinkedHashSet<>(List.of("a", "b", "c")));
            player.getCash().setTestSimpleSet3(new LinkedHashSet<>(List.of(today.minusDays(1), today)));
            player.getCash().setTestSimpleSet4(new LinkedHashSet<>(List.of(now.minusDays(1), now)));
            var g2 = new GiftInfo();
            g2.setId(2);
            g2.setPrice(200);
            g2.setCreateTime(now);
            player.getGifts().append(null);
            player.getGifts().append(g2);
            player.setCreateTime(now);
            player.setUpdateTime(now);
            var json = Jackson2Library.getInstance().dumpsToString(player);
            var any = JsoniterLibrary.getInstance().loads(json);
            assertEquals(6, any.asMap().size());
            assertEquals(123, any.toInt("uid"));
            assertEquals(4, any.get("wallet").asMap().size());
            assertEquals(5000, any.toInt("wallet", "coinTotal"));
            assertEquals(4800, any.toInt("wallet", "coin"));
            assertEquals(10, any.toInt("wallet", "diamond"));
            assertEquals(0, any.toInt("wallet", "ad"));
            assertEquals(1, any.get("equipments").asMap().size());
            assertEquals("12345678-1234-5678-9abc-123456789abc",
                    any.toString("equipments", "12345678-1234-5678-9abc-123456789abc", "id"));
            assertEquals(5, any.get("equipments", "12345678-1234-5678-9abc-123456789abc").asMap().size());
            assertEquals(1, any.toInt("equipments", "12345678-1234-5678-9abc-123456789abc", "refId"));
            assertEquals(10, any.toInt("equipments", "12345678-1234-5678-9abc-123456789abc", "atk"));
            assertEquals(0, any.toInt("equipments", "12345678-1234-5678-9abc-123456789abc", "def"));
            assertEquals(0, any.toInt("equipments", "12345678-1234-5678-9abc-123456789abc", "hp"));
            assertEquals(1, any.get("items").asMap().size());
            assertEquals(5, any.toInt("items", "2001"));
            assertEquals(4, any.get("cash").asMap().size());
            assertEquals(1, any.get("cash", "stages").asMap().size());
            assertEquals(1, any.toInt("cash", "stages", "1"));
            assertEquals(4, any.get("cash", "cards").asList().size());
            assertEquals(1, any.toInt("cash", "cards", 0));
            assertEquals(2, any.toInt("cash", "cards", 1));
            assertEquals(3, any.toInt("cash", "cards", 2));
            assertEquals(4, any.toInt("cash", "cards", 3));
            assertEquals(4, any.get("cash", "testSimpleSet").size());
            assertArrayEquals(new int[] { 1, 2, 3, 4 },
                    any.get("cash", "testSimpleSet").asList().stream().mapToInt(Any::toInt).sorted().toArray());
            assertEquals(3, any.get("cash", "testSimpleSet2").size());
            assertArrayEquals(new String[] { "a", "b", "c" }, any.get("cash", "testSimpleSet2").asList().stream()
                    .map(Any::toString).sorted().toArray(String[]::new));
            assertEquals(2, any.get("gifts").asList().size());
            assertEquals(ValueType.NULL, any.get("gifts", 0).valueType());
            assertEquals(2, any.toInt("gifts", 1, "id"));
            assertEquals(200, any.toInt("gifts", 1, "price"));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testAppendUpdates() {
        try {
            var player = new Player();
            player.setUid(123);
            player.getWallet().setCoinTotal(5000);
            player.getWallet().setCoinUsed(200);
            player.getWallet().setDiamond(10);
            var eq1 = player.getEquipments().putIfAbsent("12345678-1234-5678-9abc-123456789abc", k -> {
                var equipment = new Equipment();
                equipment.setId(k);
                equipment.setRefId(1);
                equipment.setAtk(10);
                equipment.setDef(0);
                equipment.setHp(0);
                return equipment;
            });
            player.getEquipments().putIfAbsent("11111111-2222-3333-4444-555555555555", k -> {
                var equipment = new Equipment();
                equipment.setId(k);
                equipment.setRefId(2);
                equipment.setDef(5);
                equipment.setHp(2);
                return equipment;
            });
            player.getItems().putAll(Map.of(2001, 5));
            player.getCash().setOrderIds(List.of(0, 1, 2, 3, 4));
            var now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
            var today = now.toLocalDate();
            player.getCash().setOrderDates(List.of(today.minusDays(1), today));
            player.getCash().setOrderTimes(List.of(now.minusDays(1), now));
            player.getCash().getTestDateMap().put(1, now.toLocalDate());
            player.getCash().setTestSimpleSet2(new LinkedHashSet<>(List.of("a", "b", "c")));
            player.getCash().setTestSimpleSet3(new LinkedHashSet<>(List.of(today.minusDays(1), today)));
            player.getCash().setTestSimpleSet4(new LinkedHashSet<>(List.of(now.minusDays(1), now)));
            var g1 = new GiftInfo();
            g1.setId(1);
            g1.setPrice(100);
            g1.setCreateTime(now.minusDays(1));
            player.getGifts().append(g1);
            var g2 = new GiftInfo();
            g2.setId(2);
            g2.setPrice(200);
            g2.setCreateTime(now);
            player.getGifts().append(g2);
            player.setCreateTime(now.minusDays(1));
            player.setUpdateTime(now.minusSeconds(10));

            player.reset();

            player.getWallet().setCoinTotal(5200);
            player.getWallet().setCoinUsed(300);
            player.getWallet().increaseAd();

            player.getEquipments().get("12345678-1234-5678-9abc-123456789abc").get().fullyUpdate(true).setAtk(12);
            assertTrue(player.getEquipments().remove("11111111-2222-3333-4444-555555555555").isPresent());

            var eq3 = player.getEquipments().putIfAbsent("00000000-0000-0000-0000-000000000000", k -> {
                var equipment = new Equipment();
                equipment.setId(k);
                equipment.setRefId(3);
                equipment.setDef(5);
                equipment.setHp(2);
                return equipment.fullyUpdate(true);
            });

            player.getItems().put(2002, 1);
            player.getItems().remove(2001);
            player.getCash().getStages().put(1, 1);
            player.getCash().setCards(List.of(1, 2, 3, 4));
            player.getCash().setOrderIds(null);
            player.getCash().setOrderDates(null);
            player.getCash().setOrderTimes(null);
            player.getCash().setTestDate(now.toLocalDate());
            player.getCash().getTestDateMap().remove(1);
            player.getCash().getTestDateMap().put(2, now.toLocalDate());
            player.getCash().setTestSimpleSet(new LinkedHashSet<>(List.of(1, 2, 3, 4)));
            player.getCash().setTestSimpleSet2(null);
            player.getCash().setTestSimpleSet3(null);
            player.getCash().setTestSimpleSet4(null);
            now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
            player.getGifts().remove(0);
            player.getGifts().value(1).get().setPrice(300);
            var g3 = new GiftInfo();
            g3.setId(3);
            g3.setPrice(500);
            g3.setCreateTime(now);
            player.getGifts().append(g3);
            player.setUpdateTime(now);
            player.increaseUpdateVersion();

            assertTrue(player.updated());
            var updates = new ArrayList<Bson>();
            var n = player.appendUpdates(updates);
            assertTrue(n > 0);
            assertEquals(25, n);
            assertEquals(25, updates.size());
            assertEquals(Updates.set("wt.ct", 5200L), updates.get(0));
            assertEquals(Updates.set("wt.cu", 300L), updates.get(1));
            assertEquals(Updates.set("wt.ad", 1), updates.get(2));
            assertEquals(Updates.set("eqm.12345678-1234-5678-9abc-123456789abc", eq1.toBson()), updates.get(3));
            assertEquals(Updates.set("eqm.00000000-0000-0000-0000-000000000000", eq3.toBson()), updates.get(4));
            assertEquals(Updates.unset("eqm.11111111-2222-3333-4444-555555555555"), updates.get(5));
            assertEquals(Updates.set("itm.2002", 1), updates.get(6));
            assertEquals(Updates.unset("itm.2001"), updates.get(7));
            assertEquals(Updates.set("cs.stg.1", 1), updates.get(8));
            assertEquals(
                    Updates.set("cs.cs",
                            new BsonArray(
                                    List.of(new BsonInt32(1), new BsonInt32(2), new BsonInt32(3), new BsonInt32(4)))),
                    updates.get(9));
            assertEquals(Updates.unset("cs.ois"), updates.get(10));
            assertEquals(Updates.unset("cs.ods"), updates.get(11));
            assertEquals(Updates.unset("cs.ots"), updates.get(12));
            assertEquals(Updates.set("cs.tsd", DateTimeUtil.toNumber(now.toLocalDate())), updates.get(13));
            assertEquals(Updates.set("cs.tdm.2", DateTimeUtil.toNumber(now.toLocalDate())), updates.get(14));
            assertEquals(Updates.unset("cs.tdm.1"), updates.get(15));
            assertEquals(
                    Updates.set("cs.tss",
                            new BsonArray(
                                    List.of(new BsonInt32(1), new BsonInt32(2), new BsonInt32(3), new BsonInt32(4)))),
                    updates.get(16));
            assertEquals(Updates.unset("cs.tss2"), updates.get(17));
            assertEquals(Updates.unset("cs.tss3"), updates.get(18));
            assertEquals(Updates.unset("cs.tss4"), updates.get(19));
            assertEquals(Updates.unset("gfs.0"), updates.get(20));
            assertEquals(Updates.set("gfs.1.prc", 300), updates.get(21));
            assertEquals(Updates.set("gfs.2", new BsonDocument("id", new BsonInt32(3)).append("prc", new BsonInt32(500))
                    .append("ct", new BsonDateTime(DateTimeUtil.toEpochMilli(now)))), updates.get(22));
            assertEquals(Updates.set("_uv", 1), updates.get(23));
            var zone = ZoneId.systemDefault();
            var _ut = new BsonDateTime(now.atZone(zone).toInstant().toEpochMilli());
            assertEquals(Updates.set("_ut", _ut), updates.get(24));

            player.reset();
            assertFalse(player.updated());
            var updates2 = new ArrayList<Bson>();
            var n2 = player.appendUpdates(updates2);
            assertEquals(0, n2);
            assertTrue(updates2.isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e);
        }
    }

    @Test
    public void testToUpdate() {
        try {
            var player = new Player();
            player.setUid(123);
            player.getWallet().setCoinTotal(5000);
            player.getWallet().setCoinUsed(200);
            player.getWallet().setDiamond(10);
            var eq1 = new Equipment();
            eq1.setId("12345678-1234-5678-9abc-123456789abc");
            eq1.setRefId(1);
            eq1.setAtk(10);
            eq1.setDef(0);
            eq1.setHp(0);
            player.getEquipments().put("12345678-1234-5678-9abc-123456789abc", eq1);
            var eq2 = new Equipment();
            eq2.setId("11111111-2222-3333-4444-555555555555");
            eq2.setRefId(2);
            eq2.setDef(5);
            eq2.setHp(2);
            player.getEquipments().put("11111111-2222-3333-4444-555555555555", eq2);
            player.getItems().putAll(Map.of(2001, 5));
            player.getCash().setOrderIds(List.of(0, 1, 2, 3, 4));
            var now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
            var today = now.toLocalDate();
            player.getCash().setOrderDates(List.of(today.minusDays(1), today));
            player.getCash().setOrderTimes(List.of(now.minusDays(1), now));
            player.getCash().setTestSimpleSet2(new LinkedHashSet<>(List.of("a", "b", "c")));
            player.getCash().setTestSimpleSet3(new LinkedHashSet<>(List.of(today.minusDays(1), today)));
            player.getCash().setTestSimpleSet4(new LinkedHashSet<>(List.of(now.minusDays(1), now)));
            var g1 = new GiftInfo();
            g1.setId(1);
            g1.setPrice(100);
            g1.setCreateTime(now.minusDays(1));
            player.getGifts().append(g1);
            var g2 = new GiftInfo();
            g2.setId(2);
            g2.setPrice(200);
            g2.setCreateTime(now);
            player.getGifts().append(g2);
            player.setCreateTime(now);
            player.setUpdateTime(now);

            player.reset();

            player.getWallet().setCoinTotal(5200);
            player.getWallet().setCoinUsed(300);
            player.getWallet().increaseAd();

            player.getEquipments().get("12345678-1234-5678-9abc-123456789abc").get().fullyUpdate(true).setAtk(12);
            assertTrue(player.getEquipments().remove("11111111-2222-3333-4444-555555555555").isPresent());
            var eq3 = new Equipment();
            eq3.setId("00000000-0000-0000-0000-000000000000");
            eq3.setRefId(3);
            eq3.setDef(5);
            eq3.setHp(2);
            player.getEquipments().put("00000000-0000-0000-0000-000000000000", eq3.fullyUpdate(true));

            player.getItems().put(2002, 1);
            player.getItems().remove(2001);
            player.getCash().getStages().put(1, 1);
            player.getCash().setCards(List.of(1, 2, 3, 4));
            player.getCash().setOrderIds(null);
            player.getCash().setOrderDates(null);
            player.getCash().setOrderTimes(null);
            player.getCash().setTestSimpleSet(new LinkedHashSet<>(List.of(1, 2, 3, 4)));
            player.getCash().setTestSimpleSet2(null);
            player.getCash().setTestSimpleSet3(null);
            player.getCash().setTestSimpleSet4(null);
            now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
            player.getGifts().remove(0);
            player.getGifts().value(1).get().setPrice(300);
            var g3 = new GiftInfo();
            g3.setId(3);
            g3.setPrice(500);
            g3.setCreateTime(now);
            player.getGifts().append(g3);
            player.setUpdateTime(now);
            player.increaseUpdateVersion();

            assertTrue(player.updated());
            var update = player.toUpdate();
            assertNotNull(update);
            var json = Jackson2Library.getInstance().dumpsToString(update);
            var any = JsoniterLibrary.getInstance().loads(json);
            assertEquals(5, any.asMap().size());
            assertEquals(3, any.get("wallet").asMap().size());
            assertEquals(2, any.get("equipments").asMap().size());
            assertEquals(5, any.get("equipments", "12345678-1234-5678-9abc-123456789abc").asMap().size());
            assertEquals(5, any.get("equipments", "00000000-0000-0000-0000-000000000000").asMap().size());
            assertEquals(1, any.get("items").asMap().size());
            assertEquals(3, any.get("cash").asMap().size());
            assertEquals(1, any.get("cash", "stages").asMap().size());
            assertEquals(4, any.get("cash", "cards").asList().size());
            assertEquals(4, any.get("cash", "testSimpleSet").asList().size());
            assertEquals(5200, any.toInt("wallet", "coinTotal"));
            assertEquals(4900, any.toInt("wallet", "coin"));
            assertEquals("12345678-1234-5678-9abc-123456789abc",
                    any.toString("equipments", "12345678-1234-5678-9abc-123456789abc", "id"));
            assertEquals(1, any.toInt("equipments", "12345678-1234-5678-9abc-123456789abc", "refId"));
            assertEquals(12, any.toInt("equipments", "12345678-1234-5678-9abc-123456789abc", "atk"));
            assertEquals(0, any.toInt("equipments", "12345678-1234-5678-9abc-123456789abc", "def"));
            assertEquals(0, any.toInt("equipments", "12345678-1234-5678-9abc-123456789abc", "hp"));
            assertEquals("00000000-0000-0000-0000-000000000000",
                    any.toString("equipments", "00000000-0000-0000-0000-000000000000", "id"));
            assertEquals(3, any.toInt("equipments", "00000000-0000-0000-0000-000000000000", "refId"));
            assertEquals(0, any.toInt("equipments", "00000000-0000-0000-0000-000000000000", "atk"));
            assertEquals(5, any.toInt("equipments", "00000000-0000-0000-0000-000000000000", "def"));
            assertEquals(2, any.toInt("equipments", "00000000-0000-0000-0000-000000000000", "hp"));
            assertEquals(1, any.toInt("wallet", "ad"));
            assertEquals(1, any.toInt("items", "2002"));
            assertEquals(1, any.toInt("cash", "stages", "1"));
            assertEquals(1, any.toInt("cash", "cards", 0));
            assertEquals(2, any.toInt("cash", "cards", 1));
            assertEquals(3, any.toInt("cash", "cards", 2));
            assertEquals(4, any.toInt("cash", "cards", 3));
            assertEquals(1, any.toInt("cash", "testSimpleSet", 0));
            assertEquals(2, any.toInt("cash", "testSimpleSet", 1));
            assertEquals(3, any.toInt("cash", "testSimpleSet", 2));
            assertEquals(4, any.toInt("cash", "testSimpleSet", 3));
            assertEquals(300, any.toInt("gifts", "1", "price"));
            assertEquals(3, any.toInt("gifts", "2", "id"));
            assertEquals(500, any.toInt("gifts", "2", "price"));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testToDelete() {
        try {
            var player = new Player();
            player.setUid(123);
            player.getWallet().setCoinTotal(5000);
            player.getWallet().setCoinUsed(200);
            player.getWallet().setDiamond(10);
            var eq1 = new Equipment();
            eq1.setId("12345678-1234-5678-9abc-123456789abc");
            eq1.setRefId(1);
            eq1.setAtk(10);
            eq1.setDef(0);
            eq1.setHp(0);
            player.getEquipments().put("12345678-1234-5678-9abc-123456789abc", eq1);
            var eq2 = new Equipment();
            eq2.setId("11111111-2222-3333-4444-555555555555");
            eq2.setRefId(2);
            eq2.setDef(5);
            eq2.setHp(2);
            player.getEquipments().put("11111111-2222-3333-4444-555555555555", eq2);
            player.getItems().putAll(Map.of(2001, 5));
            player.getCash().setCards(List.of(1, 2, 3, 4));
            player.getCash().setOrderIds(List.of(0, 1, 2, 3, 4));
            var now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
            var today = now.toLocalDate();
            player.getCash().setOrderDates(List.of(today.minusDays(1), today));
            player.getCash().setOrderTimes(List.of(now.minusDays(1), now));
            player.getCash().setTestSimpleSet(new LinkedHashSet<>(List.of(1, 2, 3, 4)));
            player.getCash().setTestSimpleSet2(new LinkedHashSet<>(List.of("a", "b", "c")));
            player.getCash().setTestSimpleSet3(new LinkedHashSet<>(List.of(today.minusDays(1), today)));
            player.getCash().setTestSimpleSet4(new LinkedHashSet<>(List.of(now.minusDays(1), now)));
            var g1 = new GiftInfo();
            g1.setId(1);
            g1.setPrice(100);
            g1.setCreateTime(now.minusDays(1));
            player.getGifts().append(g1);
            var g2 = new GiftInfo();
            g2.setId(2);
            g2.setPrice(200);
            g2.setCreateTime(now);
            player.getGifts().append(g2);
            player.setCreateTime(now);
            player.setUpdateTime(now);

            player.reset();

            player.getWallet().setCoinTotal(5200);
            player.getWallet().increaseAd();

            player.getEquipments().get("12345678-1234-5678-9abc-123456789abc").get().fullyUpdate(true).setAtk(12);
            assertTrue(player.getEquipments().remove("11111111-2222-3333-4444-555555555555").isPresent());
            var eq3 = new Equipment();
            eq3.setId("00000000-0000-0000-0000-000000000000");
            eq3.setRefId(3);
            eq3.setDef(5);
            eq3.setHp(2);
            player.getEquipments().put("00000000-0000-0000-0000-000000000000", eq3.fullyUpdate(true));

            player.getItems().put(2002, 1);
            player.getItems().remove(2001);
            player.getCash().setCards(null);
            player.getCash().setOrderIds(null);
            player.getCash().setOrderDates(null);
            player.getCash().setOrderTimes(null);
            player.getCash().setTestSimpleSet(null);
            player.getCash().setTestSimpleSet2(null);
            player.getCash().setTestSimpleSet3(null);
            player.getCash().setTestSimpleSet4(null);
            now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
            player.getGifts().value(0).get().setPrice(300);
            player.getGifts().remove(1);
            var g3 = new GiftInfo();
            g3.setId(3);
            g3.setPrice(500);
            g3.setCreateTime(now);
            player.getGifts().append(g3);
            player.setUpdateTime(now);
            player.increaseUpdateVersion();

            var delete = player.toDelete();
            assertNotNull(delete);
            var json = Jackson2Library.getInstance().dumpsToString(delete);
            var any = JsoniterLibrary.getInstance().loads(json);
            assertEquals(4, any.asMap().size());
            assertEquals(1, any.get("equipments").asMap().size());
            assertEquals(1, any.get("items").asMap().size());
            assertEquals(3, any.get("cash").asMap().size());
            assertEquals(1, any.toInt("equipments", "11111111-2222-3333-4444-555555555555"));
            assertEquals(1, any.toInt("items", "2001"));
            assertEquals(1, any.toInt("cash", "cards"));
            assertEquals(1, any.toInt("cash", "testSimpleSet"));
            assertEquals(1, any.toInt("cash", "testSimpleSet2"));
            assertEquals(1, any.toInt("gifts", "1"));
        } catch (Exception e) {
            fail(e);
        }
    }

}
