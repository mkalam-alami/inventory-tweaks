




import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The global mod's configuration.
 * 
 * @author Jimeo Wan
 *
 */
public class InvTweaksConfig {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger("InvTweaks");

    // Sorting settings
    public static final String PROP_ENABLE_MIDDLE_CLICK = "enableMiddleClick";
    public static final String PROP_SHOW_CHEST_BUTTONS = "showChestButtons";
    public static final String PROP_ENABLE_SORTING_ON_PICKUP = "enableSortingOnPickup";
    
    // Shortcuts
    public static final String PROP_ENABLE_SHORTCUTS = "enableShortcuts";
    public static final String PROP_SHORTCUT_PREFIX = "shortcutKey";
    public static final String PROP_SHORTCUT_ONE_ITEM = "shortcutKeyOneItem";
    public static final String PROP_SHORTCUT_ONE_STACK = "shortcutKeyOneStack";
    public static final String PROP_SHORTCUT_ALL_ITEMS = "shortcutKeyAllItems";
    public static final String PROP_SHORTCUT_DROP = "shortcutKeyDrop";
    public static final String PROP_SHORTCUT_UP = "shortcutKeyToUpperSection";
    public static final String PROP_SHORTCUT_DOWN = "shortcutKeyToLowerSection";
    
    // Sound
    public static final String PROP_ENABLE_SORTING_SOUND = "enableSortingSound";
    public static final String PROP_ENABLE_AUTO_REFILL_SOUND = "enableAutoRefillSound";

    public static final String VALUE_TRUE = "true";
    public static final String VALUE_FALSE = "false";
    public static final String VALUE_CI_COMPATIBILITY = "convenientInventoryCompatibility";
    
    public static final String LOCKED = "LOCKED";
    public static final String FROZEN = "FROZEN";
    public static final String AUTOREPLACE = "AUTOREPLACE";
    public static final String AUTOREPLACE_NOTHING = "nothing";
    public static final String DEBUG = "DEBUG";
    public static final boolean DEFAULT_AUTO_REFILL_BEHAVIOUR = true;

    private String rulesFile;
    private String treeFile;

    private InvTweaksConfigProperties properties;
    private InvTweaksItemTree tree;
    private Vector<InvTweaksConfigInventoryRuleset> rulesets;
    private int currentRuleset = 0;
    private String currentRulesetName = null;
    private Vector<String> invalidKeywords;

    private long storedConfigLastModified;

    /**
     * Creates a new configuration holder. The configuration is not yet loaded.
     */
    public InvTweaksConfig(String rulesFile, String treeFile) {
        this.rulesFile = rulesFile;
        this.treeFile = treeFile;
        reset();
    }

    public void load() throws Exception {
        
        synchronized (this) {

            // Reset all
            reset();

            // Load properties
            loadProperties();
            saveProperties(); // Needed to append non-saved properties to the file

            // Load tree
            tree = new InvTweaksItemTreeLoader().load(treeFile);

            // Read file
            File f = new File(rulesFile);
            char[] bytes = new char[(int) f.length()];
            FileReader reader = new FileReader(f);
            reader.read(bytes);

            // Split lines into an array
            String[] configLines = String.valueOf(bytes)
                    .replace("\r\n", "\n").replace('\r', '\n').split("\n");

            // Register rules in various configurations (rulesets)
            InvTweaksConfigInventoryRuleset activeRuleset = new InvTweaksConfigInventoryRuleset(tree, "Default");
            boolean defaultRuleset = true, defaultRulesetEmpty = true;
            String invalidKeyword;

            for (String line : configLines) {
                // Change ruleset
                if (line.matches("^[\\w]*\\:$")) {
                    // Make sure not to add an empty default config to the
                    // rulesets
                    if (!defaultRuleset || !defaultRulesetEmpty) {
                        activeRuleset.finalize();
                        rulesets.add(activeRuleset);
                    }
                    activeRuleset = new InvTweaksConfigInventoryRuleset(tree, 
                            line.substring(0, line.length() - 1));
                }

                // Register line
                try {
                    invalidKeyword = activeRuleset.registerLine(line);
                    if (defaultRuleset) {
                        defaultRulesetEmpty = false;
                    }
                    if (invalidKeyword != null) {
                        invalidKeywords.add(invalidKeyword);
                    }
                } catch (InvalidParameterException e) {
                    // Invalid line (comments), no problem
                }
            }

            // Finalize
            activeRuleset.finalize();
            rulesets.add(activeRuleset);
            
            // If a specific ruleset was loaded, 
            // try to choose the same again, else load the first one
            currentRuleset = 0;
            if (currentRulesetName != null) {
                int rulesetIndex = 0;
                for (InvTweaksConfigInventoryRuleset ruleset : rulesets) {
                    if (ruleset.getName().equals(currentRulesetName)) {
                        currentRuleset = rulesetIndex;
                        break;
                    }
                    rulesetIndex++;
                }
            }
            if (currentRuleset == 0) {
                if (!rulesets.isEmpty()) {
                    currentRulesetName = rulesets.get(currentRuleset).getName();
                }
                else {
                    currentRulesetName = null;
                }
            }

        }

    }

