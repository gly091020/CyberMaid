package com.gly091020.CyberMaid.client;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.maxwell.cyber_ware_port.client.ClientCyberwareSettings;
import com.maxwell.cyber_ware_port.client.screen.robosurgeon.RobosurgeonScreen;
import com.maxwell.cyber_ware_port.client.upgrades.cybereye.CyberwareMenuScreen;
import com.maxwell.cyber_ware_port.common.capability.CyberwareCapabilityProvider;
import com.maxwell.cyber_ware_port.common.capability.CyberwareUserData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;

public class MaidCyberwareMenuScreen extends CyberwareMenuScreen {
    private static final int BAR_WIDTH = 80;
    private static final int BAR_HEIGHT = 12;

    public MaidCyberwareMenuScreen(int maidId) {
        ((MaidMenuTarget) (Object) this).setMaidTargetId(maidId);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        if(isColorSettingsOpen || isHudMoveMode)return;

        EntityMaid maid = getMaid();
        if (maid == null) return;

        int renderX = this.width / 6;
        int renderY = this.height / 2 + 100;
        RobosurgeonScreen.renderEntityWithRotation(graphics, renderX, renderY, 100, 30, maid);

        CyberwareUserData data = maid.getData(CyberwareCapabilityProvider.CYBERWARE_DATA.get());
        int stored = data.getEnergyStored();
        int max = data.getMaxEnergyStored();
        int barX = renderX - BAR_WIDTH / 2;
        int barY = renderY - 200;

        graphics.fill(barX - 1, barY - 1, barX + BAR_WIDTH + 1, barY + BAR_HEIGHT + 1, 0xFF000000);
        int filled = max > 0 ? (int) ((long) stored * BAR_WIDTH / max) : 0;
        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, 0XFF555555);
        graphics.fill(barX, barY, barX + filled, barY + BAR_HEIGHT, ClientCyberwareSettings.hudColor);
        graphics.drawCenteredString(this.font, stored + "/" + max, barX + BAR_WIDTH / 2, barY - 12, ClientCyberwareSettings.hudColor);
    }

    private EntityMaid getMaid() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return null;
        Entity entity = minecraft.level.getEntity(((MaidMenuTarget) this).getMaidTargetId());
        return entity instanceof EntityMaid maid ? maid : null;
    }
}
