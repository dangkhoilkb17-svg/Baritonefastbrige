package vn.fastbridge;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class BridgeCommand {
    private final BridgeController controller;
    private final BridgeConfig config;
    public BridgeCommand(BridgeController controller, BridgeConfig config) { this.controller = controller; this.config = config; }

    public void register() {
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            String trimmed = message.trim();
            String normalized = trimmed.toLowerCase(java.util.Locale.ROOT);
            if (!(normalized.equals("#bridge") || normalized.startsWith("#bridge "))) return true;
            String command = trimmed.substring("#bridge".length()).trim();
            execute(command);
            return false;
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> dispatcher.register(
            literal("bridge")
                .executes(ctx -> { controller.start(config.defaultLength, config.defaultWidth); return 1; })
                .then(literal("auto")
                    .then(argument("width", IntegerArgumentType.integer(1, config.maxWidth))
                        .executes(ctx -> { controller.autoStart(IntegerArgumentType.getInteger(ctx, "width")); return 1; })))
                .then(argument("length", IntegerArgumentType.integer(1, config.maxLength))
                    .executes(ctx -> { controller.start(IntegerArgumentType.getInteger(ctx, "length"), config.defaultWidth); return 1; })
                    .then(argument("width", IntegerArgumentType.integer(1, config.maxWidth))
                        .executes(ctx -> { controller.start(IntegerArgumentType.getInteger(ctx, "length"), IntegerArgumentType.getInteger(ctx, "width")); return 1; })))
        ));
    }

    private void execute(String input) {
        String[] a = input.isBlank() ? new String[0] : input.split("\\s+");
        try {
            if (a.length == 0) controller.start(config.defaultLength, config.defaultWidth);
            else if (a.length == 1) controller.start(Integer.parseInt(a[0]), config.defaultWidth);
            else if (a.length == 2 && a[0].equalsIgnoreCase("auto")) controller.autoStart(Integer.parseInt(a[1]));
            else if (a.length == 2) controller.start(Integer.parseInt(a[0]), Integer.parseInt(a[1]));
            else controller.start(-1, -1);
        } catch (NumberFormatException e) { controller.start(-1, -1); }
    }
}
