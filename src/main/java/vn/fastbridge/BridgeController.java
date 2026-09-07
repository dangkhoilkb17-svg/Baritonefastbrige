package vn.fastbridge;

import baritone.api.IBaritone;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class BridgeController {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final BridgeConfig config;
    private final BridgePlanner planner = new BridgePlanner();
    private final BridgeInventoryManager inventory;
    private final BridgePlacementController placement;
    private final BridgeSafetyManager safety;
    private final BridgeProcess process;
    private final BridgeCameraLock cameraLock = new BridgeCameraLock();
    private BridgeState state = BridgeState.IDLE;
    private BridgePlan plan;
    private BlockPos pending;
    private int pendingTick;
    private int currentIndex;
    private int tick;
    private int placed;
    private int currentAttempts;

    public BridgeController(IBaritone baritone, BridgeConfig config) {
        this.config = config;
        inventory = new BridgeInventoryManager(config);
        placement = new BridgePlacementController(config);
        safety = new BridgeSafetyManager(config);
        process = new BridgeProcess(baritone);
    }

    public boolean active() { return state != BridgeState.IDLE; }

    public void start(int length, int width) {
        if (client.player == null || client.world == null) { message("Bridge unavailable: not in a world."); return; }
        if (length < 1 || length > config.maxLength || width < 1 || width > config.maxWidth) {
            message("Usage: #bridge [length 1-" + config.maxLength + "] [width 1-" + config.maxWidth + "] | auto <width> | F8 emergency stop"); return;
        }
        stopInternal(null, false);
        plan = planner.create(client.player, length, width);
        currentIndex = placed = currentAttempts = tick = 0;
        pending = null;
        state = BridgeState.PREPARING;
        BridgeInputLock.lock(client);
        cameraLock.lock(client.player);
        process.setActive(true);
        process.setGoal(plan.orderedTargets().get(0));
        message("Fast Bridge started: length=" + length + ", width=" + width + ". Predictive camera/input lock active. Press F8 for emergency stop. Baritone controls movement.");
    }

    /** Finds the first full-width solid row and bridges the gap before it. */
    public void autoStart(int width) {
        if (client.player == null || client.world == null) { message("Bridge unavailable: not in a world."); return; }
        if (width < 1 || width > config.maxWidth) {
            message("Usage: #bridge auto [width 1-" + config.maxWidth + "]"); return;
        }

        stopInternal(null, false);
        Direction forward = client.player.getHorizontalFacing();
        Direction lateral = forward.rotateYClockwise();
        BlockPos origin = client.player.getBlockPos().down();
        int length = findAutoLength(origin, forward, lateral, width);

        if (length < 1) {
            message("Auto bridge stopped: no gap found immediately ahead.");
            return;
        }
        if (length > config.maxLength) {
            message("Auto bridge stopped: no full-width shore found within maxLength=" + config.maxLength + ".");
            return;
        }

        start(length, width);
        if (active()) message("Auto bridge detected gap length=" + length + ", width=" + width + ".");
    }

    private int findAutoLength(BlockPos origin, Direction forward, Direction lateral, int width) {
        for (int row = 1; row <= config.maxLength + 1; row++) {
            if (isFullWidthSolidRow(origin, forward, lateral, width, row)) return row - 1;
        }
        return config.maxLength + 1;
    }

    private boolean isFullWidthSolidRow(BlockPos origin, Direction forward, Direction lateral, int width, int row) {
        BlockPos rowOrigin = origin.offset(forward, row);
        int center = width / 2;
        for (int lane = 0; lane < width; lane++) {
            int offset = lane - center;
            BlockPos pos = rowOrigin.offset(lateral, offset);
            if (client.world.getBlockState(pos).isReplaceable()) return false;
        }
        return true;
    }

    public void stop() { stopInternal(BridgeStopReason.CANCELLED, true); }
    public void disconnect() { if (active()) stopInternal(BridgeStopReason.DISCONNECTED, false); }

    public void tick() {
        if (!active()) return;
        tick++;
        if (client.player == null || client.world == null) { fail(BridgeStopReason.DISCONNECTED); return; }
        if (!client.player.isAlive()) { fail(BridgeStopReason.PLAYER_DEAD); return; }
        cameraLock.apply(client);
        try {
            if (!advancePastPlacedTarget()) return;
            if (currentIndex >= plan.orderedTargets().size()) { stopInternal(BridgeStopReason.COMPLETED, true); return; }
            if (!safety.hasSafeFooting(client)) { fail(BridgeStopReason.PLAYER_UNSAFE); return; }

            BlockPos target = plan.orderedTargets().get(currentIndex);
            process.setGoal(target);
            if (inventory.select(client, target) < 0) { fail(BridgeStopReason.NO_BLOCKS); return; }

            var candidate = placement.candidate(client, target, plan.forward());
            if (candidate.isPresent()) {
                cameraLock.aimAt(client.player, candidate.get().hit().getPos());
                if (placement.interact(client, candidate.get())) {
                    pending = target;
                    pendingTick = tick;
                    currentAttempts = 0;
                    if (currentIndex + 1 < plan.orderedTargets().size()) process.setGoal(plan.orderedTargets().get(currentIndex + 1));
                } else if (++currentAttempts > config.placementRetryLimit) { fail(BridgeStopReason.NO_VALID_PLACEMENT); return; }
            } else if (++currentAttempts > config.placementRetryLimit) { fail(BridgeStopReason.NO_VALID_PLACEMENT); return; }
            advancePastPlacedTarget();
        } catch (RuntimeException e) {
            BaritoneFastBridge.LOGGER.error("Bridge tick failed", e);
            fail(BridgeStopReason.INTERNAL_ERROR);
        }
    }

    private boolean advancePastPlacedTarget() {
        if (plan == null) return false;
        boolean advanced = false;
        // Only advance after an interaction has been accepted for this target.
        // A pre-existing block must not be mistaken for a bridge block.
        while (pending != null
                && currentIndex < plan.orderedTargets().size()
                && placement.isPlaced(client, plan.orderedTargets().get(currentIndex))) {
            currentIndex++; placed++; pending = null; currentAttempts = 0; advanced = true;
        }
        if (!advanced && pending != null && tick - pendingTick >= config.verifyDelayTicks && !placement.isPlaced(client, pending)) {
            pending = null;
            if (++currentAttempts > config.placementRetryLimit) { fail(BridgeStopReason.NO_VALID_PLACEMENT); return false; }
        }
        return true;
    }

    private void fail(BridgeStopReason reason) { stopInternal(reason, true); }

    private void stopInternal(BridgeStopReason reason, boolean report) {
        process.setActive(false);
        cameraLock.unlock();
        BridgeInputLock.unlock(client);
        if (report && reason != null) message("Bridge stopped. Placed: " + placed + ". Reason: " + reason.message() + ".");
        state = BridgeState.IDLE;
        pending = null; plan = null; currentIndex = currentAttempts = 0;
    }

    private void message(String s) { if (client.player != null) client.player.sendMessage(Text.literal(s), false); }
}
