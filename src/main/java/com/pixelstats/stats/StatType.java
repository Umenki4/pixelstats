package com.pixelstats.stats;

import com.pixelstats.PixelStatsMod;
import java.util.Locale;
import net.minecraft.resources.ResourceLocation;

/** The five upgradable stats. The enum order is also the network/storage order - do not reorder. */
public enum StatType {
    DEX(0xFF6BE36B),
    STR(0xFFFF6B5B),
    CON(0xFFFFB347),
    INT(0xFF6BA8FF),
    LUC(0xFFFFE45E);

    public static final int COUNT = values().length;

    /** ARGB colour used by the UI. */
    public final int color;

    StatType(int color) {
        this.color = color;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public ResourceLocation icon() {
        return new ResourceLocation(PixelStatsMod.MODID, "textures/gui/icon_" + key() + ".png");
    }

    public static StatType byId(int id) {
        StatType[] all = values();
        return id >= 0 && id < all.length ? all[id] : null;
    }

    public static StatType byName(String name) {
        for (StatType t : values()) {
            if (t.name().equalsIgnoreCase(name)) {
                return t;
            }
        }
        return null;
    }
}
