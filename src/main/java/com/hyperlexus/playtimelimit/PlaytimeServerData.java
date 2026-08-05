package com.hyperlexus.playtimelimit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class PlaytimeServerData extends SavedData {
    public static final int base_taschengled = 600000;
    public int serverAllowedPlaytime = base_taschengled; // 10 hours
    public int serverBonusTimer = 0;
    public int resetToZeroFlag = 0;

    public static PlaytimeServerData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                PlaytimeServerData::load,
                PlaytimeServerData::new,
                "playtime_limit_data"
        );
    }

    public static PlaytimeServerData load(CompoundTag tahh) {
        PlaytimeServerData data = new PlaytimeServerData();
        data.serverAllowedPlaytime = tahh.getInt("ServerAllowedPlaytime");
        data.serverBonusTimer = tahh.getInt("ServerBonusTimer");
        data.resetToZeroFlag = tahh.getInt("resetToZeroFlag");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("ServerAllowedPlaytime", this.serverAllowedPlaytime);
        tag.putInt("ServerBonusTimer", this.serverBonusTimer);
        tag.putInt("resetToZeroFlag", this.resetToZeroFlag);
        return tag;
    }
}