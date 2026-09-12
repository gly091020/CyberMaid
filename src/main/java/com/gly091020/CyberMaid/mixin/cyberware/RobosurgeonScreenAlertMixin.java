package com.gly091020.CyberMaid.mixin.cyberware;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.CyberMaid.api.MaidCyberwareItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.maxwell.cyber_ware_port.api.json.CyberwareAPI;
import com.maxwell.cyber_ware_port.client.screen.robosurgeon.RobosurgeonScreen;
import com.maxwell.cyber_ware_port.common.block.robosurgeon.RobosurgeonBlockEntity;
import com.maxwell.cyber_ware_port.common.block.robosurgeon.surgeon.SurgeryManager;
import com.maxwell.cyber_ware_port.common.item.base.ICyberware;
import com.maxwell.cyber_ware_port.common.risk.SurgeryAlert;
import com.maxwell.cyber_ware_port.common.risk.SurgeryAnalyzer;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(RobosurgeonScreen.class)
public class RobosurgeonScreenAlertMixin {
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/maxwell/cyber_ware_port/common/risk/SurgeryAnalyzer;check(Ljava/util/List;I)Lcom/maxwell/cyber_ware_port/common/risk/SurgeryAlert;"))
    private SurgeryAlert cyberMaid$check(List<Slot> slots, int maxTolerance, Operation<SurgeryAlert> original) {
        RobosurgeonScreen screen = (RobosurgeonScreen) (Object) this;
        if (!hasMaidPatient(screen) && containsMaidOnlyItem(slots)) {
            return SurgeryAlert.create("cyberware.risk.maid_only", ChatFormatting.RED);
        }

        SurgeryAlert installAlert = checkMaxInstall(slots);
        if (installAlert != null) return installAlert;
        return original.call(slots, maxTolerance);
    }

    private static boolean hasMaidPatient(RobosurgeonScreen screen) {
        RobosurgeonBlockEntity blockEntity = screen.getMenu().blockEntity;
        if (blockEntity.getLevel() == null) return false;
        return !blockEntity.getLevel().getEntities(
                EntityTypeTest.forClass(EntityMaid.class),
                AABB.encapsulatingFullBlocks(blockEntity.getBlockPos().below(), blockEntity.getBlockPos().below(2)),
                Entity::isAlive
        ).isEmpty();
    }

    private static boolean containsMaidOnlyItem(List<Slot> slots) {
        for (int i = 0; i < RobosurgeonBlockEntity.TOTAL_SLOTS && i < slots.size(); i++) {
            ItemStack stack = slots.get(i).getItem();
            if (!stack.isEmpty()
                    && !SurgeryManager.isGhost(stack)
                    && CyberwareAPI.getCyberware(stack) instanceof MaidCyberwareItem) {
                return true;
            }
        }
        return false;
    }

    private static SurgeryAlert checkMaxInstall(List<Slot> slots) {
        Map<Item, Integer> counts = new HashMap<>();
        for (int i = 0; i < RobosurgeonBlockEntity.TOTAL_SLOTS && i < slots.size(); i++) {
            ItemStack stack = slots.get(i).getItem();
            if (!stack.isEmpty()) {
                counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
        }

        for (int i = 0; i < RobosurgeonBlockEntity.TOTAL_SLOTS && i < slots.size(); i++) {
            ItemStack stack = slots.get(i).getItem();
            ICyberware cyberware = CyberwareAPI.getCyberware(stack);
            if (cyberware != null && counts.getOrDefault(stack.getItem(), 0) > cyberware.getMaxInstallAmount(stack)) {
                return SurgeryAlert.create("cyberware.risk.too_many_installs", ChatFormatting.RED);
            }
        }
        return null;
    }
}
