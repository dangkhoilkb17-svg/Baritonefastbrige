package vn.fastbridge;

import baritone.api.IBaritone;
import baritone.api.pathing.goals.Goal;
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
        goalProcess.setGoalAndPath(new AdjacentBlockGoal(goal));
    }

    public void clearGoal() {
        goal = null;
        if (active) goalProcess.setGoalAndPath(null);
    }

    public BlockPos goal() {
        return goal;
    }

    private static final class AdjacentBlockGoal implements Goal {
        private final int x;
        private final int y;
        private final int z;

        private AdjacentBlockGoal(BlockPos pos) {
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
        }

        @Override
        public boolean isInGoal(int px, int py, int pz) {
            int dx = px - x;
            int dy = py - y;
            int dz = pz - z;
            return Math.abs(dx) + Math.abs(dy < 0 ? dy + 1 : dy) + Math.abs(dz) <= 1;
        }

        @Override
        public double heuristic(int px, int py, int pz) {
            int dx = px - x;
            int dy = py - y;
            int dz = pz - z;
            return Math.abs(dx) + Math.abs(dy < 0 ? dy + 1 : dy) + Math.abs(dz);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof AdjacentBlockGoal goal)) return false;
            return x == goal.x && y == goal.y && z == goal.z;
        }

        @Override
        public int hashCode() {
            int result = Integer.hashCode(x);
            result = 31 * result + Integer.hashCode(y);
            result = 31 * result + Integer.hashCode(z);
            return result;
        }

        @Override
        public String toString() {
            return "AdjacentBlockGoal{x=" + x + ",y=" + y + ",z=" + z + "}";
        }
    }
}
