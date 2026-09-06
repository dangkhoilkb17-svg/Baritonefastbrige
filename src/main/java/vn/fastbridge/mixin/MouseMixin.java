package vn.fastbridge.mixin;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vn.fastbridge.BridgeCameraLock;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
    private void baritoneFastBridge$blockCameraRotation(double timeDelta, CallbackInfo ci) {
        if (BridgeCameraLock.isGloballyLocked()) {
            ci.cancel();
        }
    }
}
