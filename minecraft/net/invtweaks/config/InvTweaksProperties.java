package net.invtweaks.config;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Extension of the default Properties class,
 * that ensures the entries are listed in alphabetical order.
 * @author MARWANE
 *
 */
public class InvTweaksProperties extends Properties {

    private static final long serialVersionUID = 1L;
    
    private final List<String> keys = new LinkedList<String>();

    public Enumeration<Object> keys() {
        return Collections.<Object>enumeration(
                new LinkedHashSet<Object>(keys));
    }

    public Object put(String key, Object value) {
        keys.add(key);
        return super.put(key, value);
    }
    
    public void sortKeys() {
        Collections.sort(keys);
    }
}
