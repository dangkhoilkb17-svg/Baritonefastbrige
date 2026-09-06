package vn.fastbridge;

import baritone.api.IBaritone;
import baritone.api.utils.IInputOverrideHandler;
import baritone.api.utils.input.Input;

public final class BridgeMovementController {
    private final IInputOverrideHandler input;
    private final BridgeConfig config;
    public BridgeMovementController(IBaritone baritone, BridgeConfig config) {
        this.input = baritone.getInputOverrideHandler(); this.config = config;
    }
    public void apply(boolean forward, boolean emergencySneak) {
        input.setInputForceState(Input.MOVE_FORWARD, forward);
        input.setInputForceState(Input.SPRINT, forward && config.useSprint && !emergencySneak);
        input.setInputForceState(Input.SNEAK, emergencySneak || (forward && config.useSneak));
        input.setInputForceState(Input.MOVE_LEFT, false);
        input.setInputForceState(Input.MOVE_RIGHT, false);
        input.setInputForceState(Input.JUMP, false);
    }
    public void release() { input.clearAllKeys(); }
}