    public boolean refreshProperties() throws IOException {
        // Check time of last edit
        long configLastModified = new File(InvTweaksConst.CONFIG_PROPS_FILE).lastModified();
        if (storedConfigLastModified != configLastModified) {
            storedConfigLastModified = configLastModified;
            loadProperties();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Saves properties
     */
    public void saveProperties() {
        File configPropsFile = getPropertyFile();
        if (configPropsFile.exists()) {
            try {
                FileOutputStream fos = new FileOutputStream(configPropsFile);
                properties.store(fos, "Inventory Tweaks Configuration\n"+
                        "(Regarding shortcuts, all key names can be found at: http://www.lwjgl.org/javadoc/org/lwjgl/input/Keyboard.html)");
                fos.flush();
                fos.close();
                storedConfigLastModified = new File(InvTweaksConst.CONFIG_PROPS_FILE).lastModified();
            } catch (IOException e) {
                InvTweaks.logInGameStatic("Failed to save config file " + 
                        InvTweaksConst.CONFIG_PROPS_FILE);
            }
        }
    }

    public Map<String, String> getProperties(String prefix) {
        Map<String, String> result = new HashMap<String, String>();
        for (Object o : properties.keySet()) {
            String key = (String) o; 
            if (key.startsWith(prefix)) {
                result.put(key, properties.getProperty(key));
            }
        }
        return result;
    }
    
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
        saveProperties();
        if (key.equals(PROP_ENABLE_MIDDLE_CLICK)) {
            resolveConvenientInventoryConflicts();
        }
    }

    public InvTweaksItemTree getTree() {
        return tree;
    }

    public String getCurrentRulesetName() {
        return currentRulesetName;
    }

    /**
     * 
     * @param i from 0 to n-1, n being the number of available configurations.
     * @return null if the given ID is invalid
     */
    public String switchConfig(int i) {
        if (!rulesets.isEmpty() && i < rulesets.size()) {
            currentRuleset = i;
            currentRulesetName = rulesets.get(currentRuleset).getName();
            return currentRulesetName;
        } else {
            return null;
        }
    }
    
    public String switchConfig() {
        if (currentRuleset == -1) {
            return switchConfig(0);
        } else {
            return switchConfig((currentRuleset + 1) % rulesets.size());
        }
    }

    /**
     * Returns all sorting rules, themselves sorted by decreasing priority.
     * 
     * @return
     */
    public Vector<InvTweaksConfigSortingRule> getRules() {
        return rulesets.get(currentRuleset).getRules();
    }

    /**
     * Returns all invalid keywords wrote in the config file.
     */
    public Vector<String> getInvalidKeywords() {
        return invalidKeywords;
    }

    /**
     * @return The locked slots array with locked priorities. WARNING: Not a
     *         copy.
     */
    public int[] getLockPriorities() {
        return rulesets.get(currentRuleset).getLockPriorities();
    }

    /**
     * @return The inventory slots array indicating which ones are frozen.
     *         WARNING: Not a copy.
     */
    public boolean[] getFrozenSlots() {
        return rulesets.get(currentRuleset).getFrozenSlots();
    }

    /**
     * @return The locked slots only
     */
    public Vector<Integer> getLockedSlots() {
        return rulesets.get(currentRuleset).getLockedSlots();
    }

    public Level getLogLevel() {
        return (rulesets.get(currentRuleset).isDebugEnabled())
                ? Level.INFO : Level.WARNING;
    }

    public boolean isAutoRefillEnabled(int itemID, int itemDamage) {
        List<InvTweaksItemTreeItem> items = tree.getItems(itemID, itemDamage);
        Vector<String> autoReplaceRules = rulesets.get(currentRuleset).getAutoReplaceRules();
        boolean found = false;
        for (String keyword : autoReplaceRules) {
            if (keyword.equals(AUTOREPLACE_NOTHING))
                return false;
            if (tree.matches(items, keyword))
                found = true;
        }
        if (found)
            return true;
        else {
            if (autoReplaceRules.isEmpty()) {
                return DEFAULT_AUTO_REFILL_BEHAVIOUR;
            } else {
                return false;
            }
        }
    }

    /**
     * Check potential conflicts with Convenient Inventory (regarding the middle
     * click shortcut), and solve them according to the CI version.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void resolveConvenientInventoryConflicts() {
        
        boolean convenientInventoryInstalled = false;
        boolean defaultCISortingShortcutEnabled = false;
        
        //// Analyze environment
        
        try {
            // Find CI class
            Class convenientInventory = Class.forName("ConvenientInventory");
            convenientInventoryInstalled = true;
            
            // Latest versions of CI: disable CI sorting thanks to 
            // the specific field provided for InvTweaks
            Field middleClickField = null;
            try {
                middleClickField = convenientInventory.getDeclaredField("middleClickEnabled");
            } catch (NoSuchFieldException e) {
                // Do nothing
            }
            if (middleClickField != null) {
                boolean middleClickSorting = getProperty(InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK)
                        .equals(InvTweaksConfig.VALUE_TRUE);
                middleClickField.setAccessible(true);
                middleClickField.setBoolean(null, !middleClickSorting);
            }
            
            // Older versions of CI: disable InvTweaks middle click
            else {
                
                // Force mod's initialization if necessary
                // (some tweaks are needed here and below because nothing is publicly visible)
                Field initializedField =  convenientInventory.getDeclaredField("initialized");
                initializedField.setAccessible(true);
                Boolean initialized = (Boolean) initializedField.get(null);
                if (!initialized) {
                    Method initializeMethod = convenientInventory.getDeclaredMethod("initialize");
                    initializeMethod.setAccessible(true);
                    initializeMethod.invoke(null);
                }
                
                // Look for the default sorting shortcut (middle click) in CI settings.
                Field actionMapField =  convenientInventory.getDeclaredField("actionMap");
                actionMapField.setAccessible(true);
                List<Integer> actionMap[][] = (List[][]) actionMapField.get(null);
                if (actionMap != null && actionMap[7] != null) { // 7 = SORT
                    for (List<Integer> combo : actionMap[7]) {
                        if (combo != null && combo.size() == 1
                                && combo.get(0) == 2) { // 2 = Middle click
                            defaultCISortingShortcutEnabled = true;
                            break;
                        }
                    }
                }
            }
            
        }
        catch (ClassNotFoundException e) {
            // Failed to find Convenient Inventory class, not a problem
        }
        catch (Exception e) {
            InvTweaks.logInGameErrorStatic("Failed to manage Convenient Inventory compatibility", e);
        }
        
        //// Shortcuts
        
        String shortcutsProp = getProperty(InvTweaksConfig.PROP_ENABLE_SHORTCUTS);
        if (convenientInventoryInstalled
                && !shortcutsProp.equals(InvTweaksConfig.VALUE_CI_COMPATIBILITY)) {
            setProperty(InvTweaksConfig.PROP_ENABLE_SHORTCUTS,
                    InvTweaksConfig.VALUE_CI_COMPATIBILITY);
        }
        else if (!convenientInventoryInstalled
                && shortcutsProp.equals(InvTweaksConfig.VALUE_CI_COMPATIBILITY)) {
            setProperty(InvTweaksConfig.PROP_ENABLE_SHORTCUTS,
                    InvTweaksConfig.VALUE_TRUE);
        }
        
        //// Middle click
        
        // If CI's middle click is enabled, disable InvTweaks shortcut
        String middleClickProp = getProperty(InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK);
        if (defaultCISortingShortcutEnabled && 
                !middleClickProp.equals(InvTweaksConfig.VALUE_CI_COMPATIBILITY)) {
            setProperty(InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK,
                    InvTweaksConfig.VALUE_CI_COMPATIBILITY);
        }
        // If the conflict is now resolved, re-enable the shortcut
        else if (!defaultCISortingShortcutEnabled &&
                middleClickProp.equals(InvTweaksConfig.VALUE_CI_COMPATIBILITY)) {
            setProperty(InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK,
                    InvTweaksConfig.VALUE_TRUE);
        }
    }

    private void reset() {
        rulesets = new Vector<InvTweaksConfigInventoryRuleset>();
        currentRuleset = -1;

        // Default property values
        properties = new InvTweaksConfigProperties();
        
        properties.put(PROP_ENABLE_MIDDLE_CLICK, VALUE_TRUE);
        properties.put(PROP_SHOW_CHEST_BUTTONS, VALUE_TRUE);
        properties.put(PROP_ENABLE_SORTING_ON_PICKUP, VALUE_TRUE);
        properties.put(PROP_ENABLE_AUTO_REFILL_SOUND, VALUE_TRUE);
        properties.put(PROP_ENABLE_SORTING_SOUND, VALUE_TRUE);
        properties.put(PROP_ENABLE_SHORTCUTS, VALUE_TRUE);
        
        properties.put(PROP_SHORTCUT_ALL_ITEMS, "LSHIFT, RSHIFT"); // TODO
        properties.put(PROP_SHORTCUT_ONE_ITEM, "LCONTROL, RCONTROL");
        properties.put(PROP_SHORTCUT_ONE_STACK, "LSHIFT, RSHIFT");
        properties.put(PROP_SHORTCUT_UP, "UP");
        properties.put(PROP_SHORTCUT_DOWN, "DOWN");
        properties.put(PROP_SHORTCUT_DROP, "LALT, RALT");


        invalidKeywords = new Vector<String>();
    }

    private void loadProperties() throws IOException {
        File configPropsFile = getPropertyFile();
        if (configPropsFile != null) {
            FileInputStream fis = new FileInputStream(configPropsFile);
            properties.load(fis);
            fis.close();
            resolveConvenientInventoryConflicts();
        }
        properties.sortKeys();
        
        // 1.30 patch: rename wrong shortcuts
        if (((String) properties.get(PROP_SHORTCUT_DROP)).contains("META"))
            properties.setProperty(PROP_SHORTCUT_DROP, "LALT, RALT");
        if (((String) properties.get(PROP_SHORTCUT_ONE_ITEM)).contains("CTRL"))
            properties.setProperty(PROP_SHORTCUT_ONE_ITEM, "LCONTROL, RCONTROL");
        
        // Retro-compatibility: rename autoreplace
        if (properties.contains("enableAutoreplaceSound")) {
            properties.put(PROP_ENABLE_AUTO_REFILL_SOUND, 
                    (String) properties.get("enableAutoreplaceSound"));
            properties.remove("enableAutoreplaceSound");
        }
    }

    /**
     * Returns the file when the properties are stored, after making sure the
     * file exists.
     * 
     * @return May return null in case of failure while creating the file.
     */
    private File getPropertyFile() {
        File configPropsFile = new File(InvTweaksConst.CONFIG_PROPS_FILE);
        if (!configPropsFile.exists()) {
            try {
                configPropsFile.createNewFile();
            } catch (IOException e) {
                InvTweaks.logInGameStatic("Failed to create the config file "
                        + InvTweaksConst.CONFIG_PROPS_FILE);
                return null;
            }
        }
        return configPropsFile;
    }

}
