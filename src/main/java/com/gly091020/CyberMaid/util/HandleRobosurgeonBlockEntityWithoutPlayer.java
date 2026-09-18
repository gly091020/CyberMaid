package com.gly091020.CyberMaid.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.maxwell.cyber_ware_port.api.event.CyberwareSurgeryEvent;
import com.maxwell.cyber_ware_port.api.json.CyberwareAPI;
import com.maxwell.cyber_ware_port.common.block.robosurgeon.RobosurgeonBlockEntity;
import com.maxwell.cyber_ware_port.common.block.robosurgeon.surgeon.SurgeryManager;
import com.maxwell.cyber_ware_port.common.block.robosurgeon.surgeon.SurgerySyncHelper;
import com.maxwell.cyber_ware_port.common.capability.CyberwareCapabilityProvider;
import com.maxwell.cyber_ware_port.common.capability.CyberwareUserData;
import com.maxwell.cyber_ware_port.common.item.base.BodyPartType;
import com.maxwell.cyber_ware_port.common.item.base.ICyberware;
import com.maxwell.cyber_ware_port.common.util.CyberwareBodyStatus;
import com.maxwell.cyber_ware_port.init.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.maxwell.cyber_ware_port.common.block.robosurgeon.RobosurgeonBlockEntity.TOTAL_SLOTS;

public class HandleRobosurgeonBlockEntityWithoutPlayer {
    // 条件不满足时每隔这么多 tick 提醒一次，避免刷屏
    private static final int NOTIFY_INTERVAL = 40;

    // 肢体部件：这些槽位空着就不允许做手术，除非这次手术就是把缺的部件装回去
    private static final List<BodyPartType> LIMB_PARTS = List.of(
            BodyPartType.ARM_LEFT, BodyPartType.ARM_RIGHT,
            BodyPartType.HAND_LEFT, BodyPartType.HAND_RIGHT,
            BodyPartType.LEG_LEFT, BodyPartType.LEG_RIGHT,
            BodyPartType.FOOT_LEFT, BodyPartType.FOOT_RIGHT);

    public static boolean checkRequirements(LivingEntity player, RobosurgeonBlockEntity block) {
        return surgeryBlockReason(player, block) == null;
    }

    public static Component surgeryBlockReason(LivingEntity player, RobosurgeonBlockEntity block) {
        CyberwareUserData data = player.getData(CyberwareCapabilityProvider.CYBERWARE_DATA.get());
        ItemStackHandler playerBody = data.getInstalledCyberware();
        ItemStackHandler futureBody = new ItemStackHandler(TOTAL_SLOTS);
        Map<Item, Integer> futureCounts = new HashMap<>();
        List<ItemStack> futureStacks = new ArrayList<>();

        for(int i = 0; i < TOTAL_SLOTS; ++i) {
            ItemStack table = block.getItemHandler().getStackInSlot(i);
            ItemStack finalStack = SurgeryManager.isGhost(table) ? playerBody.getStackInSlot(i) : table;
            futureBody.setStackInSlot(i, finalStack.copy());
            if (!finalStack.isEmpty()) {
                futureStacks.add(finalStack);
                futureCounts.put(finalStack.getItem(), futureCounts.getOrDefault(finalStack.getItem(), 0) + finalStack.getCount());
            }
        }

        if (player instanceof EntityMaid) {
            BodyPartType missing = findMissingLimb(futureBody);
            if (missing != null) {
                return Component.translatable("cyberware.risk.maid_limb_missing", limbName(missing));
            }
        }

        for(ItemStack stack : futureStacks) {
            ICyberware cw = CyberwareAPI.getCyberware(stack);
            if (cw != null) {
                if (futureCounts.get(stack.getItem()) > cw.getMaxInstallAmount(stack)) {
                    return Component.translatable("cyberware.risk.too_many_installs");
                }

                for(Item req : cw.getPrerequisites(stack)) {
                    if (futureStacks.stream().noneMatch((s) -> s.is(req))) {
                        return Component.translatable("cyberware.risk.missing_requirement");
                    }
                }
            }
        }

        return null;
    }

    public static BodyPartType findMissingLimb(ItemStackHandler body) {
        CyberwareBodyStatus status = new CyberwareBodyStatus(body);
        for (BodyPartType part : LIMB_PARTS) {
            if (!status.hasPart(part)) {
                return part;
            }
        }
        return null;
    }

    // 女仆手术被拦下时通知主人，同一原因每隔 NOTIFY_INTERVAL tick 只发一次
    public static void notifyBlockedSurgery(EntityMaid maid, RobosurgeonBlockEntity block) {
        if (!shouldNotify(maid.level())) return;
        Component reason = surgeryBlockReason(maid, block);
        if (reason != null) {
            notifyOwner(maid, Component.translatable("cyberware.risk.surgery_blocked", reason));
        }
    }

    public static void notifyOwner(EntityMaid maid, Component message) {
        ServerPlayer owner = maid.getOwner() instanceof ServerPlayer player ? player : null;
        if (owner == null && maid.getOwnerUUID() != null && maid.getServer() != null) {
            owner = maid.getServer().getPlayerList().getPlayer(maid.getOwnerUUID());
        }
        if (owner != null) {
            owner.sendSystemMessage(message.copy().withStyle(ChatFormatting.RED));
        }
    }

    public static boolean shouldNotify(Level level) {
        return level.getGameTime() % NOTIFY_INTERVAL == 0L;
    }

    private static Component limbName(BodyPartType part) {
        Item humanPart = switch (part) {
            case ARM_LEFT -> ModItems.HUMAN_LEFT_ARM.get();
            case ARM_RIGHT -> ModItems.HUMAN_RIGHT_ARM.get();
            case HAND_LEFT -> ModItems.HUMAN_LEFT_HAND.get();
            case HAND_RIGHT -> ModItems.HUMAN_RIGHT_HAND.get();
            case LEG_LEFT -> ModItems.HUMAN_LEFT_LEG.get();
            case LEG_RIGHT -> ModItems.HUMAN_RIGHT_LEG.get();
            case FOOT_LEFT -> ModItems.HUMAN_LEFT_FOOT.get();
            case FOOT_RIGHT -> ModItems.HUMAN_RIGHT_FOOT.get();
            default -> null;
        };
        return humanPart == null ? Component.literal(part.name()) : humanPart.getDescription();
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
            HandleCyberwareSyncWithoutPlayer.syncToTrackingPlayers(player);
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
