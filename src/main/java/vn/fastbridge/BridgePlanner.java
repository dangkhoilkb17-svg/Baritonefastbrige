package vn.fastbridge;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import java.util.ArrayList;
import java.util.List;

public final class BridgePlanner {
    public BridgePlan create(ClientPlayerEntity player, int length, int width) {
        Direction forward = player.getHorizontalFacing();
        Direction lateral = forward.rotateYClockwise();
        BlockPos origin = player.getBlockPos().down();
        List<BlockPos> targets = new ArrayList<>(length * width);

        for (int row = 0; row < length; row++) {
            for (int lane = 0; lane < width; lane++) {
                int offset = lane - ((width - 1) / 2);
                targets.add(origin.offset(forward, row + 1).offset(lateral, offset));
            }
        }
        return new BridgePlan(origin, forward, lateral, length, width, List.copyOf(targets));
    }
}
