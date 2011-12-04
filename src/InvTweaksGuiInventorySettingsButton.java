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

    public void a(Minecraft minecraft, int i, int j) {
        super.a(minecraft, i, j);

        if (!isEnabled2()) {
            return;
        }

        // Display string
        InvTweaksObfuscation obf = new InvTweaksObfuscation(minecraft);
        a(obf.getFontRenderer(),
                getDisplayString(),
                getXPosition() + 5,
                getYPosition() - 1,
                getTextColor(i, j)); // Gui.drawCenteredString
    }

    /**
     * Displays inventory settings GUI
     */
    public boolean c(Minecraft minecraft, int i, int j) { /* mousePressed */
        
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