package me.matiego.serverstatus;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class Main extends JavaPlugin {

    @Getter
    private static Main instance;
    private final ConcurrentHashMap<String, Data> data = new ConcurrentHashMap<>();
    private Storage storage;
    private BukkitTask task;
    private boolean firstCheck = true;

    @Override
    public void onEnable() {
        instance = this;
        storage = new Storage(this);
        saveDefaultConfig();

        task = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            reloadConfig();
            List<String> addresses = getConfig().getStringList("addresses");
            if (addresses.isEmpty()) {
                firstCheck = false;
                return;
            }

            for (String address : addresses) {
                Data current = Data.load(address);
                if (current == null) continue;

                if (firstCheck && storage.checkIfDataIsSaved(current)) continue;

                if (current.equals(data.put(current.getAddress(), current))) continue;

                if (!current.sendWebhook(getConfig().getString("webhook-url", ""))) {
                    firstCheck = false;
                    //to avoid spam in the console if the webhook url is invalid
                    return;
                }
            }

            firstCheck = false;
        }, 20 * 10, 20 * 60 * 5);
    }

    @Override
    public void onDisable() {
        storage.saveDataToFile(data.values().stream().toList());
        data.clear();
        if (task != null) task.cancel();
        instance = null;
    }

    public static void error(@NotNull String msg, @Nullable Throwable throwable) {
        Main.getInstance().getLogger().severe(msg);

        if (throwable == null) return;
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        for (String line : stringWriter.toString().split("\n")) Main.getInstance().getLogger().severe(line);
    }
}
