package youyihj.tinkersoredictcache.mixins;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.mantle.util.RecipeMatch;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.smeltery.MeltingRecipe;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import youyihj.tinkersoredictcache.Configuration;
import youyihj.tinkersoredictcache.TinkersOreDictCache;
import youyihj.tinkersoredictcache.OreDictRecipesState;

import java.io.File;
import java.io.IOException;

/**
 * @author youyihj
 */
@Mixin(value = TinkerSmeltery.class, remap = false)
public class MixinTinkerSmeltery {
    @Inject(method = "registerRecipeOredictMelting", at = @At("HEAD"), cancellable = true)
    private static void injectRegisterRecipeOredictMeltingHead(CallbackInfo ci) {
        if (Configuration.disableOreDictMelting) {
            ci.cancel();
            return;
        }
        if (OreDictRecipesState.getCurrentState().isRead()) {
            final NBTTagCompound cacheNBT = TinkersOreDictCache.INSTANCE.cacheNBT;
            final NBTTagList recipeList = cacheNBT.getTagList("Recipes", Constants.NBT.TAG_COMPOUND);
            for (NBTBase nbtBase : recipeList) {
                final NBTTagCompound compound = (NBTTagCompound) nbtBase;
                final ItemStack item = new ItemStack(compound.getCompoundTag("item"));
                final FluidStack fluid = FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("fluid"));
                if (!item.isEmpty() && fluid != null) {
                    TinkerRegistry.registerMelting(new MeltingRecipe(RecipeMatch.of(item, fluid.amount), fluid));
                }
            }
            ci.cancel();
        }
    }

    @Inject(method = "registerRecipeOredictMelting", at = @At("TAIL"))
    private static void injectRegisterRecipeOredictMeltingTail(CallbackInfo ci) {
        if (OreDictRecipesState.getCurrentState().isScan()) {
            try {
                File file = TinkersOreDictCache.INSTANCE.cacheFile;
                if (!file.exists()) {
                    file.createNewFile();
                }
                CompressedStreamTools.write(TinkersOreDictCache.INSTANCE.cacheNBT, TinkersOreDictCache.INSTANCE.cacheFile);
            } catch (IOException e) {
                TinkersOreDictCache.INSTANCE.logger.error("failed to write cache file", e);
            }
        }
    }

    @Redirect(method = "registerRecipeOredictMelting", at = @At(value = "INVOKE", target = "Lslimeknights/tconstruct/library/TinkerRegistry;registerMelting(Lslimeknights/tconstruct/library/smeltery/MeltingRecipe;)V"))
    private static void redirectRegisterMelting(MeltingRecipe recipe) {
        TinkerRegistry.registerMelting(recipe);
        if (OreDictRecipesState.getCurrentState().isScan()) {
            final NBTTagCompound cacheNBT = TinkersOreDictCache.INSTANCE.cacheNBT;
            if (!cacheNBT.hasKey("Recipes")) {
                cacheNBT.setTag("Recipes", new NBTTagList());
            }
            final NBTTagList recipeList = cacheNBT.getTagList("Recipes", Constants.NBT.TAG_COMPOUND);
            final NBTTagCompound recipeNBT = new NBTTagCompound();
            final ItemStack input = recipe.input.getInputs().get(0);
            final FluidStack output = recipe.output;
            if (!input.isEmpty() && FluidRegistry.isFluidRegistered(output.getFluid())) {
                recipeNBT.setTag("item", input.serializeNBT());
                recipeNBT.setTag("fluid", fluidToNBT(output));
                recipeList.appendTag(recipeNBT);
            }
        }
    }

    private static NBTTagCompound fluidToNBT(FluidStack fluidStack) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("FluidName", fluidStack.getFluid().getName());
        nbt.setInteger("Amount", fluidStack.amount);

        if (fluidStack.tag != null)
        {
            nbt.setTag("Tag", fluidStack.tag);
        }
        return nbt;
    }
}
