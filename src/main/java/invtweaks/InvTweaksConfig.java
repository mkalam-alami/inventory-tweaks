package invtweaks;

import invtweaks.api.IItemTreeItem;
import invtweaks.forge.ClientProxy;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;


/**
 * The global mod's configuration.
 *
 * @author Jimeo Wan
 */
public class InvTweaksConfig {
    public static final String PROP_VERSION = "version";
    // Sorting settings
    public static final String PROP_ENABLE_MIDDLE_CLICK = "enableMiddleClick";
    public static final String PROP_SHOW_CHEST_BUTTONS = "showChestButtons";
    public static final String PROP_ENABLE_SORTING_ON_PICKUP = "enableSortingOnPickup";
    public static final String PROP_ENABLE_AUTO_EQUIP_ARMOR = "enableAutoEquipArmor";
    public static final String PROP_ENABLE_AUTO_REFILL = "enableAutoRefill";
    public static final String PROP_AUTO_REFILL_BEFORE_BREAK = "autoRefillBeforeBreak";
    public static final String PROP_AUTO_REFILL_DAMAGE_THRESHHOLD = "autoRefillDamageThreshhold";
    // Shortcuts
    public static final String PROP_ENABLE_SHORTCUTS = "enableShortcuts";
    public static final String PROP_SHORTCUT_PREFIX = "shortcutKey";
    public static final String PROP_SHORTCUT_ONE_ITEM = "shortcutKeyOneItem";
    public static final String PROP_SHORTCUT_ALL_ITEMS = "shortcutKeyAllItems";
    public static final String PROP_SHORTCUT_EVERYTHING = "shortcutKeyEverything";
    public static final String PROP_SHORTCUT_DROP = "shortcutKeyDrop";
    public static final String PROP_SHORTCUT_UP = "shortcutKeyToUpperSection";
    public static final String PROP_SHORTCUT_DOWN = "shortcutKeyToLowerSection";
    // Other
    public static final String PROP_ENABLE_SOUNDS = "enableSounds";
    public static final String PROP_ENABLE_SERVER_ITEMSWAP = "enableServerItemSwap";
    public static final String PROP_ENABLE_CONFIG_LOADED_MESSAGE = "enableConfigLoadedMesssage";
    public static final String VALUE_TRUE = "true";
    public static final String VALUE_FALSE = "false";
    public static final String LOCKED = "locked";
    public static final String FROZEN = "frozen";
    public static final String AUTOREFILL = "autorefill";
    public static final String AUTOREFILL_NOTHING = "nothing";
    public static final String DEBUG = "debug";
    public static final boolean DEFAULT_AUTO_REFILL_BEHAVIOUR = true;
    private static final Logger log = InvTweaks.log;
    private File rulesFile;
    private File treeFile;

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
    public InvTweaksConfig(File rulesFile, File treeFile) {
        this.rulesFile = rulesFile;
        this.treeFile = treeFile;

        reset();
    }

