package cpribyl.topic_naming;

import java.util.LinkedList;
import java.util.Queue;

import org.json.JSONArray;
import org.json.JSONObject;

public class StubQuery {
    final String id;
    final String name;
    final String fulltext;
    private StubQuery(String id, String name, String fulltext) {
        this.id = id;
        this.name = name;
        this.fulltext = fulltext;
    }

    public static StubQuery fromJson(JSONObject json) {
        if (!json.getString("page_type").equals("article")) {
            return null;
        }

        final String name = json.getString("page_name");
        final String id = json.getString("page_id");
        final StringBuilder fulltext = new StringBuilder();

        final JSONArray content = json.getJSONArray("content");
        Queue<JSONObject> items = new LinkedList<>();

        for (int i = 0; i < content.length(); i++) {
            items.add(content.getJSONObject(i));
        }
        while (!items.isEmpty()) {
            final JSONObject item = items.poll();
            final String contentType = item.getString("type");
            if (contentType.equals("paragraph")) {
                final String paraText = StubQuery.getParagraphText(
                    item.getJSONObject("paragraph").getJSONArray("para_body"));
                fulltext.append("\n").append(paraText);
                break;
            }
        }

        return new StubQuery(id, name, fulltext.toString());
    }

    private static String getParagraphText(JSONArray jsonArray) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject segment = jsonArray.getJSONObject(i);
            text.append(segment.getString("text"));
        }
        return text.toString();
    }

    public String getQueryText() {
        if (HyperParameters.queryIncludesFirstParagraph) {
            return name + " " + fulltext;
        } else {
            return name;
        }
    }

}
