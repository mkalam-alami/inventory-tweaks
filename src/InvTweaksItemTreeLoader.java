


import java.io.File;
import java.util.LinkedList;

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

    private InvTweaksItemTree tree;

    private String treeVersion = null;
    private int itemOrder = 0;
    private LinkedList<String> categoryStack = new LinkedList<String>();

    public InvTweaksItemTreeLoader() {
        tree = new InvTweaksItemTree();
    }

    public boolean isValidVersion(String filePath) throws Exception {
        synchronized (this) {
            treeVersion = null;
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            SAXParser parser = parserFactory.newSAXParser();
            parser.parse(new File(filePath), this);
        }
        return InvTweaksConst.TREE_VERSION.equals(treeVersion);
    }
    
    public InvTweaksItemTree load(String filePath) throws Exception {
        synchronized (this) {
            categoryStack.clear();
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            SAXParser parser = parserFactory.newSAXParser();
            parser.parse(new File(filePath), this);
            if (!categoryStack.isEmpty()) {
                InvTweaks.logInGameStatic("Warning: The tree file seems to be broken "
                        + "(is '" + categoryStack.getLast() + "' closed correctly?)");
            }
        }
        return tree;
    }

    @Override
    public void startElement(String uri, String localName,
            String name, Attributes attributes) throws SAXException {

        String rangeMinAttr = attributes.getValue(ATTR_RANGE_MIN);
        String treeVersion = attributes.getValue(ATTR_TREE_VERSION);
        
        // Category
        if (attributes.getLength() == 0 || rangeMinAttr != null  || treeVersion != null) {

            // Tree version
            if (treeVersion != null) {
                this.treeVersion = treeVersion;
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
        else {
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
    public void endElement(String uri, String localName, String name) throws SAXException {
        if (name.equals(categoryStack.getLast())) {
            categoryStack.removeLast();
        }
    }

}