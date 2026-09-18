package com.gly091020.CyberMaid.client.renderer;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityBabyFairyRenderer;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.NewEntityFairyRenderer;
import com.github.tartaricacid.touhoulittlemaid.entity.monster.EntityFairy;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class CyberFairyRenderer extends NewEntityFairyRenderer {
    private final EntityBabyFairyRenderer babyRenderer;

    public CyberFairyRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.addLayer(new CyberFairyChargeLayer(this));
        this.babyRenderer = new BabyRenderer(context);
    }

    @Override
    public void render(EntityFairy entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        if (entity.isBaby()) {
            this.babyRenderer.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        } else {
            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        }
    }

    // 幼年妖精沿用原模组的幼年模型，同样叠一层能量贴图
    private static class BabyRenderer extends EntityBabyFairyRenderer {
        BabyRenderer(EntityRendererProvider.Context context) {
            super(context);
            this.addLayer(new CyberFairyChargeLayer(this));
        }
    }
}
