package vn.fastbridge;

import baritone.api.IBaritone;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalGetToBlock;
import baritone.api.process.ICustomGoalProcess;
import net.minecraft.util.math.BlockPos;

/**
 * Baritone remains the primary pathing and movement controller.
 * Fast Bridge only supplies the next bridge target; it never injects movement inputs.
 */
public final class BridgeProcess {
    private final ICustomGoalProcess goalProcess;
    private boolean active;
    private BlockPos goal;

    public BridgeProcess(IBaritone baritone) {
        this.goalProcess = baritone.getCustomGoalProcess();
    }

    public void setActive(boolean active) {
        this.active = active;
        if (!active) {
            clearGoal();
            goalProcess.onLostControl();
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setGoal(BlockPos pos) {
        if (!active || pos == null || pos.equals(goal)) return;
        goal = pos.toImmutable();
        Goal next = new GoalGetToBlock(goal);
        goalProcess.setGoalAndPath(next);
    }

    public void clearGoal() {
        goal = null;
        if (active) goalProcess.setGoalAndPath(null);
    }

    public BlockPos goal() {
        return goal;
    }
}
