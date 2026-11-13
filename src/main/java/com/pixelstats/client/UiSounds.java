package com.pixelstats.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

/** Plays UI sound effects (non-positional). */
public final class UiSounds {
    private UiSounds() {}

    public static void play(RegistryObject<SoundEvent> sound) {
        play(sound, 1.0F, 1.0F);
    }

    public static void play(RegistryObject<SoundEvent> sound, float pitch, float volume) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound.get(), pitch, volume));
    }
}
