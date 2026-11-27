package net.researchgates;

import net.neoforged.neoforge.common.ModConfigSpec;

/** The library's common config (config/researchgates-common.toml). */
public final class GateConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue LOCK_UNOWNED;
    public static final ModConfigSpec.BooleanValue LOCK_FURNACES;
    public static final ModConfigSpec.BooleanValue TOOLTIPS;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment("Research gates: recipes locked until Thaumaturge research is done (datapack files in data/<namespace>/research_gates/).")
                .push("research_gates");
        LOCK_UNOWNED = b.comment("Whether machines nobody placed (world generation, commands, other mods) are refused gated recipes.")
                .define("lock_unowned_machines", false);
        LOCK_FURNACES = b.comment("Whether furnaces, smokers and blast furnaces obey gates (as their owner).").define("gate_furnaces", true);
        TOOLTIPS = b.comment("Whether items whose recipes are gated name the research in their tooltip.").define("tooltips", true);
        b.pop();
        SPEC = b.build();
    }

    private GateConfig() {
    }
}
