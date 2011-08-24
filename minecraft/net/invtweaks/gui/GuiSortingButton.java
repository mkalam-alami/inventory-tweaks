package net.invtweaks.gui;

import net.invtweaks.config.InvTweaksConfigManager;
import net.invtweaks.library.ContainerManager.ContainerSection;
import net.invtweaks.logic.SortingHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.src.InvTweaks;

/**
 * Chest sorting button
 * @author Jimeo Wan
 *
 */
public class GuiSortingButton extends GuiIconButton {

    private final ContainerSection section = ContainerSection.CHEST;
    
    private int algorithm;

    public GuiSortingButton(InvTweaksConfigManager cfgManager, 
            int id, int x, int y, int w, int h,
            String displayString, String tooltip,
            int algorithm) {
        super(cfgManager, id, x, y, w, h, displayString, tooltip);
        this.algorithm = algorithm;
    }

    public void drawButton(Minecraft minecraft, int i, int j) {
        super.drawButton(minecraft, i, j);
        
        if (!enabled2) {
            return;
        }
        
        // Display symbol
        int textColor = getTextColor(i, j);
        if (displayString.equals("h")) {
            drawRect(xPosition + 3, yPosition + 3, xPosition + width - 3, yPosition + 4, textColor);
            drawRect(xPosition + 3, yPosition + 6, xPosition + width - 3, yPosition + 7, textColor);
        } else if (displayString.equals("v")) {
            drawRect(xPosition + 3, yPosition + 3, xPosition + 4, yPosition + height - 3, textColor);
            drawRect(xPosition + 6, yPosition + 3, xPosition + 7, yPosition + height - 3, textColor);
        } else {
            drawRect(xPosition + 3, yPosition + 3, xPosition + width - 3, yPosition + 4, textColor);
            drawRect(xPosition + 5, yPosition + 4, xPosition + 6, yPosition + 5, textColor);
            drawRect(xPosition + 4, yPosition + 5, xPosition + 5, yPosition + 6, textColor);
            drawRect(xPosition + 3, yPosition + 6, xPosition + width - 3, yPosition + 7, textColor);
        }
    }

    /**
     * Sort container
     */
    public boolean mousePressed(Minecraft minecraft, int i, int j) {
        if (super.mousePressed(minecraft, i, j)) {
            try {
                new SortingHandler(
                        minecraft, cfgManager.getConfig(),
                        section, algorithm).sort();
            } catch (Exception e) {
                InvTweaks.logInGameErrorStatic("Failed to sort container", e);
            }
            return true;
        } else {
            return false;
        }

    }
    
}