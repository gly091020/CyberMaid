package com.gly091020.CyberMaid.mixin;

import com.github.tartaricacid.touhoulittlemaid.api.client.render.MaidRenderState;
import com.github.tartaricacid.touhoulittlemaid.compat.ysm.YsmCompat;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.tartaricacid.touhoulittlemaid.util.EntityCacheUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.maxwell.cyber_ware_port.client.screen.robosurgeon.RobosurgeonScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.github.tartaricacid.touhoulittlemaid.util.EntityCacheUtil.clearMaidDataResidue;

@Mixin(RobosurgeonScreen.class)
public abstract class RobosurgeonScreenMixin {
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getData(Lnet/neoforged/neoforge/attachment/AttachmentType;)Ljava/lang/Object;"))
    public Object maidData(LocalPlayer instance, AttachmentType<?> attachmentType, Operation<Object> original) {
        var self = ((RobosurgeonScreen) (Object) this);
        var blockEntity = self.getMenu().blockEntity;
        if (blockEntity.getLevel() != null) {
            var maid = blockEntity.getLevel().getEntities(EntityTypeTest.forClass(EntityMaid.class), AABB.encapsulatingFullBlocks(blockEntity.getBlockPos().below(), blockEntity.getBlockPos().below(2)), Entity::isAlive);
            if (!maid.isEmpty())
                return maid.getFirst().getData(attachmentType);
        }
        return original.call(instance, attachmentType);
    }

    @WrapOperation(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I"))
    private int getMaidName(GuiGraphics instance, Font p_283343_, String p_281896_, int p_283569_, int p_283418_, int p_281560_, boolean p_282130_, Operation<Integer> original, @Local(name = "drawX") int drawX){
        var self = ((RobosurgeonScreen) (Object) this);
        var blockEntity = self.getMenu().blockEntity;
        if (blockEntity.getLevel() != null) {
            var maid = blockEntity.getLevel().getEntities(EntityTypeTest.forClass(EntityMaid.class), AABB.encapsulatingFullBlocks(blockEntity.getBlockPos().below(), blockEntity.getBlockPos().below(2)), Entity::isAlive);
            if(!maid.isEmpty()) {
                var name = maid.getFirst().getName();
                return instance.drawString(p_283343_, name, drawX - p_283343_.width(name) / 2, p_283418_, p_281560_, p_282130_);
            }
        }
        return original.call(instance, p_283343_, p_281896_, p_283569_, p_283418_, p_281560_, p_282130_);
    }

    @WrapOperation(method = "renderBg", at = @At(value = "INVOKE", target = "Lcom/maxwell/cyber_ware_port/client/screen/robosurgeon/RobosurgeonScreen;renderEntityWithRotation(Lnet/minecraft/client/gui/GuiGraphics;IIIFLnet/minecraft/world/entity/LivingEntity;)V"))
    public void renderMaid(GuiGraphics pGuiGraphics, int pX, int pY, int pScale, float rotationYaw, LivingEntity pEntity, Operation<Void> original) {
        var self = ((RobosurgeonScreen) (Object) this);
        var blockEntity = self.getMenu().blockEntity;
        if (blockEntity.getLevel() != null) {
            var maid = blockEntity.getLevel().getEntities(EntityTypeTest.forClass(EntityMaid.class), AABB.encapsulatingFullBlocks(blockEntity.getBlockPos().below(), blockEntity.getBlockPos().below(2)), Entity::isAlive);
            if (!maid.isEmpty()) {
                var type = maid.getFirst().getType();
                var level = blockEntity.getLevel();
                long posId = blockEntity.getBlockPos().asLong();
                Entity entity;
                try {
                    if (type.equals(InitEntities.MAID.get())) {
                        entity = EntityCacheUtil.STATUE_CACHE.get(posId, () -> new EntityMaid(level));
                    } else {
                        entity = EntityCacheUtil.ENTITY_CACHE.get(type, () -> {
                            Entity e = type.create(level);
                            return Objects.requireNonNullElseGet(e, () -> new EntityMaid(level));
                        });
                    }

                    if (entity instanceof EntityMaid renderMaid) {
                        clearMaidDataResidue(renderMaid, true);
                        renderMaid.renderState = MaidRenderState.GARAGE_KIT;
                        if (YsmCompat.isInstalled() && renderMaid.isYsmModel()) {
                            renderMaid.tickCount = (int) level.getGameTime();
                        } else {
                            renderMaid.tickCount = 0;
                        }
                        renderMaid.setModelId(maid.getFirst().getModelId());
                        renderMaid.setYsmModel(maid.getFirst().getYsmModelId(), maid.getFirst().getYsmModelTexture(), maid.getFirst().getYsmModelName());
                        renderMaid.setIsYsmModel(maid.getFirst().isYsmModel());
                    }
                } catch (ExecutionException e) {
                    return;
                }
                if(!(entity instanceof LivingEntity livingEntity))return;
                original.call(pGuiGraphics, pX, pY, pScale, rotationYaw, livingEntity);
                return;
            }
        }
        original.call(pGuiGraphics, pX, pY, pScale, rotationYaw, pEntity);
    }

}
