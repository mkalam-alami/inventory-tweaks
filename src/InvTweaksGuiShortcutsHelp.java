import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;

public class InvTweaksGuiShortcutsHelp extends qr {

    private final static String SCREEN_TITLE = "Shortcuts help";
    
    private final static int ID_DONE = 0;

    private InvTweaksObfuscation obf;
    private qr parentScreen;
    private InvTweaksConfig config;
    
    public InvTweaksGuiShortcutsHelp(Minecraft mc, 
            qr parentScreen, InvTweaksConfig config) {
        this.obf = new InvTweaksObfuscation(mc);
        this.parentScreen = parentScreen;
        this.config = config;
    }

    public void a() { /* initGui */
        // Create Done button
        List<vj> controlList = new LinkedList<vj>();
        controlList.add(new vj(ID_DONE, 
                obf.getWidth(this) / 2 - 100, 
                obf.getHeight(this) / 6 + 168, "Done"));
        obf.setControlList(this, controlList);
    }

    public void a(int i, int j, float f) { /* drawScreen */
        
        // Note: 0x0000EEFF = blue color (currently unused)
        
        k(); // Gui.drawDefaultBackground
        a(obf.getFontRenderer(), SCREEN_TITLE, obf.getWidth(this) / 2, 20, 0xffffff); // Gui.drawCenteredString
        
        int y = obf.getHeight(this) / 6;

        /*drawShortcutLine("Move one stack",
                config.getProperty(InvTweaksConfig.PROP_SHORTCUT_ONE_STACK) + " + Click",
                0x00FFFF00, y);
        y += 12;*/
        drawShortcutLine("Move one item only",
                config.getProperty(InvTweaksConfig.PROP_SHORTCUT_ONE_ITEM) + " + Click",
                0x00FFFF00, y);
        y += 12;
        drawShortcutLine("Move all items of same type",
                config.getProperty(InvTweaksConfig.PROP_SHORTCUT_ALL_ITEMS) + " + Click",
                0x00FFFF00, y);
        y += 25;

        
        drawShortcutLine("Send to upper section",
                config.getProperty(InvTweaksConfig.PROP_SHORTCUT_UP) + " + Click",
                0x0000FF33, y);
        y += 12;
        drawShortcutLine("Send to lower section",
                config.getProperty(InvTweaksConfig.PROP_SHORTCUT_DOWN) + " + Click",
                0x0000FF33, y);
        y += 12;
        drawShortcutLine("Send to hotbar", "0-9 + Click", 0x0000FF33, y);
        y += 12;
        drawShortcutLine("Send to empty slot", "(Other shortcut) + Right click", 0x0000FF33, y);
        y += 25;
        
        drawShortcutLine("Drop",
                config.getProperty(InvTweaksConfig.PROP_SHORTCUT_DROP) + " + Click",
                0x00FF8800, y);
        y += 12;
        drawShortcutLine("Craft all", "LSHIFT, RSHIFT + Click", 0x00FF8800, y);
        y += 12;
        drawShortcutLine("Select sorting configuration", "0-9 + " +
                Keyboard.getKeyName(obf.getKeyCode(InvTweaksConst.SORT_KEY_BINDING)), 0x00FF8800, y);
        y += 25;
        
        super.a(i, j, f); // drawScreen
    }
    
    private void drawShortcutLine(String label, String value, int color, int y) {
        b(obf.getFontRenderer(), label, 50, y, -1); // drawString
        b(obf.getFontRenderer(), value.contains("DEFAULT") ? "-" : value, 
                obf.getWidth(this) / 2 + 40, y, color); // drawString
    }

    protected void a(vj guibutton) { /* actionPerformed */

        switch (obf.getId(guibutton)) {

        case ID_DONE:
            obf.displayGuiScreen(parentScreen);
            break;
        
        }
    }
    
}
