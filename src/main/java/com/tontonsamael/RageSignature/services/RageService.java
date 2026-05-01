package com.tontonsamael.RageSignature.services;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tontonsamael.RageSignature;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class RageService {
    private static RageService instance;

    public static RageService get() {
        if (instance == null) {
            instance = new RageService();
        }
        return instance;
    }

    private final Map<String, Float> playersEnergy = new HashMap<>();
    private final Map<String, Integer> playersHitSkip = new HashMap<>();
    private final Map<String, Integer> playersAbilityCooldown = new HashMap<>();
    private final Map<String, Boolean> playersAbilityFired = new HashMap<>();
    private final Map<String, Boolean> playersSwap = new HashMap<>();

    private RageService() {
    }

    public void onPlayerJoin(String username) {
        playersEnergy.put(username, 0f);
    }

    public void onAbilityFired(PlayerRef playerRef) {
        playersAbilityFired.put(playerRef.getUsername(), true);
    }

    public void onSwapItem(PlayerRef playerRef) {
        playersSwap.put(playerRef.getUsername(), true);
    }

    private void updateEnergy(Player player) {
        if (player.getReference() == null) return;
        float maxEnergy = getMaxEnergy(player.getReference(), player.getReference().getStore());
        if (maxEnergy == 0f) return;
        setRage(player.getReference(), player.getReference().getStore(), playersEnergy.computeIfAbsent(player.getDisplayName(), k -> 0f));
    }

    private float getHitStep(Player player) {
        if (player.getReference() == null) return 0f;
        float maxEnergy = getMaxEnergy(player.getReference(), player.getReference().getStore());
        if(maxEnergy == 0f) return 0f;
        return (1f / maxEnergy) * RageSignature.get().getConfig().getRatio();
    }

    public void onUpdateTick(Player player) {
        if (player.getReference() == null) return;

        if (playersSwap.computeIfAbsent(player.getDisplayName(), k -> false)) updateEnergy(player);

        if (playersAbilityFired.computeIfAbsent(player.getDisplayName(), k -> false)) {
            float energy = playersEnergy.computeIfAbsent(player.getDisplayName(), k -> 0f);
            playersAbilityFired.put(player.getDisplayName(), false);
            if (energy == 1f) {
                playersEnergy.put(player.getDisplayName(), 0f);
                playersAbilityCooldown.put(player.getDisplayName(), 2);
                return;
            }
        }

        int hitSkip = playersHitSkip.computeIfAbsent(player.getDisplayName(), k -> 0);
        if (hitSkip > 0) {
            playersHitSkip.put(player.getDisplayName(), hitSkip - 1);
            return;
        }

        int abilityCooldown = playersAbilityCooldown.computeIfAbsent(player.getDisplayName(), k -> 0);
        if (abilityCooldown > 0) {
            playersAbilityCooldown.put(player.getDisplayName(), abilityCooldown - 1);
            return;
        }

        float energy = playersEnergy.computeIfAbsent(player.getDisplayName(), k -> 0f);
        if (energy > 0) {
            energy = Math.clamp(energy - (RageSignature.get().getConfig().getDecay() / 100f), 0f, 1f);
            playersEnergy.put(player.getDisplayName(), energy);
            setRage(player.getReference(), player.getReference().getStore(), energy);
        }
    }

    public void onHitEntity(Player player) {
        if (player.getReference() == null) return;
        float energy = playersEnergy.computeIfAbsent(player.getDisplayName(), k -> 0f);
        energy = Math.clamp(energy + getHitStep(player), 0f, 1f);
        playersEnergy.put(player.getDisplayName(), energy);
        setRage(player.getReference(), player.getReference().getStore(), energy);
        playersHitSkip.put(player.getDisplayName(), RageSignature.get().getConfig().getDelay());
    }

    private float getSignatureEnergy(@Nonnull Ref<EntityStore> entityRef, @Nonnull Store<EntityStore> store) {
        int signatureEnergyIndex = EntityStatType.getAssetMap().getIndex("SignatureEnergy");
        if (signatureEnergyIndex == Integer.MIN_VALUE) {
            return 0f;
        } else {
            EntityStatMap statMap = store.getComponent(entityRef, EntityStatMap.getComponentType());
            if (statMap == null) {
                return 0f;
            } else {
                EntityStatValue statValue = statMap.get(signatureEnergyIndex);
                return statValue != null ? statValue.get() : 0f;
            }
        }
    }

    private float getMaxEnergy(@Nonnull Ref<EntityStore> entityRef, @Nonnull Store<EntityStore> store) {
        if (!entityRef.isValid()) return 0f;
        EntityStatMap statMap = store.getComponent(entityRef, EntityStatMap.getComponentType());
        if (statMap == null) return 0f;
        EntityStatValue signatureCharge = statMap.get(DefaultEntityStatTypes.getSignatureEnergy());
        if (signatureCharge == null) return 0f;
        return signatureCharge.getMax();
    }

    private void setRage(@Nonnull Ref<EntityStore> entityRef, @Nonnull Store<EntityStore> store, float value) {
        if (!entityRef.isValid()) return;
        float current = getSignatureEnergy(entityRef, store);
        if (current == value) return;

        float maxEnergy = getMaxEnergy(entityRef, store);
        EntityStatMap statMap = store.getComponent(entityRef, EntityStatMap.getComponentType());
        if (statMap == null || maxEnergy == 0f) return;

        statMap.setStatValue(DefaultEntityStatTypes.getSignatureEnergy(), value * maxEnergy);

    }
}
