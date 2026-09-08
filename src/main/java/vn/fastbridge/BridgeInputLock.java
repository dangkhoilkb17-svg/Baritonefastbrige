package vn.fastbridge;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

public final class BridgeInputLock {
    private static volatile boolean locked;

    private BridgeInputLock() {}

    public static void lock(MinecraftClient client) {
        locked = true;
        clearVanillaKeyStates();
    }

    public static void unlock(MinecraftClient client) {
        locked = false;
        clearVanillaKeyStates();
    }

    public static boolean isLocked() {
        return locked;
    }

    public static void clearVanillaKeyStates() {
        KeyBinding.unpressAll();
    }
}
