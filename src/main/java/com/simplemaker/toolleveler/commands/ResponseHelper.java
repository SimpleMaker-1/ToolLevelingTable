package com.simplemaker.toolleveler.commands;

import com.simplemaker.toolleveler.ToolLeveler;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;

public final class ResponseHelper {

    public static void sendMessageConfigReload(CommandSourceStack source) {
        sendMessage(source,
                Component.translatable("commands.toolleveler.config.reload")
                        .withStyle(ChatFormatting.WHITE),
                true);
    }

    public static void sendMessageConfigReset(CommandSourceStack source) {
        sendMessage(source,
                Component.translatable("commands.toolleveler.config.reset")
                        .withStyle(ChatFormatting.WHITE),
                true);
    }

    public static Component start() {
        return Component.literal("[" + ToolLeveler.MOD_NAME + "] ")
                .withStyle(ChatFormatting.GOLD);
    }

    public static void sendMessage(CommandSourceStack source, Component message, boolean broadcastToOps) {
        Component full = start().copy().append(message);
        source.sendSuccess(() -> full, broadcastToOps);
    }

    public static Component clickableLink(String url, String displayText) {
        return Component.literal(displayText)
                .withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                );
    }
}