package com.example.worldpreset;

import com.example.worldpreset.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.world.level.GameType;

public class WorldPresetMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ModConfig config = ModConfig.getInstance();

            if (!config.gameRules.isEmpty()) {
                for (var entry : config.gameRules.entrySet()) {
                    String command = "gamerule " + entry.getKey() + " " + entry.getValue();
                    server.getCommands().performPrefixedCommand(
                        server.createCommandSourceStack(), command
                    );
                }
            }

            if (!config.gamemode.isEmpty()) {
                GameType gameType = GameType.byName(config.gamemode);
                if (gameType != null) {
                    server.setDefaultGameType(gameType);
                }
            }
        });
    }
}