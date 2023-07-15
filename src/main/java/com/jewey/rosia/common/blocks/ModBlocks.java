package com.jewey.rosia.common.blocks;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.blocks.custom.*;
import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.blocks.entity.block_entity.*;
import com.jewey.rosia.common.items.ModCreativeModeTab;
import com.jewey.rosia.common.items.ModItems;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.wood.HorizontalSupportBlock;
import net.dries007.tfc.common.blocks.wood.VerticalSupportBlock;
import net.dries007.tfc.util.registry.RegistrationHelpers;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Rosia.MOD_ID);

    public static final Supplier<? extends Block> FIRE_BOX = register("fire_box",
            () -> new fire_box(ExtendedProperties.of(Material.METAL, MaterialColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).lightLevel((state) -> state.getValue(fire_box.HEAT) * 2)
                    .pathType(BlockPathTypes.DAMAGE_FIRE).blockEntity(ModBlockEntities.FIRE_BOX_BLOCK_ENTITY)
                    .serverTicks(FireBoxBlockEntity::serverTick)), ModCreativeModeTab.ROSIA_TAB);

    public static final RegistryObject<Block> MACHINE_FRAME = registerBlock("machine_frame",
            () -> new Block(BlockBehaviour.Properties.of(Material.METAL).strength(5f).requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)), ModCreativeModeTab.ROSIA_TAB);

    public static final Supplier<? extends Block> STEAM_GENERATOR = register("steam_generator",
            () -> new steam_generator(ExtendedProperties.of(Material.METAL, MaterialColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).pathType(BlockPathTypes.DAMAGE_FIRE)
                    .blockEntity(ModBlockEntities.STEAM_GENERATOR_BLOCK_ENTITY)
                    .serverTicks(SteamGeneratorBlockEntity::serverTick)), ModCreativeModeTab.ROSIA_TAB);

    public static final RegistryObject<Block> NICKEL_IRON_BATTERY = registerBlock("nickel_iron_battery",
            () -> new nickel_iron_battery(BlockBehaviour.Properties.of(Material.METAL).strength(5f).requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)), ModCreativeModeTab.ROSIA_TAB);

    public static final Supplier<? extends Block> WATER_PUMP = register("water_pump",
            () -> new water_pump(ExtendedProperties.of(Material.METAL, MaterialColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).blockEntity(ModBlockEntities.WATER_PUMP_BLOCK_ENTITY)
                    .serverTicks(WaterPumpBlockEntity::serverTick).noOcclusion()), ModCreativeModeTab.ROSIA_TAB);

    public static final RegistryObject<Block> AUTO_QUERN = registerBlock("auto_quern",
            () -> new auto_quern(BlockBehaviour.Properties.of(Material.METAL).strength(5f).requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)), ModCreativeModeTab.ROSIA_TAB);

    public static final Supplier<? extends Block> EXTRUDING_MACHINE = register("extruding_machine",
            () -> new extruding_machine(ExtendedProperties.of(Material.METAL, MaterialColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).blockEntity(ModBlockEntities.EXTRUDING_MACHINE_BLOCK_ENTITY)
                    .serverTicks(ExtrudingMachineBlockEntity::serverTick).noOcclusion()), ModCreativeModeTab.ROSIA_TAB);

    public static final Supplier<? extends Block> ROLLING_MACHINE = register("rolling_machine",
            () -> new rolling_machine(ExtendedProperties.of(Material.METAL, MaterialColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).blockEntity(ModBlockEntities.ROLLING_MACHINE_BLOCK_ENTITY)
                    .serverTicks(RollingMachineBlockEntity::serverTick).noOcclusion()), ModCreativeModeTab.ROSIA_TAB);

    public static final Supplier<? extends Block> ELECTRIC_FORGE = register("electric_forge",
            () -> new electric_forge(ExtendedProperties.of(Material.METAL, MaterialColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).lightLevel((state) -> state.getValue(electric_forge.HEAT) * 2)
                    .pathType(BlockPathTypes.DAMAGE_FIRE).blockEntity(ModBlockEntities.ELECTRIC_FORGE_BLOCK_ENTITY)
                    .serverTicks(ElectricForgeBlockEntity::serverTick)), ModCreativeModeTab.ROSIA_TAB);

    public static final RegistryObject<Block> CANNING_PRESS = registerBlock("canning_press",
            () -> new canning_press(BlockBehaviour.Properties.of(Material.METAL).strength(5f).requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)), ModCreativeModeTab.ROSIA_TAB);

    public static final Supplier<? extends Block> ELECTRIC_GRILL = register("electric_grill",
            () -> new electric_grill(ExtendedProperties.of(Material.METAL, MaterialColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).lightLevel((state) -> state.getValue(electric_grill.ON) ? 12 : 0)
                    .pathType(BlockPathTypes.DAMAGE_FIRE).blockEntity(ModBlockEntities.ELECTRIC_GRILL_BLOCK_ENTITY)
                    .serverTicks(ElectricGrillBlockEntity::serverTick)), ModCreativeModeTab.ROSIA_TAB);

    public static final Supplier<? extends Block> FRIDGE = register("fridge",
            () -> new fridge(ExtendedProperties.of(Material.METAL, MaterialColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).blockEntity(ModBlockEntities.FRIDGE_BLOCK_ENTITY)
                    .serverTicks(FridgeBlockEntity::serverTick)), ModCreativeModeTab.ROSIA_TAB);

    public static final Supplier<? extends Block> ELECTRIC_LANTERN = register("electric_lantern",
            () -> new electric_lantern(ExtendedProperties.of(Material.METAL, MaterialColor.METAL).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.METAL).lightLevel((state) -> state.getValue(electric_lantern.ON) ? 15 : 0)
                    .blockEntity(ModBlockEntities.ELECTRIC_LANTERN_BLOCK_ENTITY)
                    .serverTicks(ElectricLanternBlockEntity::serverTick)), ModCreativeModeTab.ROSIA_TAB);

    public static final Supplier<? extends Block> CHARCOAL_KILN = register("charcoal_kiln",
            () -> new charcoal_kiln(ExtendedProperties.of(Material.STONE, MaterialColor.COLOR_RED).strength(5f).requiresCorrectToolForDrops()
                    .randomTicks().sound(SoundType.STONE).lightLevel((state) -> state.getValue(charcoal_kiln.LIT) ? 8 : 0)
                    .blockEntity(ModBlockEntities.CHARCOAL_KILN_BLOCK_ENTITY)
                    .serverTicks(CharcoalKilnBlockEntity::serverTick)), ModCreativeModeTab.ROSIA_TAB);



    // DON'T MAKE ITEMS FOR THE SUPPORT BEAMS IT SCREWS UP EVERYTHING!!!
    public static final Supplier<? extends Block> IRON_SUPPORT_VERTICAL = registerBlockNoItem("iron_support_vertical",
            () -> new VerticalSupportBlock(ExtendedProperties.of(Material.METAL, MaterialColor.METAL).strength(5f)
                    .requiresCorrectToolForDrops().sound(SoundType.METAL)));
    public static final Supplier<? extends Block> IRON_SUPPORT_HORIZONTAL = registerBlockNoItem("iron_support_horizontal",
            () -> new HorizontalSupportBlock(ExtendedProperties.of(Material.METAL, MaterialColor.METAL).strength(5f)
                    .requiresCorrectToolForDrops().sound(SoundType.METAL)));


    // PATHS
    public static final RegistryObject<Block> ANDESITE_PATH = registerBlock("andesite_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of(Material.STONE).strength(5f,10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)), ModCreativeModeTab.ROSIA_TAB);
    public static final RegistryObject<Block> BASALT_PATH = registerBlock("basalt_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of(Material.STONE).strength(5f,10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)), ModCreativeModeTab.ROSIA_TAB);
    public static final RegistryObject<Block> CHALK_PATH = registerBlock("chalk_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of(Material.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)), ModCreativeModeTab.ROSIA_TAB);
    public static final RegistryObject<Block> CHERT_PATH = registerBlock("chert_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of(Material.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)), ModCreativeModeTab.ROSIA_TAB);
    public static final RegistryObject<Block> CLAYSTONE_PATH = registerBlock("claystone_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of(Material.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)), ModCreativeModeTab.ROSIA_TAB);
    public static final RegistryObject<Block> CONGLOMERATE_PATH = registerBlock("conglomerate_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of(Material.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)), ModCreativeModeTab.ROSIA_TAB);
    public static final RegistryObject<Block> DACITE_PATH = registerBlock("dacite_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of(Material.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)), ModCreativeModeTab.ROSIA_TAB);
    public static final RegistryObject<Block> DIORITE_PATH = registerBlock("diorite_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of(Material.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)), ModCreativeModeTab.ROSIA_TAB);
    public static final RegistryObject<Block> DOLOMITE_PATH = registerBlock("dolomite_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of(Material.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)), ModCreativeModeTab.ROSIA_TAB);
    public static final RegistryObject<Block> GABBRO_PATH = registerBlock("gabbro_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of(Material.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)), ModCreativeModeTab.ROSIA_TAB);
    public static final RegistryObject<Block> GNEISS_PATH = registerBlock("gneiss_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of(Material.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)), ModCreativeModeTab.ROSIA_TAB);
    public static final RegistryObject<Block> GRANITE_PATH = registerBlock("granite_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of(Material.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)), ModCreativeModeTab.ROSIA_TAB);
    public static final RegistryObject<Block> LIMESTONE_PATH = registerBlock("limestone_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of(Material.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)), ModCreativeModeTab.ROSIA_TAB);
    public static final RegistryObject<Block> MARBLE_PATH = registerBlock("marble_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of(Material.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)), ModCreativeModeTab.ROSIA_TAB);
    public static final RegistryObject<Block> PHYLLITE_PATH = registerBlock("phyllite_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of(Material.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)), ModCreativeModeTab.ROSIA_TAB);
    public static final RegistryObject<Block> QUARTZITE_PATH = registerBlock("quartzite_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of(Material.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)), ModCreativeModeTab.ROSIA_TAB);
    public static final RegistryObject<Block> RHYOLITE_PATH = registerBlock("rhyolite_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of(Material.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)), ModCreativeModeTab.ROSIA_TAB);
    public static final RegistryObject<Block> SCHIST_PATH = registerBlock("schist_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of(Material.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)), ModCreativeModeTab.ROSIA_TAB);
    public static final RegistryObject<Block> SHALE_PATH = registerBlock("shale_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of(Material.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)), ModCreativeModeTab.ROSIA_TAB);
    public static final RegistryObject<Block> SLATE_PATH = registerBlock("slate_path",
            () -> new StonePathBlock(BlockBehaviour.Properties.of(Material.STONE).strength(5f, 10)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)), ModCreativeModeTab.ROSIA_TAB);



    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier, @Nullable Function<T, ? extends BlockItem> blockItemFactory)
    {
        return RegistrationHelpers.registerBlock(ModBlocks.BLOCKS, ModItems.ITEMS, name, blockSupplier, blockItemFactory);
    }

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block, CreativeModeTab tab) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn, tab);
        return toReturn;
    }
    private static <T extends Block> RegistryObject<T> registerBlockNoItem(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block, CreativeModeTab tab) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(),
                new Item.Properties().tab(tab)));
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier, CreativeModeTab group) {
        return register(name, blockSupplier, block -> {
            if (block instanceof MultiblockDevice device) {
                return device.blockItemSupplier(group).get();
            }
            else {
                return new BlockItem(block, new Item.Properties().tab(group));
            }
        });
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

}
