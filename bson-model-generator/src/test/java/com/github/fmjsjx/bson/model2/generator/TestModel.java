package com.github.fmjsjx.bson.model2.generator;

import com.github.fmjsjx.bson.model2.generator.model.Equipment;
import com.github.fmjsjx.bson.model2.generator.model.GisCoordinates;
import com.github.fmjsjx.bson.model2.generator.model.Player;
import com.github.fmjsjx.libcommon.json.Jackson2Library;
import com.github.fmjsjx.libcommon.util.DateTimeUtil;
import com.mongodb.client.model.Updates;
import org.bson.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestModel {

    private static Player testPlayer1() {
        var now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        var player = new Player();
        player.setUid(1);
        player.getBasicInfo().setName("test");
        player.getBasicInfo().setAvatar("");
        player.getBasicInfo().setLastLoginTime(now);
        player.getBasicInfo().setGis(new GisCoordinates());
        player.getBasicInfo().getGis().setLongitude(121.569894);
        player.getBasicInfo().getGis().setLatitude(31.251832);
        player.getWallet().setCoinTotal(100);
        player.getItems().put(1001, 3);
        player.getItems().put(2001, 1);
        player.setCreateTime(now);
        player.setUpdateTime(now);
        return player;
    }

    private static Equipment testEquipment1() {
        var e = new Equipment();
        e.setId(UUID.randomUUID().toString());
        e.setRefId(1);
        e.setAtk(10);
        return e;
    }

    @Test
    public void testBasic() {
        var player = testPlayer1();
        var equipment = testEquipment1();
        player.getEquipments().put(equipment.getId(), equipment);

        var bson = new BsonDocument("_id", new BsonInt32(1))
                .append("bi",
                        new BsonDocument("n", new BsonString("test"))
                                .append("a", new BsonString(""))
                                .append("llt", new BsonDateTime(DateTimeUtil.toEpochMilli(player.getCreateTime())))
                                .append("g", new BsonDocument("lo", new BsonDouble(121.569894)).append("la", new BsonDouble(31.251832)))
                ).append("w",
                        new BsonDocument("ct", new BsonInt64(100))
                                .append("cu", new BsonInt64(0))
                                .append("d", new BsonInt64(0))
                                .append("ad", new BsonInt32(0))
                ).append("e",
                        new BsonDocument(
                                equipment.getId(),
                                new BsonDocument("i", new BsonString(equipment.getId()))
                                        .append("ri", new BsonInt32(1))
                                        .append("a", new BsonInt32(10))
                                        .append("d", new BsonInt32(0))
                                        .append("h", new BsonInt32(0))
                        )
                ).append("i",
                        new BsonDocument("1001", new BsonInt32(3))
                                .append("2001", new BsonInt32(1))
                ).append("_uv", new BsonInt32(0))
                .append("_ct", new BsonDateTime(DateTimeUtil.toEpochMilli(player.getUpdateTime())))
                .append("_ut", new BsonDateTime(DateTimeUtil.toEpochMilli(player.getUpdateTime())));
        assertEquals(bson.toBsonDocument().toJson(), player.toBson().toBsonDocument().toJson());

        var json = """
                {"uid":1,"basicInfo":{"name":"test","avatar":"","lastLoginAt":${now},"gis":{"longitude":121.569894,"latitude":31.251832}},"wallet":{"coinTotal":100,"coin":100,"diamond":0,"ad":0},"equipments":{"${equipment.id}":{"id":"${equipment.id}","refId":1,"atk":10,"def":0,"hp":0}},"items":{"1001":3,"2001":1},"createdAt":${now},"updatedAt":${now}}""";
        json = json.replace("${now}", String.valueOf(DateTimeUtil.toEpochMilli(player.getCreateTime())));
        json = json.replace("${equipment.id}", equipment.getId());
        assertEquals(json, Jackson2Library.defaultInstance().dumpsToString(player.toData()));
    }

    @Test
    public void testToUpdates() {
        var player = testPlayer1();
        var equipment = testEquipment1();
        player.getEquipments().put(equipment.getId(), equipment);
        var updates = player.toUpdates();
        assertEquals(11, updates.size());
        assertEquals(Updates.set("_id", 1), updates.get(0));
        assertEquals(Updates.set("bi.n", "test"), updates.get(1));
        assertEquals(Updates.set("bi.a", ""), updates.get(2));
        assertEquals(Updates.set("bi.llt", new BsonDateTime(DateTimeUtil.toEpochMilli(player.getCreateTime()))), updates.get(3));
        assertEquals(Updates.set("bi.g", new BsonDocument("lo", new BsonDouble(121.569894)).append("la", new BsonDouble(31.251832))), updates.get(4));
        assertEquals(Updates.set("w.ct", 100L), updates.get(5));
        assertEquals(
                Updates.set(
                        "e." + equipment.getId(),
                        new BsonDocument("i", new BsonString(equipment.getId()))
                                .append("ri", new BsonInt32(1))
                                .append("a", new BsonInt32(10))
                                .append("d", new BsonInt32(0))
                                .append("h", new BsonInt32(0))
                ),
                updates.get(6));
        assertEquals(Updates.set("i.1001", new BsonInt32(3)), updates.get(7));
        assertEquals(Updates.set("i.2001", new BsonInt32(1)), updates.get(8));
        assertEquals(Updates.set("_ct", new BsonDateTime(DateTimeUtil.toEpochMilli(player.getCreateTime()))), updates.get(9));
        assertEquals(Updates.set("_ut", new BsonDateTime(DateTimeUtil.toEpochMilli(player.getCreateTime()))), updates.get(10));

        player.fullyUpdate(true);
        updates = player.toUpdates();
        assertEquals(1, updates.size());
        var bson = new BsonDocument("_id", new BsonInt32(1))
                .append("bi",
                        new BsonDocument("n", new BsonString("test"))
                                .append("a", new BsonString(""))
                                .append("llt", new BsonDateTime(DateTimeUtil.toEpochMilli(player.getCreateTime())))
                                .append("g", new BsonDocument("lo", new BsonDouble(121.569894)).append("la", new BsonDouble(31.251832)))
                ).append("w",
                        new BsonDocument("ct", new BsonInt64(100))
                                .append("cu", new BsonInt64(0))
                                .append("d", new BsonInt64(0))
                                .append("ad", new BsonInt32(0))
                ).append("e",
                        new BsonDocument(
                                equipment.getId(),
                                new BsonDocument("i", new BsonString(equipment.getId()))
                                        .append("ri", new BsonInt32(1))
                                        .append("a", new BsonInt32(10))
                                        .append("d", new BsonInt32(0))
                                        .append("h", new BsonInt32(0))
                        )
                ).append("i",
                        new BsonDocument("1001", new BsonInt32(3))
                                .append("2001", new BsonInt32(1))
                ).append("_uv", new BsonInt32(0))
                .append("_ct", new BsonDateTime(DateTimeUtil.toEpochMilli(player.getUpdateTime())))
                .append("_ut", new BsonDateTime(DateTimeUtil.toEpochMilli(player.getUpdateTime())));
        assertEquals(Updates.set("", bson).toBsonDocument().toJson(), updates.get(0).toBsonDocument().toJson());

        player.reset();
        updates = player.toUpdates();
        assertEquals(0, updates.size());

        player.getBasicInfo().setGis(null);
        player.getWallet().addCoinTotal(10);
        player.getEquipments().remove(equipment.getId());
        player.getItems().remove(2001);
        updates = player.toUpdates();
        assertEquals(4, updates.size());
        assertEquals(Updates.unset("bi.g"), updates.get(0));
        assertEquals(Updates.set("w.ct", 110L), updates.get(1));
        assertEquals(Updates.unset("e." + equipment.getId()), updates.get(2));
        assertEquals(Updates.unset("i.2001"), updates.get(3));
    }

    @Test
    public void testDeepCopy() {
        var player = testPlayer1();
        var equipment = testEquipment1();
        player.getEquipments().put(equipment.getId(), equipment);

        var copy = player.deepCopy();

        var bson = new BsonDocument("_id", new BsonInt32(1))
                .append("bi",
                        new BsonDocument("n", new BsonString("test"))
                                .append("a", new BsonString(""))
                                .append("llt", new BsonDateTime(DateTimeUtil.toEpochMilli(player.getCreateTime())))
                                .append("g", new BsonDocument("lo", new BsonDouble(121.569894)).append("la", new BsonDouble(31.251832)))
                ).append("w",
                        new BsonDocument("ct", new BsonInt64(100))
                                .append("cu", new BsonInt64(0))
                                .append("d", new BsonInt64(0))
                                .append("ad", new BsonInt32(0))
                ).append("e",
                        new BsonDocument(
                                equipment.getId(),
                                new BsonDocument("i", new BsonString(equipment.getId()))
                                        .append("ri", new BsonInt32(1))
                                        .append("a", new BsonInt32(10))
                                        .append("d", new BsonInt32(0))
                                        .append("h", new BsonInt32(0))
                        )
                ).append("i",
                        new BsonDocument("1001", new BsonInt32(3))
                                .append("2001", new BsonInt32(1))
                ).append("_uv", new BsonInt32(0))
                .append("_ct", new BsonDateTime(DateTimeUtil.toEpochMilli(player.getUpdateTime())))
                .append("_ut", new BsonDateTime(DateTimeUtil.toEpochMilli(player.getUpdateTime())));
        assertEquals(bson.toBsonDocument().toJson(), copy.toBson().toBsonDocument().toJson());
        var json = """
                {"uid":1,"basicInfo":{"name":"test","avatar":"","lastLoginAt":${now},"gis":{"longitude":121.569894,"latitude":31.251832}},"wallet":{"coinTotal":100,"coin":100,"diamond":0,"ad":0},"equipments":{"${equipment.id}":{"id":"${equipment.id}","refId":1,"atk":10,"def":0,"hp":0}},"items":{"1001":3,"2001":1},"createdAt":${now},"updatedAt":${now}}""";
        json = json.replace("${now}", String.valueOf(DateTimeUtil.toEpochMilli(player.getCreateTime())));
        json = json.replace("${equipment.id}", equipment.getId());
        assertEquals(json, Jackson2Library.defaultInstance().dumpsToString(copy.toData()));
    }

    @Test
    public void testDeleted() {
        var player = testPlayer1();
        var equipment = testEquipment1();
        player.getEquipments().put(equipment.getId(), equipment);
        player.getBasicInfo().getGis().setHeight(123.0);
        player.reset();

        player.getBasicInfo().getGis().setHeight(null);
        player.getEquipments().remove(equipment.getId());
        assertEquals(
                """
                        {"basicInfo":{"gis":{"height":1}},"equipments":{"${equipment.id}":1}}""".replace("${equipment.id}", equipment.getId()),
                Jackson2Library.defaultInstance().dumpsToString(player.toDeletedData())
        );
    }

}
