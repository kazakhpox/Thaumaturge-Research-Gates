package net.researchgates;

import java.lang.reflect.Method;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Thaumaturge Research Gates: lock any recipe behind Thaumaturge research, the way Thaumaturge locks its own arcane workbench,
 * crucible and infusion recipes. Gates are datapack files ({@link Gate}), checked with Thaumaturge's own {@code ResearchGate.passes}
 * ({@link ResearchGates}); the crafting grid, stonecutter, smithing table and furnaces obey them, and Create's machines when Create
 * is installed. Machines ask about the player who placed them ({@link Owners}).
 */
@Mod(ResearchGatesMod.MOD_ID)
public final class ResearchGatesMod {
    public static final String MOD_ID = "researchgates";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Method knowledgeOf;
    private static Method isResearchComplete;
    private static Method researchOf;
    private static Method hasCompleted;
    private static boolean reflected;

    public ResearchGatesMod(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, GateConfig.SPEC);
        ResearchGates.register();
        Owners.register(modBus);
        if (ModList.get().isLoaded("create")) {
            net.researchgates.compat.CreateCompat.register();
            modBus.addListener(AddPackFindersEvent.class, ResearchGatesMod::addMechanoturgy);
        }
        NeoForge.EVENT_BUS.addListener(ItemTooltipEvent.class, ResearchGatesMod::onTooltip);
    }

    /**
     * The built-in Mechanoturgy datapack ({@code datapacks/mechanoturgy}), offered only when Create is installed: a Thaumonomicon
     * chapter whose research opens Create's machines, and the gates that lock them. While the config's
     * {@code mechanoturgy.auto_enable} is on, it is switched on by itself in new worlds and in worlds that never had it; either
     * way it shows in the world's data pack list and can be switched on or off there or with {@code /datapack}.
     */
    private static void addMechanoturgy(AddPackFindersEvent event) {
        // A built-in pack is switched on by itself where a world has no say on it yet; with auto_enable off it is only offered.
        PackSource source = GateConfig.AUTO_MECHANOTURGY.get() ? PackSource.BUILT_IN
                : PackSource.create(name -> Component.translatable("pack.nameAndSource", name,
                        Component.translatable("pack.source.builtin")).withStyle(ChatFormatting.GRAY), false);
        event.addPackFinders(ResourceLocation.fromNamespaceAndPath(MOD_ID, "datapacks/mechanoturgy"), PackType.SERVER_DATA,
                Component.translatable("pack.researchgates.mechanoturgy"), source, false, Pack.Position.TOP);
    }

    /** A research's name as the Thaumonomicon shows it. */
    public static Component researchName(ResourceLocation research) {
        return Component.translatable("research." + research.getNamespace() + "." + research.getPath() + ".title");
    }

    /**
     * Whether the player has completed the research: the fallback when this Thaumaturge has no {@code ResearchGate}. Tries
     * {@code KnowledgeAccess.of(player).isResearchComplete}, then {@code PlayerResearchAccess.of(player).hasCompleted}.
     */
    public static boolean completed(Player player, ResourceLocation research) {
        if (player == null || research == null) {
            return false;
        }
        if (!reflected) {
            reflected = true;
            try {
                knowledgeOf = Class.forName("com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess").getMethod("of", Player.class);
                isResearchComplete = knowledgeOf.getReturnType().getMethod("isResearchComplete", ResourceLocation.class);
            } catch (ReflectiveOperationException | RuntimeException e) {
                knowledgeOf = null;
            }
            try {
                researchOf = Class.forName("com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess").getMethod("of", Player.class);
                hasCompleted = researchOf.getReturnType().getMethod("hasCompleted", ResourceLocation.class);
            } catch (ReflectiveOperationException | RuntimeException e) {
                researchOf = null;
            }
            if (knowledgeOf == null && researchOf == null) {
                LOGGER.error("Research Gates: this Thaumaturge has no way to ask about research; every gate stays closed");
            }
        }
        try {
            if (knowledgeOf != null) {
                return (boolean) isResearchComplete.invoke(knowledgeOf.invoke(null, player), research);
            }
            if (researchOf != null) {
                return (boolean) hasCompleted.invoke(researchOf.invoke(null, player), research);
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.error("Research Gates: asking about {} failed", research, e);
        }
        return false;
    }

    private static void onTooltip(ItemTooltipEvent event) {
        if (event.getEntity() == null || !GateConfig.TOOLTIPS.get()) {
            return;
        }
        Gate gate = ResearchGates.closedGateForItem(event.getEntity(), event.getItemStack());
        if (gate != null) {
            event.getToolTip().add(ResearchGates.lockedMessage(gate));
        }
    }
}
