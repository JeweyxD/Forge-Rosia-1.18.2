package com.jewey.rosia.event;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.recipe.*;
import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = Rosia.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {

    @SubscribeEvent
    public static void registerRecipeTypes(final RegistryEvent.Register<RecipeSerializer<?>> event) {
        Registry.register(Registry.RECIPE_TYPE, AutoQuernRecipe.Type.ID, AutoQuernRecipe.Type.INSTANCE);
        Registry.register(Registry.RECIPE_TYPE, ExtrudingMachineRecipe.Type.ID, ExtrudingMachineRecipe.Type.INSTANCE);
        Registry.register(Registry.RECIPE_TYPE, RollingMachineRecipe.Type.ID, RollingMachineRecipe.Type.INSTANCE);
        Registry.register(Registry.RECIPE_TYPE, ScrapingMachineRecipe.Type.ID, ScrapingMachineRecipe.Type.INSTANCE);
        Registry.register(Registry.RECIPE_TYPE, BoilingCauldronRecipe.Type.ID, BoilingCauldronRecipe.Type.INSTANCE);
        Registry.register(Registry.RECIPE_TYPE, LavaBasinRecipe.Type.ID, LavaBasinRecipe.Type.INSTANCE);
    }
}
