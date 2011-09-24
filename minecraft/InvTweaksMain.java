import java.awt.Container;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;


public class InvTweaksMain {
    
    private final static int WIDTH = 800;
    private final static int HEIGHT = 480;
    private final static String[] LANGUAGES = 
        new String[]{
            "de:Deutsch",
            "en:English",
            "fr:Français",
        };
    private final static String DEFAULT_LANGUAGE = "en";
    
    private final static JTextArea readmeTextArea = new JTextArea();

    public static void main(String[] args) throws IOException {
        showReadmeWindow();
    }
    
    private static void showReadmeWindow() {
        
        // Init
        Rectangle windowBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Too bad, we stick with the Java look and feel.
        }
        
        
        
        // Readme textarea creation
        readmeTextArea.setEditable(false);
        readmeTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane readmePane = new JScrollPane(readmeTextArea, 
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // Fill textarea
        loadReadme(readmeTextArea, null);
        
        

        // Menu bar
        JMenu menuFile = new JMenu("File");
        JMenuItem menuFileExit = new JMenuItem("Exit");
        menuFileExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                System.exit(0);
            }
        });
        menuFile.add(menuFileExit);

        JMenu menuLanguage = new JMenu("Language");
        for (String language : LANGUAGES) {
            String[] languageInfo = language.split(":");
            JMenuItem languageItem = new JMenuItem(languageInfo[1]);
            languageItem.setName(languageInfo[0]);
            languageItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JMenuItem menuItem = (JMenuItem) e.getSource();
                    loadReadme(readmeTextArea, menuItem.getName());
                }
            });
            menuLanguage.add(languageItem);
        }
        
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menuFile);
        menuBar.add(menuLanguage);
        
        
        
        // Frame creation
        JFrame frame = new JFrame();
        frame.setBounds((int) (windowBounds.getCenterX()-WIDTH/2),
                (int) (windowBounds.getCenterY()-HEIGHT/2), WIDTH, HEIGHT);
        frame.setTitle("Inventory Tweaks "+InvTweaksConst.MOD_VERSION+" - Readme");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setJMenuBar(menuBar);
        
        // Frame components layout
        Container pane = frame.getContentPane();
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
        pane.add(readmePane);
        
        
        
        // Show
        frame.pack();
        frame.setVisible(true);
        
    }

    private static void loadReadme(JTextArea textArea, String language) {

        if (language != null && language.equals(DEFAULT_LANGUAGE)) {
            language = null;
        }
        
        InputStream is = null;
        
        try {
            is = ClassLoader.getSystemResourceAsStream(
                    "doc/README"+((language != null) ? "-"+language : "")+".txt");
            if (is != null) {
                textArea.setText("");
                String line = "";
                int i;
                while ((i = is.read()) != -1) {
                    line += (char) i;
                    if ((char) i == '\n') {
                        textArea.append(line);
                        line = "";
                    }
                }
                textArea.setCaretPosition(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            textArea.append("ERROR: Failed to load Readme, sorry about that.\nBut you can still browse to: http://wan.ka.free.fr/?invtweaks");
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // Do nothing
                }
            }
        }
        
    }

}
