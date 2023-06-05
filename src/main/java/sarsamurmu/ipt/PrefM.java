package sarsamurmu.ipt;

import sarsamurmu.ipt.tasks.LintTask;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class PrefM {
    private static final Preferences rootPrefs = Preferences.userRoot().node("sarsamurmu.ipt");
    private static final Preferences prefs = rootPrefs.node("main");
    private static final Map<String, PresetPref> presetPrefMap = new HashMap<>();
    private static String currentPreset = getCurrentPreset();

    public static PresetPref get() {
        if (presetPrefMap.containsKey(currentPreset)) {
            return presetPrefMap.get(currentPreset);
        } else {
            PresetPref presetPref = new PresetPref(currentPreset);
            presetPrefMap.put(currentPreset, presetPref);
            return presetPref;
        }
    }

    public static final String DEFAULT_PRESET = "Default";

    private static final String
            KEY_PRESETS = "presets",
            KEY_CURRENT_PRESET = "current_preset",
            KEY_WINDOW_POSITION = "window_position",
            KEY_WINDOW_SIZE = "window_size",
            KEY_WINDOW_STATE = "window_state";

    public static String[] getPresets() {
        return prefs.get(KEY_PRESETS, DEFAULT_PRESET).split(",");
    }

    public static void addPreset(String name) {
        prefs.put(KEY_PRESETS, prefs.get(KEY_PRESETS, DEFAULT_PRESET) + "," + name);
    }

    public static void deletePreset(String name) {
        List<String> presetsList = new ArrayList<>(Arrays.asList(getPresets()));
        presetsList.remove(name);
        presetPrefMap.get(name).remove();
        presetPrefMap.remove(name);
        prefs.put(KEY_PRESETS, String.join(",", presetsList));
    }

    public static void setCurrentPreset(String name) {
        currentPreset = name;
        prefs.put(KEY_CURRENT_PRESET, name);
    }

    public static String getCurrentPreset() {
        return currentPreset = prefs.get(KEY_CURRENT_PRESET, DEFAULT_PRESET);
    }

    public static void setWindowLocation(Point point) {
        prefs.put(KEY_WINDOW_POSITION, point.x + "," + point.y);
    }

    public static Point getWindowLocation() {
        String[] p = prefs.get(KEY_WINDOW_POSITION, "0,0").split(",");
        return new Point(Integer.parseInt(p[0]), Integer.parseInt(p[1]));
    }

    public static void setWindowSize(Dimension dim) {
        prefs.put(KEY_WINDOW_SIZE, dim.width + "," + dim.height);
    }

    public static Dimension getWindowSize() {
        String[] p = prefs.get(KEY_WINDOW_SIZE, "700,600").split(",");
        return new Dimension(Integer.parseInt(p[0]), Integer.parseInt(p[1]));
    }

    public static void setWindowState(int state) {
        prefs.putInt(KEY_WINDOW_STATE, state);
    }

    public static int getWindowState() {
        return prefs.getInt(KEY_WINDOW_STATE, JFrame.NORMAL);
    }

    // Includes all the preferences that depends on the preset
    public static class PresetPref {
        private final Preferences prefs;

        private static final String
                KEY_DRAWABLE_PATH = "drawable_path",
                KEY_APPFILTER_PATH = "appfilter_path",
                KEY_ICONS_PATH = "icons_path";

        PresetPref(String presetName) {
            prefs = rootPrefs.node("preset_" + presetName);
        }

        public void remove() {
            try {
                prefs.removeNode();
            } catch (BackingStoreException e) {
                throw new RuntimeException(e);
            }
        }

        public String getDrawablePath() {
            return prefs.get(KEY_DRAWABLE_PATH, "");
        }

        public void setDrawablePath(String path) {
            prefs.put(KEY_DRAWABLE_PATH, path);
        }

        public String getAppfilterPath() {
            return prefs.get(KEY_APPFILTER_PATH, "");
        }

        public void setAppfilterPath(String path) {
            prefs.put(KEY_APPFILTER_PATH, path);
        }

        public String getIconsPath() {
            return prefs.get(KEY_ICONS_PATH, "");
        }

        public void setIconsPath(String path) {
            prefs.put(KEY_ICONS_PATH, path);
        }

        public boolean isLintSettingEnabled(LintTask.ResultType resultType) {
            return prefs.getBoolean("lint_settings_" + resultType.name().toLowerCase(), true);
        }

        public void setLintSetting(LintTask.ResultType resultType, boolean value) {
            prefs.putBoolean("lint_settings_" + resultType.name().toLowerCase(), value);
        }

        public String getExclusions(LintTask.ResultType resultType) {
            return prefs.get("lint_exclusion_" + resultType.name().toLowerCase(), "");
        }

        public void setExclusions(LintTask.ResultType resultType, String value) {
            prefs.put("lint_exclusion_" + resultType.name().toLowerCase(), value.trim());
        }
    }
}
