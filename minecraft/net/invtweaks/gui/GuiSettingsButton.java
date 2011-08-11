package net.invtweaks.gui;

import java.util.concurrent.TimeoutException;

import net.invtweaks.config.InvTweaksConfig;
import net.invtweaks.config.InvTweaksConfigManager;
import net.invtweaks.library.ContainerManager.ContainerSection;
import net.invtweaks.library.ContainerSectionManager;
import net.invtweaks.library.Obfuscation;
import net.minecraft.client.Minecraft;
import net.minecraft.src.InvTweaks;

/**
 * Button that opens the inventory & chest settings screen.
 * @author Jimeo Wan
 *
 */
public class GuiSettingsButton extends GuiIconButton {
    
    public GuiSettingsButton(InvTweaksConfigManager cfgManager,
            int id, int x, int y, int w, int h, String displayString) {
        super(cfgManager, id, x, y, w, h, displayString);
    }

    public void drawButton(Minecraft minecraft, int i, int j) {
        super.drawButton(minecraft, i, j);

        if (!enabled2) {
            return;
        }

        // Display string
        drawCenteredString(minecraft.fontRenderer, displayString,
                xPosition + 5, yPosition - 1, getTextColor(i, j));
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