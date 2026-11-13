package com.pixelstats.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.pixelstats.config.PixelConfig;
import com.pixelstats.network.ModNetwork;
import com.pixelstats.stats.PlayerStats;
import com.pixelstats.stats.StatEffects;
import com.pixelstats.stats.StatType;
import com.pixelstats.stats.StatsApi;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * /pixelstats (alias /ps) - operator (permission level 2) test commands:
 *   info [player]
 *   add|remove|set <players> <stat> <amount>
 *   reset <players> [stat]
 *   givelevels <players> <amount>      (conjures Minecraft XP levels = upgrade currency)
 */
public final class PixelStatsCommand {
    private enum Mode { ADD, REMOVE, SET }

    private static final SimpleCommandExceptionType UNKNOWN_STAT =
            new SimpleCommandExceptionType(Component.translatable("command.pixelstats.unknown_stat"));

    private PixelStatsCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> root = dispatcher.register(
                Commands.literal("pixelstats")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("info")
                                .executes(ctx -> info(ctx, ctx.getSource().getPlayerOrException()))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> info(ctx, EntityArgument.getPlayer(ctx, "target")))))
                        .then(modifyNode("add", Mode.ADD))
                        .then(modifyNode("remove", Mode.REMOVE))
                        .then(modifyNode("set", Mode.SET))
                        .then(Commands.literal("reset")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(ctx -> reset(ctx, null))
                                        .then(statArgument().executes(ctx -> reset(ctx, parseStat(ctx))))))
                        .then(Commands.literal("givelevels")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100000))
                                                .executes(PixelStatsCommand::giveLevels)))));

        dispatcher.register(Commands.literal("ps")
                .requires(source -> source.hasPermission(2))
                .redirect(root));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> modifyNode(String name, Mode mode) {
        return Commands.literal(name)
                .then(Commands.argument("targets", EntityArgument.players())
                        .then(statArgument()
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0, 1_000_000))
                                        .executes(ctx -> applyModify(ctx, mode)))));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> statArgument() {
        return Commands.argument("stat", StringArgumentType.word())
                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                        Arrays.stream(StatType.values()).map(StatType::name), builder));
    }

    private static StatType parseStat(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        StatType type = StatType.byName(StringArgumentType.getString(ctx, "stat"));
        if (type == null) {
            throw UNKNOWN_STAT.create();
        }
        return type;
    }

    private static int info(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        PlayerStats stats = StatsApi.get(player);
        String name = player.getGameProfile().getName();
        ctx.getSource().sendSuccess(() -> Component.translatable("command.pixelstats.info", name,
                stats.get(StatType.DEX), stats.get(StatType.STR), stats.get(StatType.CON),
                stats.get(StatType.INT), stats.get(StatType.LUC)), false);
        return 1;
    }

    private static int applyModify(CommandContext<CommandSourceStack> ctx, Mode mode) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
        StatType type = parseStat(ctx);
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        long max = PixelConfig.get(PixelConfig.MAX_POINTS);

        for (ServerPlayer player : targets) {
            PlayerStats stats = StatsApi.get(player);
            long current = stats.get(type);
            long result = switch (mode) {
                case ADD -> current + amount;
                case REMOVE -> current - amount;
                case SET -> amount;
            };
            stats.set(type, (int) Math.max(0L, Math.min(max, result)));
            StatEffects.apply(player);
            ModNetwork.syncTo(player);
        }
        int count = targets.size();
        ctx.getSource().sendSuccess(() -> Component.translatable("command.pixelstats.modified", type.name(), count), true);
        return count;
    }

    private static int reset(CommandContext<CommandSourceStack> ctx, StatType only) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
        for (ServerPlayer player : targets) {
            PlayerStats stats = StatsApi.get(player);
            if (only == null) {
                stats.resetAll();
            } else {
                stats.set(only, 0);
            }
            StatEffects.apply(player);
            ModNetwork.syncTo(player);
        }
        int count = targets.size();
        ctx.getSource().sendSuccess(() -> only == null
                ? Component.translatable("command.pixelstats.reset", count)
                : Component.translatable("command.pixelstats.reset_one", only.name(), count), true);
        return count;
    }

    private static int giveLevels(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        for (ServerPlayer player : targets) {
            player.giveExperienceLevels(amount);
        }
        int count = targets.size();
        ctx.getSource().sendSuccess(() -> Component.translatable("command.pixelstats.givelevels", amount, count), true);
        return count;
    }
}
