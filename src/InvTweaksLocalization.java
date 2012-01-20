import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;


/**
 * The global mod's configuration.
 * 
 * @author Jimeo Wan
 *
 */
public class InvTweaksLocalization {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger("InvTweaks");
    private static final String DEFAULT_LANGUAGE = "en_US";
    private static Properties mappings = new Properties();
    private static String loadedLanguage = null;
    
    public synchronized static String get(String key) {

        String currentLanguage = InvTweaksObfuscation.getCurrentLanguage();
        if (!currentLanguage.equals(loadedLanguage)) {
            loadedLanguage = load(currentLanguage);
        }
        
        return mappings.getProperty(key, "???");
        
    }
    
    private static String load(String currentLanguage) {
        
        mappings.clear();

        try {
            URL langFileUrl = InvTweaksLocalization.class.getResource("lang/" + currentLanguage + ".properties");
            if (langFileUrl == null) {
                System.out.println("lang/" + DEFAULT_LANGUAGE + ".properties");
                langFileUrl = InvTweaksLocalization.class.getResource("lang/" + DEFAULT_LANGUAGE + ".properties");
            }
            mappings.load(new FileInputStream(new File(langFileUrl.toURI())));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return loadedLanguage;
    }

}