package com.gly091020.CyberMaid.util;

import com.maxwell.cyber_ware_port.api.event.CyberwareSurgeryEvent;
import com.maxwell.cyber_ware_port.api.json.CyberwareAPI;
import com.maxwell.cyber_ware_port.common.block.robosurgeon.RobosurgeonBlockEntity;
import com.maxwell.cyber_ware_port.common.block.robosurgeon.surgeon.SurgeryManager;
import com.maxwell.cyber_ware_port.common.block.robosurgeon.surgeon.SurgerySyncHelper;
import com.maxwell.cyber_ware_port.common.capability.CyberwareCapabilityProvider;
import com.maxwell.cyber_ware_port.common.capability.CyberwareUserData;
import com.maxwell.cyber_ware_port.common.item.base.ICyberware;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.maxwell.cyber_ware_port.common.block.robosurgeon.RobosurgeonBlockEntity.TOTAL_SLOTS;

public class HandleRobosurgeonBlockEntityWithoutPlayer {
    public static boolean checkRequirements(LivingEntity player, RobosurgeonBlockEntity block) {
        CyberwareUserData data = player.getData(CyberwareCapabilityProvider.CYBERWARE_DATA.get());
        ItemStackHandler playerBody = data.getInstalledCyberware();
        Map<Item, Integer> futureCounts = new HashMap<>();
        List<ItemStack> futureBody = new ArrayList<>();

        for(int i = 0; i < TOTAL_SLOTS; ++i) {
            ItemStack table = block.getItemHandler().getStackInSlot(i);
            ItemStack finalStack = SurgeryManager.isGhost(table) ? playerBody.getStackInSlot(i) : table;
            if (!finalStack.isEmpty()) {
                futureBody.add(finalStack);
                futureCounts.put(finalStack.getItem(), futureCounts.getOrDefault(finalStack.getItem(), 0) + finalStack.getCount());
            }
        }

        for(ItemStack stack : futureBody) {
            ICyberware cw = CyberwareAPI.getCyberware(stack);
            if (cw != null) {
                if (futureCounts.get(stack.getItem()) > cw.getMaxInstallAmount(stack)) {
                    return false;
                }

                for(Item req : cw.getPrerequisites(stack)) {
                    if (futureBody.stream().noneMatch((s) -> s.is(req))) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static boolean needsSurgery(LivingEntity livingEntity, RobosurgeonBlockEntity blockEntity) {
        CyberwareUserData data = livingEntity.getData(CyberwareCapabilityProvider.CYBERWARE_DATA.get());
        ItemStackHandler playerBody = data.getInstalledCyberware();

        for(int i = 0; i < TOTAL_SLOTS; ++i) {
            ItemStack table = blockEntity.getItemHandler().getStackInSlot(i);
            if (!SurgeryManager.isGhost(table) && !ItemStack.matches(table, playerBody.getStackInSlot(i))) {
                return true;
            }
        }

        return false;
    }

    public static void performSurgery(LivingEntity player, RobosurgeonBlockEntity blockEntity) {
        if (checkRequirements(player, blockEntity)) {
            CyberwareSurgeryEvent.Pre preEvent = new CyberwareSurgeryEvent.Pre(player, blockEntity);
            NeoForge.EVENT_BUS.post(preEvent);
            CyberwareUserData userData = player.getData(CyberwareCapabilityProvider.CYBERWARE_DATA.get());
            ItemStackHandler body = userData.getInstalledCyberware();
            List<ItemStack> before = snapshot(body);
            HandleSurgeryWithoutPlayer.execute(player, blockEntity.getItemHandler(), body);
            dispatchSurgeryChanges(player, before, body);
            HandleCyberwareUserDataWithoutPlayer.recalculateCapacity(player, userData);
            populateGhostItems(player, blockEntity);
            player.level().playSound(null, player.blockPosition(), SoundEvents.IRON_GOLEM_HURT, SoundSource.PLAYERS, 1.0F, 1.0F);
            NeoForge.EVENT_BUS.post(new CyberwareSurgeryEvent.Post(player, blockEntity));
        }
    }

    private static List<ItemStack> snapshot(ItemStackHandler handler) {
        List<ItemStack> stacks = new ArrayList<>(handler.getSlots());
        for (int i = 0; i < handler.getSlots(); i++) {
            stacks.add(handler.getStackInSlot(i).copy());
        }
        return stacks;
    }

    private static void dispatchSurgeryChanges(LivingEntity entity, List<ItemStack> before, ItemStackHandler after) {
        for (int i = 0; i < before.size(); i++) {
            ItemStack oldStack = before.get(i);
            ItemStack newStack = after.getStackInSlot(i);
            if (ItemStack.matches(oldStack, newStack)) continue;

            ICyberware oldCyberware = CyberwareAPI.getCyberware(oldStack);
            ICyberware newCyberware = CyberwareAPI.getCyberware(newStack);
            if (oldCyberware != null) oldCyberware.onRemoved(entity, oldStack);
            if (newCyberware != null) newCyberware.onInstalled(entity, newStack);
        }
    }

    public static void populateGhostItems(LivingEntity player,  RobosurgeonBlockEntity blockEntity) {
        CyberwareUserData userData = player.getData(CyberwareCapabilityProvider.CYBERWARE_DATA.get());
        if (SurgerySyncHelper.updateGhosts(userData.getInstalledCyberware(), blockEntity.getItemHandler())) {
            blockEntity.setChanged();
            if (blockEntity.getLevel() != null) {
                blockEntity.getLevel().sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
            }
        }

    }
}
