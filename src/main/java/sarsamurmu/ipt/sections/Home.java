package sarsamurmu.ipt.sections;

import net.miginfocom.layout.AC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import sarsamurmu.ipt.Main;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Home extends JPanel {
    private int column = 2;
    private int currentColumn;

    public Home() {
        super(new MigLayout("fill"));

        createPanel();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension dim = Home.this.getSize();
                if (dim.width < 950) {
                    column = 2;
                } else {
                    column = 3;
                }
                if (currentColumn != column) {
                    Home.this.remove(0);
                    createPanel();
                }
            }
        });
    }

    private void createPanel() {
        currentColumn = column;
        JPanel panel = new JPanel(new MigLayout(new LC().fillX().wrapAfter(column), new AC().grow())) {{
            add(new HomeItem(
                    "Linter",
                    "Find all problems in your icon pack",
                    () -> Main.setSection("Linter")), "growx");
            add(new HomeItem(
                    "Drawable Maker",
                    "Make drawable.xml from icon files",
                    () -> Main.setSection("DrawableMaker")), "growx");
            /*add(new HomeItem(
                    "Card 3",
                    "Card 3 desc",
                    () -> {}), "growx");
            add(new HomeItem(
                    "Card 4",
                    "Card 4 desc",
                    () -> {}), "growx");
            add(new HomeItem(
                    "Card 5",
                    "Card 5 desc",
                    () -> {}), "growx");*/
        }};

        add(panel, "grow");
    }

    private static class HomeItem extends JPanel {
        private final Color outline = new Color(199, 199, 199);
        private final Color hoverOutline = new Color(0, 158, 255, 255);

        private Border getBorder(Color color) {
            return new LineBorder(color, 1);
        }

        public HomeItem(String titleText, String subtitleText, Runnable onClick) {
            super(new MigLayout("wrap, fillx, ins 20"));
            setBorder(getBorder(outline));
            setMaximumSize(new Dimension(100000, 300));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onClick.run();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    setBorder(getBorder(hoverOutline));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBorder(getBorder(outline));
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }
            });

            add(new JLabel(titleText) {{
                setFont(getFont().deriveFont(20F));
            }});
            add(new JLabel(subtitleText));
        }
    }
}
