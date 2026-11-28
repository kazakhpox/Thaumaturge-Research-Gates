package net.researchgates;

import net.neoforged.neoforge.common.ModConfigSpec;

/** The library's common config (config/researchgates-common.toml). */
public final class GateConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue LOCK_UNOWNED;
    public static final ModConfigSpec.BooleanValue LOCK_FURNACES;
    public static final ModConfigSpec.BooleanValue TOOLTIPS;
    public static final ModConfigSpec.BooleanValue AUTO_MECHANOTURGY;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment("Research gates: recipes locked until Thaumaturge research is done (datapack files in data/<namespace>/research_gates/).")
                .push("research_gates");
        LOCK_UNOWNED = b.comment("Whether machines nobody placed (world generation, commands, other mods) are refused gated recipes.")
                .define("lock_unowned_machines", false);
        LOCK_FURNACES = b.comment("Whether furnaces, smokers and blast furnaces obey gates (as their owner).").define("gate_furnaces", true);
        TOOLTIPS = b.comment("Whether items whose recipes are gated name the research in their tooltip.").define("tooltips", true);
        b.pop();
        b.comment("The built-in Mechanoturgy datapack (only with Create): a Thaumonomicon chapter whose research opens Create's machines.")
                .push("mechanoturgy");
        AUTO_MECHANOTURGY = b.comment("Whether the datapack switches itself on in new worlds, and in old worlds that never had it.",
                        "Off: it stays in the world's data pack list, to switch on by hand there or with",
                        "/datapack enable \"mod/researchgates:datapacks/mechanoturgy\". A world keeps whatever it has already.")
                .define("auto_enable", true);
        b.pop();
        SPEC = b.build();
    }

    private GateConfig() {
    }
}
