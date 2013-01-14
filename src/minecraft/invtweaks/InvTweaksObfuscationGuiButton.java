package invtweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/**
 * Obfuscation layer for gui buttons.
 * @author Jimeo Wan
 *
 */
public class InvTweaksObfuscationGuiButton extends GuiButton {

    public InvTweaksObfuscationGuiButton(int id, int x, int y, int w, int h, String string) {
        super(id, x, y, w, h, string);
    }

    protected String getDisplayString() {
        return displayString;
    }

    protected int getTexture(Minecraft mc, String texture) {
        return mc.renderEngine.getTexture(texture);
    }

    protected boolean isEnabled() {
        return enabled;
    }

    protected int getXPosition() {
        return xPosition;
    }

    protected int getYPosition() {
        return yPosition;
    }

    protected int getWidth() {
        return width;
    }

    protected int getHeight() {
        return height;
    }
}
