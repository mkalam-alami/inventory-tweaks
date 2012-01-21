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
    private final static int ID_EDITSHORTCUTS = 100;
    
    private final static String LABEL_SORT_ON_PICKUP = "Sort on pickup";
    private final static String LABEL_EQUIP_ARMOR = "Auto-equip armor";
    private final static String LABEL_ENABLE_SOUNDS = "Sound";

    private final static String SP_ONLY = ": Only in SP";
    
    public InvTweaksGuiSettingsAdvanced(Minecraft mc, ug parentScreen, InvTweaksConfig config) {
        super(mc, parentScreen, config);
    }

    public void c() { /* initGui */
    	super.c();

        List<Object> controlList = obf.getControlList(this);
        Point p = new Point();
        int i = 0;

        // Create large buttons

        moveToButtonCoords(1, p);
        controlList.add(new zr(ID_EDITSHORTCUTS, p.getX() + 55, obf.getHeight(this) / 6 + 144, "Open the shortcuts mappings file..."));

        // Create settings buttons
        
        moveToButtonCoords(i++, p);
        InvTweaksGuiTooltipButton sortOnPickupBtn = new InvTweaksGuiTooltipButton(ID_SORT_ON_PICKUP, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_ENABLE_SORTING_ON_PICKUP, LABEL_SORT_ON_PICKUP), "Avoids to fill the hotbar with\neverything that you pickup");
        controlList.add(sortOnPickupBtn);
        if (obf.isMultiplayerWorld()) {
            // Sorting on pickup unavailable in SMP
            obf.setEnabled(sortOnPickupBtn, false);
            obf.setDisplayString(sortOnPickupBtn, LABEL_SORT_ON_PICKUP + SP_ONLY);
            sortOnPickupBtn.setTooltip(sortOnPickupBtn.getTooltip() + "\n(Single player only)");
        }
        
        moveToButtonCoords(i++, p);
        InvTweaksGuiTooltipButton autoEquipArmorBtn = new InvTweaksGuiTooltipButton(ID_AUTO_EQUIP_ARMOR, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_ENABLE_AUTO_EQUIP_ARMOR, LABEL_EQUIP_ARMOR), "Equips the best available\narmor when sorting");
        controlList.add(autoEquipArmorBtn);
        
        moveToButtonCoords(i++, p);
        moveToButtonCoords(i++, p);
        InvTweaksGuiTooltipButton enableSoundsBtn = new InvTweaksGuiTooltipButton(ID_ENABLE_SOUNDS, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_ENABLE_SOUNDS, LABEL_ENABLE_SOUNDS), "Play sounds when sorting\nor when auto-refill is triggered");
        controlList.add(enableSoundsBtn);
        
        // Check if links to files are supported, if not disable the buttons
        if (!Desktop.isDesktopSupported()) {
            for (Object o : controlList) {
            	zr button = (zr) o;
                if (obf.getId(button) == ID_EDITSHORTCUTS) {
                    obf.setEnabled(button, false);
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
        b(obf.getFontRenderer(), InvTweaksLocalization.get("invtweaks.settings.pvpwarning.pt1"), p.getX(), 100, 0x999999); // Gui.drawCenteredString
        b(obf.getFontRenderer(), InvTweaksLocalization.get("invtweaks.settings.pvpwarning.pt2"), p.getX(), 110, 0x999999);
    }
    
    protected void a(zr guibutton) { /* actionPerformed */
    	
        switch (obf.getId(guibutton)) {

        // Toggle auto-refill sound
        case ID_SORT_ON_PICKUP:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_ENABLE_SORTING_ON_PICKUP, LABEL_SORT_ON_PICKUP);
            break;

        // Toggle shortcuts
        case ID_AUTO_EQUIP_ARMOR:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_ENABLE_AUTO_EQUIP_ARMOR, LABEL_EQUIP_ARMOR);
            break;
            
        // Toggle sounds
        case ID_ENABLE_SOUNDS:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_ENABLE_SOUNDS, LABEL_ENABLE_SOUNDS);
            break;

        // Open shortcuts mappings in external editor
        case ID_EDITSHORTCUTS:
            try {
                Desktop.getDesktop().open(new File(InvTweaksConst.CONFIG_PROPS_FILE));
            } catch (Exception e) {
                InvTweaks.logInGameErrorStatic("invtweaks.settings.mappingsfile.error", e);
            }
            break;
            
        // Back to main settings screen
        case ID_DONE:
                obf.displayGuiScreen(new InvTweaksGuiSettings(mc, parentScreen, config));
                
        }

    }

}
