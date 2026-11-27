package net.researchgates.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.researchgates.GateConfig;
import net.researchgates.Owners;
import net.researchgates.ResearchGates;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

/** Furnaces, smokers and blast furnaces: a gated recipe their owner has not opened does not burn. */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class FurnaceMixin {
    @Inject(method = "canBurn", at = @At("RETURN"), cancellable = true)
    private static void researchgates$gate(RegistryAccess access, RecipeHolder<?> recipe, NonNullList<ItemStack> inventory, int max,
                                                  AbstractFurnaceBlockEntity furnace, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || recipe == null || !GateConfig.LOCK_FURNACES.get() || furnace.getLevel() == null) {
            return;
        }
        ItemStack output = recipe.value().getResultItem(access);
        if (!ResearchGates.mayUse(furnace.getLevel(), Owners.ownerOf(furnace), recipe, output)) {
            cir.setReturnValue(false);
        }
    }
}
