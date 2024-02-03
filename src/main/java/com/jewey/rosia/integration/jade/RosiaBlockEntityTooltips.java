package com.jewey.rosia.integration.jade;

import com.jewey.rosia.common.blocks.custom.*;
import com.jewey.rosia.common.blocks.entity.block_entity.*;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.compat.jade.common.BlockEntityTooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import java.util.function.BiConsumer;

import static net.dries007.tfc.compat.jade.common.BlockEntityTooltips.heat;
import static net.dries007.tfc.compat.jade.common.BlockEntityTooltips.timeLeft;

public class RosiaBlockEntityTooltips {
    public static void register(BiConsumer<BlockEntityTooltip, Class<? extends Block>> registerBlock)
    {
        registerBlock.accept(KILN, charcoal_kiln.class);
        registerBlock.accept(FIRE_BOX, fire_box.class);
        registerBlock.accept(STEAM_GENERATOR, steam_generator.class);
        registerBlock.accept(ELECTRIC_FORGE, electric_forge.class);
        registerBlock.accept(BOILING_CAULDRON, boiling_cauldron.class);
        registerBlock.accept(LAVA_BASIN, lava_basin.class);
    }

    public static final BlockEntityTooltip KILN = (level, state, pos, entity, tooltip) -> {
        if (entity instanceof CharcoalKilnBlockEntity kiln && state.getBlock() instanceof charcoal_kiln)
        {
            if (state.getValue(charcoal_kiln.LIT))
            {
                final long ticksLeft = kiln.getRemainingTicks();
                if (ticksLeft > 0)
                {
                    timeLeft(level, tooltip, ticksLeft);
                }
            }
        }
    };
    public static final BlockEntityTooltip FIRE_BOX = (level, state, pos, entity, tooltip) -> {
        if (entity instanceof FireBoxBlockEntity forge)
        {
            heat(tooltip, forge.getTemperature());
        }
    };
    public static final BlockEntityTooltip STEAM_GENERATOR = (level, state, pos, entity, tooltip) -> {
        if (entity instanceof SteamGeneratorBlockEntity generator)
        {
            generator.getCapability(HeatCapability.BLOCK_CAPABILITY).ifPresent(cap -> heat(tooltip, cap.getTemperature()));
        }
    };
    public static final BlockEntityTooltip ELECTRIC_FORGE = (level, state, pos, entity, tooltip) -> {
        if (entity instanceof ElectricForgeBlockEntity forge)
        {
            heat(tooltip, forge.getTemperature());
        }
    };
    public static final BlockEntityTooltip BOILING_CAULDRON = (level, state, pos, entity, tooltip) -> {
        if (entity instanceof BoilingCauldronBlockEntity boiler)
        {
            heat(tooltip, boiler.getTemperature());
        }
        if (state.getValue(boiling_cauldron.LIT))
        {
            tooltip.accept(Component.nullToEmpty("Boiling!"));
        }
    };
    public static final BlockEntityTooltip LAVA_BASIN = (level, state, pos, entity, tooltip) -> {
        if (entity instanceof LavaBasinBlockEntity boiler)
        {
            heat(tooltip, boiler.getTemperature());
        }
    };
}
