package net.invtweaks.gui;

import java.util.concurrent.TimeoutException;

import net.invtweaks.config.InvTweaksConfig;
import net.invtweaks.config.InvTweaksConfigManager;
import net.invtweaks.library.ContainerSectionManager;
import net.invtweaks.library.Obfuscation;
import net.invtweaks.library.ContainerManager.ContainerSection;
import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiButton;
import net.minecraft.src.InvTweaks;

import org.lwjgl.opengl.GL11;

public class GuiSettingsButton extends GuiButton {
    
    private InvTweaksConfigManager cfgManager;
    
    public GuiSettingsButton(InvTweaksConfigManager cfgManager,
            int id, int x, int y, int w, int h, String displayString) {
        super(id, x, y, w, h, displayString);
        this.cfgManager = cfgManager;
    }

    public void drawButton(Minecraft minecraft, int i, int j) {

        if (!enabled2) {
            return;
        }

        // TODO Refactoring
        // Draw little button
        // (use the 4 corners of the texture to fit best its small size)
        GL11.glBindTexture(3553, minecraft.renderEngine.getTexture("/gui/gui.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        boolean flag = i >= xPosition && j >= yPosition && i < xPosition + width && j < yPosition + height;
        int k = getHoverState(flag);
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

        // Display string
        drawCenteredString(minecraft.fontRenderer, displayString,
                xPosition + 5, yPosition - 1, textColor);
    }

    /**
     * Sort container
     */
    public boolean mousePressed(Minecraft minecraft, int i, int j) {
        
        InvTweaksConfig config = cfgManager.getConfig();
        
        if (super.mousePressed(minecraft, i, j)) {
            // Put hold item down if necessary
            ContainerSectionManager containerMgr;
            
            try {
                containerMgr = new ContainerSectionManager(
                        minecraft, ContainerSection.INVENTORY);
                if (Obfuscation.getHoldStackStatic(minecraft) != null) {
                    try {
                        // Put hold item down
                        for (int k = containerMgr.getSize() - 1; k >= 0; k--) {
                            if (containerMgr.getItemStack(k) == null) {
                                containerMgr.leftClick(k);
                                break;
                            }
                        }
                    } catch (TimeoutException e) {
                        InvTweaks.logInGameErrorStatic("Failed to put item down", e);
                    }
                }
            } catch (Exception e) {
                InvTweaks.logInGameErrorStatic("Failed to set up settings button", e);
            }
            
            // Refresh config
            cfgManager.makeSureConfigurationIsLoaded();

            // Display menu
            minecraft.displayGuiScreen(new GuiInventorySettings(Obfuscation.getCurrentScreenStatic(minecraft), config));
            return true;
        } else {
            return false;
        }

    }
    
}