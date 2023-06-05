package sarsamurmu.ipt.tasks;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

public class LintTask {
    public String appfilterPath;
    public String drawablePath;
    public String iconFilesDir;
    public ResultMode resultMode = new ResultMode();
    public HashMap<ResultType, String> exclusions = new HashMap<>();

    public static String extRegex = "\\.(png|webp|xml)$";

    private final HashSet<String> components = new HashSet<>();
    private final HashSet<String> calendarComponents = new HashSet<>();
    private final HashSet<String> appfilterDrawables = new HashSet<>();
    private final HashSet<String> drawables = new HashSet<>();
    private final HashSet<String> calendarPrefixes = new HashSet<>();
    private final HashSet<String> icons = new HashSet<>();
    private final HashMap<ResultType, HashSet<String>> resultItems = new HashMap<>();

    public LintTask(String appfilterPath, String drawablePath, String iconsPath) {
        this.appfilterPath = appfilterPath;
        this.drawablePath = drawablePath;
        this.iconFilesDir = iconsPath;
    }

    private void putIf(ResultType resultType, HashSet<String> value) {
        if (resultMode.get(resultType)) resultItems.put(resultType, value);
    }

    private Document loadDocument(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) return null;

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void loadIcons() {
        File dir = new File(iconFilesDir);
        if (!dir.exists()) return;
        for (String name : dir.list()) {
            icons.add(name.replaceAll(extRegex, ""));
        }
    }

    private void loadDrawable() {
        Document doc = loadDocument(drawablePath);
        if (doc == null) return;

        HashSet<String> dupDrawables = new HashSet<>();
        HashSet<String> inDrawablesNotInFiles = new HashSet<>();

        NodeList list = doc.getElementsByTagName("item");

        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String value = ((Element) node).getAttribute("drawable");
                if (drawables.contains(value)) {
                    dupDrawables.add(value);
                } else {
                    drawables.add(value);
                    if (!icons.contains(value)) {
                        inDrawablesNotInFiles.add(value);
                    }
                }
            }
        }

        putIf(ResultType.DuplicatedDrawable, dupDrawables);
        putIf(ResultType.InDrawableNotInFiles, inDrawablesNotInFiles);
    }

    private String cleanComponentInfo(String str) {
        return str.substring(14, str.length() - 1);
    }

    private void loadAppfilter() {
        Document doc = loadDocument(appfilterPath);
        if (doc == null) return;

        HashSet<String> dupComponents = new HashSet<>();
        HashSet<String> inAppfilterNotInFiles = new HashSet<>();
        HashSet<String> inAppfilterNotInDrawables = new HashSet<>();

        NodeList itemList = doc.getElementsByTagName("item");
        for (int i = 0; i < itemList.getLength(); i++) {
            Node node = itemList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) node;
                String component = el.getAttribute("component");
                String drawable = el.getAttribute("drawable");
                if (components.contains(component)) {
                    dupComponents.add(cleanComponentInfo(component));
                } else {
                    components.add(component);
                }
                appfilterDrawables.add(drawable);
                if (!icons.contains(drawable)) {
                    inAppfilterNotInFiles.add(drawable);
                }
                if (!drawables.contains(drawable)) {
                    inAppfilterNotInDrawables.add(drawable);
                }
            }
        }

        putIf(ResultType.DuplicatedComponents, dupComponents);
        putIf(ResultType.InAppfilterNotInDrawable, inAppfilterNotInDrawables);
        putIf(ResultType.InAppfilterNotInFiles, inAppfilterNotInFiles);

        HashSet<String> dupCalendarComponents = new HashSet<>();

        NodeList calendarList = doc.getElementsByTagName("calendar");
        for (int i = 0; i < calendarList.getLength(); i++) {
            Node node = calendarList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) node;
                String component = el.getAttribute("component");
                if (calendarComponents.contains(component)) {
                    dupCalendarComponents.add(cleanComponentInfo(component));
                } else {
                    calendarComponents.add(component);
                }
                calendarPrefixes.add(el.getAttribute("prefix"));
            }
        }

        putIf(ResultType.DuplicatedCalendarComponents, dupCalendarComponents);
    }

    public String run() {
        loadIcons();
        loadDrawable();
        loadAppfilter();

        HashSet<String> notMappedDrawables = new HashSet<>();
        HashSet<String> inFilesNotInDrawable = new HashSet<>();

        for (String drawable : drawables) {
            if (!appfilterDrawables.contains(drawable)) {
                String prefix = drawable.replaceAll("\\d+$", "");
                if (!calendarPrefixes.contains(prefix)) {
                    notMappedDrawables.add(drawable);
                }
            }
        }

        for (String icon : icons) {
            if (!drawables.contains(icon)) {
                inFilesNotInDrawable.add(icon);
            }
        }

        putIf(ResultType.NotMappedDrawable, notMappedDrawables);
        putIf(ResultType.InFilesNotInDrawable, inFilesNotInDrawable);

        StringBuilder sb = new StringBuilder();
        for (ResultType resultType : ResultType.values()) {
            if (!resultItems.containsKey(resultType)) continue;
            HashSet<String> items = filterResult(resultType, resultItems.get(resultType));
            if (items.size() == 0) continue;

            String ban = strRepeat("#", resultType.heading.length() + 7 * 2) + "\n";
            sb.append(ban);
            sb.append("###### ").append(resultType.heading).append(" ######\n");
            sb.append(ban).append("\n");
            List<String> sortedList = new ArrayList<>(items);
            sortedList.sort(Comparator.naturalOrder());
            for (String item : sortedList) {
                sb.append(item).append("\n");
            }
            sb.append("\n\n");
        }

        return sb.toString();
    }

    private HashSet<String> filterResult(ResultType resultType, HashSet<String> items) {
        String exclusion = exclusions.getOrDefault(resultType, "");
        if (exclusion.contentEquals("")) return items;

        HashSet<String> filtered = new HashSet<>();
        String[] regexes = exclusion.split("\n");
        for (String item : items) {
            boolean exclude = false;
            for (String regex : regexes) {
                exclude = exclude || item.matches(regex);
            }
            if (!exclude) filtered.add(item);
        }
        return filtered;
    }

    private String strRepeat(String str, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    private void log(String str) {
        System.out.println(str);
    }

    public enum ResultType {
        DuplicatedDrawable
                ("Drawables which are duplicated in drawable.xml"),
        DuplicatedComponents
                ("Components which are duplicated in appfilter.xml"),
        DuplicatedCalendarComponents
                ("Calendar components which are duplicated in appfilter.xml"),
        InAppfilterNotInFiles
                ("Icons which are in appfilter.xml but image file is missing"),
        InAppfilterNotInDrawable
                ("Icons which are in appfilter.xml but not in drawable.xml"),
        InDrawableNotInFiles
                ("Icons which are in drawable.xml but image file is missing"),
        NotMappedDrawable
                ("Icons which are in drawable.xml but not mapped in appfilter.xml"),
        InFilesNotInDrawable
                ("Image files which are not included in drawable.xml");

        public final String heading;

        ResultType(String heading) {
            this.heading = heading;
        }
    }

    public class ResultMode {
        private final HashMap<ResultType, Boolean> mode = new HashMap<>();

        public ResultMode set(ResultType resultType, Boolean value) {
            mode.put(resultType, value);
            return this;
        }

        public Boolean get(ResultType resultType) {
            return mode.getOrDefault(resultType, true);
        }
    }
}
