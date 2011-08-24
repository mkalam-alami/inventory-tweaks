package net.invtweaks.gui;

import java.util.LinkedList;
import java.util.List;

import net.invtweaks.Const;
import net.invtweaks.config.InvTweaksConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;

import org.lwjgl.input.Keyboard;

public class GuiShortcutsHelp extends GuiScreen {

    private final static String SCREEN_TITLE = "Shortcuts help";
    
    private final static int ID_DONE = 0;

    private Minecraft mc;
    private GuiScreen parentScreen;
    private InvTweaksConfig config;
    
    public GuiShortcutsHelp(Minecraft mc, 
            GuiScreen parentScreen, InvTweaksConfig config) {
        this.mc = mc;
        this.parentScreen = parentScreen;
        this.config = config;
    }

    public void initGui() {
        // Create Done button
        List<GuiButton> controlList = new LinkedList<GuiButton>();
        controlList.add(new GuiButton(ID_DONE, 
                width / 2 - 100, 
                height / 6 + 168, "Done"));
        this.controlList = controlList;
    }

    public void drawScreen(int i, int j, float f) {
        
        drawDefaultBackground();
        drawCenteredString(fontRenderer, SCREEN_TITLE, width / 2, 20, 0xffffff);
        
        int y = height / 6;

        drawShortcutLine("Move", "Left click", 0x0000EEFF, y);
        y += 12;
        drawShortcutLine("Move to empty slot", "Right click", 0x0000EEFF, y);
        y += 20;
        
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
        y += 20;

        drawShortcutLine("Move to upper section",
                config.getProperty(InvTweaksConfig.PROP_SHORTCUT_UP) + " + Click",
                0x0000FF33, y);
        y += 12;
        drawShortcutLine("Move to lower section",
                config.getProperty(InvTweaksConfig.PROP_SHORTCUT_DOWN) + " + Click",
                0x0000FF33, y);
        y += 12;
        drawShortcutLine("Move to hotbar", "0-9 + Click", 0x0000FF33, y);
        y += 20;

        drawShortcutLine("Drop",
                config.getProperty(InvTweaksConfig.PROP_SHORTCUT_DROP) + " + Click",
                0x00FF8800, y);
        y += 12;
        drawShortcutLine("Craft all", "LSHIFT, RSHIFT + Click", 0x00FF8800, y);
        y += 12;
        drawShortcutLine("Select sorting configuration", "0-9 + " +
                Keyboard.getKeyName(Const.SORT_KEY_BINDING.keyCode), 0x00FF8800, y);
        y += 12;
        
        super.drawScreen(i, j, f);
    }
    
    private void drawShortcutLine(String label, String value, int color, int y) {
        drawString(fontRenderer, label, 50, y, -1);
        drawString(fontRenderer, value.contains("DEFAULT") ? "-" : value, width / 2 + 40, y, color);
    }

    protected void actionPerformed(GuiButton guibutton) {

        switch (guibutton.id) {

        case ID_DONE:
            mc.displayGuiScreen(parentScreen);
            break;
        
        }
    }
    
}
