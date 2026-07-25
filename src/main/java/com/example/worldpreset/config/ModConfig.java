package com.example.worldpreset.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
                GSON.toJson(getInstance(), writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ModConfig load() {
        if (Files.exists(CONFIG_FILE)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                return GSON.fromJson(reader, ModConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ModConfig();
    }

    public static Path getDatapacksDir() {
        try {
            Files.createDirectories(DATAPACKS_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return DATAPACKS_DIR;
    }

    public static void copyDatapacksToWorld(String worldName) {
        Path sourceDir = getDatapacksDir();
        Path worldDir = FabricLoader.getInstance().getGameDir().resolve("saves").resolve(worldName);
        Path worldDatapacks = worldDir.resolve("datapacks");

        if (!Files.exists(sourceDir)) return;

        try {
            Files.createDirectories(worldDatapacks);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
                for (Path source : stream) {
                    Path target = worldDatapacks.resolve(source.getFileName());
                    if (!Files.exists(target)) {
                        if (Files.isDirectory(source)) {
                            copyDirectory(source, target);
                        } else {
                            Files.copy(source, target);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        Files.createDirectories(target);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(source)) {
            for (Path file : stream) {
                Path targetFile = target.resolve(file.getFileName());
                if (Files.isDirectory(file)) {
                    copyDirectory(file, targetFile);
                } else {
                    Files.copy(file, targetFile);
                }
            }
        }
    }
}