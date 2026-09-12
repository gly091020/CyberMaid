package com.gly091020.CyberMaid.util;

import com.maxwell.cyber_ware_port.api.event.CyberwareRejectionEvent;
import com.maxwell.cyber_ware_port.api.json.CyberwareAPI;
import com.maxwell.cyber_ware_port.common.capability.CyberwareUserData;
import com.maxwell.cyber_ware_port.common.item.base.BodyPartType;
import com.maxwell.cyber_ware_port.common.item.base.ICyberware;
import com.maxwell.cyber_ware_port.common.util.CyberwareBodyStatus;
import com.maxwell.cyber_ware_port.config.CyberwareConfig;
import com.gly091020.CyberMaid.api.MaidCyberwareTick;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.maxwell.cyber_ware_port.common.capability.CyberwareUserData.isItemPowered;

public class HandleCyberwareUserDataWithoutPlayer {
    public static void recalculateCapacity(LivingEntity player, CyberwareUserData data) {
        float oldMaxHealth = player.getHealth();
        float oldMaxHealthVal = player.getMaxHealth();
        float healthRatio = oldMaxHealthVal > 0.0F ? oldMaxHealth / oldMaxHealthVal : 1.0F;
        AttributeMap attributeMap = player.getAttributes();

        for (AttributeInstance instance : attributeMap.getSyncableAttributes()) {
            List<ResourceLocation> toRemove = new ArrayList<>();
            instance.getModifiers().forEach((mod) -> {
                if (mod.id().getPath().startsWith("cyberware_slot_")) {
                    toRemove.add(mod.id());
                }

            });
            Objects.requireNonNull(instance);
            toRemove.forEach(instance::removeModifier);
        }

        int totalCapacity = 0;

        for (int i = 0; i < data.getInstalledCyberware().getSlots(); ++i) {
            ItemStack stack = data.getInstalledCyberware().getStackInSlot(i);
            ICyberware cyberware = CyberwareAPI.getCyberware(stack);
            if (cyberware != null) {
                int count = stack.getCount();
                if (cyberware.hasEnergyProperties(stack)) {
                    totalCapacity += cyberware.getEnergyStorage(stack) * count;
                }

                if (cyberware.isActive(stack)) {
                    boolean consumesEnergy = cyberware.hasEnergyProperties(stack) && cyberware.getEnergyConsumption(stack) > 0;
                    if (!consumesEnergy || data.isPowered()) {
                        int finalI = i;
                        cyberware.getAttributeModifiers(stack).forEach((attribute, originalModifier) -> {
                            AttributeInstance instance = attributeMap.getInstance(attribute);
                            if (instance != null) {
                                ResourceLocation slotId = originalModifier.id().withSuffix("_slot_" + finalI);
                                AttributeModifier newModifier = new AttributeModifier(slotId, originalModifier.amount() * (double) count, originalModifier.operation());
                                instance.removeModifier(slotId);
                                instance.addTransientModifier(newModifier);
                            }

                        });
                    }
                }
            }
        }

        var extraData = MixinUtil.extraCyberwareUserData(data);
        extraData.setMaxEnergy(totalCapacity);
        if (data.getEnergyStored() > totalCapacity) {
            extraData.setCurrentEnergy(totalCapacity);
        }

        player.setHealth(player.getMaxHealth() * Math.min(healthRatio, 1.0F));
    }

    public static void killPlayer(LivingEntity player, final String suffix) {
        Holder<DamageType> fellOutOfWorldHolder = player.damageSources().fellOutOfWorld().typeHolder();
        DamageSource source = new DamageSource(fellOutOfWorldHolder) {
            public @NotNull Component getLocalizedDeathMessage(@NotNull LivingEntity entity) {
                return Component.translatable("death.attack." + suffix, entity.getDisplayName());
            }
        };
        player.hurt(source, Float.MAX_VALUE);
    }

    public static void checkSurvival(LivingEntity player, CyberwareBodyStatus status, CyberwareUserData data) {
        if (!status.hasPart(BodyPartType.BRAIN)) {
            killPlayer(player, "cyberware.brainless");
        } else if (!status.hasPart(BodyPartType.HEART)) {
            killPlayer(player, "cyberware.heartless");
        } else if (!status.hasPart(BodyPartType.MUSCLE)) {
            killPlayer(player, "cyberware.nomuscles");
        } else if (!status.hasPart(BodyPartType.BONES)) {
            killPlayer(player, "cyberware.cyberware_missing_bone");
        } else {
            if (!status.hasPart(BodyPartType.EYES)) {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false));
            }

            if (!status.hasPart(BodyPartType.LUNGS)) {
                int air = player.getAirSupply();
                if (air > -20) {
                    player.setAirSupply(air - 1);
                    if (air <= 0 && player.tickCount % 20 == 0) {
                        player.hurt(player.damageSources().drown(), 2.0F);
                    }
                }
            }

