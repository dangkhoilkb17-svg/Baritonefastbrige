package vn.fastbridge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class BridgeConfig {
    public int defaultLength = 64;
    public int defaultWidth = 1;
    public int maxLength = 4096;
    public int maxWidth = 7;
    public int placementRetryLimit = 3;
    public boolean useSprint = true;
    public boolean useSneak = false;
    public double reach = 4.5;
    public int verifyDelayTicks = 2;
    public List<String> preferredBlocks = List.of("minecraft:stone", "minecraft:cobblestone", "minecraft:deepslate", "minecraft:dirt", "minecraft:netherrack");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static BridgeConfig load() {
        Path p = FabricLoader.getInstance().getConfigDir().resolve("baritone-fast-bridge.json");
        try {
            if (Files.exists(p)) return sanitize(GSON.fromJson(Files.readString(p), BridgeConfig.class));
            BridgeConfig c = new BridgeConfig();
            Files.writeString(p, GSON.toJson(c));
            return c;
        } catch (IOException | RuntimeException e) {
            BaritoneFastBridge.LOGGER.warn("Cannot load bridge config, using defaults", e);
            return new BridgeConfig();
        }
    }
    private static BridgeConfig sanitize(BridgeConfig c) {
        if (c == null) return new BridgeConfig();
        c.defaultLength = Math.max(1, c.defaultLength); c.defaultWidth = Math.max(1, c.defaultWidth);
        c.maxLength = Math.max(c.defaultLength, c.maxLength); c.maxWidth = Math.max(c.defaultWidth, c.maxWidth);
        c.placementRetryLimit = Math.max(1, c.placementRetryLimit);
        c.reach = Math.max(3.0, Math.min(6.0, c.reach)); c.verifyDelayTicks = Math.max(1, c.verifyDelayTicks);
        if (c.preferredBlocks == null) c.preferredBlocks = List.of();
        return c;
    }
}
