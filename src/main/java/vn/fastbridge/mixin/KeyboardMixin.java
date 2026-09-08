package vn.fastbridge.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vn.fastbridge.BaritoneFastBridge;
import vn.fastbridge.BridgeInputLock;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void baritoneFastBridge$blockPlayerKeyboardInput(
            long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (!BridgeInputLock.isLocked()) return;

        if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_F8) {
            BaritoneFastBridge.emergencyStop();
            ci.cancel();
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.currentScreen instanceof ChatScreen) return;

        if (action != GLFW.GLFW_RELEASE
                && (client.options.chatKey.matchesKey(key, scancode)
                    || client.options.commandKey.matchesKey(key, scancode))) {
            return;
        }

        ci.cancel();
    }
}
