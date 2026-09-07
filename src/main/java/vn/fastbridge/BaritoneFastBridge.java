package vn.fastbridge;

import baritone.api.IBaritone;
import baritone.api.IBaritoneProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BaritoneFastBridge implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("baritone-fast-bridge");
    private static BridgeController controller;

    @Override public void onInitializeClient() {
        BridgeConfig config = BridgeConfig.load();
        IBaritoneProvider provider = loadBaritoneProvider();
        IBaritone baritone = provider.getPrimaryBaritone();
        controller = new BridgeController(baritone, config);
        new BridgeCommand(controller, config).register();
        ClientTickEvents.END_CLIENT_TICK.register(client -> controller.tick());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> controller.disconnect());
        LOGGER.info("Baritone Fast Bridge initialized");
    }

    private static IBaritoneProvider loadBaritoneProvider() {
        try {
            Class<?> providerClass = Class.forName("baritone.BaritoneProvider");
            return (IBaritoneProvider) providerClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException | ClassCastException e) {
            throw new IllegalStateException("Baritone runtime provider is unavailable", e);
        }
    }

    /** Emergency stop used by the hard input lock. F8 always releases bridge control. */
    public static void emergencyStop() {
        if (controller != null && controller.active()) {
            controller.stop();
        }
    }
}
