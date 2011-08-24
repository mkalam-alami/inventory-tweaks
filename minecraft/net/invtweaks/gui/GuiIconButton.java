package net.invtweaks.gui;

import net.invtweaks.config.InvTweaksConfigManager;
import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

/**
 * Icon-size button, which get drawns in a specific way to fit its small size.
 * @author Jimeo Wan
 *
 */
public class GuiIconButton extends GuiTooltipButton {
    
    protected InvTweaksConfigManager cfgManager;
    
    public GuiIconButton(InvTweaksConfigManager cfgManager,
            int id, int x, int y, int w, int h, 
            String displayString, String tooltip) {
        super(id, x, y, w, h, displayString, tooltip);
        this.cfgManager = cfgManager;
    }

    public void drawButton(Minecraft minecraft, int i, int j) {
        super.drawButton(minecraft, i, j);

        if (!enabled2) {
            return;
        }
        
        // Draw background (use the 4 corners of the texture to fit best its small size)
        GL11.glBindTexture(3553, minecraft.renderEngine.getTexture("/gui/gui.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int k = getHoverState(isMouseOverButton(i, j));
        drawTexturedModalRect(xPosition, yPosition, 1, 46 + k * 20 + 1, width / 2, height / 2);
        drawTexturedModalRect(xPosition, yPosition + height / 2, 1, 46 + k * 20 + 20 - height / 2 - 1, width / 2, height / 2);
        drawTexturedModalRect(xPosition + width / 2, yPosition, 200 - width / 2 - 1, 46 + k * 20 + 1, width / 2, height / 2);
        drawTexturedModalRect(xPosition + width / 2, yPosition + height / 2, 200 - width / 2 - 1, 46 + k * 20 + 19 - height / 2, width / 2,
                height / 2);
        
    }
}