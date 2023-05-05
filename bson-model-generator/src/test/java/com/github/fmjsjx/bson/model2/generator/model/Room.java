package com.github.fmjsjx.bson.model2.generator.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.fmjsjx.bson.model.core.BsonUtil;
import com.github.fmjsjx.bson.model2.core.*;
import org.bson.*;
import org.bson.conversions.Bson;

import java.util.*;

public class Room extends RootModel<Room> {

    public static final String BNAME_PLAYERS = "players";

    private List<Player> players;

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    @Override
    public BsonDocument toBson() {
        return new BsonDocument();
    }

    @Override
    public Room load(BsonDocument src) {
        resetStates();
        players = BsonUtil.arrayValue(src, BNAME_PLAYERS, (BsonDocument v) -> new Player().load(v)).orElse(null);
        return this;
    }

    @Override
    public JsonNode toJsonNode() {
        var jsonNode = JsonNodeFactory.instance.objectNode();
        var players = this.players;
        if (players != null) {
            var playersArrayNode = jsonNode.arrayNode(players.size());
            players.stream().map(Player::toJsonNode).forEach(playersArrayNode::add);
            jsonNode.set(BNAME_PLAYERS, playersArrayNode);
        }
        return jsonNode;
    }

    @Override
    public Object toData() {
        var data = new LinkedHashMap<>();
        var players = this.players;
        if (players != null) {
            data.put("players", players.stream().map(Player::toData).toList());
        }
        return data;
    }

    @Override
    public boolean anyUpdated() {
        return false;
    }

    @Override
    protected void resetChildren() {
    }

    @Override
    protected int deletedSize() {
        return 0;
    }

    @Override
    public boolean anyDeleted() {
        return false;
    }

    @Override
    public Room clean() {
        players = null;
        resetStates();
        return this;
    }

    @Override
    public Room deepCopy() {
        var copy = new Room();
        deepCopyTo(copy, false);
        return copy;
    }

    @Override
    public void deepCopyFrom(Room src) {
        var players = src.players;
        if (players != null) {
            var playersCopy = new ArrayList<Player>(players.size());
            for (var playersCopyValue : players) {
                if (playersCopyValue == null) {
                    playersCopy.add(null);
                } else {
                    playersCopy.add(playersCopyValue.deepCopy());
                }
            }
            this.players = playersCopy;
        }
    }

    @Override
    protected void appendFieldUpdates(List<Bson> updates) {
    }

    @Override
    protected void loadObjectNode(JsonNode src) {
        resetStates();
        players = BsonUtil.listValue(src, BNAME_PLAYERS, v -> new Player().load(v)).orElse(null);
    }

    @Override
    protected void appendUpdateData(Map<Object, Object> data) {
    }

    @Override
    public Object toDeletedData() {
        return null;
    }

    @Override
    protected void appendDeletedData(Map<Object, Object> data) {
    }

    @Override
    public String toString() {
        return "Room(" + "players=" + players +
                ")";
    }

}
