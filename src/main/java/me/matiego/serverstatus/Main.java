package me.matiego.serverstatus;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;

public final class Main extends JavaPlugin {

    @Getter
    private static Main instance;
    private final HashMap<String, Data> data = new HashMap<>();
    private BukkitTask task;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        task = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            reloadConfig();
            List<String> addresses = getConfig().getStringList("addresses");
            if (addresses.isEmpty()) return;

            for (String address : addresses) {
                Data current = Data.load(address);
                if (current == null) continue;

                if (current.equals(data.remove(address))) continue;
                data.put(current.getAddress(), current);

                if (!current.sendWebhook(getConfig().getString("webhook-url", ""))) {
                    //to avoid spam on the console if the webhook url is invalid
                    return;
                }
            }
        }, 20 * 10, 20 * 60 * 5);
    }

    @Override
    public void onDisable() {
        data.clear();
        if (task != null) task.cancel();
    }

    public static void error(@NotNull String msg, @Nullable Throwable throwable) {
        Main.getInstance().getLogger().severe(msg);

        if (throwable == null) return;
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        for (String line : stringWriter.toString().split("\n")) Main.getInstance().getLogger().severe(line);
    }
}
