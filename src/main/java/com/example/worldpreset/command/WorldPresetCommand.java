package com.example.worldpreset.command;

import com.example.worldpreset.config.ModConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.util.stream.Collectors;

public class WorldPresetCommand {
    private static final SuggestionProvider<FabricClientCommandSource> PRESET_SUGGESTIONS =
        (context, builder) -> {
            Registry<WorldPreset> registry = context.getSource().getClient()
                .level.registryAccess().lookup(Registries.WORLD_PRESET).orElse(null);
            if (registry != null) {
                return SharedSuggestionProvider.suggestResource(
                    registry.keySet().stream(),
                    builder
                );
            }
            return builder.buildFuture();
        };

    private static final SuggestionProvider<FabricClientCommandSource> GAMEMODE_SUGGESTIONS =
        (context, builder) -> SharedSuggestionProvider.suggest(
            new String[]{"survival", "creative", "adventure", "spectator"},
            builder
        );

    private static final SuggestionProvider<FabricClientCommandSource> GAMERULE_SUGGESTIONS =
        (context, builder) -> {
            Registry<GameRule<?>> registry = context.getSource().getClient()
                .level.registryAccess().lookup(Registries.GAME_RULE).orElse(null);
            if (registry != null) {
                return SharedSuggestionProvider.suggestResource(
                    registry.keySet().stream(),
                    builder
                );
            }
            return builder.buildFuture();
        };

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(
            ClientCommands.literal("worldpreset")
                .then(ClientCommands.literal("preset")
                    .then(ClientCommands.literal("set")
                        .then(ClientCommands.argument("preset", StringArgumentType.word())
                            .suggests(PRESET_SUGGESTIONS)
                            .executes(WorldPresetCommand::setPreset)
                        )
                    )
                    .then(ClientCommands.literal("list")
                        .executes(WorldPresetCommand::listPresets)
                    )
                    .then(ClientCommands.literal("clear")
                        .executes(WorldPresetCommand::clearPreset)
                    )
                )
                .then(ClientCommands.literal("datapack")
                    .then(ClientCommands.literal("add")
                        .then(ClientCommands.argument("name", StringArgumentType.string())
                            .executes(WorldPresetCommand::addDatapack)
                        )
                    )
                    .then(ClientCommands.literal("remove")
                        .then(ClientCommands.argument("name", StringArgumentType.string())
                            .suggests((context, builder) -> {
                                ModConfig config = ModConfig.getInstance();
                                return SharedSuggestionProvider.suggest(config.datapacks, builder);
                            })
                            .executes(WorldPresetCommand::removeDatapack)
                        )
                    )
                    .then(ClientCommands.literal("list")
                        .executes(WorldPresetCommand::listDatapacks)
                    )
                    .then(ClientCommands.literal("clear")
                        .executes(WorldPresetCommand::clearDatapacks)
                    )
                )
                .then(ClientCommands.literal("gamerule")
                    .then(ClientCommands.argument("rule", StringArgumentType.word())
                        .suggests(GAMERULE_SUGGESTIONS)
                        .then(ClientCommands.argument("value", StringArgumentType.greedyString())
                            .executes(WorldPresetCommand::setGameRule)
                        )
                    )
                    .then(ClientCommands.literal("list")
                        .executes(WorldPresetCommand::listGameRules)
                    )
                )
                .then(ClientCommands.literal("gamemode")
                    .then(ClientCommands.argument("mode", StringArgumentType.word())
                        .suggests(GAMEMODE_SUGGESTIONS)
                        .executes(WorldPresetCommand::setGamemode)
                    )
                    .then(ClientCommands.literal("clear")
                        .executes(WorldPresetCommand::clearGamemode)
                    )
                )
                .then(ClientCommands.literal("config")
                    .executes(WorldPresetCommand::showConfig)
                )
        );
    }

    private static int setPreset(CommandContext<FabricClientCommandSource> context) {
        String presetId = StringArgumentType.getString(context, "preset");
        Identifier id = Identifier.tryParse(presetId);
        if (id == null) {
            id = Identifier.withDefaultNamespace(presetId);
        }

        Registry<WorldPreset> registry = context.getSource().getClient()
            .level.registryAccess().lookup(Registries.WORLD_PRESET).orElse(null);
        if (registry == null || !registry.containsKey(id)) {
            context.getSource().sendFeedback(Component.translatable("commands.worldpreset.unknown_preset", presetId));
            return 0;
        }

        ModConfig config = ModConfig.getInstance();
        config.worldPreset = id.toString();
        ModConfig.save();
        context.getSource().sendFeedback(Component.translatable("commands.worldpreset.presets.saved", id.toString()));
        return 1;
    }

    private static int listPresets(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.translatable("commands.worldpreset.presets.list_title"));
        Registry<WorldPreset> registry = context.getSource().getClient()
            .level.registryAccess().lookup(Registries.WORLD_PRESET).orElse(null);
        if (registry != null) {
            registry.keySet().forEach(key -> {
                context.getSource().sendFeedback(Component.translatable(
                    "commands.worldpreset.presets.list_entry", key.toString()));
            });
        }
        return 1;
    }

    private static int clearPreset(CommandContext<FabricClientCommandSource> context) {
        ModConfig config = ModConfig.getInstance();
        config.worldPreset = "";
        ModConfig.save();
        context.getSource().sendFeedback(Component.translatable("commands.worldpreset.presets.cleared"));
        return 1;
    }

    private static int addDatapack(CommandContext<FabricClientCommandSource> context) {
        String name = StringArgumentType.getString(context, "name");
        ModConfig config = ModConfig.getInstance();
        if (config.datapacks.contains(name)) {
            context.getSource().sendFeedback(Component.translatable("commands.worldpreset.datapack.already_exists", name));
            return 0;
        }
        config.datapacks.add(name);
        ModConfig.save();
        context.getSource().sendFeedback(Component.translatable("commands.worldpreset.datapacks.added", name));
        return 1;
    }

    private static int removeDatapack(CommandContext<FabricClientCommandSource> context) {
        String name = StringArgumentType.getString(context, "name");
        ModConfig config = ModConfig.getInstance();
        if (!config.datapacks.remove(name)) {
            context.getSource().sendFeedback(Component.translatable("commands.worldpreset.datapack.not_found", name));
            return 0;
        }
        ModConfig.save();
        context.getSource().sendFeedback(Component.translatable("commands.worldpreset.datapacks.removed", name));
        return 1;
    }

    private static int listDatapacks(CommandContext<FabricClientCommandSource> context) {
        ModConfig config = ModConfig.getInstance();
        if (config.datapacks.isEmpty()) {
            context.getSource().sendFeedback(Component.translatable("commands.worldpreset.datapacks.none"));
            return 0;
        }
        context.getSource().sendFeedback(Component.translatable("commands.worldpreset.datapacks.list_title"));
        config.datapacks.forEach(name -> {
            context.getSource().sendFeedback(Component.translatable("commands.worldpreset.datapacks.list_entry", name));
        });
        return 1;
    }

    private static int clearDatapacks(CommandContext<FabricClientCommandSource> context) {
        ModConfig config = ModConfig.getInstance();
        config.datapacks.clear();
        ModConfig.save();
        context.getSource().sendFeedback(Component.translatable("commands.worldpreset.datapacks.cleared"));
        return 1;
    }

    private static int setGameRule(CommandContext<FabricClientCommandSource> context) {
        String ruleName = StringArgumentType.getString(context, "rule");
        String value = StringArgumentType.getString(context, "value");

        Registry<GameRule<?>> registry = context.getSource().getClient()
            .level.registryAccess().lookup(Registries.GAME_RULE).orElse(null);
        if (registry == null) {
            context.getSource().sendFeedback(Component.translatable("commands.worldpreset.unknown_gamerule", ruleName));
            return 0;
        }

        boolean found = registry.keySet().stream().anyMatch(
            key -> key.getPath().equals(ruleName) || key.toString().equals(ruleName)
        );
        if (!found) {
            context.getSource().sendFeedback(Component.translatable("commands.worldpreset.unknown_gamerule", ruleName));
            return 0;
        }

        ModConfig config = ModConfig.getInstance();
        config.gameRules.put(ruleName, value);
        ModConfig.save();
        context.getSource().sendFeedback(Component.translatable("commands.worldpreset.gamerules.set", ruleName, value));
        return 1;
    }

    private static int listGameRules(CommandContext<FabricClientCommandSource> context) {
        ModConfig config = ModConfig.getInstance();
        if (config.gameRules.isEmpty()) {
            context.getSource().sendFeedback(Component.translatable("commands.worldpreset.gamerules.none"));
            return 0;
        }
        context.getSource().sendFeedback(Component.translatable("commands.worldpreset.gamerules.list_title"));
        config.gameRules.forEach((rule, value) -> {
            context.getSource().sendFeedback(Component.translatable(
                "commands.worldpreset.gamerules.list_entry", rule, value));
        });
        return 1;
    }

    private static int setGamemode(CommandContext<FabricClientCommandSource> context) {
        String mode = StringArgumentType.getString(context, "mode").toLowerCase();
        GameType gameType = switch (mode) {
            case "survival", "s", "0" -> GameType.SURVIVAL;
            case "creative", "c", "1" -> GameType.CREATIVE;
            case "adventure", "a", "2" -> GameType.ADVENTURE;
            case "spectator", "sp", "3" -> GameType.SPECTATOR;
            default -> null;
        };

        if (gameType == null) {
            context.getSource().sendFeedback(Component.translatable("commands.worldpreset.unknown_gamemode", mode));
            return 0;
        }

        ModConfig config = ModConfig.getInstance();
        config.gamemode = gameType.getName();
        ModConfig.save();
        context.getSource().sendFeedback(Component.translatable("commands.worldpreset.gamemode.set", gameType.getName()));
        return 1;
    }

    private static int clearGamemode(CommandContext<FabricClientCommandSource> context) {
        ModConfig config = ModConfig.getInstance();
        config.gamemode = "";
        ModConfig.save();
        context.getSource().sendFeedback(Component.translatable("commands.worldpreset.gamemode.cleared"));
        return 1;
    }

    private static int showConfig(CommandContext<FabricClientCommandSource> context) {
        ModConfig config = ModConfig.getInstance();
        String preset = config.worldPreset.isEmpty() ? "none" : config.worldPreset;
        String gamemode = config.gamemode.isEmpty() ? "none" : config.gamemode;
        String datapacks = config.datapacks.isEmpty() ? "none" : String.join(", ", config.datapacks);
        String gameRules = config.gameRules.isEmpty() ? "none"
            : config.gameRules.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", "));

        context.getSource().sendFeedback(Component.translatable(
            "commands.worldpreset.config.show", preset, gamemode, datapacks, gameRules
        ));
        return 1;
    }
}