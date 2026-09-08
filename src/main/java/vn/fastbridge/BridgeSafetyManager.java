package vn.fastbridge;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public final class BridgeSafetyManager {
    public boolean hasSafeFooting(MinecraftClient client) {
        if (client.player == null || client.world == null || !client.player.isAlive()) return false;
        BlockPos feetFloor = client.player.getBlockPos().down();
        return !client.world.getBlockState(feetFloor).isReplaceable();
    }
}
