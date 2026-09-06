package vn.fastbridge;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

/**
 * Hard client-input lock used while Fast Bridge owns the player.
 * Vanilla player input is suppressed; Baritone's input overrides remain available.
 */
public final class BridgeInputLock {
    private static volatile boolean locked;

    private BridgeInputLock() {}

    public static void lock(MinecraftClient client) {
        locked = true;
        clearVanillaKeyStates(client);
    }

    public static void unlock(MinecraftClient client) {
        locked = false;
        clearVanillaKeyStates(client);
    }

    public static boolean isLocked() {
        return locked;
    }

    /** Clears physical vanilla key state without touching Baritone's input overrides. */
    public static void clearVanillaKeyStates(MinecraftClient client) {
        if (client == null || client.options == null) return;
        for (KeyBinding key : client.options.allKeys) {
            key.setPressed(false);
        }
    }
}
