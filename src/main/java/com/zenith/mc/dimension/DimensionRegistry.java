// Auto-Generated by ZenithProxy Data Generator
package com.zenith.mc.dimension;

import com.zenith.mc.Registry;

public final class DimensionRegistry {
    public static final Registry<DimensionData> REGISTRY = new Registry<DimensionData>(4);

    public static final DimensionData OVERWORLD = REGISTRY.register(new DimensionData(0, "overworld", -64, 320, 384));

    public static final DimensionData OVERWORLD_CAVES = REGISTRY.register(new DimensionData(1, "overworld_caves", -64, 320, 384));

    public static final DimensionData THE_END = REGISTRY.register(new DimensionData(2, "the_end", 0, 256, 256));

    public static final DimensionData THE_NETHER = REGISTRY.register(new DimensionData(3, "the_nether", 0, 256, 256));
}
