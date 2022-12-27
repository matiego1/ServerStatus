package me.matiego.serverstatus;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class Data {
    public Data(@NotNull String address, boolean online, @Nullable String motd, @Nullable List<String> players) {
        this.address = address;
        this.online = online;
        this.motd = motd;
        this.players = players;
    }

    @NotNull
    @Getter
    private final String address;
    @Getter
    private final boolean online;
    @Getter
    @Nullable
    private final String motd;
    @Getter
    @Nullable
    private final List<String> players;

    public void sendWebhook(@NotNull String url) {
        try (WebhookClient client = WebhookClient.withUrl(url)) {
            WebhookMessageBuilder builder = new WebhookMessageBuilder();
            builder.setUsername("Status serwera " + getAddress());

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("**Czy online:** ");
            if (isOnline()) {
                stringBuilder.append("`TAK`\n");
                stringBuilder.append("**MOTD:** `").append(getMotd()).append("`\n");
                List<String> players = getPlayers();
                stringBuilder.append("**Gracze online (").append(Objects.requireNonNull(players).size()).append("):**\n");
                for (String player : players) {
                    stringBuilder.append("- `").append(player).append("`\n");
                }
            } else {
                stringBuilder.append("`NIE`\n");
            }
            builder.setContent(stringBuilder.toString());

            client.send(builder.build());
        } catch (Exception e) {
            Main.error("An error occurred while sending the webhook.", e);
        }
    }

    public static @Nullable Data load(@NotNull String address) {
        try {
            URL url = new URL("https://api.mcstatus.io/v2/status/java/" + address);
            Scanner s = new Scanner(url.openStream());

            StringBuilder builder = new StringBuilder();
            while (s.hasNext()) {
                builder.append(s.next());
            }
            s.close();

            JsonObject json = JsonParser.parseString(builder.toString()).getAsJsonObject();
            if (json == null) return null;

            boolean online = json.get("online").getAsBoolean();
            if (!online) return new Data(address, false, null, null);

            List<String> players = new ArrayList<>();
            for (JsonElement e : json.getAsJsonObject("players").getAsJsonArray("list")) {
                players.add(e.getAsJsonObject().get("name_raw").getAsString());
            }
            return new Data(
                    address,
                    json.get("online").getAsBoolean(),
                    json.getAsJsonObject("motd").get("raw").getAsString(),
                    players
            );
        } catch(Exception e) {
            Main.error("An error occurred while loading server information.", e);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Data data = (Data) o;
        return isOnline() == data.isOnline() && check(getMotd(), data.getMotd()) && check(getPlayers(), data.getPlayers());
    }

    private boolean check(@Nullable Object a, @Nullable Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}