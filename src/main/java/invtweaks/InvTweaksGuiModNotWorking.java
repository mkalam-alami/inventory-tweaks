package invtweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;
import org.apache.logging.log4j.Logger;

/**
 * A help menu for the NoCheatPlus conflict.
 *
 * @author Jimeo Wan
 */
public class InvTweaksGuiModNotWorking extends InvTweaksGuiSettingsAbstract {

    private static final Logger log = InvTweaks.log;

    public InvTweaksGuiModNotWorking(Minecraft mc, GuiScreen parentScreen, InvTweaksConfig config) {
        super(mc, parentScreen, config);
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        super.drawScreen(i, j, f);

        int x = width / 2;
        drawCenteredString(obf.getFontRenderer(), StatCollector.translateToLocal("invtweaks.help.bugsorting.pt1"), x,
                           80, 0xBBBBBB);
        drawCenteredString(obf.getFontRenderer(), StatCollector.translateToLocal("invtweaks.help.bugsorting.pt2"), x,
                           95, 0xBBBBBB);
        drawCenteredString(obf.getFontRenderer(), StatCollector.translateToLocal("invtweaks.help.bugsorting.pt3"), x,
                           110, 0xBBBBBB);
        drawCenteredString(obf.getFontRenderer(), StatCollector.translateToLocal("invtweaks.help.bugsorting.pt4"), x,
                           150, 0xFFFF99);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        // GuiButton
        switch(guibutton.id) {
            // Back to main settings screen
            case ID_DONE:
                obf.displayGuiScreen(new InvTweaksGuiSettings(mc, parentScreen, config));
        }
    }

}
