package invtweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;

import java.util.LinkedList;
import java.util.List;


public class InvTweaksGuiShortcutsHelp extends GuiScreen {

    private final static int ID_DONE = 0;

    private InvTweaksObfuscation obf;
    private GuiScreen parentScreen;
    private InvTweaksConfig config;

    public InvTweaksGuiShortcutsHelp(Minecraft mc, GuiScreen parentScreen, InvTweaksConfig config) {
        this.obf = new InvTweaksObfuscation(mc);
        this.parentScreen = parentScreen;
        this.config = config;
    }

    public void initGui() {
        // Create Done button
        List<Object> controlList = new LinkedList<Object>();
        controlList.add(new GuiButton(ID_DONE, width / 2 - 100, height / 6 + 168, "Done"));
        buttonList = controlList;
    }

    @Override
    public void drawScreen(int i, int j, float f) {

        // Note: 0x0000EEFF = blue color (currently unused)

        drawDefaultBackground();
        drawCenteredString(obf.getFontRenderer(),
                "WARNING: Since 1.3.1, shortcuts won't work as expected. Looking for a workaround...",
                width / 2, 5, 0xff0000);
        drawCenteredString(obf.getFontRenderer(), StatCollector.translateToLocal("invtweaks.help.shortcuts.title"),
                width / 2, 20, 0xffffff); // Gui.drawCenteredString
        String clickLabel = StatCollector.translateToLocal("invtweaks.help.shortcuts.click");

        int y = height / 6 - 2;

        drawShortcutLine(StatCollector.translateToLocal("invtweaks.help.shortcuts.onestack"),
                "LSHIFT " + StatCollector.translateToLocal("invtweaks.help.shortcuts.or") + " RSHIFT + " +
                        clickLabel, 0x00FFFF00, y);
        y += 12;
        drawShortcutLine("", buildUpOrDownLabel(InvTweaksConfig.PROP_SHORTCUT_UP, obf.getKeyBindingForwardKeyCode(),
                        StatCollector.translateToLocal(
                                "invtweaks.help.shortcuts.forward")) + " + " + clickLabel,
                0x00FFFF00, y);
        y += 12;
        drawShortcutLine("", buildUpOrDownLabel(InvTweaksConfig.PROP_SHORTCUT_DOWN, obf.getKeyBindingBackKeyCode(),
                        StatCollector.translateToLocal(
                                "invtweaks.help.shortcuts.backwards")) + " + " + clickLabel,
                0x00FFFF00, y);
        y += 12;
        drawShortcutLine(StatCollector.translateToLocal("invtweaks.help.shortcuts.oneitem"),
                config.getProperty(InvTweaksConfig.PROP_SHORTCUT_ONE_ITEM) + " + " + clickLabel, 0x00FFFF00,
                y);
        y += 12;
        drawShortcutLine(StatCollector.translateToLocal("invtweaks.help.shortcuts.allitems"),
                config.getProperty(InvTweaksConfig.PROP_SHORTCUT_ALL_ITEMS) + " + " + clickLabel, 0x00FFFF00,
                y);
        y += 12;
        drawShortcutLine(StatCollector.translateToLocal("invtweaks.help.shortcuts.everything"),
                config.getProperty(InvTweaksConfig.PROP_SHORTCUT_EVERYTHING) + " + " + clickLabel, 0x00FFFF00,
                y);
        y += 19;

        drawShortcutLine(StatCollector.translateToLocal("invtweaks.help.shortcuts.hotbar"), "0-9 + " + clickLabel,
                0x0000FF33, y);
        y += 12;
        drawShortcutLine(StatCollector.translateToLocal("invtweaks.help.shortcuts.emptyslot"),
                StatCollector.translateToLocal("invtweaks.help.shortcuts.rightclick"), 0x0000FF33, y);
        y += 12;
        drawShortcutLine(StatCollector.translateToLocal("invtweaks.help.shortcuts.drop"),
                config.getProperty(InvTweaksConfig.PROP_SHORTCUT_DROP) + " + " + clickLabel, 0x0000FF33, y);
        y += 19;

        drawShortcutLine(StatCollector.translateToLocal("invtweaks.help.shortcuts.craftall"),
                "LSHIFT, RSHIFT + " + clickLabel, 0x00FF8800, y);
        y += 12;
        drawShortcutLine(StatCollector.translateToLocal("invtweaks.help.shortcuts.craftone"),
                config.getProperty(InvTweaksConfig.PROP_SHORTCUT_ONE_ITEM) + " + " + clickLabel, 0x00FF8800,
                y);
        y += 19;

        String sortKeyName = getKeyName(config.getSortKeyCode(), "(Sort Key)");
        drawShortcutLine(StatCollector.translateToLocal("invtweaks.help.shortcuts.selectconfig"),
                "0-9 + " + sortKeyName, 0x0088FFFF, y);

        super.drawScreen(i, j, f);
    }

    protected void actionPerformed(GuiButton guibutton) {
        // GuiButton
        switch(guibutton.id) {
            case ID_DONE:
                obf.displayGuiScreen(parentScreen);
                break;
        }
    }

    protected void keyTyped(char c, int keyCode) {
        if(keyCode == Keyboard.KEY_ESCAPE) {
            obf.displayGuiScreen(parentScreen);
        }
    }

    private String buildUpOrDownLabel(String shortcutProp, int keyCode, String defaultKeyName) {
        String shortcutLabel = config.getProperty(shortcutProp);
        String keyLabel = getKeyName(keyCode, defaultKeyName);
        if(keyLabel.equals(shortcutLabel)) {
            return keyLabel;
        } else {
            return keyLabel + "/" + shortcutLabel;
        }
    }

    protected String getKeyName(int keyCode, String defaultValue) {
        try {
            return Keyboard.getKeyName(keyCode);
        } catch(Exception e) {
            return defaultValue;
        }
    }

    private void drawShortcutLine(String label, String value, int color, int y) {
        drawString(obf.getFontRenderer(), label, 30, y, -1); // drawString
        if(value != null) {
            drawString(obf.getFontRenderer(), value.contains("DEFAULT") ? "-" : value
                            .replaceAll(", ", " " + StatCollector.translateToLocal("invtweaks.help.shortcuts.or") + " "),
                    width / 2 - 30, y, color); // drawString
        }
    }

}
