package invtweaks;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

/**
 * Icon-size button, which get drawns in a specific way to fit its small size.
 * @author Jimeo Wan
 *
 */
public class InvTweaksGuiIconButton extends InvTweaksGuiTooltipButton {
    
    protected InvTweaksConfigManager cfgManager;
    
    private boolean useCustomTexture;
    
    public InvTweaksGuiIconButton(InvTweaksConfigManager cfgManager,
            int id, int x, int y, int w, int h, 
            String displayString, String tooltip, boolean useCustomTexture) {
        super(id, x, y, w, h, displayString, tooltip);
        this.cfgManager = cfgManager;
        this.useCustomTexture = useCustomTexture;
    }

    public void drawButton(Minecraft minecraft, int i, int j) {
        super.drawButton(minecraft, i, j);

        // Draw background (use the 4 corners of the texture to fit best its small size)
        int k = getHoverState(isMouseOverButton(i, j));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        
        if (useCustomTexture) {
            GL11.glBindTexture(3553, getTexture(minecraft, "/gui/button10px.png"));
            drawTexturedModalRect(getXPosition(), getYPosition(), (k - 1) * 10, 0, getWidth(), getHeight());
        }
        else {
            GL11.glBindTexture(3553, getTexture(minecraft, "/gui/gui.png"));
            drawTexturedModalRect(getXPosition(), getYPosition(), 1, 46 + k * 20 + 1, getWidth() / 2, getHeight() / 2);
            drawTexturedModalRect(getXPosition(), getYPosition() + getHeight() / 2, 1, 46 + k * 20 + 20 - getHeight() / 2 - 1, getWidth() / 2, getHeight() / 2);
            drawTexturedModalRect(getXPosition() + getWidth() / 2, getYPosition(), 200 - getWidth() / 2 - 1, 46 + k * 20 + 1, getWidth() / 2, getHeight() / 2);
            drawTexturedModalRect(getXPosition() + getWidth() / 2, getYPosition() + getHeight() / 2, 200 - getWidth() / 2 - 1, 46 + k * 20 + 19 - getHeight() / 2, getWidth() / 2,
                    getHeight() / 2);
        }
        
    }

}