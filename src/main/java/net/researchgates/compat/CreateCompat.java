package net.researchgates.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.researchgates.ResearchGates;

/**
 * Create, when installed: item application (an alloy or ingot used on a log makes a casing) is refused while its recipe is gated.
 * The deployer and mechanical crafter are mixins ({@code researchgates.create.mixins.json}).
 */
public final class CreateCompat {
    private CreateCompat() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, PlayerInteractEvent.RightClickBlock.class, CreateCompat::onUseOnBlock);
    }

    private static void onUseOnBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack held = event.getItemStack();
        if (held.isEmpty()) {
            return;
        }
        RecipeType<?> type = BuiltInRegistries.RECIPE_TYPE.get(ResourceLocation.fromNamespaceAndPath("create", "item_application"));
        if (type == null) {
            return;
        }
        ItemStack block = new ItemStack(event.getLevel().getBlockState(event.getPos()).getBlock().asItem());
        for (RecipeHolder<?> holder : event.getLevel().getRecipeManager().getRecipes()) {
            Recipe<?> recipe = holder.value();
            if (recipe.getType() != type || recipe.getIngredients().size() < 2) {
                continue;
            }
            Ingredient target = recipe.getIngredients().get(0);
            Ingredient applied = recipe.getIngredients().get(1);
            if (!target.test(block) || !applied.test(held)) {
                continue;
            }
            Component refusal = ResearchGates.refusal(player, holder, recipe.getResultItem(event.getLevel().registryAccess()));
            if (refusal != null) {
                player.displayClientMessage(refusal, true);
                event.setCanceled(true);
                return;
            }
        }
    }
}
