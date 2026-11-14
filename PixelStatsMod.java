package com.pixelstats;

import com.pixelstats.config.PixelConfig;
import com.pixelstats.network.ModNetwork;
import com.pixelstats.registry.ModSounds;
import com.pixelstats.stats.PlayerStats;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/** Pixel Stats - endless RPG stat upgrades (DEX / STR / CON / INT / LUC). */
@Mod(PixelStatsMod.MODID)
public class PixelStatsMod {
    public static final String MODID = "pixelstats";

    public PixelStatsMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModSounds.SOUNDS.register(modBus);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::registerCapabilities);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, PixelConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerStats.class);
    }
}
