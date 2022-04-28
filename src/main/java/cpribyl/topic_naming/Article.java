package cpribyl.topic_naming;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Article
 * Represents an article.
 */
public class Article {
    final String id; // unique id in corpus
    final String name; // article name (possibly not unique)
    final String fulltext;  // Includes text of all paragraphs in the article.
    final Set<String> headings = new HashSet<>(20);
    Map<String, String> paragraphs = new HashMap<>(40);

    private Article(String id, String name, String fulltext, Set<String> headings) {
        this.id = id;
        this.name = name;
        this.fulltext = fulltext;
        this.headings.addAll(headings);
    }

    public static Article fromCarJson(JSONObject json) {
        if (!json.getString("page_type").equals("article")) {
            return null;
        }

        final String name = json.getString("page_name");
        final String id = json.getString("page_id");
        final StringBuilder fulltext = new StringBuilder();
        final Set<String> headings = new TreeSet<>();
        final Map<String, String> paragraphs = new HashMap<>(40);

        final JSONArray content = json.getJSONArray("content");
        Deque<JSONObject> items = new LinkedList<>();

        for (int i = 0; i < content.length(); i++) {
            items.addLast(content.getJSONObject(i));
        }

        // loop over each item in the article, processing each one according to their type.
        String currentHeading = "";
        while (!items.isEmpty()) {
            final JSONObject item = items.poll();
            final String contentType = item.getString("type");
            switch (contentType) {
                case "section" -> {
                    final String heading = item.getString("heading_text");
                    headings.add(heading);
                    currentHeading = heading;
                    final JSONArray subItems = item.getJSONArray("content");
                    for (int i = subItems.length() - 1; i >= 0; i--) {
                        JSONObject nestedItem = subItems.getJSONObject(i);
                        if (nestedItem.getString("type").equals("paragraph")) {
                            items.addFirst(nestedItem);
                        }
                    }
                } case "paragraph" -> {
                    final String paraText = Article.getParagraphText(
                            item.getJSONObject("paragraph").getJSONArray("para_body"));
                    fulltext.append("\n").append(paraText);
                    paragraphs.put(paraText, currentHeading);
                }
            }
        }

        // For our purposes, we only want articles that have at least one heading.
        Article article = new Article(id, name, fulltext.toString(), headings);
        article.paragraphs = paragraphs;
        try {
            assert(article.headings.size() > 0);
            return article;
        } catch (AssertionError err) {
            err.printStackTrace();
            return null;
        }
    }

    public static Article fromTremaJson(JSONObject json) {
        if (!json.getString("page_type").equals("article")) {
            return null;
        }

        final String name = json.getString("page_name");
        final String id = json.getString("page_id");
        final StringBuilder fulltext = new StringBuilder();
        final Set<String> headings = new TreeSet<>();

        final JSONArray content = json.getJSONArray("content");
        Queue<JSONObject> items = new LinkedList<>();

        for (int i = 0; i < content.length(); i++) {
            items.add(content.getJSONObject(i));
        }

        // loop over each item in the article, processing each one according to their type.
        while (!items.isEmpty()) {
            final JSONObject item = items.poll();
            final String contentType = item.getString("type");
            switch (contentType) {
                case "section" -> {
                    final String heading = item.getString("heading_text");
                    // heading text may be included in the fulltext
                    if (HyperParameters.fulltextIncludesHeadings) {
                        fulltext.append("\n").append(heading);
                    }
                    headings.add(heading);
                    // we process subsections in the article breadth-first,
                    // which may change the paragraph ordering.
                    // fixme: make the fulltext reflect the proper paragraph ordering.
                    JSONArray subItems = item.getJSONArray("content");
                    for (int i = 0; i < subItems.length(); i++) {
                        items.add(subItems.getJSONObject(i));
                    }
                }
                case "paragraph" -> {
                    final String paraText = Article.getParagraphText(
                            item.getJSONObject("paragraph").getJSONArray("para_body"));
                    fulltext.append("\n").append(paraText);
                }
            }
        }

        // For our purposes, we only want articles that have at least one heading.
        Article article = new Article(id, name, fulltext.toString(), headings);
        try {
            assert(article.headings.size() > 0);
            return article;
        } catch (AssertionError err) {
            err.printStackTrace();
            return null;
        }
    }

    public static String getParagraphText(JSONArray jsonArray) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject segment = jsonArray.getJSONObject(i);
            text.append(segment.getString("text"));
        }
        return text.toString();
    }

    /**
     * Obtains the paragraph id of a paragraph
     * @param paragraph - A JsonArray representation of the pragraph
     * @return
     */
    public static String getParagraphId(JSONArray paragraph) {
        return "";
    }

    public String getFulltext() {
        return fulltext;
    }

    public String getPageId() {
        return id;
    }

    /**
     * Retrieve a string representation of this document's set of headings.
     * @return each heading in the article, separated by commas
     */
    public String getSectionsAsString() {
        StringBuilder sb = new StringBuilder();
        for (String heading: headings) {
            sb.append(heading).append(",");
        }
        if (sb.length() > 0) {
            sb.delete(sb.length()-1, sb.length());
        }
        return sb.toString();
    }

    public Set<String> getSections() {
        return headings;
    }

    /**
     * A map containing a map of {paragraph fulltext -> section heading} for the article
     * @return {paragraph fulltext -> section heading} for every paragraph in the article.
     * Paragraphs before the first heading have the empty string as their heading.
     */
    public Map<String, String> getParagraphs() {
        return paragraphs;
    }

}
