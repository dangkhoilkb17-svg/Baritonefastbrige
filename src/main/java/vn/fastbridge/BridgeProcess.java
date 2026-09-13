package vn.fastbridge;

import baritone.api.IBaritone;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.process.ICustomGoalProcess;
import net.minecraft.util.math.BlockPos;

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
        goalProcess.setGoalAndPath(new GoalNear(goal, 1));
    }

    public void clearGoal() {
        goal = null;
        if (active) goalProcess.setGoalAndPath(null);
    }

    public BlockPos goal() {
        return goal;
    }

}
