package com.pixelstats.events;

import com.pixelstats.PixelStatsMod;
import com.pixelstats.command.PixelStatsCommand;
import com.pixelstats.config.PixelConfig;
import com.pixelstats.network.ModNetwork;
import com.pixelstats.registry.ModSounds;
import com.pixelstats.stats.PlayerStats;
import com.pixelstats.stats.PlayerStatsProvider;
import com.pixelstats.stats.StatEffects;
import com.pixelstats.stats.StatMath;
import com.pixelstats.stats.StatType;
import com.pixelstats.stats.StatsApi;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.common.capabilities.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/** Server-side game logic of Pixel Stats (registered automatically on the Forge event bus). */
@Mod.EventBusSubscriber(modid = PixelStatsMod.MODID)
public final class StatEvents {
    private static final ResourceLocation CAP_ID = new ResourceLocation(PixelStatsMod.MODID, "stats");

    private StatEvents() {}

    // ------------------------------------------------------------------ data attach / copy / sync

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            PlayerStatsProvider provider = new PlayerStatsProvider();
            event.addCapability(CAP_ID, provider);
            event.addListener(provider::invalidate);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.isWasDeath() && !PixelConfig.get(PixelConfig.KEEP_ON_DEATH)) {
            return;
        }
        Player original = event.getOriginal();
        original.reviveCaps();
        try {
            StatsApi.get(event.getEntity()).copyFrom(StatsApi.get(original));
        } finally {
            original.invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            StatEffects.apply(player);
            ModNetwork.syncTo(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            StatEffects.apply(player);
            if (!event.isEndConquered()) {
                player.setHealth(player.getMaxHealth());
            }
            ModNetwork.syncTo(player);
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ModNetwork.syncTo(player);
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        PixelStatsCommand.register(event.getDispatcher());
    }

    // ------------------------------------------------------------------ DEX: dodge

    @SubscribeEvent
    public static void onAttacked(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        DamageSource source = event.getSource();
        // only dodge hits that come from some entity (mobs, players, arrows...), never fall/void/hunger/etc.
        if (source.getEntity() == null && source.getDirectEntity() == null) {
            return;
        }
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || player.invulnerableTime > 10) {
            return;
        }
        int dex = StatsApi.get(player).get(StatType.DEX);
        if (dex <= 0 || player.getRandom().nextDouble() >= StatMath.dexDodge(dex)) {
            return;
        }
        event.setCanceled(true);

        ServerLevel level = player.serverLevel();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.DODGE.get(),
                SoundSource.PLAYERS, 0.8F, 0.95F + player.getRandom().nextFloat() * 0.1F);
        level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 1.0, player.getZ(),
                10, 0.3, 0.4, 0.3, 0.03);
        player.displayClientMessage(
                Component.translatable("message.pixelstats.dodge").withStyle(ChatFormatting.GREEN), true);
    }

    // ------------------------------------------------------------------ DEX ranged damage + LUC ranged crit

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Entity direct = source.getDirectEntity();
        if (!(direct instanceof Projectile) || isIronsSpellsEntity(direct)) {
            return; // melee is handled by attributes / CriticalHitEvent, spells by INT
        }
        PlayerStats stats = StatsApi.get(player);
        float amount = event.getAmount();

        int dex = stats.get(StatType.DEX);
        if (dex > 0) {
            amount *= 1.0F + (float) StatMath.dexRanged(dex);
        }

        int luc = stats.get(StatType.LUC);
        if (luc > 0 && player.getRandom().nextDouble() < StatMath.lucCrit(luc)) {
            amount *= 1.5F;
            LivingEntity victim = event.getEntity();
            player.serverLevel().sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY(0.5), victim.getZ(),
                    12, 0.3, 0.3, 0.3, 0.2);
        }
        event.setAmount(amount);
    }

    private static boolean isIronsSpellsEntity(Entity entity) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return key != null && key.getNamespace().equals("irons_spellbooks");
    }

    // ------------------------------------------------------------------ LUC melee crit

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.isVanillaCritical()) {
            return;
        }
        if (player.getAttackStrengthScale(0.5F) < 0.9F) {
            return; // only fully charged swings can crit
        }
        int luc = StatsApi.get(player).get(StatType.LUC);
        if (luc > 0 && player.getRandom().nextDouble() < StatMath.lucCrit(luc)) {
            event.setResult(Event.Result.ALLOW);
            event.setDamageModifier(1.5F);
        }
    }

    // ------------------------------------------------------------------ CON: resist harmful effects

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        MobEffectInstance instance = event.getEffectInstance();
        if (instance.getEffect().getCategory() != MobEffectCategory.HARMFUL) {
            return;
        }
        int con = StatsApi.get(player).get(StatType.CON);
        if (con > 0 && player.getRandom().nextDouble() < StatMath.conEffectResist(con)) {
            event.setResult(Event.Result.DENY);
        }
    }

    // ------------------------------------------------------------------ CON: regeneration

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !event.side.isServer()) {
            return;
        }
        Player player = event.player;
        if (player.tickCount % 40 != 0 || !player.isAlive() || player.getHealth() >= player.getMaxHealth()) {
            return;
        }
        int con = StatsApi.get(player).get(StatType.CON);
        if (con > 0 && player.getFoodData().getFoodLevel() > 0) {
            player.heal((float) StatMath.conRegen(con));
        }
    }
}