            if (!status.hasPart(BodyPartType.STOMACH)) {
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 1, false, false));
            }

            if (status.getLegCount() == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2, false, false));
            }
        }
    }

    private static void checkRejection(LivingEntity player, CyberwareUserData data) {
        int currentTolerance = data.getTolerance(player);
        if (currentTolerance <= 0) {
            if (MixinUtil.extraCyberwareUserData(data).getRespawnGracePeriod() <= 0) {
                killPlayer(player, "cyberware.noessence");
            } else if (player.tickCount % 400 == 0) {
                player.sendSystemMessage(Component.translatable("cyberware.message.critical_condition").withStyle(ChatFormatting.RED));
            }

        } else if (data.getImmunityTime() <= 0) {
            int rejectionThreshold = CyberwareConfig.CRITICAL_ESSENCE.get();
            if (currentTolerance < rejectionThreshold) {
                CyberwareRejectionEvent event = new CyberwareRejectionEvent(player, currentTolerance);
                NeoForge.EVENT_BUS.post(event);
                if (event.isCanceled()) {
                    return;
                }

                if (player.tickCount % 100 == 0) {
                    player.setHealth(player.getHealth() - 2.0F);
                }

                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1, false, false));
                if (player.getRandom().nextFloat() < 0.01F && !player.getMainHandItem().isEmpty()) {
                    ItemStack stackToDrop = player.getMainHandItem().copy();
                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    drop(player, stackToDrop);
                }
            }

        }
    }

    // 为其他生物调用
    public static void tick(LivingEntity player, CyberwareUserData data) {
        var extraData = MixinUtil.extraCyberwareUserData(data);
        if (data.getImmunityTime() > 0) {
            extraData.setToleranceImmunityTime(data.getImmunityTime() - 1);
        }

        if (extraData.getRespawnGracePeriod() > 0) {
            data.setRespawnGracePeriod(extraData.getRespawnGracePeriod() - 1);
        }

        if (data.getEmpTicks() > 0) {
            data.setEmpTicks(data.getEmpTicks() - 1);
        }

        if (extraData.getNeedsCapacityUpdate()) {
            recalculateCapacity(player, data);
            extraData.setNeedsCapacityUpdate(false);
        }

        CyberwareBodyStatus status = new CyberwareBodyStatus(data.getInstalledCyberware());
        checkSurvival(player, status, data);
        checkRejection(player, data);

        for(int i = 0; i < data.getInstalledCyberware().getSlots(); ++i) {
            ItemStack stack = data.getInstalledCyberware().getStackInSlot(i);
            ICyberware cyberware = CyberwareAPI.getCyberware(stack);
            if (cyberware != null && isItemPowered(data, cyberware, stack)) {
                cyberware.onSystemTick(player, stack);
                if (cyberware instanceof MaidCyberwareTick maidTick) {
                    maidTick.onMaidTick(player, stack);
                }
            }
        }

        if (player.tickCount % 20 == 0) {
            processPowerTick(player, data);
        }
    }

    private static void processPowerTick(LivingEntity player, CyberwareUserData data) {
        var extraData = MixinUtil.extraCyberwareUserData(data);
        int totalProduction = 0;
        int totalConsumption = 0;

        for(int i = 0; i < data.getInstalledCyberware().getSlots(); ++i) {
            ItemStack stack = data.getInstalledCyberware().getStackInSlot(i);
            ICyberware cw = CyberwareAPI.getCyberware(stack);
            if (cw != null && cw.hasEnergyProperties(stack)) {
                int count = stack.getCount();
                ICyberware.StackingRule rule = cw.getStackingEnergyRule(stack);
                totalProduction += rule.calculate(cw.getEnergyGeneration(stack), count);
                totalConsumption += rule.calculate(cw.getEnergyConsumption(stack), count);
            }
        }

        extraData.setLastProduction(totalProduction);
        extraData.setLastConsumption(totalConsumption);
        boolean currentlyPowered = false;
        if (data.getMaxEnergyStored() > 0) {
            data.receiveEnergy(totalProduction, false);
            currentlyPowered = data.getEnergyStored() >= totalConsumption;
            if (totalConsumption > 0) {
                if (currentlyPowered) {
                    data.extractEnergy(totalConsumption, false);
                } else {
                    extraData.setCurrentEnergy(0);
                }
            }
        } else {
            currentlyPowered = totalConsumption == 0 || totalProduction >= totalConsumption;
            extraData.setCurrentEnergy(0);
        }

        if (extraData.getIsPowered() != currentlyPowered) {
            extraData.setIsPowered(currentlyPowered);
            recalculateCapacity(player, data);
        }

        if(!(player.level() instanceof ServerLevel serverLevel))return;
        serverLevel.getServer().getPlayerList().getPlayers().forEach(p -> HandleCyberwareSyncWithoutPlayer.syncToPlayer(p, player));
    }

    public static ItemEntity drop(LivingEntity entity, ItemStack stack){
        double d0 = entity.getEyeY() - (double)0.3F;
        ItemEntity itementity = new ItemEntity(entity.level(), entity.getX(), d0, entity.getZ(), stack);
        itementity.setPickUpDelay(40);

        float f8 = Mth.sin(entity.getXRot() * ((float)Math.PI / 180F));
        float f2 = Mth.cos(entity.getXRot() * ((float)Math.PI / 180F));
        float f3 = Mth.sin(entity.getYRot() * ((float)Math.PI / 180F));
        float f4 = Mth.cos(entity.getYRot() * ((float)Math.PI / 180F));
        float f5 = entity.getRandom().nextFloat() * ((float)Math.PI * 2F);
        float f6 = 0.02F * entity.getRandom().nextFloat();
        itementity.setDeltaMovement((double)(-f3 * f2 * 0.3F) + Math.cos(f5) * (double)f6, -f8 * 0.3F + 0.1F + (entity.getRandom().nextFloat() - entity.getRandom().nextFloat()) * 0.1F, (double)(f4 * f2 * 0.3F) + Math.sin(f5) * (double)f6);
        entity.level().addFreshEntity(itementity);
        return itementity;
    }
}
