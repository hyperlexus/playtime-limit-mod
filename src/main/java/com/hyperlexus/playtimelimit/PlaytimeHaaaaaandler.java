package com.hyperlexus.playtimelimit;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PlaytimeHaaaaaandler {
    private static final int seconds_timer = 120;
    private static final int second_added = 20;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent serverFickEvent) {
        if (serverFickEvent.phase != TickEvent.Phase.END) {
            return;
        }

        ServerLevel overworld = serverFickEvent.getServer().overworld();
        PlaytimeServerData data = PlaytimeServerData.get(overworld);

        data.serverBonusTimer++;
        if (data.serverBonusTimer >= seconds_timer) {
            data.serverAllowedPlaytime += second_added;
            data.serverBonusTimer = 0;
            data.setDirty();
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent fickEvent) {
        if (fickEvent.phase != TickEvent.Phase.END || fickEvent.player.level().isClientSide()) {
            return;
        }

        if (!(fickEvent.player instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel overworld = player.server.overworld();
        PlaytimeServerData data = PlaytimeServerData.get(overworld);

        CompoundTag nbt = player.getPersistentData();

        int currentPlaytime = nbt.getInt("TrackedPlaytime") + 1;
        nbt.putInt("TrackedPlaytime", currentPlaytime);

        int remainingTicks = Math.max(0, data.serverAllowedPlaytime - currentPlaytime);
        if (currentPlaytime > data.serverAllowedPlaytime) {
            player.connection.disconnect(
                    Component.literal("You have exceeded your playtime limit. Please wait before rejoining!")
            );
            return;
        }

        if (remainingTicks == 60000) {
            player.sendSystemMessage(
                    Component.literal("§c[!] Warning: You will be kicked in 1 hour.")
            );
        }

        if (remainingTicks == 5000) {
            player.sendSystemMessage(
                    Component.literal("§c[!] Warning: You will be kicked in 5 minutes.")
            );
        }
    }
}