    public void load() throws Exception {

        synchronized(this) {

            // Reset all
            reset();

            // Load properties
            loadProperties();
            saveProperties(); // Needed to append non-saved properties to the file

            if(tree != null) {
                MinecraftForge.EVENT_BUS.unregister(tree);
            }
            // Load tree
            tree = InvTweaksItemTreeLoader.load(treeFile);

            // Read file
            char[] bytes = new char[(int) rulesFile.length()];
            FileReader reader = null;
            try {
                reader = new FileReader(rulesFile);
                reader.read(bytes);
            } finally {
                if(reader != null) {
                    reader.close();
                }
            }

            // Split lines into an array
            String[] configLines = String.valueOf(bytes).replace("\r\n", "\n").replace('\r', '\n').split("\n");

            // Register rules in various configurations (rulesets)
            InvTweaksConfigInventoryRuleset activeRuleset = new InvTweaksConfigInventoryRuleset(tree, "Default");
            boolean defaultRuleset = true, defaultRulesetEmpty = true;
            String invalidKeyword;

            for(String line : configLines) {
                String trimmedLine = line.trim();
                if(!trimmedLine.isEmpty()) {
                    // Change ruleset
                    if(trimmedLine.matches("^[\\w]*[\\s]*\\:$")) {
                        // Make sure not to add an empty default config to the rulesets
                        if(!defaultRuleset || !defaultRulesetEmpty) {
                            activeRuleset.finalizeRules();
                            rulesets.add(activeRuleset);
                        }
                        activeRuleset = new InvTweaksConfigInventoryRuleset(tree, trimmedLine
                                .substring(0, trimmedLine.length() - 1));
                    }

                    // Register line
                    else {
                        try {
                            invalidKeyword = activeRuleset.registerLine(trimmedLine);
                            if(defaultRuleset) {
                                defaultRulesetEmpty = false;
                            }
                            if(invalidKeyword != null) {
                                invalidKeywords.add(invalidKeyword);
                            }
                        } catch(InvalidParameterException e) {
                            // Invalid line (comments), no problem
                        }
                    }
                }
            }

            // Finalize
            activeRuleset.finalizeRules();
            rulesets.add(activeRuleset);

            // If a specific ruleset was loaded, 
            // try to choose the same again, else load the first one
            currentRuleset = 0;
            if(currentRulesetName != null) {
                int rulesetIndex = 0;
                for(InvTweaksConfigInventoryRuleset ruleset : rulesets) {
                    if(ruleset.getName().equals(currentRulesetName)) {
                        currentRuleset = rulesetIndex;
                        break;
                    }
                    rulesetIndex++;
                }
            }
            if(currentRuleset == 0) {
                if(!rulesets.isEmpty()) {
                    currentRulesetName = rulesets.get(currentRuleset).getName();
                } else {
                    currentRulesetName = null;
                }
            }

        }

    }

