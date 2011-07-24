package net.invtweaks.gui;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiSmallButton;


public class InvTweaksGuiOptions extends GuiScreen {

	private final static String SCREEN_TITLE = "Inventory and chests settings";
	
	private GuiScreen parentScreen;
	
	public InvTweaksGuiOptions(GuiScreen guiscreen) {
		parentScreen = guiscreen;
	}

	// Lead to retrieve the world's path?
    // ISaveFormat isaveformat = mc.getSaveLoader();
    
	@SuppressWarnings("unchecked")
	public void initGui() {
		
		// TODO: Mod translation?
		// FIXME: Lost items on "..." with item in hand
		
		int x = width / 2 - 155;
		int y = height / 6;

		controlList.add(new GuiSmallButton(1, x, y, "Middle click: ON"));
		controlList.add(new GuiSmallButton(2, x + 160, y, "Chest buttons: ON"));
		controlList.add(new GuiSmallButton(3, x, y + 24, "Convenient shortcuts: OFF"));
		controlList.add(new GuiSmallButton(4, x + 160, y + 24, "Autoreplace: ALL"));

		controlList.add(new GuiButton(100, x + 55, height / 6 + 96, "Edit sorting rules..."));
		controlList.add(new GuiButton(102, x + 55, height / 6 + 120, "Open the item tree file..."));
		controlList.add(new GuiButton(102, x + 55, height / 6 + 144, "Open help in browser..."));
		controlList.add(new GuiButton(200, x + 55, height / 6 + 168, "Done"));
		
		// Disable unimplemented buttons
		for (Object o : controlList) {
			GuiButton button = (GuiButton) o;
			if (button.id != 200) {
				button.enabled = false;
			}
		}
		
	}

	protected void actionPerformed(GuiButton guibutton) {

		switch (guibutton.id) {
			case 1: // Autoreplace
				guibutton.displayString = (guibutton.displayString.equals("Autoreplace: ALL"))
						? "Autoreplace: NOTHING" : "Autoreplace: ALL";
				break;
			case 200: // Done
				mc.displayGuiScreen(parentScreen);
		}
		
	}

	public void drawScreen(int i, int j, float f) {
		drawDefaultBackground();
		drawCenteredString(fontRenderer, SCREEN_TITLE, width / 2, 20, 0xffffff);
		super.drawScreen(i, j, f);
	}
	
}
