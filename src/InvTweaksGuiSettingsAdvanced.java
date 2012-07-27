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
    private final static int ID_EDITSHORTCUTS = 100;
    
    private static String LABEL_SORT_ON_PICKUP;
    private static String LABEL_EQUIP_ARMOR;
    private static String LABEL_ENABLE_SOUNDS;

    private static String SP_ONLY;
    
    public InvTweaksGuiSettingsAdvanced(Minecraft mc, anm parentScreen, InvTweaksConfig config) {
        super(mc, parentScreen, config);
        
        LABEL_SORT_ON_PICKUP = InvTweaksLocalization.get("invtweaks.settings.advanced.sortonpickup");
		LABEL_EQUIP_ARMOR = InvTweaksLocalization.get("invtweaks.settings.advanced.autoequip");
		LABEL_ENABLE_SOUNDS = InvTweaksLocalization.get("invtweaks.settings.advanced.sounds");
		SP_ONLY = ": " + InvTweaksLocalization.get("invtweaks.settings.advanced.sortonpickup.soloonly");
    }

    public void w_() { /* initGui */
    	super.w_();

        List<Object> controlList = obf.getControlList(this);
        Point p = new Point();
        int i = 0;

        // Create large buttons

        moveToButtonCoords(1, p);
        controlList.add(new amg(ID_EDITSHORTCUTS, p.getX() + 55, obf.getHeight(this) / 6 + 144, InvTweaksLocalization.get("invtweaks.settings.advanced.mappingsfile")));

        // Create settings buttons
        
        moveToButtonCoords(i++, p);
        InvTweaksGuiTooltipButton sortOnPickupBtn = new InvTweaksGuiTooltipButton(ID_SORT_ON_PICKUP, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_ENABLE_SORTING_ON_PICKUP, LABEL_SORT_ON_PICKUP), InvTweaksLocalization.get("invtweaks.settings.advanced.sortonpickup.tooltip"));
        controlList.add(sortOnPickupBtn);
        if (obf.isMultiplayerWorld()) {
            // Sorting on pickup unavailable in SMP
            obf.setEnabled(sortOnPickupBtn, false);
            obf.setDisplayString(sortOnPickupBtn, LABEL_SORT_ON_PICKUP + SP_ONLY);
            sortOnPickupBtn.setTooltip(sortOnPickupBtn.getTooltip() + "\n(" + InvTweaksLocalization.get("invtweaks.settings.advanced.sortonpickup.tooltip.soloonly") + ")");
        }
        
        moveToButtonCoords(i++, p);
        InvTweaksGuiTooltipButton autoEquipArmorBtn = new InvTweaksGuiTooltipButton(ID_AUTO_EQUIP_ARMOR, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_ENABLE_AUTO_EQUIP_ARMOR, LABEL_EQUIP_ARMOR), 
                InvTweaksLocalization.get("invtweaks.settings.advanced.autoequip.tooltip"));
        controlList.add(autoEquipArmorBtn);
        
        moveToButtonCoords(i++, p);
        moveToButtonCoords(i++, p);
        InvTweaksGuiTooltipButton enableSoundsBtn = new InvTweaksGuiTooltipButton(ID_ENABLE_SOUNDS, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_ENABLE_SOUNDS, LABEL_ENABLE_SOUNDS),
                InvTweaksLocalization.get("invtweaks.settings.advanced.sounds.tooltip"));
        controlList.add(enableSoundsBtn);
        
        // Check if links to files are supported, if not disable the buttons
        if (!Desktop.isDesktopSupported()) {
            for (Object o : controlList) {
            	if (obf.isGuiButton(o)) {
            	    amg button = obf.asGuiButton(o);
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
        b(obf.getFontRenderer(), InvTweaksLocalization.get("invtweaks.settings.pvpwarning.pt1"), p.getX(), 100, 0x999999); // Gui.drawCenteredString
        b(obf.getFontRenderer(), InvTweaksLocalization.get("invtweaks.settings.pvpwarning.pt2"), p.getX(), 110, 0x999999);
    }
    
    protected void a(amg guibutton) { /* actionPerformed */
    	
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
                InvTweaks.logInGameErrorStatic("invtweaks.settings.advanced.mappingsfile.error", e);
            }
            break;
            
        // Back to main settings screen
        case ID_DONE:
                obf.displayGuiScreen(new InvTweaksGuiSettings(mc, parentScreen, config));
                
        }

    }

}
