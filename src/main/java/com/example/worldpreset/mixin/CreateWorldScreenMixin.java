package com.example.worldpreset.mixin;

import com.example.worldpreset.config.ModConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {

    @Shadow
    abstract java.nio.file.Path getOrCreateTempDataPackDir();

    protected CreateWorldScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        ModConfig config = ModConfig.getInstance();
        CreateWorldScreen self = (CreateWorldScreen) (Object) this;

        if (!config.worldPreset.isEmpty()) {
            Identifier presetId = Identifier.tryParse(config.worldPreset);
            if (presetId == null) {
                presetId = Identifier.withDefaultNamespace(config.worldPreset);
            }

            RegistryAccess.Frozen registryAccess = self.getUiState().getSettings().worldgenLoadContext();
            Registry<WorldPreset> registry = registryAccess.lookup(Registries.WORLD_PRESET).orElse(null);
            if (registry != null && registry.containsKey(presetId)) {
                Holder<WorldPreset> ref = registry.get(presetId).orElse(null);
                if (ref != null) {
                    self.getUiState().setWorldType(new WorldCreationUiState.WorldTypeEntry(ref));
                }
            }
        }

        if (!config.gamemode.isEmpty()) {
            GameType gameType = GameType.byName(config.gamemode);
            if (gameType != null) {
                WorldCreationUiState.SelectedGameMode selectedGameMode = switch (gameType) {
                    case SURVIVAL -> WorldCreationUiState.SelectedGameMode.SURVIVAL;
                    case CREATIVE -> WorldCreationUiState.SelectedGameMode.CREATIVE;
                    default -> WorldCreationUiState.SelectedGameMode.SURVIVAL;
                };
                self.getUiState().setGameMode(selectedGameMode);
            }
        }
    }

    @Inject(method = "onCreate", at = @At("HEAD"))
    private void onBeforeCreate(CallbackInfo ci) {
        ModConfig config = ModConfig.getInstance();
        if (config.datapacks.isEmpty()) {
            return;
        }

        Path tempDir = this.getOrCreateTempDataPackDir();
        Path sourceDir = ModConfig.getDatapacksDir();
        try {
            for (String datapack : config.datapacks) {
                Path source = sourceDir.resolve(datapack).normalize();
                if (!source.startsWith(sourceDir)
                    || source.equals(sourceDir)
                    || !Files.exists(source)) {
                    continue;
                }

                Path target = tempDir.resolve(source.getFileName());
                if (!Files.exists(target)) {
                    Files.copy(source, target);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}