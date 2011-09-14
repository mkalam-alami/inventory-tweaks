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

        if (!isEnabled2()) {
            return;
        }

        // Display string
        InvTweaksObfuscation obf = new InvTweaksObfuscation(minecraft);
        drawCenteredString(obf.getFontRenderer(),
                obf.getDisplayString(this),
                obf.getXPosition(this) + 5,
                obf.getYPosition(this) - 1,
                getTextColor(i, j));
    }

    /**
     * Sort container
     */
    public boolean mousePressed(Minecraft minecraft, int i, int j) {
        
        InvTweaksObfuscation obf = new InvTweaksObfuscation(minecraft);
        InvTweaksConfig config = cfgManager.getConfig();
        
        if (super.c(minecraft, i, j)) { // mousePressed
            // Put hold item down if necessary
            InvTweaksContainerSectionManager containerMgr;
            
            try {
                containerMgr = new InvTweaksContainerSectionManager(
                        minecraft, InvTweaksContainerSection.INVENTORY);
                if (obf.getHoldStack() != null) {
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
            obf.displayGuiScreen(
                    new InvTweaksGuiInventorySettings(minecraft,
                    obf.getCurrentScreen(), config));
            return true;
        } else {
            return false;
        }
    }
    
}