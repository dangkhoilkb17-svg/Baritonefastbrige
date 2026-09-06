package vn.fastbridge;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/** Locks the player's view direction while an automatic bridge is active. */
public final class BridgeCameraLock {
    private static volatile boolean globallyLocked;

    private boolean locked;
    private float yaw;
    private float pitch;

    public void lock(ClientPlayerEntity player) {
        if (player == null) return;
        if (!locked) {
            yaw = player.getYaw();
            pitch = player.getPitch();
            locked = true;
            globallyLocked = true;
        }
        apply(player);
    }

    public void apply(MinecraftClient client) {
        if (!locked || client.player == null) return;
        apply(client.player);
    }

    private void apply(ClientPlayerEntity player) {
        player.setYaw(yaw);
        player.setPitch(pitch);
        player.setHeadYaw(yaw);
        player.setBodyYaw(yaw);
    }

    public void unlock() {
        locked = false;
        globallyLocked = false;
    }

    public boolean isLocked() {
        return locked;
    }

    public static boolean isGloballyLocked() {
        return globallyLocked;
    }
}
