import invtweaks.InvTweaksConst;

import java.awt.Desktop;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

import org.lwjgl.util.Point;


/**
 * The inventory and chest advanced settings menu.
 * 
 * @author Jimeo Wan
 * 
 */
public class InvTweaksGuiSettingsAdvanced extends InvTweaksGuiSettingsAbstract {
	
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger("InvTweaks");

    private final static int ID_SORT_ON_PICKUP = 1;
    private final static int ID_AUTO_EQUIP_ARMOR = 2;
    private final static int ID_ENABLE_SOUNDS = 3;
    private final static int ID_CHESTS_BUTTONS = 4;
    private final static int ID_SLOW_SORTING = 5;
    private final static int ID_EDITSHORTCUTS = 100;
    
    private static String labelChestButtons;
    private static String labelSortOnPickup;
    private static String labelEquipArmor;
    private static String labelEnableSounds;
    private static String labelSlowSorting;

    public InvTweaksGuiSettingsAdvanced(Minecraft mc, aue parentScreen, InvTweaksConfig config) {
        super(mc, parentScreen, config);
        
        labelSortOnPickup = InvTweaksLocalization.get("invtweaks.settings.advanced.sortonpickup");
		labelEquipArmor = InvTweaksLocalization.get("invtweaks.settings.advanced.autoequip");
		labelEnableSounds = InvTweaksLocalization.get("invtweaks.settings.advanced.sounds");
	    labelChestButtons = InvTweaksLocalization.get("invtweaks.settings.chestbuttons");
        labelSlowSorting = InvTweaksLocalization.get("invtweaks.settings.slowsorting");
    }

    public void A_() { /* initGui */
    	super.A_();

        List<Object> controlList = obf.getControlList(this);
        Point p = new Point();
        int i = 0;

        // Create large buttons

        moveToButtonCoords(1, p);
        controlList.add(new ast(ID_EDITSHORTCUTS, p.getX() + 55, obf.getWindowHeight(this) / 6 + 144, InvTweaksLocalization.get("invtweaks.settings.advanced.mappingsfile")));

        // Create settings buttons
        
        i += 2;
        moveToButtonCoords(i++, p);
        InvTweaksGuiTooltipButton sortOnPickupBtn = new InvTweaksGuiTooltipButton(ID_SORT_ON_PICKUP, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_ENABLE_SORTING_ON_PICKUP, labelSortOnPickup), InvTweaksLocalization.get("invtweaks.settings.advanced.sortonpickup.tooltip"));
        controlList.add(sortOnPickupBtn);

        moveToButtonCoords(i++, p);
        InvTweaksGuiTooltipButton enableSoundsBtn = new InvTweaksGuiTooltipButton(ID_ENABLE_SOUNDS, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_ENABLE_SOUNDS, labelEnableSounds),
                InvTweaksLocalization.get("invtweaks.settings.advanced.sounds.tooltip"));
        controlList.add(enableSoundsBtn);
        
        moveToButtonCoords(i++, p);
        controlList.add(new InvTweaksGuiTooltipButton(ID_CHESTS_BUTTONS, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_SHOW_CHEST_BUTTONS, labelChestButtons), InvTweaksLocalization.get("invtweaks.settings.chestbuttons.tooltip")));

        moveToButtonCoords(i++, p);
        InvTweaksGuiTooltipButton autoEquipArmorBtn = new InvTweaksGuiTooltipButton(ID_AUTO_EQUIP_ARMOR, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_ENABLE_AUTO_EQUIP_ARMOR, labelEquipArmor), InvTweaksLocalization.get("invtweaks.settings.advanced.autoequip.tooltip"));
        controlList.add(autoEquipArmorBtn);

        i += 3;
        moveToButtonCoords(i++, p);
        InvTweaksGuiTooltipButton slowSortingBtn = new InvTweaksGuiTooltipButton(ID_SLOW_SORTING, p.getX(), p.getY() + 10,
        		computeBooleanButtonLabel(InvTweaksConfig.PROP_SLOW_SORTING, labelSlowSorting), null);
        controlList.add(slowSortingBtn);
        
        // Check if links to files are supported, if not disable the buttons
        if (!Desktop.isDesktopSupported()) {
            for (Object o : controlList) {
            	if (obf.isGuiButton(o)) {
            	    ast button = obf.asGuiButton(o);
	                if (obf.getId(button) == ID_EDITSHORTCUTS) {
	                    obf.setEnabled(button, false);
	                }
            	}
            }
        }

        // Save control list
        obf.setControlList(this, controlList);

    }

	public void a(int i, int j, float f) { /* drawScreen */
        super.a(i, j, f);
        
        Point p = new Point();
        moveToButtonCoords(1, p);
        b(obf.getFontRenderer(), InvTweaksLocalization.get("invtweaks.settings.pvpwarning.pt1"), p.getX(), 40, 0x999999); // Gui.drawCenteredString
        b(obf.getFontRenderer(), InvTweaksLocalization.get("invtweaks.settings.pvpwarning.pt2"), p.getX(), 50, 0x999999);
        
        b(obf.getFontRenderer(), InvTweaksLocalization.get("invtweaks.settings.slowsorting.pt1"), p.getX(), 115, 0x999999); 
        b(obf.getFontRenderer(), InvTweaksLocalization.get("invtweaks.settings.slowsorting.pt2"), p.getX(), 125, 0x999999);
        b(obf.getFontRenderer(), InvTweaksLocalization.get("invtweaks.settings.slowsorting.pt3"), p.getX(), 135, 0x999999);
    }
    
    protected void a(ast guibutton) { /* actionPerformed */
    	
        switch (obf.getId(guibutton)) {

        // Toggle auto-refill sound
        case ID_SORT_ON_PICKUP:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_ENABLE_SORTING_ON_PICKUP, labelSortOnPickup);
            break;

        // Toggle shortcuts
        case ID_AUTO_EQUIP_ARMOR:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_ENABLE_AUTO_EQUIP_ARMOR, labelEquipArmor);
            break;
            
        // Toggle sounds
        case ID_ENABLE_SOUNDS:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_ENABLE_SOUNDS, labelEnableSounds);
            break;

        // Toggle chest buttons
        case ID_CHESTS_BUTTONS:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_SHOW_CHEST_BUTTONS, labelChestButtons);
            break;
            
        // Toggle sounds
        case ID_SLOW_SORTING:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_SLOW_SORTING, labelSlowSorting);
            break;
                
        // Open shortcuts mappings in external editor
        case ID_EDITSHORTCUTS:
            try {
                Desktop.getDesktop().open(new File(InvTweaksConst.CONFIG_PROPS_FILE));
            } catch (Exception e) {
                InvTweaks.logInGameErrorStatic("invtweaks.settings.advanced.mappingsfile.error", e);
            }
            break;
            
        // Back to main settings screen
        case ID_DONE:
                obf.displayGuiScreen(new InvTweaksGuiSettings(mc, parentScreen, config));
                
        }

    }

}
