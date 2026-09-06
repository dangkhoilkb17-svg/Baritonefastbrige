package vn.fastbridge;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public final class BridgeSafetyManager {
    private final BridgeConfig config;
    public BridgeSafetyManager(BridgeConfig config) { this.config = config; }

    public boolean hasSafeFooting(MinecraftClient client) {
        if (client.player == null || client.world == null || !client.player.isAlive()) return false;
        BlockPos feetFloor = client.player.getBlockPos().down();
        return !client.world.getBlockState(feetFloor).isReplaceable();
    }

    public boolean mayMove(MinecraftClient client, BridgePlan plan) {
        if (!hasSafeFooting(client)) return false;
        BlockPos aheadFloor = client.player.getBlockPos().down().offset(plan.forward());
        return !client.world.getBlockState(aheadFloor).isReplaceable();
    }

    public int rowAt(MinecraftClient c, BridgePlan p) {
        double dx = c.player.getX() - (p.origin().getX() + .5);
        double dz = c.player.getZ() - (p.origin().getZ() + .5);
        return (int) Math.floor(dx * p.forward().getOffsetX() + dz * p.forward().getOffsetZ());
    }
}
