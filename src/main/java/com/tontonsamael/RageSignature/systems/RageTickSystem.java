package com.tontonsamael.RageSignature.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tontonsamael.RageSignature.services.RageService;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class RageTickSystem extends EntityTickingSystem<EntityStore> {
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
