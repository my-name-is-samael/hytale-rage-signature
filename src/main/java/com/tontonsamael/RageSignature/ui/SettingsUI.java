package com.tontonsamael.RageSignature.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.tontonsamael.RageSignature;
import com.tontonsamael.RageSignature.config.RageConfig;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.util.Set;

public class SettingsUI extends InteractiveCustomUIPage<SettingsUI.UIData> {
    private static final String ACTION = "Action";
    private static final String VALUE = "@Value";

    private enum SettingsActions {
        CLOSE, UPDATE_DECAY, UPDATE_DELAY, UPDATE_RATIO
    }

    public static class UIData {
        public static final BuilderCodec<UIData> CODEC = BuilderCodec.builder(UIData.class, UIData::new)
                .append(new KeyedCodec<>(ACTION, Codec.STRING),
                        (data, value) -> data.action = value,
                        data -> data.action).add()
                .append(new KeyedCodec<>(VALUE, Codec.FLOAT),
                        (data, value) -> data.value = value,
                        data -> data.value).add()
                .build();

        private String action;
        private float value;
    }

    public SettingsUI(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, UIData.CODEC);
    }

    private boolean checkAccess(Store<EntityStore> store, Ref<EntityStore> ref) {
        try {
            UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
            assert uuidComponent != null;
            PermissionsModule perms = PermissionsModule.get();
            Set<String> groups = perms.getGroupsForUser(uuidComponent.getUuid());
            return groups.contains("OP");
        } catch (Exception _) {
            //
        }
        return false;
    }

    private void closePage(Ref<EntityStore> playerRef, Store<EntityStore> store) {
        Player player = store.getComponent(playerRef, Player.getComponentType());
        assert player != null;
        player.getPageManager().setPage(playerRef, store, Page.None);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder cmd,
                      @Nonnull UIEventBuilder events,
                      @Nonnull Store<EntityStore> store) {

        if (!checkAccess(store, ref)) {
            closePage(ref, store);
            return;
        }

        cmd.append("RageSignature/Settings.ui");
        updateStates(cmd, events, RageSignature.get().getConfig(), true);

        events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#Delay #Slider",
                new EventData()
                        .append(ACTION, SettingsActions.UPDATE_DELAY.name())
                        .append(VALUE, "#Delay #Slider.Value"),
                false
        );
        events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#Delay #Field",
                new EventData()
                        .append(ACTION, SettingsActions.UPDATE_DELAY.name())
                        .append(VALUE, "#Delay #Field.Value"),
                false
        );

        events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#Decay #Slider",
                new EventData()
                        .append(ACTION, SettingsActions.UPDATE_DECAY.name())
                        .append(VALUE, "#Decay #Slider.Value"),
                false
        );
        events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#Decay #Field",
                new EventData()
                        .append(ACTION, SettingsActions.UPDATE_DECAY.name())
                        .append(VALUE, "#Decay #Field.Value"),
                false
        );

        events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#Ratio #Slider",
                new EventData()
                        .append(ACTION, SettingsActions.UPDATE_RATIO.name())
                        .append(VALUE, "#Ratio #Slider.Value"),
                false
        );
        events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#Ratio #Field",
                new EventData()
                        .append(ACTION, SettingsActions.UPDATE_RATIO.name())
                        .append(VALUE, "#Ratio #Field.Value"),
                false
        );

        events.addEventBinding(CustomUIEventBindingType.Activating,
                "#CloseButton", EventData.of(ACTION, SettingsActions.CLOSE.name()));
    }

    private void updateStates(UICommandBuilder uiBuilder, UIEventBuilder eventsBuilder, RageConfig conf, boolean init) {
        UICommandBuilder uiCmd = uiBuilder != null ? uiBuilder : new UICommandBuilder();
        UIEventBuilder events = eventsBuilder != null ? eventsBuilder : new UIEventBuilder();

        uiCmd.set("#Decay #Slider.Value", conf.getDecay());
        uiCmd.set("#Decay #Field.Value", conf.getDecay());
        uiCmd.set("#Delay #Slider.Value", conf.getDelay());
        uiCmd.set("#Delay #Field.Value", conf.getDelay());
        uiCmd.set("#Ratio #Slider.Value", (int)(conf.getRatio() * 100));
        uiCmd.set("#Ratio #Field.Value", (int)(conf.getRatio() * 100));

        if (!init) {
            this.sendUpdate(uiCmd, events, false);
        }
    }

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public void handleDataEvent(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl Store<EntityStore> store, @NonNullDecl UIData data) {
        if (!checkAccess(store, ref) ||
                SettingsActions.CLOSE.name().equals(data.action)
        ) {
            closePage(ref, store);
            return;
        }
        RageConfig conf = RageSignature.get().getConfig();

        if (SettingsActions.UPDATE_DECAY.name().equals(data.action)) {
            conf.setDecay(Math.clamp((int)data.value, 1, 100));
            RageSignature.get().saveConfig();
            updateStates(null, null, conf, false);
        } else if (SettingsActions.UPDATE_DELAY.name().equals(data.action)) {
            conf.setDelay(Math.clamp((int)data.value, 2, 60));
            RageSignature.get().saveConfig();
            updateStates(null, null, conf, false);
        } else if ( SettingsActions.UPDATE_RATIO.name().equals(data.action) ) {
            conf.setRatio((float)Math.clamp(data.value / 100, 0.01, Math.abs(data.value)));
            RageSignature.get().saveConfig();
            updateStates(null, null, conf, false);
        }
    }
}
