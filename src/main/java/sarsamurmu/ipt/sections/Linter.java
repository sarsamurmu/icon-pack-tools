package sarsamurmu.ipt.sections;

import com.formdev.flatlaf.fonts.jetbrains_mono.FlatJetBrainsMonoFont;
import net.miginfocom.swing.MigLayout;
import sarsamurmu.ipt.Main;
import sarsamurmu.ipt.PrefM;
import sarsamurmu.ipt.tasks.LintTask;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

import static sarsamurmu.ipt.Utils.*;

public class Linter extends JPanel {
    private JButton drawableChooseBtn;
    private JTextField drawableTextField;
    private JButton appfilterChooseBtn;
    private JTextField appfilterTextField;
    private JButton iconsChooseBtn;
    private JTextField iconsTextField;
    private JButton newPresetBtn;
    private JButton deletePresetBtn;
    private JButton settingsBtn;
    private JComboBox<String> presetsComboBox;
    private JButton runBtn;
    private JTextArea resultTextArea;

    public Linter() {
        super(new MigLayout("fill, wrap 3", "[][][grow]", "[][][][][grow]"));

        add(new JLabel("Appfilter File: "));
        add(appfilterChooseBtn = new JButton("Choose"));
        add(appfilterTextField = new JTextField(), "growx");
        add(new JLabel("Drawable File: "));
        add(drawableChooseBtn = new JButton("Choose"));
        add(drawableTextField = new JTextField(), "growx");
        add(new JLabel("Icons directory: "));
        add(iconsChooseBtn = new JButton("Choose"));
        add(iconsTextField = new JTextField(), "growx");
        add(new JPanel(new MigLayout("insets 0, fill", "[][][][][grow][]")) {{
            add(new JLabel("Preset: "));
            add(presetsComboBox = new JComboBox<>(), "width 200!");
            add(newPresetBtn = new JButton("New"));
            add(deletePresetBtn = new JButton("Delete"));
            add(settingsBtn = new JButton("Settings"));
            add(new JPanel(), "growx");
            add(runBtn = new JButton("Run Lint"));
        }}, "wrap, span 3, grow 100");

        add(new JScrollPane(resultTextArea = new JTextArea() {{
            setFont(new Font(FlatJetBrainsMonoFont.FAMILY, Font.PLAIN, 12));
        }}), "span 3, grow");

        init();
    }

    private void updateFields() {
        drawableTextField.setText(PrefM.get().getDrawablePath());
        appfilterTextField.setText(PrefM.get().getAppfilterPath());
        iconsTextField.setText(PrefM.get().getIconsPath());
        deletePresetBtn.setEnabled(!PrefM.getCurrentPreset().contentEquals(PrefM.DEFAULT_PRESET));
        presetsComboBox.setSelectedItem(PrefM.getCurrentPreset());
    }

