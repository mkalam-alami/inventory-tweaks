import java.util.concurrent.TimeoutException;

import net.minecraft.client.Minecraft;

/**
 * Button that opens the inventory & chest settings screen.
 * @author Jimeo Wan
 *
 */
public class InvTweaksGuiInventorySettingsButton extends InvTweaksGuiIconButton {
    
    public InvTweaksGuiInventorySettingsButton(InvTweaksConfigManager cfgManager,
            int id, int x, int y, int w, int h,
            String displayString, String tooltip) {
        super(cfgManager, id, x, y, w, h, displayString, tooltip);
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
            InvTweaksContainerSectionManager containerMgr;
            
            try {
                containerMgr = new InvTweaksContainerSectionManager(
                        minecraft, InvTweaksContainerSection.INVENTORY);
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
            minecraft.displayGuiScreen(new InvTweaksGuiInventorySettings(minecraft,
                    Obfuscation.getCurrentScreenStatic(minecraft), config));
            return true;
        } else {
            return false;
        }
    }
    
}