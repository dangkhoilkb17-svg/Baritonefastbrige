package vn.fastbridge.mixin;

import net.minecraft.client.Mouse;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vn.fastbridge.BridgeCameraLock;
import vn.fastbridge.BridgeInputLock;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
    private void baritoneFastBridge$blockCameraRotation(double timeDelta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (BridgeCameraLock.isGloballyLocked() && client.currentScreen == null) {
            ci.cancel();
        }
    }

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void baritoneFastBridge$blockPlayerMouseButtons(
            long window, int button, int action, int mods, CallbackInfo ci) {
        if (!BridgeInputLock.isLocked()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        // GUI input is allowed so the chat screen can still be used to issue #bridge stop.
        if (client.currentScreen == null) ci.cancel();
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void baritoneFastBridge$blockHotbarScroll(
            long window, double horizontal, double vertical, CallbackInfo ci) {
        if (!BridgeInputLock.isLocked()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen == null) ci.cancel();
    }
}
