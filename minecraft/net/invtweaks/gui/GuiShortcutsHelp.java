package net.invtweaks.gui;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.util.Point;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;

public class GuiShortcutsHelp extends GuiScreen {

    private final static String SCREEN_TITLE = "Shortcuts help";
    
    private final static int ID_DONE = 0;

    private Minecraft mc;
    private GuiScreen parentScreen;
    
    public GuiShortcutsHelp(Minecraft mc, GuiScreen parentScreen) {
        this.mc = mc;
        this.parentScreen = parentScreen;
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
        super.drawScreen(i, j, f);
    }

    protected void actionPerformed(GuiButton guibutton) {

        switch (guibutton.id) {

        case ID_DONE:
            mc.displayGuiScreen(parentScreen);
            break;
        
        }
    }
    
}