    private void init() {
        for (String preset : PrefM.getPresets()) {
            presetsComboBox.addItem(preset);
        }
        updateFields();

        appfilterChooseBtn.addMouseListener(onClick(() -> {
            String prevPath = PrefM.get().getAppfilterPath();
            File prevFile = null;
            if (prevPath != null) {
                prevFile = new File(prevPath);
                if (!prevFile.exists()) prevFile = null;
            }
            String xmlPath = chooseXMLFile(prevFile);
            if (xmlPath != null) {
                PrefM.get().setAppfilterPath(xmlPath);
                appfilterTextField.setText(xmlPath);
            }
        }));

        Debouncer appfilterTextUpdater = new Debouncer(() -> PrefM.get().setAppfilterPath(appfilterTextField.getText()), 1000);
        appfilterTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                appfilterTextUpdater.call();
            }
        });

        drawableChooseBtn.addMouseListener(onClick(() -> {
            String prevPath = PrefM.get().getDrawablePath();
            File prevFile = null;
            if (prevPath != null) {
                prevFile = new File(prevPath);
                if (!prevFile.exists()) prevFile = null;
            }
            String xmlPath = chooseXMLFile(prevFile);
            if (xmlPath != null) {
                PrefM.get().setDrawablePath(xmlPath);
                drawableTextField.setText(xmlPath);
            }
        }));

        Debouncer drawableTextUpdater = new Debouncer(() -> PrefM.get().setDrawablePath(drawableTextField.getText()), 1000);
        drawableTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                drawableTextUpdater.call();
            }
        });

        iconsChooseBtn.addMouseListener(onClick(() -> {
            String prevPath = PrefM.get().getIconsPath();
            if (prevPath != null) {
                if (!new File(prevPath).exists()) prevPath = null;
            }
            String dir = chooseDirectory(prevPath);
            if (dir != null) {
                PrefM.get().setIconsPath(dir);
                iconsTextField.setText(dir);
            }
        }));

        Debouncer iconsTextUpdater = new Debouncer(() -> PrefM.get().setIconsPath(iconsTextField.getText()), 1000);
        iconsTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                iconsTextUpdater.call();
            }
        });

        presetsComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String current = (String) e.getItem();
                deletePresetBtn.setEnabled(!current.contentEquals(PrefM.DEFAULT_PRESET));
                PrefM.setCurrentPreset(current);
                updateFields();
            }
        });

        newPresetBtn.addMouseListener(onClick(() -> {
            String result;

            while (true) {
                result = (String) JOptionPane.showInputDialog(
                        Main.frame, "Enter new preset's name", "New Preset",
                        JOptionPane.PLAIN_MESSAGE, null, null, null);

                if (result == null) return;

                boolean duplicated = false;
                for (String preset : PrefM.getPresets()) {
                    if (preset.contentEquals(result)) {
                        JOptionPane.showMessageDialog(Main.frame,
                                String.format("Preset with name \"%s\" already exists. Please enter a different name.", result));
                        duplicated = true;
                        break;
                    }
                }
                if (!duplicated) break;
            }

            PrefM.addPreset(result);
            presetsComboBox.addItem(result);
            presetsComboBox.setSelectedItem(result);
        }));

        deletePresetBtn.addMouseListener(onClick(() -> {
            if (!deletePresetBtn.isEnabled()) return;
            String current = (String) presetsComboBox.getSelectedItem();
            int n = JOptionPane.showOptionDialog(Main.frame,
                    String.format("You're about to delete preset - \"%s\". Are you sure?", current), "Delete Preset",
                    JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
            if (n == 0) {
                PrefM.deletePreset(current);
                presetsComboBox.removeItem(current);
            }
        }));

        settingsBtn.addMouseListener(onClick(this::showSettingsDialog));

        runBtn.addMouseListener(onClick(() -> {
            DefaultCaret caret = (DefaultCaret) resultTextArea.getCaret();
            caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

            LintTask task = new LintTask(
                    appfilterTextField.getText(),
                    drawableTextField.getText(),
                    iconsTextField.getText()
            );

            for (LintTask.ResultType resultType : LintTask.ResultType.values()) {
                task.resultMode.set(resultType, PrefM.get().isLintSettingEnabled(resultType));
                task.exclusions.put(resultType, PrefM.get().getExclusions(resultType));
            }

            resultTextArea.setText(task.run());
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        }));
    }

    private void showSettingsDialog() {
        JDialog dialog = makeDialog("Preset settings", new Dimension(600, 400));

        JPanel panel = new JPanel(new MigLayout("wrap 1", "[grow]", "[][grow][]")) {{
            add(new JLabel("Checks to Run"));
            add(new JPanel(new MigLayout("wrap 2", "[grow][]")) {{
                for (LintTask.ResultType resultType : LintTask.ResultType.values()) {
                    add(new JCheckBox(resultType.heading, PrefM.get().isLintSettingEnabled(resultType)) {{
                        addItemListener(e -> PrefM.get().setLintSetting(resultType, e.getStateChange() == ItemEvent.SELECTED));
                    }}, "growx");

                    add(new JButton("Manage exclusions") {{
                        addMouseListener(onClick(() -> showManageExclusionDialog(resultType)));
                    }});
                }
            }}, "grow");
            add(new JPanel(new MigLayout("", "[grow][]")) {{
                add(new JPanel(), "growx");
                add(new JButton("OK") {{
                    addMouseListener(onClick(() -> dialog.setVisible(false)));
                }});
            }}, "growx");
        }};

        dialog.setResizable(false);
        dialog.setContentPane(panel);

        dialog.setVisible(true);
    }

    private void showManageExclusionDialog(LintTask.ResultType resultType) {
        JDialog dialog = makeDialog("Manage exclusions", new Dimension(400, 400));
        final JTextArea[] textArea = new JTextArea[1];
        dialog.setContentPane(new JPanel(new MigLayout("wrap 1", "[grow]", "[|grow|]")) {{
            add(new JLabel("<html>Separate elements by newline. Regular expressions are supported.</html"));

            add(new JScrollPane(textArea[0] = new JTextArea() {{
                setText(PrefM.get().getExclusions(resultType));
                setFont(new Font(FlatJetBrainsMonoFont.FAMILY, Font.PLAIN, 12));
            }}), "grow");

            add(new JPanel(new MigLayout("", "[grow][]")) {{
                add(new JPanel(), "growx");
                add(new JButton("OK") {{
                    addMouseListener(onClick(() -> {
                        PrefM.get().setExclusions(resultType, textArea[0].getText());
                        dialog.setVisible(false);
                    }));
                }});
            }}, "growx");
        }});

        dialog.setMinimumSize(new Dimension(300, 300));
        dialog.setVisible(true);
    }
}
