package com.example.worldpreset.mixin;

import com.example.worldpreset.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
abstract class ScreenMixin {
    @Shadow
    @Final
    protected Minecraft minecraft;
}

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends ScreenMixin {

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        ModConfig config = ModConfig.getInstance();
        CreateWorldScreen self = (CreateWorldScreen) (Object) this;

        if (!config.worldPreset.isEmpty()) {
            Identifier presetId = Identifier.tryParse(config.worldPreset);
            if (presetId == null) {
                presetId = Identifier.withDefaultNamespace(config.worldPreset);
            }

            Registry<WorldPreset> registry = this.minecraft.level.registryAccess()
                .lookup(Registries.WORLD_PRESET).orElse(null);
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
}