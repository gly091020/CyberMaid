package com.gly091020.CyberMaid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.maxwell.cyber_ware_port.common.block.surgerychamber.SurgeryChamberBlock;
import com.maxwell.cyber_ware_port.init.ModBlocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import static com.maxwell.cyber_ware_port.common.capability.CyberwareCapabilityProvider.CYBERWARE_DATA;

@EventBusSubscriber
public class EventHandler {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerEntity(Capabilities.EnergyStorage.ENTITY, InitEntities.MAID.get(), (player, side) -> player.getData(CYBERWARE_DATA.get()));
    }

    @SubscribeEvent
    public static void putMaidIntoSurgeryChamberBlock(PlayerInteractEvent.RightClickBlock event){
        if(!(event.getEntity().getFirstPassenger() instanceof EntityMaid maid))return;
        var pos = event.getHitVec().getBlockPos();
        var state = event.getEntity().level().getBlockState(pos);
        if(!state.is(ModBlocks.SURGERY_CHAMBER))return;
        var half = state.getOptionalValue(SurgeryChamberBlock.HALF);
        if(half.isPresent() && half.get() == DoubleBlockHalf.UPPER)
            pos = pos.below(1);
        maid.stopRiding();
        maid.setPos(pos.getCenter().subtract(0, 7 / 16f, 0));
        maid.setInSittingPose(true);
        event.setCanceled(true);
    }
}
