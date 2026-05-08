package com.tontonsamael.ragesignature.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.ValueType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tontonsamael.ragesignature.services.RageService;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class RageTickSystems {
    public static class PlayerSecond extends EntityTickingSystem<EntityStore> {
        private float secondTick = 0f;

        @Override
        public void tick(float dt, int i, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
            secondTick -= dt;
            if (secondTick <= 0) {
                secondTick += 1f;
                Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(i);
                if (!entityRef.isValid()) return;
                Player player = store.getComponent(entityRef, Player.getComponentType());
                if (player == null) return;
                RageService.get().onUpdateTick(player);

            }
        }

        @NullableDecl
        @Override
        public Query<EntityStore> getQuery() {
            return Player.getComponentType();
        }
    }

    public static class PotionDetection extends EntityTickingSystem<EntityStore> {
        private final Map<String, Float> counters = new HashMap<>();

        @Override
        public void tick(float dt, int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
            Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
            Player player = store.getComponent(entityRef, Player.getComponentType());
            EffectControllerComponent effects = store.getComponent(entityRef, EffectControllerComponent.getComponentType());
            EntityStatMap entityStatMap = store.getComponent(entityRef, EntityStatMap.getComponentType());
            if (player == null || effects == null || entityStatMap == null) return;

            Map<String, Float> filtered = new HashMap<>();
            effects.getActiveEffects().values().forEach(effect -> {
                EntityEffect entity = EntityEffect.getAssetMap().getAsset(effect.getEntityEffectIndex());
                if (entity == null || entity.getEntityStats() == null) return;

                entity.getEntityStats().forEach((statIndex, val) -> {
                    EntityStatValue statId = entityStatMap.get(statIndex);
                    if (statId != null && statId.getId().equals("SignatureEnergy")) {
                        if (entity.getValueType() != ValueType.Percent) return;
                        filtered.put(entity.getId(), val / 100);
                    }
                });
            });


            AtomicReference<Float> added = new AtomicReference<>(0f);
            // init new counters
            filtered.forEach((id, _) -> {
                if (!counters.containsKey(id)) counters.put(id, 5f);
            });
            Set<String> removedCounters = new HashSet<>();
            counters.forEach((id, val) -> {
                // remove obsolete counters
                if (!filtered.containsKey(id)) {
                    removedCounters.add(id);
                    return;
                }

                // detect effect tick (every 5s)
                float newVal = val - dt;
                if (newVal <= 0.05f) {
                    newVal += 5f;
                    added.set(filtered.get(id));
                }
                counters.put(id, newVal);
            });
            removedCounters.forEach(counters::remove);
            if (added.get() > 0f) RageService.get().addEnergy(player, added.get());
        }

        @NullableDecl
        @Override
        public Query<EntityStore> getQuery() {
            return Player.getComponentType();
        }
    }
}
