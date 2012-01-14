import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;


public class InvTweaksGuiShortcutsHelp extends ug /* GuiScreen */ {

    private final static String SCREEN_TITLE = "Shortcuts help";
    
    private final static int ID_DONE = 0;

    private InvTweaksObfuscation obf;
    private ug parentScreen;
    private InvTweaksConfig config;
    
    public InvTweaksGuiShortcutsHelp(Minecraft mc, 
            ug parentScreen, InvTweaksConfig config) {
        this.obf = new InvTweaksObfuscation(mc);
        this.parentScreen = parentScreen;
        this.config = config;
    }

    public void a() { /* initGui */
        // Create Done button
        List<zr> controlList = new LinkedList<zr>(); /* GuiButton */
        controlList.add(new zr(ID_DONE, 
                obf.getWidth(this) / 2 - 100, 
                obf.getHeight(this) / 6 + 168, "Done"));
        obf.setControlList(this, controlList);
    }

    public void a(int i, int j, float f) { /* drawScreen */
        
        // Note: 0x0000EEFF = blue color (currently unused)
        
        k(); // Gui.drawDefaultBackground
        a(obf.getFontRenderer(), SCREEN_TITLE, obf.getWidth(this) / 2, 20, 0xffffff); // Gui.drawCenteredString
        
        int y = obf.getHeight(this) / 6;

        drawShortcutLine("Move one stack",
                config.getProperty(InvTweaksConfig.PROP_SHORTCUT_ONE_STACK) + " + Click",
                0x00FFFF00, y);
        y += 12;
        drawShortcutLine("Move one item only",
                config.getProperty(InvTweaksConfig.PROP_SHORTCUT_ONE_ITEM) + " + Click",
                0x00FFFF00, y);
        y += 12;
        drawShortcutLine("Move all items of same type",
                config.getProperty(InvTweaksConfig.PROP_SHORTCUT_ALL_ITEMS) + " + Click",
                0x00FFFF00, y);
        y += 25;

        
        drawShortcutLine("Send to upper section*",
                config.getProperty(InvTweaksConfig.PROP_SHORTCUT_UP) + " + Click",
                0x0000FF33, y);
        y += 12;
        drawShortcutLine("Send to lower section",
                config.getProperty(InvTweaksConfig.PROP_SHORTCUT_DOWN) + " + Click",
                0x0000FF33, y);
        y += 12;
        drawShortcutLine("Send to hotbar", "0-9 + Click", 0x0000FF33, y);
        y += 12;
        drawShortcutLine("Send to empty slot", "(Any shortcut) + Right instead of left click", 0x0000FF33, y);
        y += 25;
        
        drawShortcutLine("Drop",
                config.getProperty(InvTweaksConfig.PROP_SHORTCUT_DROP) + " + Click",
                0x00FF8800, y);
        y += 12;
        drawShortcutLine("Craft all", "LSHIFT, RSHIFT + Click", 0x00FF8800, y);
        y += 12;
        
        String sortKeyName;
        try {
        	sortKeyName = Keyboard.getKeyName(obf.getKeyCode(InvTweaksConst.SORT_KEY_BINDING));
        }
        catch (Exception e) {
        	sortKeyName = "(Sort key)";
        }
        drawShortcutLine("Select sorting configuration", "0-9 + " +sortKeyName, 0x00FF8800, y);
        y += 20;
        
        b(obf.getFontRenderer(), "*Useful on brewing stands, crafting tables, etc.", 30, y, 0x00666666); // drawString
        
        super.a(i, j, f); // drawScreen
    }
    
    private void drawShortcutLine(String label, String value, int color, int y) {
        b(obf.getFontRenderer(), label, 30, y, -1); // drawString
        if (value != null) {
	        b(obf.getFontRenderer(), value.contains("DEFAULT") ? "-" : value.replaceAll(", ", " or "), 
	                obf.getWidth(this) / 2 - 30, y, color); // drawString
        }
    }

    protected void a(zr guibutton) { /* actionPerformed */

        switch (obf.getId(guibutton)) {

        case ID_DONE:
            obf.displayGuiScreen(parentScreen);
            break;
        
        }
    }
    
}
