package net.invtweaks.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.invtweaks.Const;
import net.invtweaks.logic.InventoryAlgorithms;
import net.minecraft.client.Minecraft;
import net.minecraft.src.InvTweaks;

/**
 * Handles the (re)loading of the configuration,
 * and all that is related to file extraction/moves.
 * 
 * @author Jimeo Wan
 *
 */
public class InvTweaksConfigManager {

    private static final Logger log = Logger.getLogger("InvTweaks");

    private Minecraft mc;
    
    /**
     * The mod's configuration.
     */
    private InvTweaksConfig config = null;
    private long storedConfigLastModified = 0;

    /**
     * The mod's logic for sorting and moving items.
     */
    private InventoryAlgorithms inventoryAlgorithms = null;
    
    public InvTweaksConfigManager(Minecraft mc) {
        this.mc = mc;
    }
    
    public InventoryAlgorithms getInventoryAlgorithms() {
        return inventoryAlgorithms;
    }
    
    public InvTweaksConfig getConfig() {
        return config;
    }

    // TODO Only reload modified file(s)
    public boolean makeSureConfigurationIsLoaded() {

        // Load properties
        try {
            if (config != null && config.refreshProperties()) {
                InvTweaks.logInGameStatic("Mod properties loaded");
            }
        } catch (IOException e) {
            InvTweaks.logInGameErrorStatic("Failed to refresh properties from file", e);
        }

        // Load rules + tree files
        long configLastModified = computeConfigLastModified();
        if (config != null) {
            // Check time of last edit for both configuration files.
            if (storedConfigLastModified != configLastModified) {
                return loadConfig(); // Reload
            } else {
                return true;
            }
        } else {
            storedConfigLastModified = configLastModified;
            if (loadConfig()) { // Reload
                resolveConvenientInventoryConflicts();
                return true;
            }
            else {
                return false;
            }
        }
    }

    /**
     * Check potential conflicts with Convenient Inventory (regarding the middle
	 * click shortcut), and solve them (by disabling middle click for InvTweaks).
	 * Should be called only once by game since CI only loads on startup.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void resolveConvenientInventoryConflicts() {
        
        if (config == null) {
            return;
        }
        
        boolean defaultCISortingShortcutEnabled = false;
        
        try {
            // Find CI class
            Class convenientInventory = Class.forName("ConvenientInventory");
            
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
        catch (ClassNotFoundException e) {
            // Failed to find Convenient Inventory class, not a problem
        }
        catch (Exception e) {
            InvTweaks.logInGameErrorStatic("Failed to manage Convenient Inventory compatibility", e);
        }
        
        // If CI's middle click is enabled, disable InvTweaks shortcut
        String middleClickProp = config.getProperty(InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK);
        if (defaultCISortingShortcutEnabled && 
                !middleClickProp.equals(InvTweaksConfig.VALUE_CI_COMPATIBILITY)) {
            config.setProperty(InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK,
                    InvTweaksConfig.VALUE_CI_COMPATIBILITY);
        }
        // If the conflict is now resolved, re-enable the shortcut
        else if (!defaultCISortingShortcutEnabled &&
                middleClickProp.equals(InvTweaksConfig.VALUE_CI_COMPATIBILITY)) {
            config.setProperty(InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK,
                    InvTweaksConfig.VALUE_TRUE);
        }
    }

    private long computeConfigLastModified() {
        return new File(Const.CONFIG_RULES_FILE).lastModified()
        + new File(Const.CONFIG_TREE_FILE).lastModified();
    }

    /**
     * Tries to load mod configuration from file, with error handling. If it
     * fails, the config attribute will remain null.
     * 
     * @param config
     */
    private boolean loadConfig() {

        // Compatibility: Move/Remove old files

        if (new File(Const.OLD_CONFIG_RULES_FILE).exists()) {
            if (new File(Const.CONFIG_RULES_FILE).exists()) {
                backupFile(new File(Const.CONFIG_RULES_FILE), Const.CONFIG_RULES_FILE);
            }
            new File(Const.OLD_CONFIG_RULES_FILE).renameTo(new File(Const.CONFIG_RULES_FILE));
        }
        if (new File(Const.OLD_CONFIG_TREE_FILE).exists()) {
            backupFile(new File(Const.OLD_CONFIG_TREE_FILE), Const.CONFIG_TREE_FILE);
        }

        // Create missing files

        if (!new File(Const.CONFIG_RULES_FILE).exists() && 
                extractFile(Const.DEFAULT_CONFIG_FILE, Const.CONFIG_RULES_FILE)) {
            InvTweaks.logInGameStatic(Const.CONFIG_RULES_FILE + " missing, creating default one.");
        }
        if (!new File(Const.CONFIG_TREE_FILE).exists() && 
                extractFile(Const.DEFAULT_CONFIG_TREE_FILE, Const.CONFIG_TREE_FILE)) {
            InvTweaks.logInGameStatic(Const.CONFIG_TREE_FILE + " missing, creating default one.");
        }

        storedConfigLastModified = computeConfigLastModified();

        // Load

        String error = null;

        try {
            
            // Configuration creation
            if (config == null) {
                config = new InvTweaksConfig(Const.CONFIG_RULES_FILE, Const.CONFIG_TREE_FILE);
                inventoryAlgorithms = new InventoryAlgorithms(mc, config); // Load
                                                                           // algorithm
            }
            
            // Configuration loading
            config.load();
            
            log.setLevel(config.getLogLevel());
            InvTweaks.logInGameStatic("Configuration loaded");
            showConfigErrors(config);
        } catch (FileNotFoundException e) {
            error = "Config file not found";
        } catch (Exception e) {
            error = "Error while loading config: " + e.getMessage();
        }

        if (error != null) {
            InvTweaks.logInGameStatic(error);
            log.severe(error);
            config = null;
            return false;
        } else {
            return true;
        }
    }

