package me.matiego.serverstatus;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class Main extends JavaPlugin {

    private Data last;
    private BukkitTask task;
    @Getter
    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        task = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            reloadConfig();
            String address = getConfig().getString("address");
            if (address == null) {
                getLogger().severe("An error occurred while loading the server address from the configuration file.");
                return;
            }
            Data current = Data.load(address);
            if (current == null) return;
            if (current.equals(last)) return;
            last = current;
            current.sendWebhook(getConfig().getString("webhook-url", ""));
        }, 20 * 10, 20 * 60 * 5);
    }

    @Override
    public void onDisable() {
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
