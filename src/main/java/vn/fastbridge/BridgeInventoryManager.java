package vn.fastbridge;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.TntBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import java.util.Comparator;

public final class BridgeInventoryManager {
    private final BridgeConfig config;
    public BridgeInventoryManager(BridgeConfig config) { this.config = config; }

    public int select(MinecraftClient client, BlockPos placementPos) {
        if (client.player == null || client.world == null) return -1;
        return java.util.stream.IntStream.range(0, 9)
            .filter(slot -> acceptable(client.player.getInventory().getStack(slot), client, placementPos))
            .boxed().min(Comparator.comparingInt(slot -> preference(client.player.getInventory().getStack(slot))))
            .map(slot -> { client.player.getInventory().selectedSlot = slot; return slot; }).orElse(-1);
    }

    private int preference(ItemStack stack) {
        Identifier id = Registries.ITEM.getId(stack.getItem());
        int i = config.preferredBlocks.indexOf(id.toString());
        return i < 0 ? 1000 : i;
    }

    private boolean acceptable(ItemStack stack, MinecraftClient client, BlockPos pos) {
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem item)) return false;
        Block block = item.getBlock();
        if (block instanceof FallingBlock || block instanceof BlockWithEntity || block instanceof TntBlock) return false;
        BlockState state = block.getDefaultState();
        return state.isFullCube(client.world, pos) && state.getFluidState().isEmpty() && state.canPlaceAt(client.world, pos);
    }
}
