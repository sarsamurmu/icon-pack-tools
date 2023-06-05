package sarsamurmu.ipt;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.fonts.inter.FlatInterFont;
import com.formdev.flatlaf.fonts.jetbrains_mono.FlatJetBrainsMonoFont;
import net.miginfocom.swing.MigLayout;
import sarsamurmu.ipt.sections.DrawableMaker;
import sarsamurmu.ipt.sections.Home;
import sarsamurmu.ipt.sections.Linter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import static sarsamurmu.ipt.Utils.*;

public class Main {
    public static JFrame frame;
    public static JPanel card;

    public static void setSection(String id) {
        if (card != null) {
            ((CardLayout) card.getLayout()).show(card, id);
        }
    }

    public static void main(String[] args) throws IOException {
        FlatJetBrainsMonoFont.installLazy();
        FlatInterFont.installLazy();

        FlatLaf.setPreferredFontFamily(FlatInterFont.FAMILY);

        //System.setProperty("awt.useSystemAAFontSettings", "on");
        //System.setProperty("swing.aatext", "on");
        try {
            //Font font = Font.createFont(Font.TRUETYPE_FONT, loadResource("Sora-Regular.ttf"));
            //GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            //FlatLaf.setPreferredFontFamily(font.getFontName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        FlatLightLaf.setup();

        UIManager.put("CheckBox.iconTextGap", 8);

        card = new JPanel(new CardLayout());
        card.add(new Home(), "Home");
        card.add(new Linter(), "Linter");
        card.add(new DrawableMaker(), "DrawableMaker");

        frame = new JFrame("Icon Pack Tools");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(700, 600));
        frame.setContentPane(card);
        frame.setJMenuBar(createMenuBar());
        frame.setLocation(PrefM.getWindowLocation());
        frame.setSize(PrefM.getWindowSize());
        frame.setExtendedState(PrefM.getWindowState());

        ArrayList<Image> iconImages = new ArrayList<>();
        for (int size : new int[]{16, 20, 24, 28, 32, 40, 48, 56, 64, 72, 80, 96, 128, 256}) {
            iconImages.add(ImageIO.read(loadResource("icons/icon_" + size + ".png")));
        }

        frame.setIconImages(iconImages);

        Utils.Debouncer onMove = new Utils.Debouncer(() -> PrefM.setWindowLocation(frame.getLocation()), 1000);
        Utils.Debouncer onResize = new Utils.Debouncer(() -> PrefM.setWindowSize(frame.getSize()), 1000);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                onMove.call();
            }

            @Override
            public void componentResized(ComponentEvent e) {
                onResize.call();
            }
        });
        frame.addWindowStateListener(e -> PrefM.setWindowState(e.getNewState()));

        //setSection("Linter");

        frame.setVisible(true);
    }

    private static JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(new JMenu("File") {{
            setMnemonic(KeyEvent.VK_F);
            /*getAccessibleContext().setAccessibleDescription(
                "The only menu in this program that has menu items");*/

            add(new JMenuItem("Home") {{
                setAccelerator(KeyStroke.getKeyStroke(
                        KeyEvent.VK_1, InputEvent.ALT_DOWN_MASK));
                addActionListener(e -> setSection("Home"));
            }});
        }});

        menuBar.add(new JMenu("Help") {{
            setMnemonic(KeyEvent.VK_H);

            add(new JMenuItem("About") {{
                addActionListener(e -> showAboutDialog());
            }});
        }});

        return menuBar;
    }

    private static void showAboutDialog() {
        JDialog dialog = makeDialog("About", new Dimension(450, 220));
        JPanel panel = new JPanel(new MigLayout("wrap 1", "[grow][]", "[grow][]")) {{
            add(new JPanel(new MigLayout()) {{
                add(new JLabel() {{
                    try {
                        setIcon(new ImageIcon(ImageIO.read(loadResource("icons/icon_64.png"))));
                    } catch (Exception ignored) {
                    }
                }}, "x 8, y 16");
                add(new JPanel(new MigLayout("wrap 1")) {{
                    add(new JLabel("Icon Pack Tools") {{
                        setFont(getFont().deriveFont(20F));
                        //setFont(new Font("Sora", Font.PLAIN, 20));
                    }});
                    add(new JLabel("v" + BuildConfig.APP_VERSION));
                    add(new JLabel("Copyright \u00A9 2023 Sarsa Murmu"));
                    add(new JPanel(new MigLayout("ins 0, gap 0")) {{
                        final String link = "https://github.com/sarsamurmu/icon-pack-tools";

                        add(new JLabel("GitHub - "));
                        add(new JLabel(link) {{
                            setForeground(new Color(0, 113, 225));
                            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                            addMouseListener(onClick(() -> {
                                try {
                                    Desktop.getDesktop().browse(URI.create(link));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }));
                        }});
                    }});
                }}, "grow");
            }}, "grow");
            add(new JPanel(new MigLayout("", "[grow][]")) {{
                add(new JPanel(), "growx");
                add(new JButton("Close") {{
                    addMouseListener(onClick(() -> dialog.setVisible(false)));
                }});
            }}, "growx");
        }};

        dialog.setResizable(false);
        dialog.setContentPane(panel);

        dialog.setVisible(true);
    }
}