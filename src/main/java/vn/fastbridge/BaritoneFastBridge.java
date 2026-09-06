package vn.fastbridge;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BaritoneFastBridge implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("baritone-fast-bridge");
    private BridgeController controller;
    @Override public void onInitializeClient() {
        BridgeConfig config = BridgeConfig.load();
        IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        controller = new BridgeController(baritone, config);
        new BridgeCommand(controller, config).register();
        ClientTickEvents.END_CLIENT_TICK.register(client -> controller.tick());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> controller.disconnect());
        LOGGER.info("Baritone Fast Bridge initialized");
    }
}
