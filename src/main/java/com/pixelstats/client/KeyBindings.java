package com.pixelstats.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {
    /** Default key: K (can be changed in Options > Controls > Pixel Stats). */
    public static final KeyMapping OPEN = new KeyMapping(
            "key.pixelstats.open",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories.pixelstats");

    private KeyBindings() {}
}
