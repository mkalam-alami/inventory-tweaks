package net.invtweaks.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.invtweaks.Const;
import net.invtweaks.tree.ItemTree;
import net.invtweaks.tree.ItemTreeItem;
import net.invtweaks.tree.ItemTreeLoader;
import net.minecraft.src.InvTweaks;

/**
 * The global mod's configuration.
 * 
 * @author Jimeo Wan
 *
 */
public class InvTweaksConfig {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger("InvTweaks");

    public static final String PROP_ENABLE_MIDDLE_CLICK = "enableMiddleClick";
    public static final String PROP_SHOW_CHEST_BUTTONS = "showChestButtons";
    public static final String PROP_ENABLE_SORTING_ON_PICKUP = "enableSortingOnPickup";
    public static final String PROP_ENABLE_AUTOREPLACE_SOUND = "enableAutoreplaceSound";
    public static final String PROP_ENABLE_SORTING_SOUND = "enableSortingSound";
    public static final String PROP_CONVENIENT_INVENTORY_COMPATIBILITY = "convenientInventoryCompatibility";

    public static final String LOCKED = "LOCKED";
    public static final String FROZEN = "FROZEN";
    public static final String AUTOREPLACE = "AUTOREPLACE";
    public static final String AUTOREPLACE_NOTHING = "nothing";
    public static final String DEBUG = "DEBUG";
    public static final boolean DEFAULT_AUTOREPLACE_BEHAVIOUR = true;

    private String rulesFile;
    private String treeFile;

    private Properties properties;
    private ItemTree tree;
    private Vector<InventoryConfigRuleset> rulesets;
    private int currentRuleset;
    private Vector<String> invalidKeywords;

    private long storedConfigLastModified;

    /**
     * Creates a new configuration holder. The configuration is not yet loaded.
     */
    public InvTweaksConfig(String rulesFile, String treeFile) {
        this.rulesFile = rulesFile;
        this.treeFile = treeFile;
        init();
    }

    public void load() throws Exception {

        synchronized (this) {

            // Reset all
            init();

            // Load properties
            loadProperties();
            saveProperties(); // Needed to append non-saved properties to the
                              // file

            // Load tree
            tree = new ItemTreeLoader().load(treeFile);

            // Read file
            File f = new File(rulesFile);
            char[] bytes = new char[(int) f.length()];
            FileReader reader = new FileReader(f);
            reader.read(bytes);

            // Split lines into an array
            String[] configLines = String.valueOf(bytes)
                    .replace("\r\n", "\n").replace('\r', '\n').split("\n");

            // Register rules in various configurations (rulesets)
            InventoryConfigRuleset ruleset = new InventoryConfigRuleset(tree, "Default");
            boolean defaultRuleset = true, defaultRulesetEmpty = true;
            String invalidKeyword;

            for (String line : configLines) {
                // Change ruleset
                if (line.matches("^[\\w]*\\:$")) {
                    // Make sure not to add an empty default config to the
                    // rulesets
                    if (!defaultRuleset || !defaultRulesetEmpty) {
                        ruleset.finalize();
                        rulesets.add(ruleset);
                    }
                    ruleset = new InventoryConfigRuleset(tree, 
                            line.substring(0, line.length() - 1));
                }

                // Register line
                try {
                    invalidKeyword = ruleset.registerLine(line);
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

            ruleset.finalize();
            rulesets.add(ruleset);
            currentRuleset = 0;

        }

    }

    public boolean refreshProperties() throws IOException {
        // Check time of last edit
        long configLastModified = new File(Const.CONFIG_PROPS_FILE).lastModified();
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
                properties.store(fos, "Inventory Tweaks Configuration");
                fos.flush();
                fos.close();
                storedConfigLastModified = new File(Const.CONFIG_PROPS_FILE).lastModified();
            } catch (IOException e) {
                InvTweaks.logInGameStatic("Failed to save config file " + 
                        Const.CONFIG_PROPS_FILE);
            }
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
        saveProperties();
    }

    public ItemTree getTree() {
        return tree;
    }

    public String getCurrentRulesetName() {
        return rulesets.get(currentRuleset).getName();
    }

    public String switchConfig() {
        if (!rulesets.isEmpty()) {
            if (currentRuleset == -1) {
                currentRuleset = 0;
            } else {
                currentRuleset = (currentRuleset + 1) % rulesets.size();
            }
            return rulesets.get(currentRuleset).getName();
        } else {
            return null;
        }
    }

    /**
     * Returns all sorting rules, themselves sorted by decreasing priority.
     * 
     * @return
     */
    public Vector<InventoryConfigRule> getRules() {
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

    public boolean autoreplaceEnabled(int itemID, int itemDamage) {
        List<ItemTreeItem> items = tree.getItems(itemID, itemDamage);
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
                return DEFAULT_AUTOREPLACE_BEHAVIOUR;
            } else {
                return false;
            }
        }
    }

    private void init() {
        rulesets = new Vector<InventoryConfigRuleset>();
        currentRuleset = -1;

        // Default property values
        properties = new Properties();
        properties.setProperty(PROP_ENABLE_MIDDLE_CLICK, "true");
        properties.setProperty(PROP_SHOW_CHEST_BUTTONS, "true");
        properties.setProperty(PROP_ENABLE_SORTING_ON_PICKUP, "true");
        properties.setProperty(PROP_ENABLE_AUTOREPLACE_SOUND, "true");
        properties.setProperty(PROP_ENABLE_SORTING_SOUND, "true");

        invalidKeywords = new Vector<String>();
    }

    private void loadProperties() throws IOException {
        File configPropsFile = getPropertyFile();
        if (configPropsFile != null) {
            FileInputStream fis = new FileInputStream(configPropsFile);
            properties.load(fis);
            fis.close();
        }
    }

    /**
     * Returns the file when the properties are stored, after making sure the
     * file exists.
     * 
     * @return May return null in case of failure while creating the file.
     */
    private File getPropertyFile() {
        File configPropsFile = new File(Const.CONFIG_PROPS_FILE);
        if (!configPropsFile.exists()) {
            try {
                configPropsFile.createNewFile();
            } catch (IOException e) {
                InvTweaks.logInGameStatic("Failed to create the config file "
                        + Const.CONFIG_PROPS_FILE);
                return null;
            }
        }
        return configPropsFile;
    }

}
