package com.jewey.rosia.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jewey.rosia.Rosia;
import net.dries007.tfc.util.JsonHelpers;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class LavaBasinRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final ItemStack output;

    @Override
    public boolean isSpecial() {
        return true;
    }

    public LavaBasinRecipe(ResourceLocation id, ItemStack output) {
        this.id = id;
        this.output = output;
    }

    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        return true;
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer) {
        return output;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return output.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<LavaBasinRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "lava_basin";
    }

    public static class Serializer implements RecipeSerializer<LavaBasinRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(Rosia.MOD_ID,"lava_basin");

        @Override
        public LavaBasinRecipe fromJson(ResourceLocation id, JsonObject json) {
            final ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));

            return new LavaBasinRecipe(id, output);
        }

        @Override
        public LavaBasinRecipe fromNetwork(@NotNull ResourceLocation id, FriendlyByteBuf buf) {
            ItemStack output = buf.readItem();

            return new LavaBasinRecipe(id, output);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, LavaBasinRecipe recipe) {
            buf.writeInt(recipe.getIngredients().size());
            for (Ingredient ing : recipe.getIngredients()) {
                ing.toNetwork(buf);
            }
            buf.writeItemStack(recipe.getResultItem(), false);
        }

        @Override
        public RecipeSerializer<?> setRegistryName(ResourceLocation name) {
            return INSTANCE;
        }

        @Nullable
        @Override
        public ResourceLocation getRegistryName() {
            return ID;
        }

        @Override
        public Class<RecipeSerializer<?>> getRegistryType() {
            return Serializer.castClass(RecipeSerializer.class);
        }

        @SuppressWarnings("unchecked") // Need this wrapper, because generics
        private static <G> Class<G> castClass(Class<?> cls) {
            return (Class<G>)cls;
        }
    }
}
