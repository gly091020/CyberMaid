package com.gly091020.CyberMaid.client.renderer;

import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.SimpleBedrockModel;
import com.github.tartaricacid.touhoulittlemaid.entity.monster.EntityFairy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

// 在原模型上叠一层闪电苦力怕式的能量贴图
public class CyberFairyChargeLayer extends RenderLayer<EntityFairy, SimpleBedrockModel<EntityFairy>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/creeper/creeper_armor.png");

    public CyberFairyChargeLayer(RenderLayerParent<EntityFairy, SimpleBedrockModel<EntityFairy>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, EntityFairy entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        VertexConsumer consumer = bufferSource.getBuffer(
                RenderType.energySwirl(TEXTURE, ageInTicks * 0.01F, ageInTicks * 0.01F));
        this.getParentModel().renderToBuffer(poseStack, consumer, packedLight,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F), 0xFFFFFFFF);
    }
}
