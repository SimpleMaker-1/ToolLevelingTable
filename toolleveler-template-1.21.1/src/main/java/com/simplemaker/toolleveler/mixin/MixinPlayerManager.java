package com.simplemaker.toolleveler.mixin;

import com.simplemaker.toolleveler.config.util.ConfigSyncing;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class MixinPlayerManager {

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void toolleveler$onPlayerJoin(Connection connection, ServerPlayer player, CallbackInfo ci) {
        ConfigSyncing.syncAllConfigsToOneClient(player);
    }
}