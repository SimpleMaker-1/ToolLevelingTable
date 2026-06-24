package com.simplemaker.toolleveler.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simplemaker.toolleveler.ToolLeveler;
import com.simplemaker.toolleveler.config.util.ConfigManager;
import com.simplemaker.toolleveler.config.util.ConfigSyncing;
import com.simplemaker.toolleveler.network.packets.OpenItemValueScreenPacket;
import com.simplemaker.toolleveler.utils.ProjectLinks;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ToolLevelerCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands
                .literal(ToolLeveler.MOD_ID)
                .then(Commands.literal("config").requires((source) -> source.hasPermission(3))
                        .then(Commands.literal("reload").executes(ToolLevelerCommand::configReload))
                        .then(Commands.literal("reset").executes(ToolLevelerCommand::configReset)))
                .then(Commands.literal("openitemvalues").executes(ToolLevelerCommand::showScreen));

        ProjectLinks.registerAsCommand(command);
        dispatcher.register(command);
    }

    private static int configReload(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ConfigManager.loadAndVerifyConfigs();
        ConfigSyncing.syncAllConfigsToAllClients(context.getSource().getLevel());
        ResponseHelper.sendMessageConfigReload(source);
        return 1;
    }

    private static int configReset(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        // for loop over all configs and reset them
        ConfigManager.resetAllConfigs();
        ConfigSyncing.syncAllConfigsToAllClients(context.getSource().getLevel());
        ResponseHelper.sendMessageConfigReset(source);
        return 1;
    }

    private static int showScreen(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PacketDistributor.sendToPlayer(player, new OpenItemValueScreenPacket());
        return 1;
    }

}
