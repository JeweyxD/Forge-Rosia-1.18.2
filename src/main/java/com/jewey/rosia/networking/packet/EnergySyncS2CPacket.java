package com.jewey.rosia.networking.packet;

import com.jewey.rosia.common.blocks.entity.block_entity.*;
import com.jewey.rosia.common.container.*;
import com.jewey.rosia.screen.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EnergySyncS2CPacket {
    private final int energy;
    private final BlockPos pos;

    public EnergySyncS2CPacket(int energy, BlockPos pos) {
        this.energy = energy;
        this.pos = pos;
    }

    public EnergySyncS2CPacket(FriendlyByteBuf buf) {
        this.energy = buf.readInt();
        this.pos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(energy);
        buf.writeBlockPos(pos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if(Minecraft.getInstance().level.getBlockEntity(pos) instanceof SteamGeneratorBlockEntity blockEntity) {
                blockEntity.setEnergyLevel(energy);

                if(Minecraft.getInstance().player.containerMenu instanceof SteamGeneratorContainer menu &&
                        menu.getBlockEntity().getBlockPos().equals(pos)) {
                    blockEntity.setEnergyLevel(energy);
                }
            }
            if(Minecraft.getInstance().level.getBlockEntity(pos) instanceof AutoQuernBlockEntity blockEntity) {
                blockEntity.setEnergyLevel(energy);

                if(Minecraft.getInstance().player.containerMenu instanceof AutoQuernMenu menu &&
                        menu.getBlockEntity().getBlockPos().equals(pos)) {
                    blockEntity.setEnergyLevel(energy);
                }
            }
            if(Minecraft.getInstance().level.getBlockEntity(pos) instanceof NickelIronBatteryBlockEntity blockEntity) {
                blockEntity.setEnergyLevel(energy);

                if(Minecraft.getInstance().player.containerMenu instanceof NickelIronBatteryContainer menu &&
                        menu.getBlockEntity().getBlockPos().equals(pos)) {
                    blockEntity.setEnergyLevel(energy);
                }
            }
            if(Minecraft.getInstance().level.getBlockEntity(pos) instanceof WaterPumpBlockEntity blockEntity) {
                blockEntity.setEnergyLevel(energy);

                if(Minecraft.getInstance().player.containerMenu instanceof WaterPumpContainer menu &&
                        menu.getBlockEntity().getBlockPos().equals(pos)) {
                    blockEntity.setEnergyLevel(energy);
                }
            }
            if(Minecraft.getInstance().level.getBlockEntity(pos) instanceof ExtrudingMachineBlockEntity blockEntity) {
                blockEntity.setEnergyLevel(energy);

                if(Minecraft.getInstance().player.containerMenu instanceof ExtrudingMachineMenu menu &&
                        menu.getBlockEntity().getBlockPos().equals(pos)) {
                    blockEntity.setEnergyLevel(energy);
                }
            }
            if(Minecraft.getInstance().level.getBlockEntity(pos) instanceof RollingMachineBlockEntity blockEntity) {
                blockEntity.setEnergyLevel(energy);

                if(Minecraft.getInstance().player.containerMenu instanceof RollingMachineMenu menu &&
                        menu.getBlockEntity().getBlockPos().equals(pos)) {
                    blockEntity.setEnergyLevel(energy);
                }
            }
            if(Minecraft.getInstance().level.getBlockEntity(pos) instanceof ElectricForgeBlockEntity blockEntity) {
                blockEntity.setEnergyLevel(energy);

                if(Minecraft.getInstance().player.containerMenu instanceof ElectricForgeContainer menu &&
                        menu.getBlockEntity().getBlockPos().equals(pos)) {
                    blockEntity.setEnergyLevel(energy);
                }
            }
            if(Minecraft.getInstance().level.getBlockEntity(pos) instanceof ElectricGrillBlockEntity blockEntity) {
                blockEntity.setEnergyLevel(energy);

                if(Minecraft.getInstance().player.containerMenu instanceof ElectricGrillContainer menu &&
                        menu.getBlockEntity().getBlockPos().equals(pos)) {
                    blockEntity.setEnergyLevel(energy);
                }
            }
            if(Minecraft.getInstance().level.getBlockEntity(pos) instanceof FridgeBlockEntity blockEntity) {
                blockEntity.setEnergyLevel(energy);

                if(Minecraft.getInstance().player.containerMenu instanceof FridgeContainer menu &&
                        menu.getBlockEntity().getBlockPos().equals(pos)) {
                    blockEntity.setEnergyLevel(energy);
                }
            }
            if(Minecraft.getInstance().level.getBlockEntity(pos) instanceof CanningPressBlockEntity blockEntity) {
                blockEntity.setEnergyLevel(energy);

                if(Minecraft.getInstance().player.containerMenu instanceof CanningPressMenu menu &&
                        menu.getBlockEntity().getBlockPos().equals(pos)) {
                    blockEntity.setEnergyLevel(energy);
                }
            }
        });
        return true;
    }
}