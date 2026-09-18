package com.gly091020.CyberMaid.entity;

import com.github.tartaricacid.touhoulittlemaid.entity.monster.EntityFairy;
import com.gly091020.CyberMaid.CyberMaid;
import com.maxwell.cyber_ware_port.common.entity.ICyberwareMob;
import com.maxwell.cyber_ware_port.init.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.List;

// 掉落走前置模组的赛博生物管线：掉落内容由 cyber_maid:cyber_fairy_drops 标签决定，掉落物会被标成非全新
public class CyberFairy extends EntityFairy implements ICyberwareMob {
    public static final TagKey<Item> DROP_TAG = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(CyberMaid.MODID, "cyber_fairy_drops"));

    public CyberFairy(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        // 原模组妖精要靠 FlyingMoveControl 第一次移动后才 noGravity，赛博妖精直接常驻悬空
        this.setNoGravity(true);
    }

    // 直接沿用原模组妖精的属性（含 FLYING_SPEED，飞行能力靠它 + FlyingMoveControl）
    public static AttributeSupplier.Builder createAttributes() {
        return EntityFairy.createFairyAttributes();
    }

    @Override
    public List<Item> getSpecialDrops() {
        return this.level().registryAccess().lookupOrThrow(Registries.ITEM)
                .get(DROP_TAG)
                .map(holders -> holders.stream().map(Holder::value).toList())
                .orElse(List.of());
    }

    // 普通池里全是前置模组自家物品，这里全部排除，赛博妖精只掉本模组的部件
    @Override
    public List<Item> getForbiddenDrops() {
        return ModItems.ITEMS.getEntries().stream().map(holder -> (Item) holder.get()).toList();
    }
}
