package com.jewey.rosia.event;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.blocks.entity.block_entity.renderer.CanningPressBlockEntityRenderer;
import net.dries007.tfc.client.RenderHelpers;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.resource.PathResourcePack;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;


public class ModClientEvents {

    public static void init()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(ModClientEvents::onTextureStitch);
        bus.addListener(ModClientEvents::registerRenderers);
    }
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        final ResourceLocation sheet = event.getAtlas().location();
        if (sheet.equals(RenderHelpers.BLOCKS_ATLAS)) {
                event.addSprite(new ResourceLocation("rosia:block/metal/full/invar"));
                event.addSprite(new ResourceLocation("rosia:block/metal/full/purple_steel"));
                event.addSprite(new ResourceLocation("rosia:block/metal/full/weak_purple_steel"));
        }
    }

    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.CANNING_PRESS_BLOCK_ENTITY.get(),
                CanningPressBlockEntityRenderer::new);
    }
}