    public boolean refreshProperties() throws IOException {
        // Check time of last edit
        long configLastModified = InvTweaksConst.CONFIG_PROPS_FILE.lastModified();
        if(storedConfigLastModified != configLastModified) {
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
        if(configPropsFile.exists()) {
            try {
                FileOutputStream fos = new FileOutputStream(configPropsFile);
                properties.store(fos,
                        "Inventory Tweaks Configuration\n" + "(Regarding shortcuts, all key names can be found at: http://www.lwjgl.org/javadoc/org/lwjgl/input/Keyboard.html)");
                fos.flush();
                fos.close();
                storedConfigLastModified = InvTweaksConst.CONFIG_PROPS_FILE.lastModified();
            } catch(IOException e) {
                InvTweaks.logInGameStatic("Failed to save config file " + InvTweaksConst.CONFIG_PROPS_FILE);
            }
        }
    }

    public Map<String, String> getProperties(String prefix) {
        Map<String, String> result = new HashMap<String, String>();
        for(Object o : properties.keySet()) {
            String key = (String) o;
            if(key.startsWith(prefix)) {
                result.put(key, properties.getProperty(key));
            }
        }
        return result;
    }

    /**
     * Get a configuration property value
     *
     * @param key
     * @return The value or "" (never null)
     */
    public String getProperty(String key) {
        String value = properties.getProperty(key);
        return (value != null) ? value : "";
    }

    public int getIntProperty(String key) {
        return Integer.parseInt(getProperty(key));
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
        saveProperties();
    }

    public InvTweaksItemTree getTree() {
        return tree;
    }

    public String getCurrentRulesetName() {
        return currentRulesetName;
    }

    /**
     * @param i from 0 to n-1, n being the number of available configurations.
     * @return null if the given ID is invalid or the config is already enabled
     */
    public String switchConfig(int i) {
        if(!rulesets.isEmpty() && i < rulesets.size() && i != currentRuleset) {
            currentRuleset = i;
            currentRulesetName = rulesets.get(currentRuleset).getName();
            return currentRulesetName;
        } else {
            return null;
        }
    }

    public String switchConfig() {
        if(currentRuleset == -1) {
            return switchConfig(0);
        } else {
            return switchConfig((currentRuleset + 1) % rulesets.size());
        }
    }

    /**
     * @return all sorting rules, themselves sorted by decreasing priority
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
     * @return The locked slots array with locked priorities. WARNING: Not a copy.
     */
    public int[] getLockPriorities() {
        return rulesets.get(currentRuleset).getLockPriorities();
    }

    /**
     * @return The inventory slots array indicating which ones are frozen. WARNING: Not a copy.
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

    public boolean isAutoRefillEnabled(String itemID, int itemDamage) {
        if(!getProperty(PROP_ENABLE_AUTO_REFILL).equals(VALUE_FALSE)) {
            List<IItemTreeItem> items = tree.getItems(itemID, itemDamage);
            Vector<String> autoReplaceRules = rulesets.get(currentRuleset).getAutoReplaceRules();
            boolean found = false;
            for(String keyword : autoReplaceRules) {
                if(keyword.equals(AUTOREFILL_NOTHING)) {
                    return false;
                }
                if(tree.matches(items, keyword)) {
                    found = true;
                }
            }
            if(found) {
                return true;
            } else {
                if(autoReplaceRules.isEmpty()) {
                    return DEFAULT_AUTO_REFILL_BEHAVIOUR;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    private void reset() {
        rulesets = new Vector<InvTweaksConfigInventoryRuleset>();
        currentRuleset = -1;

        // Default property values
        properties = new InvTweaksConfigProperties();

        properties.put(PROP_ENABLE_MIDDLE_CLICK, VALUE_TRUE);
        properties.put(PROP_SHOW_CHEST_BUTTONS, VALUE_TRUE);
        properties.put(PROP_ENABLE_SORTING_ON_PICKUP, VALUE_FALSE);
        properties.put(PROP_ENABLE_AUTO_REFILL, VALUE_TRUE);
        properties.put(PROP_AUTO_REFILL_BEFORE_BREAK, VALUE_FALSE);
        properties.put(PROP_AUTO_REFILL_DAMAGE_THRESHHOLD, "5");
        properties.put(PROP_ENABLE_SOUNDS, VALUE_TRUE);
        properties.put(PROP_ENABLE_SHORTCUTS, VALUE_TRUE);
        properties.put(PROP_ENABLE_AUTO_EQUIP_ARMOR, VALUE_FALSE);
        properties.put(PROP_ENABLE_SERVER_ITEMSWAP, VALUE_TRUE);
        properties.put(PROP_ENABLE_CONFIG_LOADED_MESSAGE, VALUE_FALSE);

        properties.put(PROP_SHORTCUT_ALL_ITEMS, "LCONTROL+LSHIFT, RCONTROL+RSHIFT");
        properties.put(PROP_SHORTCUT_EVERYTHING, "SPACE");
        properties.put(PROP_SHORTCUT_ONE_ITEM, "LCONTROL, RCONTROL");
        properties.put(PROP_SHORTCUT_UP, "UP");
        properties.put(PROP_SHORTCUT_DOWN, "DOWN");
        properties.put(PROP_SHORTCUT_DROP, "LALT, RALT");

        properties.put(PROP_VERSION, InvTweaksConst.MOD_VERSION.split(" ")[0]);

        invalidKeywords = new Vector<String>();
    }

    private void loadProperties() throws IOException {
        File configPropsFile = getPropertyFile();
        InvTweaksConfigProperties newProperties = new InvTweaksConfigProperties();
        if(configPropsFile != null) {
            FileInputStream fis = new FileInputStream(configPropsFile);
            newProperties.load(fis);
            fis.close();
        }
        newProperties.sortKeys();

        if(newProperties.get(PROP_VERSION) != null) {
            // Override default values
            for(Entry<Object, Object> entry : newProperties.entrySet()) {
                properties.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Returns the file when the properties are stored, after making sure the file exists.
     *
     * @return May return null in case of failure while creating the file.
     */
    private File getPropertyFile() {
        File configPropsFile = InvTweaksConst.CONFIG_PROPS_FILE;
        if(!configPropsFile.exists()) {
            try {
                configPropsFile.createNewFile();
            } catch(IOException e) {
                InvTweaks.logInGameStatic("invtweaks.propsfile.errors");
                return null;
            }
        }
        return configPropsFile;
    }

    public int getSortKeyCode() {
        return ClientProxy.KEYBINDING_SORT.getKeyCode();
    }

}