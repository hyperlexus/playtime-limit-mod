package com.hyperlexus.playtimelimit;
import com.mojang.brigadier.CommandDispatcher;
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
        int totalSeconds = realRemainingTicks / 20;

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

        String timeString = String.format("%04d:%02d:%01d:%02d:%02d:%02d", years, weeks, days, hours, minutes, seconds);

        player.sendSystemMessage(
                Component.literal("§eTime remaining: §f" + timeString)
        );

        return 1;
    }
}