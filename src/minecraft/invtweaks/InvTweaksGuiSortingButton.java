package invtweaks;

import net.minecraft.client.Minecraft;

/**
 * Chest sorting button
 * @author Jimeo Wan
 *
 */
public class InvTweaksGuiSortingButton extends InvTweaksGuiIconButton {

    private final InvTweaksContainerSection section = InvTweaksContainerSection.CHEST;
    
    private int algorithm;
    private int rowSize;

    public InvTweaksGuiSortingButton(InvTweaksConfigManager cfgManager, 
            int id, int x, int y, int w, int h,
            String displayString, String tooltip,
            int algorithm, int rowSize, boolean useCustomTexture) {
        super(cfgManager, id, x, y, w, h, displayString, tooltip, useCustomTexture);
        this.algorithm = algorithm;
        this.rowSize = rowSize;
    }

    public void drawButton(Minecraft minecraft, int i, int j) {
        super.drawButton(minecraft, i, j);
        
        // Display symbol
        int textColor = getTextColor(i, j);
        if (getDisplayString().equals("h")) {
            drawRect(getXPosition() + 3, getYPosition() + 3, getXPosition() + getWidth() - 3, getYPosition() + 4, textColor);
            drawRect(getXPosition() + 3, getYPosition() + 6, getXPosition() + getWidth() - 3, getYPosition() + 7, textColor);
        } else if (getDisplayString().equals("v")) {
            drawRect(getXPosition() + 3, getYPosition() + 3, getXPosition() + 4, getYPosition() + getHeight() - 3, textColor);
            drawRect(getXPosition() + 6, getYPosition() + 3, getXPosition() + 7, getYPosition() + getHeight() - 3, textColor);
        } else {
            drawRect(getXPosition() + 3, getYPosition() + 3, getXPosition() + getWidth() - 3, getYPosition() + 4, textColor);
            drawRect(getXPosition() + 5, getYPosition() + 4, getXPosition() + 6, getYPosition() + 5, textColor);
            drawRect(getXPosition() + 4, getYPosition() + 5, getXPosition() + 5, getYPosition() + 6, textColor);
            drawRect(getXPosition() + 3, getYPosition() + 6, getXPosition() + getWidth() - 3, getYPosition() + 7, textColor);
        }
    }

    /**
     * Sort container
     */
    public boolean mousePressed(Minecraft minecraft, int i, int j) {
        if (super.mousePressed(minecraft, i, j)) {
            try {
                new InvTweaksHandlerSorting(
                        minecraft, cfgManager.getConfig(),
                        section, algorithm, rowSize).sort();
            } catch (Exception e) {
                InvTweaks.logInGameErrorStatic("invtweaks.sort.chest.error", e);
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }

    }
    
}
