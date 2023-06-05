package sarsamurmu.ipt.sections;

import com.formdev.flatlaf.fonts.jetbrains_mono.FlatJetBrainsMonoFont;
import net.miginfocom.swing.MigLayout;
import sarsamurmu.ipt.tasks.LintTask;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;
import java.util.*;

import static java.awt.dnd.DnDConstants.ACTION_COPY;

public class DrawableMaker extends JPanel {
    private final WeakHashMap<String, JTextArea> textAreas = new WeakHashMap<>();
    private final DropTarget dropTarget = new DropTarget() {
        @SuppressWarnings("unchecked")
        @Override
        public synchronized void drop(DropTargetDropEvent e) {
            try {
                e.acceptDrop(ACTION_COPY);

                List<File> droppedFiles = (List<File>) e.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                List<File> dirs = new ArrayList<>();
                List<String> files = new ArrayList<>();

                for (File file : droppedFiles) {
                    if (file.isDirectory()) {
                        dirs.add(file);
                    } else {
                        files.add(file.getName());
                    }
                }

                StringBuilder drawable = new StringBuilder();
                StringBuilder iconPack = new StringBuilder();
                final String header = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n\n";
                final String footer = "</resources>";
                drawable.append(header);
                iconPack.append(header);

                if (files.size() != 0) {
                    Map<String, String> uncategorized = itemStr("UNCATEGORIZED", files);
                    drawable.append(uncategorized.get("drawable"));
                    iconPack.append(uncategorized.get("icon_pack"));
                }

                for (File dir : dirs) {
                    Map<String, String> strs = itemStr(dir.getName(),
                            Arrays.asList(Objects.requireNonNull(dir.list())));
                    drawable.append(strs.get("drawable"));
                    iconPack.append(strs.get("icon_pack"));
                }

                drawable.append(footer);
                iconPack.append(footer);

                for (JTextArea textArea : textAreas.values()) {
                    ((DefaultCaret) textArea.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
                }

                textAreas.get("drawable").setText(drawable.toString());
                textAreas.get("icon_pack").setText(iconPack.toString());

                for (JTextArea textArea : textAreas.values()) {
                    ((DefaultCaret) textArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    public DrawableMaker() {
        super(new MigLayout("fill"), false);

        JTabbedPane tabbedPane = new JTabbedPane();
        String[] itemNames = new String[]{"drawable", "icon_pack"};
        String hint = "\n".repeat(10) + " ".repeat(34) + "Drop files or folders here";

        for (String name : itemNames) {
            JTextArea textArea = new JTextArea(hint) {{
                setFont(new Font(FlatJetBrainsMonoFont.FAMILY, Font.PLAIN, 12));
                if (name.equals("drawable")) setDropTarget(dropTarget);
            }};
            textAreas.put(name, textArea);

            JPanel panel = new JPanel(new MigLayout("fill, ins 0"), false);
            panel.add(new JScrollPane(textArea), "grow");
            tabbedPane.addTab(name + ".xml", null, panel);
        }

        tabbedPane.addChangeListener(e -> textAreas.get(itemNames[tabbedPane.getSelectedIndex()])
                .setDropTarget(dropTarget));

        add(tabbedPane, "grow");
    }

    private Map<String, String> itemStr(String category, List<String> items) {
        Map<String, String> map = new HashMap<>();
        // drawable
        StringBuilder d = new StringBuilder();
        // icon_pack
        StringBuilder i = new StringBuilder();

        if (items.size() != 0) {
            d.append(String.format("  <category title=\"%s\"/>\n", category));
            i.append(String.format("  <string-array name=\"%s\">\n", category));

            for (String item : items) {
                d.append(String.format("  <item drawable=\"%s\"/>\n",
                        item.replaceAll(LintTask.extRegex, "")));
                i.append(String.format("    <item>%s</item>\n",
                        item.replaceAll(LintTask.extRegex, "")));
            }

            i.append("  </string-array>\n");

            d.append("\n");
            i.append("\n");
        }

        map.put("drawable", d.toString());
        map.put("icon_pack", i.toString());

        return map;
    }
}
