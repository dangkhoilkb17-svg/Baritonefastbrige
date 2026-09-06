package vn.fastbridge;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public record PlacementCandidate(BlockPos target, BlockHitResult hit) {}
