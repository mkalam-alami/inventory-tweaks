package invtweaks;

import invtweaks.api.container.ContainerSection;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeoutException;

/**
 * Button that opens the inventory & chest settings screen.
 *
 * @author Jimeo Wan
 */
public class InvTweaksGuiSettingsButton extends InvTweaksGuiIconButton {

    private static final Logger log = InvTweaks.log;

    public InvTweaksGuiSettingsButton(InvTweaksConfigManager cfgManager, int id, int x, int y, int w, int h,
                                      String displayString, String tooltip, boolean useCustomTexture) {
        super(cfgManager, id, x, y, w, h, displayString, tooltip, useCustomTexture);
    }

    @Override
    public void func_146112_a(Minecraft minecraft, int i, int j) {
        super.func_146112_a(minecraft, i, j);

        // Display string
        InvTweaksObfuscation obf = new InvTweaksObfuscation(minecraft);
        drawCenteredString(obf.getFontRenderer(), field_146126_j, field_146128_h + 5, field_146129_i - 1,
                           getTextColor(i, j));
    }

    /**
     * Displays inventory settings GUI
     */
    @Override
    public boolean func_146116_c(Minecraft minecraft, int i, int j) {

        InvTweaksObfuscation obf = new InvTweaksObfuscation(minecraft);
        InvTweaksConfig config = cfgManager.getConfig();

        if(super.func_146116_c(minecraft, i, j)) {
            // Put hold item down if necessary
            InvTweaksContainerSectionManager containerMgr;

            try {
                containerMgr = new InvTweaksContainerSectionManager(minecraft, ContainerSection.INVENTORY);
                if(obf.getHeldStack() != null) {
                    try {
                        // Put hold item down
                        for(int k = containerMgr.getSize() - 1; k >= 0; k--) {
                            if(containerMgr.getItemStack(k) == null) {
                                containerMgr.leftClick(k);
                                break;
                            }
                        }
                    } catch(TimeoutException e) {
                        InvTweaks.logInGameErrorStatic("invtweaks.sort.releaseitem.error", e);
                    }
                }
            } catch(Exception e) {
                log.error("mousePressed", e);
            }

            // Refresh config
            cfgManager.makeSureConfigurationIsLoaded();

            // Display menu
            obf.displayGuiScreen(new InvTweaksGuiSettings(minecraft, obf.getCurrentScreen(), config));
            return true;
        } else {
            return false;
        }
    }

}
