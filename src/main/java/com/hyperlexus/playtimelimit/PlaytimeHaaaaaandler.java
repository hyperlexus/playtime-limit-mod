package com.hyperlexus.playtimelimit;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.player.PlayerEvent;

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
            if (data.serverAllowedPlaytime < 2147483000) {
                data.serverAllowedPlaytime += second_added;
            }
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

        // if offline during last reset
        int playerEpoch = nbt.getInt("resetToZeroFlag");
        if (playerEpoch < data.resetToZeroFlag) {
            nbt.putInt("TrackedPlaytime", 0);
            nbt.putInt("resetToZeroFlag", data.resetToZeroFlag);
        }

        int currentPlaytime = nbt.getInt("TrackedPlaytime") + 1;
        nbt.putInt("TrackedPlaytime", currentPlaytime);

        int remainingTicks = Math.max(0, data.serverAllowedPlaytime - currentPlaytime);
        if (currentPlaytime > data.serverAllowedPlaytime) {
            player.connection.disconnect(
                    Component.literal("You have exceeded the playtime limit (" + PlaytimeCommand.formatInTime(currentPlaytime) + ").")
            );
            return;
        }

        if (remainingTicks == 60000) {
            player.displayClientMessage(
                    Component.literal("§c[!] Warning: You will be kicked in 1 hour."), true
            );
        }

        if (remainingTicks == 5000) {
            player.displayClientMessage(
                    Component.literal("§4[!] Warning: You will be kicked in 5 minutes."), true
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        CompoundTag oldNbt = event.getOriginal().getPersistentData();
        CompoundTag newNbt = event.getEntity().getPersistentData();
        newNbt.putInt("TrackedPlaytime", oldNbt.getInt("TrackedPlaytime"));
        newNbt.putInt("resetToZeroFlag", oldNbt.getInt("resetToZeroFlag"));
        newNbt.putInt("ServerBonusTimer", oldNbt.getInt("ServerBonusTimer"));
    }
}