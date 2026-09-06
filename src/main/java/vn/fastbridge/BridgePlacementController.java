package vn.fastbridge;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import java.util.Optional;

public final class BridgePlacementController {
    private final BridgeConfig config;
    public BridgePlacementController(BridgeConfig config) { this.config = config; }

    public boolean isPlaced(MinecraftClient c, BlockPos pos) {
        return c.world != null && !c.world.getBlockState(pos).isReplaceable();
    }

    public Optional<PlacementCandidate> candidate(MinecraftClient c, BlockPos target) {
        return candidate(c, target, null);
    }

    public Optional<PlacementCandidate> candidate(MinecraftClient c, BlockPos target, Direction forward) {
        if (c.player == null || c.world == null || !c.world.getBlockState(target).isReplaceable()) return Optional.empty();

        if (forward != null) {
            Optional<PlacementCandidate> preferred = fromSupport(c, target, target.offset(forward.getOpposite()), forward);
            if (preferred.isPresent()) return preferred;
        }

        // Fallback order: horizontal sides first, then vertical faces. This keeps
        // normal bridge placement preferred while still allowing recovery around
        // uneven terrain.
        Direction[] order = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP, Direction.DOWN};
        for (Direction side : order) {
            Optional<PlacementCandidate> candidate = fromSupport(c, target, target.offset(side), side.getOpposite());
            if (candidate.isPresent()) return candidate;
        }
        return Optional.empty();
    }

    private Optional<PlacementCandidate> fromSupport(MinecraftClient c, BlockPos target, BlockPos support, Direction clickedFace) {
        if (c.world.getBlockState(support).isReplaceable()) return Optional.empty();
        Vec3d hitPos = Vec3d.ofCenter(support).add(Vec3d.of(clickedFace.getVector()).multiply(0.5));
        if (c.player.getEyePos().squaredDistanceTo(hitPos) > config.reach * config.reach) return Optional.empty();
        return Optional.of(new PlacementCandidate(target, new BlockHitResult(hitPos, clickedFace, support, false)));
    }

    public boolean interact(MinecraftClient c, PlacementCandidate candidate) {
        if (c.player == null || c.interactionManager == null) return false;
        if (!(c.player.getMainHandStack().getItem() instanceof BlockItem)) return false;
        ActionResult result = c.interactionManager.interactBlock(c.player, Hand.MAIN_HAND, candidate.hit());
        if (result.isAccepted()) c.player.swingHand(Hand.MAIN_HAND);
        return result.isAccepted();
    }
}
