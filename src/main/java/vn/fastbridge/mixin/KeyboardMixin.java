package vn.fastbridge.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vn.fastbridge.BridgeInputLock;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void baritoneFastBridge$blockPlayerKeyboardInput(
            long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (!BridgeInputLock.isLocked()) return;

        MinecraftClient client = MinecraftClient.getInstance();

        // Chat is the deliberate exception: it lets the player issue #bridge stop.
        // Once chat is open, its own text/Enter/Escape handling remains available.
        if (client.currentScreen instanceof ChatScreen) return;

        // Permit opening the configured chat/command key while blocking all world keys.
        if (action != GLFW.GLFW_RELEASE
                && (client.options.chatKey.matchesKey(key, scancode)
                    || client.options.commandKey.matchesKey(key, scancode))) {
            return;
        }

        ci.cancel();
    }
}
