package invtweaks;

import invtweaks.api.IItemTreeListener;
import net.minecraftforge.common.MinecraftForge;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Loads the item tree by parsing the XML file.
 *
 * @author Jimeo Wan
 */
public class InvTweaksItemTreeLoader extends DefaultHandler {

    private final static String ATTR_ID = "id";
    private final static String ATTR_DAMAGE = "damage";
    private final static String ATTR_RANGE_DMIN = "dmin"; // Damage ranges
    private final static String ATTR_RANGE_DMAX = "dmax";
    private final static String ATTR_OREDICT_NAME = "oreDictName"; // OreDictionary names
    private final static String ATTR_TREE_VERSION = "treeVersion";

    private static InvTweaksItemTree tree;

    private static String treeVersion;
    private static int itemOrder;
    private static LinkedList<String> categoryStack;

    private static boolean treeLoaded = false;
    private static final List<IItemTreeListener> onLoadListeners = new ArrayList<IItemTreeListener>();

    private static void init() {
        treeVersion = null;
        tree = new InvTweaksItemTree();
        itemOrder = 0;
        categoryStack = new LinkedList<String>();
    }

    public synchronized static InvTweaksItemTree load(File file) throws Exception {
        init();

        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        SAXParser parser = parserFactory.newSAXParser();
        parser.parse(file, new InvTweaksItemTreeLoader());

        // Tree loaded event
        synchronized(onLoadListeners) {
            treeLoaded = true;
            for(IItemTreeListener onLoadListener : onLoadListeners) {
                onLoadListener.onTreeLoaded(tree);
            }
        }

        MinecraftForge.EVENT_BUS.register(tree);

        return tree;
    }

    public synchronized static boolean isValidVersion(File file) throws Exception {
        init();

        if(file.exists()) {
            treeVersion = null;
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            SAXParser parser = parserFactory.newSAXParser();
            parser.parse(file, new InvTweaksItemTreeLoader());
            return InvTweaksConst.TREE_VERSION.equals(treeVersion);
        } else {
            return false;
        }
    }

    public synchronized static void addOnLoadListener(IItemTreeListener listener) {
        onLoadListeners.add(listener);
        if(treeLoaded) {
            // Late event triggering
            listener.onTreeLoaded(tree);
        }
    }

    public synchronized static boolean removeOnLoadListener(IItemTreeListener listener) {
        return onLoadListeners.remove(listener);
    }


    @Override
    public synchronized void startElement(String uri, String localName, String name, Attributes attributes)
            throws SAXException {

        String rangeDMinAttr = attributes.getValue(ATTR_RANGE_DMIN);
        String newTreeVersion = attributes.getValue(ATTR_TREE_VERSION);
        String oreDictNameAttr = attributes.getValue(ATTR_OREDICT_NAME);

        // Category
        if(attributes.getLength() == 0 || treeVersion == null || rangeDMinAttr != null) {

            // Tree version
            if(treeVersion == null) {
                treeVersion = newTreeVersion;
            }

            if(categoryStack.isEmpty()) {
                // Root category
                tree.setRootCategory(new InvTweaksItemTreeCategory(name));
            } else {
                // Normal category
                tree.addCategory(categoryStack.getLast(), new InvTweaksItemTreeCategory(name));
            }

            // Handle damage ranges
            if(rangeDMinAttr != null) {
                String id = attributes.getValue(ATTR_ID);
                int rangeDMin = Integer.parseInt(rangeDMinAttr);
                int rangeDMax = Integer.parseInt(attributes.getValue(ATTR_RANGE_DMAX));
                for(int damage = rangeDMin; damage <= rangeDMax; damage++) {
                    tree.addItem(name, new InvTweaksItemTreeItem((name + id + "-" + damage).toLowerCase(), id, damage,
                                                                 itemOrder++));
                }
            }

            categoryStack.add(name);
        }

        // Item
        else if(attributes.getValue(ATTR_ID) != null) {
            String id = attributes.getValue(ATTR_ID);
            int damage = InvTweaksConst.DAMAGE_WILDCARD;
            if(attributes.getValue(ATTR_DAMAGE) != null) {
                damage = Integer.parseInt(attributes.getValue(ATTR_DAMAGE));
            }
            tree.addItem(categoryStack.getLast(),
                         new InvTweaksItemTreeItem(name.toLowerCase(), id, damage, itemOrder++));
        } else if(oreDictNameAttr != null) {
            tree.registerOre(categoryStack.getLast(), name.toLowerCase(), oreDictNameAttr, itemOrder++);
        }
    }

    @Override
    public synchronized void endElement(String uri, String localName, String name) throws SAXException {
        if(!categoryStack.isEmpty() && name.equals(categoryStack.getLast())) {
            categoryStack.removeLast();
        }
    }

}