package com.hyperlexus.playtimelimit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class PlaytimeServerData extends SavedData {
    public int serverAllowedPlaytime = 600000; // 10 hours
    public int serverBonusTimer = 0;

    public static PlaytimeServerData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                PlaytimeServerData::load,
                PlaytimeServerData::new,
                "playtime_limit_data"
        );
    }

    public static PlaytimeServerData load(CompoundTag tag) {
        PlaytimeServerData data = new PlaytimeServerData();
        data.serverAllowedPlaytime = tag.getInt("ServerAllowedPlaytime");
        data.serverBonusTimer = tag.getInt("ServerBonusTimer");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("ServerAllowedPlaytime", this.serverAllowedPlaytime);
        tag.putInt("ServerBonusTimer", this.serverBonusTimer);
        return tag;
    }
}