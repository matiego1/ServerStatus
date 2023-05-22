package me.matiego.serverstatus;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Storage {
    public Storage(@NotNull Main plugin) {
        this.plugin = plugin;
    }

    private final Main plugin;
    private final String FILE_NAME = "storage.yml";

    public void saveDataToFile(@NotNull List<Data> data) {
        FileConfiguration config = loadConfig(true);
        if (config == null) return;
        for (Data current : data) {
            config.set(convertAddress(current.getAddress()), current.toString());
        }
        saveConfig(config);
    }

    public boolean checkIfDataIsSaved(@NotNull FileConfiguration config, @NotNull Data data) {
        return data.toString().equals(config.getString(convertAddress(data.getAddress())));
    }

    private @NotNull String convertAddress(@NotNull String address) {
        return address.replace(".", "_");
    }

    public @Nullable FileConfiguration loadConfig(boolean replaceOldFile) {
        try {
            File file = new File(plugin.getDataFolder(), FILE_NAME);
            if (!file.exists()) {
                plugin.saveResource(FILE_NAME, replaceOldFile);
            }

            FileConfiguration config = new YamlConfiguration();
            config.load(file);
            return config;
        } catch (Exception e) {
            Main.error("An error occurred while loading " + FILE_NAME, e);
        }
        return null;
    }

    private void saveConfig(@NotNull FileConfiguration config) {
        try {
            File file = new File(plugin.getDataFolder(), FILE_NAME);
            config.save(file);
        } catch (IOException e) {
            Main.error("An error occurred while saving " + FILE_NAME, e);
        }
    }
}
