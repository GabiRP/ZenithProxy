package com.zenith.feature.world.raycast;

import com.zenith.mc.block.Block;
import com.zenith.mc.block.BlockRegistry;
import com.zenith.mc.block.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record BlockRaycastResult(boolean hit, int x, int y, int z, @Nullable RayIntersection intersection, Block block) {
    public static BlockRaycastResult miss() {
        return new BlockRaycastResult(false, 0, 0, 0, null, BlockRegistry.AIR);
    }

    public @NotNull Direction direction() {
        return intersection == null ? Direction.UP : intersection.intersectingFace();
    }
}
