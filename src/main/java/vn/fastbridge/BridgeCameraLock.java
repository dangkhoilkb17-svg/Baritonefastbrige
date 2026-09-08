package vn.fastbridge;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

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

    public void aimAt(ClientPlayerEntity player, Vec3d target) {
        if (player == null || target == null) return;
        if (!locked) lock(player);

        Vec3d eye = player.getEyePos();
        double dx = target.x - eye.x;
        double dy = target.y - eye.y;
        double dz = target.z - eye.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        yaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        pitch = (float) (-Math.toDegrees(Math.atan2(dy, horizontal)));
        pitch = MathHelper.clamp(pitch, -90.0f, 90.0f);
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
