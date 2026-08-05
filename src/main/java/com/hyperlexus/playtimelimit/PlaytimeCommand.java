package com.hyperlexus.playtimelimit;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PlaytimeCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("timeleft")
                        .executes(PlaytimeCommand::checkTimeLeft)
        );

        // req: op lv2, opt: arg int ticks
        dispatcher.register(
                Commands.literal("resetplaytime")
                        .requires(source -> source.hasPermission(2))
                        // no arg / resets to default
                        .executes(context -> executeReset(context, -1))
                        // arg resets with int ticks
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(0, 1000000000))
                                .executes(context -> executeReset(context, IntegerArgumentType.getInteger(context, "ticks")))
                        )
        );

        dispatcher.register(
                Commands.literal("playtime")
                        .executes(PlaytimeCommand::checkPlaytime)
        );
    }

    private static int executeReset(CommandContext<CommandSourceStack> context, int customTicks) {
        ServerLevel overworld = context.getSource().getServer().overworld();
        PlaytimeServerData data = PlaytimeServerData.get(overworld);

        int newTicks = (customTicks == -1) ? PlaytimeServerData.base_taschengled : customTicks;

        data.serverAllowedPlaytime = newTicks;
        data.serverBonusTimer = 0;
        data.resetToZeroFlag++;
        data.setDirty();

        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            CompoundTag nbt = player.getPersistentData();
            nbt.putInt("TrackedPlaytime", 0);
            nbt.putInt("resetToZeroFlag", data.resetToZeroFlag);
        }

        context.getSource().sendSystemMessage(
                Component.literal("§a[!] Playtime for all players has been reset. Starting ticks are now §f" + newTicks/20 + " seconds.")
        );

        return 1;
    }

    private static int checkTimeLeft(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            return 0;
        }

        ServerLevel overworld = player.server.overworld();
        PlaytimeServerData data = PlaytimeServerData.get(overworld);

        CompoundTag nbt = player.getPersistentData();
        int tracked = nbt.contains("TrackedPlaytime") ? nbt.getInt("TrackedPlaytime") : 0;
        int remainingTicks = Math.max(0, data.serverAllowedPlaytime - tracked);

        int realRemainingTicks = (int)(remainingTicks * 6.0 / 5.0);

        player.sendSystemMessage(
                Component.literal("§eTime remaining: §f" + formatInTime(realRemainingTicks))
        );

        return 1;
    }

    private static int checkPlaytime(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) { return 0; }

        CompoundTag nbt = player.getPersistentData();
        int tracked = nbt.contains("TrackedPlaytime") ? nbt.getInt("TrackedPlaytime") : 0;

        player.sendSystemMessage(
                Component.literal("§gTotal time played: §f" + formatInTime(tracked))
        );

        return 1;
    }

    public static String formatInTime(int ticks) {
        int totalSeconds = ticks / 20;

        int years = totalSeconds / 31536000;
        totalSeconds %= 31536000;
        int weeks = totalSeconds / 604800;
        totalSeconds %= 604800;
        int days = totalSeconds / 86400;
        totalSeconds %= 86400;
        int hours = totalSeconds / 3600;
        totalSeconds %= 3600;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        // zeit ist geld lil bro
        return String.format("%04d:%02d:%01d:%02d:%02d:%02d", years, weeks, days, hours, minutes, seconds);
    }

}