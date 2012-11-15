import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;


public class InvTweaksGuiShortcutsHelp extends aue /* GuiScreen */ {
    
    private final static int ID_DONE = 0;

    private InvTweaksObfuscation obf;
    private aue parentScreen;
    private InvTweaksConfig config;
    
    public InvTweaksGuiShortcutsHelp(Minecraft mc, 
    		aue parentScreen, InvTweaksConfig config) {
        this.obf = new InvTweaksObfuscation(mc);
        this.parentScreen = parentScreen;
        this.config = config;
    }

    public void A_() { /* initGui */
        // Create Done button
        List<Object> controlList = new LinkedList<Object>(); /* GuiButton */
        controlList.add(new ast(ID_DONE, 
                obf.getWindowWidth(this) / 2 - 100, 
                obf.getWindowHeight(this) / 6 + 168, "Done"));
        obf.setControlList(this, controlList);
    }

    public void a(int i, int j, float f) { /* drawScreen */
        
        // Note: 0x0000EEFF = blue color (currently unused)
        
        z_(); // Gui.drawDefaultBackground
        a(obf.getFontRenderer(), "WARNING: Since 1.3.1, shortcuts won't work as expected. Looking for a workaround...", obf.getWindowWidth(this) / 2, 5, 0xff0000);
        a(obf.getFontRenderer(), InvTweaksLocalization.get("invtweaks.help.shortcuts.title"), obf.getWindowWidth(this) / 2, 20, 0xffffff); // Gui.drawCenteredString
        String clickLabel =  InvTweaksLocalization.get("invtweaks.help.shortcuts.click");
        
        int y = obf.getWindowHeight(this) / 6 - 2;

        drawShortcutLine(InvTweaksLocalization.get("invtweaks.help.shortcuts.onestack"), "LSHIFT " + InvTweaksLocalization.get("invtweaks.help.shortcuts.or") + " RSHIFT + " + clickLabel, 0x00FFFF00, y);
        y += 12;
        drawShortcutLine("", buildUpOrDownLabel(InvTweaksConfig.PROP_SHORTCUT_UP, obf.getKeyBindingForwardKeyCode(), InvTweaksLocalization.get("invtweaks.help.shortcuts.forward"))
        		+ " + " + clickLabel, 0x00FFFF00, y);
        y += 12;
        drawShortcutLine("", buildUpOrDownLabel(InvTweaksConfig.PROP_SHORTCUT_DOWN, obf.getKeyBindingBackKeyCode(), InvTweaksLocalization.get("invtweaks.help.shortcuts.backwards")) 
        		+ " + " + clickLabel, 0x00FFFF00, y);
        y += 12;
        drawShortcutLine(InvTweaksLocalization.get("invtweaks.help.shortcuts.oneitem"),
                config.getProperty(InvTweaksConfig.PROP_SHORTCUT_ONE_ITEM) + " + " + clickLabel,
                0x00FFFF00, y);
        y += 12;
        drawShortcutLine(InvTweaksLocalization.get("invtweaks.help.shortcuts.allitems"),
                config.getProperty(InvTweaksConfig.PROP_SHORTCUT_ALL_ITEMS) + " + " + clickLabel,
                0x00FFFF00, y);
        y += 12;
        drawShortcutLine(InvTweaksLocalization.get("invtweaks.help.shortcuts.everything"),
                config.getProperty(InvTweaksConfig.PROP_SHORTCUT_EVERYTHING) + " + " + clickLabel,
                0x00FFFF00, y);
        y += 19;
        
        drawShortcutLine(InvTweaksLocalization.get("invtweaks.help.shortcuts.hotbar"), "0-9 + " + clickLabel, 0x0000FF33, y);
        y += 12;
        drawShortcutLine(InvTweaksLocalization.get("invtweaks.help.shortcuts.emptyslot"), InvTweaksLocalization.get("invtweaks.help.shortcuts.rightclick"), 0x0000FF33, y);
        y += 12;
        drawShortcutLine(InvTweaksLocalization.get("invtweaks.help.shortcuts.drop"),
                config.getProperty(InvTweaksConfig.PROP_SHORTCUT_DROP) + " + " + clickLabel,
                0x0000FF33, y);
        y += 19;
        
        drawShortcutLine(InvTweaksLocalization.get("invtweaks.help.shortcuts.craftall"), "LSHIFT, RSHIFT + " + clickLabel, 0x00FF8800, y);
        y += 12;
        drawShortcutLine(InvTweaksLocalization.get("invtweaks.help.shortcuts.craftone"), config.getProperty(InvTweaksConfig.PROP_SHORTCUT_ONE_ITEM) + " + " + clickLabel, 0x00FF8800, y);
        y += 19;
        
        String sortKeyName = getKeyName(config.getSortKeyCode(), "(Sort Key)");
        drawShortcutLine(InvTweaksLocalization.get("invtweaks.help.shortcuts.selectconfig"), "0-9 + " +sortKeyName, 0x0088FFFF, y);
        
        super.a(i, j, f); // drawScreen
    }

    protected void a(ast guibutton) { /* actionPerformed */

        switch (obf.getId(guibutton)) {

        case ID_DONE:
            obf.displayGuiScreen(parentScreen);
            break;
        
        }
    }

    private String buildUpOrDownLabel(String shortcutProp, int keyCode, String defaultKeyName) {
    	String shortcutLabel = config.getProperty(shortcutProp);
    	String keyLabel = getKeyName(keyCode, defaultKeyName);
    	if (keyLabel.equals(shortcutLabel)) {
    		return keyLabel;
    	}
    	else {
    		return keyLabel + "/" + shortcutLabel;
    	}
    }
    
    protected String getKeyName(int keyCode, String defaultValue) {
        try {
        	return Keyboard.getKeyName(keyCode);
        }
        catch (Exception e) {
        	return defaultValue;
        }
    }
    
    private void drawShortcutLine(String label, String value, int color, int y) {
        b(obf.getFontRenderer(), label, 30, y, -1); // drawString
        if (value != null) {
	        b(obf.getFontRenderer(), value.contains("DEFAULT") ? "-" : value.replaceAll(", ", " " + InvTweaksLocalization.get("invtweaks.help.shortcuts.or") + " "), 
	                obf.getWindowWidth(this) / 2 - 30, y, color); // drawString
        }
    }
    
}
