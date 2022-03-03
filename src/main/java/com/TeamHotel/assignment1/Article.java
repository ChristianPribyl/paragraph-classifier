package com.TeamHotel.assignment1;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Article
 * Represents a wikipedia article.
 * @ThreadSafe
 */
public class Article {
    final String id; // trec id
    final String name; // we use article names in the query
    final String fulltext;  // Includes text of all paragraphs in the article.
                            // A Hyperparameter determines if we include the heading text.
                            // infoboxes and lists are not included.
    final Set<String> headings;

    private Article(String id, String name, String fulltext, Set<String> headings) {
        this.id = id;
        this.name = name;
        this.fulltext = fulltext;
        this.headings = headings;
    }

    public static Article fromJson(JSONObject json) {
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
            if (contentType.equals("section")) {
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
            } else if (contentType.equals("paragraph")) {
                final String paraText = Article.getParagraphText(
                    item.getJSONObject("paragraph").getJSONArray("para_body"));
                fulltext.append("\n").append(paraText);
            } else if (contentType.equals("list")) {
                // fixme: we can probably use the list text
            } else if (contentType.equals("infobox")) {
                // fixme: can we use anything in the info boxes?
            } else {
                System.err.println("Unexpected content type: " + contentType);
            }
        }

        // For our purposes, we only want articles that have at least one heading.
        Article article = new Article(id, name, fulltext.toString(), headings);
        try {
            assert(article.headings.size() > 0);
            //assert(article.getSectionsAsString().length() > 2);
            //assert(article.getFulltext().length() > 2);
            return article;
        } catch (AssertionError err) {
            err.printStackTrace();
            return null;
        }
    }

    private static String getParagraphText(JSONArray jsonArray) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject segment = jsonArray.getJSONObject(i);
            text.append(segment.getString("text"));
        }
        return text.toString();
    }

    public String getFulltext() {
        return fulltext;
    }

    public String getPageId() {
        return id;
    }

    /**
     * Retrieve a string representation of this document's set of headings.
     * @return each heading in the article, seperated by commas
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

}