    private void backupFile(File file, String baseName) {
        String newFileName;
        if (new File(baseName + ".bak").exists()) {
            int i = 1;
            while (new File(baseName + ".bak" + i).exists()) {
                i++;
            }
            newFileName = baseName + ".bak" + i;
        } else {
            newFileName = baseName + ".bak";
        }
        file.renameTo(new File(newFileName));
    }

    private boolean extractFile(String resource, String destination) {

        String resourceContents = "";
        URL resourceUrl = InvTweaks.class.getResource(resource);

        // Extraction from minecraft.jar
        if (resourceUrl != null) {
            try {
                Object o = resourceUrl.getContent();
                if (o instanceof InputStream) {
                    InputStream content = (InputStream) o;
                    while (content.available() > 0) {
                        byte[] bytes = new byte[content.available()];
                        content.read(bytes);
                        resourceContents += new String(bytes);
                    }
                }
            } catch (IOException e) {
                resourceUrl = null;
            }
        }

        // Extraction from mods folder
        if (resourceUrl == null) {

            File modFolder = new File(Const.MINECRAFT_DIR + File.separatorChar + "mods");

            File[] zips = modFolder.listFiles();
            if (zips != null && zips.length > 0) {
                for (File zip : zips) {
                    try {
                        ZipFile invTweaksZip = new ZipFile(zip);
                        ZipEntry zipResource = invTweaksZip.getEntry(resource);
                        if (zipResource != null) {
                            InputStream content = invTweaksZip.getInputStream(zipResource);
                            while (content.available() > 0) {
                                byte[] bytes = new byte[content.available()];
                                content.read(bytes);
                                resourceContents += new String(bytes);
                            }
                            break;
                        }
                    } catch (Exception e) {
                        log.warning("Failed to extract " + resource + " from mod: " + e.getMessage());
                    }
                }
            }
        }

        // Write to destination
        if (!resourceContents.isEmpty()) {
            try {
                FileWriter f = new FileWriter(destination);
                f.write(resourceContents);
                f.close();
                return true;
            } catch (IOException e) {
                InvTweaks.logInGameStatic("The mod won't work, because " + destination + " creation failed!");
                log.severe("Cannot create " + destination + " file: " + e.getMessage());
                return false;
            }
        } else {
            InvTweaks.logInGameStatic("The mod won't work, because " + resource + " could not be found!");
            log.severe("Cannot create " + destination + " file: " + resource + " not found");
            return false;
        }
    }

    private void showConfigErrors(InvTweaksConfig config) {
        Vector<String> invalid = config.getInvalidKeywords();
        if (invalid.size() > 0) {
            String error = "Invalid keywords found: ";
            for (String keyword : config.getInvalidKeywords()) {
                error += keyword + " ";
            }
            InvTweaks.logInGameStatic(error);
        }
    }

}
