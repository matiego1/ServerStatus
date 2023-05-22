package me.matiego.serverstatus;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
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
            config.set(current.getAddress(), current.toString());
        }
    }

    public boolean checkIfDataIsSaved(@NotNull Data data) {
        FileConfiguration config = loadConfig(false);
        if (config == null) return false;
        return data.toString().equals(config.getString(data.getAddress()));
    }

    private @Nullable FileConfiguration loadConfig(boolean replaceFile) {
        try {
            File file = new File(plugin.getDataFolder(), FILE_NAME);
            if (!file.exists()) {
                plugin.saveResource(FILE_NAME, replaceFile);
            }

            FileConfiguration config = new YamlConfiguration();
            config.load(file);
            return config;
        } catch (Exception e) {
            Main.error("An error occurred while loading " + FILE_NAME, e);
        }
        return null;
    }
}
