package com.example.worldpreset.config;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WorldPresetConfigScreen extends Screen {
    private static final List<String> GAMEMODES = List.of(
        "", "survival", "creative", "adventure", "spectator"
    );

    private final Screen parent;
    private EditBox worldPresetField;
    private CycleButton<String> gamemodeButton;
    private EditBox datapacksField;
    private EditBox gameRulesField;
    private Button saveButton;
    private Component validationError;
    private int contentLeft;
    private int formTop;

    public WorldPresetConfigScreen(Screen parent) {
        super(Component.translatable("screen.worldpreset.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ModConfig config = ModConfig.getInstance();
        int contentWidth = Math.min(310, Math.max(120, this.width - 40));
        this.contentLeft = (this.width - contentWidth) / 2;
        this.formTop = Math.max(24, (this.height - 172) / 2);

        this.worldPresetField = new EditBox(
            this.font,
            this.contentLeft,
            this.formTop + 11,
            contentWidth,
            20,
            Component.translatable("screen.worldpreset.config.world_preset")
        );
        this.worldPresetField.setMaxLength(256);
        this.worldPresetField.setValue(config.worldPreset == null ? "" : config.worldPreset);
        this.worldPresetField.setHint(Component.translatable("screen.worldpreset.config.world_preset.hint"));
        this.addRenderableWidget(this.worldPresetField);

        String gamemode = GAMEMODES.contains(config.gamemode) ? config.gamemode : "";
        this.gamemodeButton = CycleButton.<String>builder(
                WorldPresetConfigScreen::gamemodeText,
                gamemode
            )
            .withValues(GAMEMODES)
            .create(
                this.contentLeft,
                this.formTop + 37,
                contentWidth,
                20,
                Component.translatable("screen.worldpreset.config.gamemode")
            );
        this.addRenderableWidget(this.gamemodeButton);

        this.datapacksField = new EditBox(
            this.font,
            this.contentLeft,
            this.formTop + 74,
            contentWidth,
            20,
            Component.translatable("screen.worldpreset.config.datapacks")
        );
        this.datapacksField.setMaxLength(2048);
        this.datapacksField.setValue(config.datapacks == null ? "" : String.join(", ", config.datapacks));
        this.datapacksField.setHint(Component.translatable("screen.worldpreset.config.datapacks.hint"));
        this.addRenderableWidget(this.datapacksField);

        this.gameRulesField = new EditBox(
            this.font,
            this.contentLeft,
            this.formTop + 111,
            contentWidth,
            20,
            Component.translatable("screen.worldpreset.config.game_rules")
        );
        this.gameRulesField.setMaxLength(4096);
        this.gameRulesField.setValue(formatGameRules(config.gameRules));
        this.gameRulesField.setHint(Component.translatable("screen.worldpreset.config.game_rules.hint"));
        this.addRenderableWidget(this.gameRulesField);

        int buttonWidth = (contentWidth - 8) / 3;
        int buttonY = this.formTop + 150;
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.worldpreset.config.reset"),
                button -> this.resetFields()
            )
            .bounds(this.contentLeft, buttonY, buttonWidth, 20)
            .build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.worldpreset.config.cancel"),
                button -> this.onClose()
            )
            .bounds(this.contentLeft + buttonWidth + 4, buttonY, buttonWidth, 20)
            .build());
        this.saveButton = Button.builder(
                Component.translatable("screen.worldpreset.config.save"),
                button -> this.saveAndClose()
            )
            .bounds(this.contentLeft + (buttonWidth + 4) * 2, buttonY, buttonWidth, 20)
            .build();
        this.addRenderableWidget(this.saveButton);

        this.worldPresetField.setResponder(value -> this.validateInput());
        this.datapacksField.setResponder(value -> this.validateInput());
        this.gameRulesField.setResponder(value -> this.validateInput());
        this.validateInput();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, this.title, this.width / 2, this.formTop - 20, 0xFFFFFF);
        graphics.text(
            this.font,
            Component.translatable("screen.worldpreset.config.world_preset"),
            this.contentLeft,
            this.formTop,
            0xA0A0A0
        );
        graphics.text(
            this.font,
            Component.translatable("screen.worldpreset.config.datapacks"),
            this.contentLeft,
            this.formTop + 63,
            0xA0A0A0
        );
        graphics.text(
            this.font,
            Component.translatable("screen.worldpreset.config.game_rules"),
            this.contentLeft,
            this.formTop + 100,
            0xA0A0A0
        );
        if (this.validationError != null) {
            graphics.centeredText(
                this.font,
                this.validationError,
                this.width / 2,
                this.formTop + 137,
                0xFF5555
            );
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(this.parent);
    }

    private void resetFields() {
        this.worldPresetField.setValue("");
        this.gamemodeButton.setValue("");
        this.datapacksField.setValue("");
        this.gameRulesField.setValue("");
        this.validateInput();
    }

    private void saveAndClose() {
        this.validateInput();
        if (this.validationError != null) {
            return;
        }

        ModConfig config = ModConfig.getInstance();
        config.worldPreset = normalizePreset(this.worldPresetField.getValue());
        config.gamemode = this.gamemodeButton.getValue();
        config.datapacks = parseDatapacks(this.datapacksField.getValue());
        config.gameRules = parseGameRules(this.gameRulesField.getValue());
        ModConfig.save();
        this.onClose();
    }

    private void validateInput() {
        String preset = this.worldPresetField.getValue().trim();
        if (!preset.isEmpty() && normalizePreset(preset) == null) {
            this.validationError = Component.translatable("screen.worldpreset.config.error.preset", preset);
            this.saveButton.active = false;
            return;
        }

        for (String datapack : parseDatapacks(this.datapacksField.getValue())) {
            if (!isValidDatapackName(datapack)) {
                this.validationError = Component.translatable("screen.worldpreset.config.error.datapack", datapack);
                this.saveButton.active = false;
                return;
            }
        }

        if (parseGameRules(this.gameRulesField.getValue()) == null) {
            this.validationError = Component.translatable("screen.worldpreset.config.error.game_rule");
            this.saveButton.active = false;
            return;
        }

        this.validationError = null;
        this.saveButton.active = true;
    }

    private static Component gamemodeText(String gamemode) {
        String suffix = gamemode.isEmpty() ? "none" : gamemode;
        return Component.translatable("screen.worldpreset.config.gamemode." + suffix);
    }

    private static String normalizePreset(String value) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        Identifier identifier = Identifier.tryParse(trimmed);
        if (identifier == null && !trimmed.contains(":")) {
            identifier = Identifier.tryParse("minecraft:" + trimmed);
        }
        return identifier == null ? null : identifier.toString();
    }

    private static List<String> parseDatapacks(String value) {
        List<String> datapacks = new ArrayList<>();
        Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(entry -> !entry.isEmpty())
            .distinct()
            .forEach(datapacks::add);
        return datapacks;
    }

    private static boolean isValidDatapackName(String name) {
        return !name.equals(".")
            && !name.equals("..")
            && !name.contains("/")
            && !name.contains("\\");
    }

    private static Map<String, String> parseGameRules(String value) {
        Map<String, String> gameRules = new LinkedHashMap<>();
        if (value.isBlank()) {
            return gameRules;
        }

        for (String assignment : value.split(",")) {
            int separator = assignment.indexOf('=');
            if (separator <= 0 || separator == assignment.length() - 1) {
                return null;
            }

            String rule = assignment.substring(0, separator).trim();
            String ruleValue = assignment.substring(separator + 1).trim();
            if (rule.isEmpty() || ruleValue.isEmpty() || rule.chars().anyMatch(Character::isWhitespace)) {
                return null;
            }
            gameRules.put(rule, ruleValue);
        }
        return gameRules;
    }

    private static String formatGameRules(Map<String, String> gameRules) {
        if (gameRules == null || gameRules.isEmpty()) {
            return "";
        }
        return gameRules.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .reduce((left, right) -> left + ", " + right)
            .orElse("");
    }
}