package com.tontonsamael.RageSignature.event;

import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChain;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChains;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketWatcher;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.tontonsamael.RageSignature.services.RageService;

import javax.annotation.Nonnull;

public class PacketHandler implements PlayerPacketWatcher {
    @Override
    public void accept(@Nonnull PlayerRef playerRef, @Nonnull Packet packet) {
        if (!(packet instanceof SyncInteractionChains syncPacket)) return;

        for (SyncInteractionChain chain : syncPacket.updates) {
            if (chain.interactionType == InteractionType.Ability1) {
                RageService.get().onAbilityFired(playerRef);
            } else if (chain.interactionType == InteractionType.SwapFrom) {
                RageService.get().onSwapItem(playerRef);
            }
        }
    }
}
