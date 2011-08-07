package net.invtweaks.gui;

import net.invtweaks.config.InvTweaksConfigManager;
import net.invtweaks.library.ContainerManager.ContainerSection;
import net.invtweaks.logic.SortingHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiButton;
import net.minecraft.src.InvTweaks;

import org.lwjgl.opengl.GL11;

public class GuiSortingButton extends GuiButton {

    private InvTweaksConfigManager cfgManager;
    private boolean buttonClicked = false;
    private int algorithm;

    public GuiSortingButton(InvTweaksConfigManager cfgManager, 
            int id, int x, int y, int w, int h,
            String displayString, int algorithm) {
        super(id, x, y, w, h, displayString);
        this.algorithm = algorithm;
        this.cfgManager = cfgManager;
    }

    public void drawButton(Minecraft minecraft, int i, int j) {

        if (!enabled2) {
            return;
        }

        // Draw little button
        // (use the 4 corners of the texture to fit best its small size)
        GL11.glBindTexture(3553, minecraft.renderEngine.getTexture("/gui/gui.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        boolean flag = i >= xPosition && j >= yPosition && i < xPosition + width && j < yPosition + height;
        int k = getHoverState(flag) - ((buttonClicked) ? 1 : 0);
        drawTexturedModalRect(xPosition, yPosition, 1, 46 + k * 20 + 1, width / 2, height / 2);
        drawTexturedModalRect(xPosition, yPosition + height / 2, 1, 46 + k * 20 + 20 - height / 2 - 1, width / 2, height / 2);
        drawTexturedModalRect(xPosition + width / 2, yPosition, 200 - width / 2 - 1, 46 + k * 20 + 1, width / 2, height / 2);
        drawTexturedModalRect(xPosition + width / 2, yPosition + height / 2, 200 - width / 2 - 1, 46 + k * 20 + 19 - height / 2, width / 2,
                height / 2);

        // Button status specific behaviour
        int textColor = 0xffe0e0e0;
        if (!enabled) {
            textColor = 0xffa0a0a0;
        } else if (flag) {
            textColor = 0xffffffa0;
        }

        // Display symbol
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
                        ContainerSection.CHEST, algorithm).sort();
            } catch (Exception e) {
                InvTweaks.logInGameErrorStatic("Failed to sort container", e);
            }
            return true;
        } else {
            return false;
        }

    }
    
}