package net.researchgates.devtools;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.researchgates.Owners;
import net.researchgates.ResearchGates;

/**
 * Dev only ("./gradlew runCheck"): a player without research, against the test gates in this mod's data: oak stairs gated, oak
 * slabs gated with negate (so open to them), stone bricks on the stonecutter gated, smelting gated except charcoal. Furnaces: one
 * owned by that player, one by nobody. Prints what each gives, then stops the server.
 */
@Mod("researchgates_devtools")
public final class RecipeCheck {
    private static final String P = "[researchgates.check] ";
    private static final BlockPos[] FURNACES = new BlockPos[2];
    private static int ticks;

    public RecipeCheck() {
        if (System.getProperty("researchgates.check") == null) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, event -> start(event.getServer().overworld()));
    }

    private static void start(ServerLevel level) {
        System.out.println(P + "gates loaded: " + ResearchGates.gates().size() + ", create loaded: "
                + net.neoforged.fml.ModList.get().isLoaded("create"));
        var server = level.getServer();
        System.out.println(P + "data packs on: " + server.getPackRepository().getSelectedIds());
        System.out.println(P + "mechanoturgy available: " + server.getPackRepository().getAvailableIds().contains("mod/researchgates:datapacks/mechanoturgy")
                + ", on: " + server.getPackRepository().getSelectedIds().contains("mod/researchgates:datapacks/mechanoturgy")
                + ", auto_enable: " + net.researchgates.GateConfig.AUTO_MECHANOTURGY.get());
        System.out.println(P + "mechanoturgy: " + ResearchGates.gates().stream().filter(g -> g.id.getNamespace().equals("createthaumaresearch")).count()
                + " gates, " + server.getResourceManager().listResources("thaumaturge/research_entry",
                        id -> id.getNamespace().equals("createthaumaresearch")).size() + " research entries, "
                + server.getResourceManager().listResources("thaumaturge/research_category",
                        id -> id.getNamespace().equals("createthaumaresearch")).size() + " category");
        var shaft = server.getRecipeManager().byKey(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("create", "crafting/kinetics/shaft"));
        System.out.println(P + "create:crafting/kinetics/shaft gated by: " + shaft.map(h -> ResearchGates.gatesFor(h, h.value().getResultItem(server.registryAccess()))
                .stream().map(g -> g.id.toString()).toList()).orElse(null));
        var player = FakePlayerFactory.getMinecraft(level);
        craft(level, player, Items.OAK_PLANKS, new int[]{1, 4, 5, 7, 8, 9}, "oak stairs (gated)");
        craft(level, player, Items.OAK_PLANKS, new int[]{7, 8, 9}, "oak slabs (gated, negate)");
        craft(level, player, Items.OAK_PLANKS, new int[]{2, 5}, "sticks (free)");

        var cutter = new StonecutterMenu(1, player.getInventory(), ContainerLevelAccess.create(level, player.blockPosition()));
        cutter.getSlot(0).set(new ItemStack(Items.STONE, 4));
        cutter.slotsChanged(cutter.container);
        for (int i = 0; i < cutter.getRecipes().size(); i++) {
            String made = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(
                    cutter.getRecipes().get(i).value().getResultItem(level.registryAccess()).getItem()).getPath();
            if (made.equals("stone_bricks") || made.equals("stone_slab")) {
                cutter.clickMenuButton(player, i);
                System.out.println(P + "stonecutter " + made + (made.equals("stone_bricks") ? " (gated)" : " (free)") + ": "
                        + cutter.getSlot(1).getItem());
            }
        }

        FURNACES[0] = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, level.getSharedSpawnPos()).above();
        FURNACES[1] = FURNACES[0].east(2);
        for (int i = 0; i < 2; i++) {
            level.setChunkForced(FURNACES[i].getX() >> 4, FURNACES[i].getZ() >> 4, true);
            level.setBlockAndUpdate(FURNACES[i], Blocks.FURNACE.defaultBlockState());
            var furnace = (AbstractFurnaceBlockEntity) level.getBlockEntity(FURNACES[i]);
            furnace.setItem(0, new ItemStack(Items.RAW_IRON, 2));
            furnace.setItem(1, new ItemStack(Items.COAL, 2));
            if (i == 0) {
                Owners.setOwner(furnace, player.getUUID());
            }
        }
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, event -> {
            if (++ticks == 500) {
                for (int i = 0; i < 2; i++) {
                    var furnace = (AbstractFurnaceBlockEntity) level.getBlockEntity(FURNACES[i]);
                    System.out.println(P + "furnace smelting raw iron (gated), " + (i == 0 ? "owned by the player" : "owned by nobody")
                            + ": output " + furnace.getItem(2) + ", input left " + furnace.getItem(0));
                }
                level.getServer().halt(false);
            }
        });
    }

    private static void craft(ServerLevel level, net.minecraft.world.entity.player.Player player, Item item, int[] slots, String what) {
        var menu = new CraftingMenu(0, player.getInventory(), ContainerLevelAccess.create(level, player.blockPosition()));
        for (int slot : slots) {
            menu.getSlot(slot).set(new ItemStack(item));
        }
        menu.slotsChanged(null);
        System.out.println(P + "crafting " + what + ": " + menu.getSlot(0).getItem());
    }
}
