package com.tontonsamael.RageSignature.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tontonsamael.RageSignature.services.RageService;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class RageHitListener extends EntityEventSystem<EntityStore, Damage> {

    public RageHitListener() {
        super(Damage.class);
    }

    @Override
    public void handle(int entityId, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl Damage event) {
        if (event.isCancelled()) return;

        boolean isProjectileSource = event.getSource() instanceof Damage.ProjectileSource;
        boolean isEntitySource = event.getSource() instanceof Damage.EntitySource;
        if (!isProjectileSource && !isEntitySource) return;

        final Ref<EntityStore> attackerRef = isEntitySource ? ((Damage.EntitySource) event.getSource()).getRef() : ((Damage.ProjectileSource) event.getSource()).getRef();
        Player attacker = store.getComponent(attackerRef, Player.getComponentType());
        if (attacker == null) return;

        RageService.get().onHitEntity(attacker);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}