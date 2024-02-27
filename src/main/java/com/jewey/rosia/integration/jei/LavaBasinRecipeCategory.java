package com.jewey.rosia.integration.jei;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.blocks.ModBlocks;
import com.jewey.rosia.recipe.LavaBasinRecipe;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.dries007.tfc.compat.jei.JEIIntegration;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class LavaBasinRecipeCategory implements IRecipeCategory<LavaBasinRecipe> {

    public final static ResourceLocation UID = new ResourceLocation(Rosia.MOD_ID, "lava_basin");
    public final static ResourceLocation TEXTURE =
            new ResourceLocation(Rosia.MOD_ID, "textures/gui/lava_basin_jei.png");

    private final IDrawable background;
    private final IDrawable icon;
    protected final IDrawableStatic slot;
    protected final IDrawableStatic fire;
    protected final IDrawableAnimated fireAnimated;

    public LavaBasinRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 97, 25);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(ModBlocks.BOILING_CAULDRON.get()));

        this.slot = helper.getSlotDrawable();
        this.fire = helper.createDrawable(TEXTURE, 99, 0, 14, 14);
        IDrawableStatic fireAnimated = helper.createDrawable(TEXTURE, 99, 15, 14, 14);
        this.fireAnimated = helper.createAnimatedDrawable(fireAnimated, 160, IDrawableAnimated.StartDirection.TOP, false);
    }

    @Override
    public void draw(LavaBasinRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        fire.draw(stack, 41, 6);
        fireAnimated.draw(stack, 41, 6);
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends LavaBasinRecipe> getRecipeClass() {
        return LavaBasinRecipe.class;
    }

    @Override
    public Component getTitle() {
        return new TextComponent("Lava Basin");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull LavaBasinRecipe recipe, @Nonnull IFocusGroup focusGroup) {
        //Fluid input
        final FluidStack inputFluid = new FluidStack(Fluids.LAVA, 100);
        IRecipeSlotBuilder fluidOutput = builder.addSlot(RecipeIngredientRole.INPUT, 5, 5);
        fluidOutput.addIngredient(JEIIntegration.FLUID_STACK, inputFluid);
        fluidOutput.setFluidRenderer(1, false, 16, 16);
        fluidOutput.setBackground(slot, -1, -1);

        //Item output
        builder.addSlot(RecipeIngredientRole.OUTPUT, 77, 5).addItemStack(recipe.getResultItem());
    }
}
