package com.jewey.rosia.common.blocks;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.blocks.custom.auto_quern;
import com.jewey.rosia.common.items.ModCreativeModeTab;
import com.jewey.rosia.common.items.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Rosia.MOD_ID);


    public static final RegistryObject<Block> TESTBLOCK = registerBlock("testblock",
            () -> new Block(BlockBehaviour.Properties.of(Material.METAL).strength(1f).requiresCorrectToolForDrops()),
            ModCreativeModeTab.ROSIA_TAB);
    public static final RegistryObject<Block> AUTO_QUERN = registerBlock("auto_quern",
            () -> new auto_quern(BlockBehaviour.Properties.of(Material.METAL).strength(1f).requiresCorrectToolForDrops()),
            ModCreativeModeTab.ROSIA_TAB);


    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block, CreativeModeTab tab) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn, tab);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block, CreativeModeTab tab) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(),
                new Item.Properties().tab(tab)));
    }


    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

}