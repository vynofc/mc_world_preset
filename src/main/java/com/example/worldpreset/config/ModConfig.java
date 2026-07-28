package com.example.worldpreset.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("worldpreset");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.json");
    private static final Path DATAPACKS_DIR = CONFIG_DIR.resolve("datapacks");
    private static ModConfig INSTANCE;

    public String worldPreset = "";
    public String gamemode = "";
    public List<String> datapacks = new ArrayList<>();
    public Map<String, String> gameRules = new LinkedHashMap<>();

    public static ModConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
            Files.createDirectories(DATAPACKS_DIR);
            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
                ModConfig config = getInstance();
                config.normalize();
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ModConfig load() {
        if (Files.exists(CONFIG_FILE)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                ModConfig config = GSON.fromJson(reader, ModConfig.class);
                if (config != null) {
                    config.normalize();
                    return config;
                }
            } catch (IOException | JsonParseException e) {
                e.printStackTrace();
            }
        }
        return new ModConfig();
    }

    private void normalize() {
        if (this.worldPreset == null) {
            this.worldPreset = "";
        }
        if (this.gamemode == null) {
            this.gamemode = "";
        }

        if (this.datapacks == null) {
            this.datapacks = new ArrayList<>();
        } else {
            this.datapacks = new ArrayList<>(new LinkedHashSet<>(this.datapacks));
            this.datapacks.removeIf(Objects::isNull);
        }

        if (this.gameRules == null) {
            this.gameRules = new LinkedHashMap<>();
        } else {
            Map<String, String> normalizedRules = new LinkedHashMap<>();
            this.gameRules.forEach((rule, value) -> {
                if (rule != null && value != null) {
                    normalizedRules.put(rule, value);
                }
            });
            this.gameRules = normalizedRules;
        }
    }

    public static Path getDatapacksDir() {
        try {
            Files.createDirectories(DATAPACKS_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return DATAPACKS_DIR;
    }

    }