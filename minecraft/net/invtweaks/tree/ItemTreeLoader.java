package net.invtweaks.tree;

import java.io.File;
import java.util.LinkedList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.minecraft.src.InvTweaks;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ItemTreeLoader extends DefaultHandler {

    private final static String ATTR_RANGE_MIN = "min";
    private final static String ATTR_RANGE_MAX = "max";
    private final static String ATTR_ID = "id";
    private final static String ATTR_DAMAGE = "damage";

    private ItemTree tree;

    private int itemOrder = 0;
    private LinkedList<String> categoryStack = new LinkedList<String>();

    public ItemTreeLoader() {
        tree = new ItemTree();
    }

    public ItemTree load(String file) throws Exception {
        tree.reset();
        categoryStack.clear();
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        SAXParser parser = parserFactory.newSAXParser();
        parser.parse(new File(file), this);
        if (!categoryStack.isEmpty()) {
            InvTweaks.logInGameStatic("Warning: The tree file seems to be broken "
                    + "(is '" + categoryStack.getLast() + "' closed correctly?)");
        }
        return tree;
    }

    @Override
    public void startElement(String uri, String localName,
            String name, Attributes attributes) throws SAXException {

        String rangeMinAttr = attributes.getValue(ATTR_RANGE_MIN);

        // Category
        if (attributes.getLength() == 0 || rangeMinAttr != null) {

            if (categoryStack.isEmpty()) {
                // Root category
                tree.setRootCategory(new ItemTreeCategory(name));
            } else {
                // Normal category
                tree.addCategory(categoryStack.getLast(), new ItemTreeCategory(name));
            }

            // Handle item ranges
            if (rangeMinAttr != null) {
                int rangeMin = Integer.parseInt(rangeMinAttr);
                int rangeMax = Integer.parseInt(attributes.getValue(ATTR_RANGE_MAX));
                for (int i = rangeMin; i <= rangeMax; i++) {
                    tree.addItem(name, new ItemTreeItem((name + i).toLowerCase(),
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
            tree.addItem(categoryStack.getLast(), new ItemTreeItem(name.toLowerCase(),
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