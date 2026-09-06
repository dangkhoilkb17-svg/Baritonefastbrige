package vn.fastbridge;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import java.util.List;

public record BridgePlan(BlockPos origin, Direction forward, Direction lateral, int length, int width,
                         List<BlockPos> orderedTargets) {
    public BlockPos target(int row, int lane) {
        int offset = lane - (width - 1) / 2;
        return origin.offset(forward, row + 1).offset(lateral, offset);
    }
}
