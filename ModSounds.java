package com.pixelstats.registry;

import com.pixelstats.PixelStatsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PixelStatsMod.MODID);

    public static final RegistryObject<SoundEvent> UI_OPEN = reg("ui.open");
    public static final RegistryObject<SoundEvent> UI_CLOSE = reg("ui.close");
    public static final RegistryObject<SoundEvent> UI_HOVER = reg("ui.hover");
    public static final RegistryObject<SoundEvent> UI_CLICK = reg("ui.click");
    public static final RegistryObject<SoundEvent> UI_UPGRADE = reg("ui.upgrade");
    public static final RegistryObject<SoundEvent> UI_DENY = reg("ui.deny");
    public static final RegistryObject<SoundEvent> DODGE = reg("dodge");

    private ModSounds() {}

    private static RegistryObject<SoundEvent> reg(String name) {
        return SOUNDS.register(name,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PixelStatsMod.MODID, name)));
    }
}
