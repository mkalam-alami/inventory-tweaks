package invtweaks;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Loads the item tree by parsing the XML file.
 * @author Jimeo Wan
 *
 */
public class InvTweaksItemTreeLoader extends DefaultHandler {

    private final static String ATTR_RANGE_MIN = "min";
    private final static String ATTR_RANGE_MAX = "max";
    private final static String ATTR_ID = "id";
    private final static String ATTR_DAMAGE = "damage";
    private final static String ATTR_TREE_VERSION = "treeVersion";

    private static InvTweaksItemTree tree;

    private static String treeVersion;
    private static int itemOrder;
    private static LinkedList<String> categoryStack;

    private static Boolean treeLoaded;
    private static List<InvTweaksItemTreeListener> onLoadListeners = new ArrayList<InvTweaksItemTreeListener>();

    private static void init() {
    	treeLoaded = false;
    	treeVersion = null;
    	tree = new InvTweaksItemTree();
    	itemOrder = 0;
    	categoryStack = new LinkedList<String>();
    }
    
    public synchronized static InvTweaksItemTree load(String filePath) throws Exception {
    	init();
    	
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        SAXParser parser = parserFactory.newSAXParser();
        parser.parse(new File(filePath), new InvTweaksItemTreeLoader());
    
        // Tree loaded event
        synchronized (onLoadListeners) {
	        treeLoaded = true;
	        for (InvTweaksItemTreeListener onLoadListener : onLoadListeners) {
	        	onLoadListener.onTreeLoaded(tree);
	        }
		}
        
        return tree;
    }
    
    public synchronized static boolean isValidVersion(String filePath) throws Exception {
		init();
		
		File file = new File(filePath);
		if (file.exists()) {
	        treeVersion = null;
	        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
	        SAXParser parser = parserFactory.newSAXParser();
	        parser.parse(file, new InvTweaksItemTreeLoader());
		    return InvTweaksConst.TREE_VERSION.equals(treeVersion);
		}
		else {
			return false;
		}
	}

	public synchronized static void addOnLoadListener(InvTweaksItemTreeListener listener) {
    	onLoadListeners.add(listener);
    	if (treeLoaded) {
    		// Late event triggering
    		listener.onTreeLoaded(tree);
    	}
    }
    
    public synchronized static boolean removeOnLoadListener(InvTweaksItemTreeListener listener) {
    		return onLoadListeners.remove(listener);
    }


    @Override
    public synchronized void startElement(String uri, String localName,
            String name, Attributes attributes) throws SAXException {

        String rangeMinAttr = attributes.getValue(ATTR_RANGE_MIN);
        String newTreeVersion = attributes.getValue(ATTR_TREE_VERSION);
        
        // Category
        if (attributes.getLength() == 0 || rangeMinAttr != null  || treeVersion == null) {

            // Tree version
            if (treeVersion == null) {
                treeVersion = newTreeVersion;
            }
            
            if (categoryStack.isEmpty()) {
                // Root category
                tree.setRootCategory(new InvTweaksItemTreeCategory(name));
            } else {
                // Normal category
                tree.addCategory(categoryStack.getLast(), new InvTweaksItemTreeCategory(name));
            }

            // Handle item ranges
            if (rangeMinAttr != null) {
                int rangeMin = Integer.parseInt(rangeMinAttr);
                int rangeMax = Integer.parseInt(attributes.getValue(ATTR_RANGE_MAX));
                for (int i = rangeMin; i <= rangeMax; i++) {
                    tree.addItem(name, new InvTweaksItemTreeItem((name + i).toLowerCase(),
                            i, -1, itemOrder++));
                }
            }
            categoryStack.add(name);
        }

        // Item
        else if (attributes.getValue(ATTR_ID) != null) {
            int id = Integer.parseInt(attributes.getValue(ATTR_ID));
            int damage = -1;
            if (attributes.getValue(ATTR_DAMAGE) != null) {
                damage = Integer.parseInt(attributes.getValue(ATTR_DAMAGE));
            }
            tree.addItem(categoryStack.getLast(), new InvTweaksItemTreeItem(name.toLowerCase(),
                    id, damage, itemOrder++));
        }
    }

    @Override
    public synchronized void endElement(String uri, String localName, String name) throws SAXException {
        if (!categoryStack.isEmpty() && name.equals(categoryStack.getLast())) {
            categoryStack.removeLast();
        }
    }

}