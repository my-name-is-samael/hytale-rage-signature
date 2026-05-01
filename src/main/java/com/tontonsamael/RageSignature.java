package com.tontonsamael;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PacketFilter;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import com.tontonsamael.RageSignature.commands.SettingsCommand;
import com.tontonsamael.RageSignature.config.RageConfig;
import com.tontonsamael.RageSignature.event.PacketHandler;
import com.tontonsamael.RageSignature.systems.RageHitListener;
import com.tontonsamael.RageSignature.systems.RageInitListener;
import com.tontonsamael.RageSignature.systems.RageTickSystem;

import javax.annotation.Nonnull;

public class RageSignature extends JavaPlugin {
    private static RageSignature INSTANCE;

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final Config<RageConfig> config = this.withConfig("baseConfig", RageConfig.CODEC);

    public RageSignature(@Nonnull JavaPluginInit init) {
        super(init);
        INSTANCE = this;
    }

    private PacketFilter inboundFilter;

    @Override
    protected void setup() {
        saveConfig();

        this.getCommandRegistry().registerCommand(new SettingsCommand());

        this.getEntityStoreRegistry().registerSystem(new RageTickSystem());
        PacketHandler handler = new PacketHandler();
        inboundFilter = PacketAdapters.registerInbound(handler);
        this.getEntityStoreRegistry().registerSystem(new RageInitListener());
        this.getEntityStoreRegistry().registerSystem(new RageHitListener());

        LOGGER.atInfo().log("Prototype is loaded !");
    }

    @Override
    protected void shutdown() {
        if (inboundFilter != null) {
            PacketAdapters.deregisterInbound(inboundFilter);
        }
    }

    public static RageSignature get() {
        return INSTANCE;
    }

    public RageConfig getConfig() {
        return config.get();
    }
    public void saveConfig() {
        config.save();
    }
}