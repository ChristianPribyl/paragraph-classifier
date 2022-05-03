package cpribyl.topic_naming.treminar;

import cpribyl.topic_naming.index.Paragraph;
import org.json.JSONArray;
import org.json.JSONObject;

public class MutableParagraph extends Paragraph {
  private MutableParagraph(String fulltext) {
    super(fulltext);
  }

  @Override
  public String getId() {
    throw new IllegalStateException("Treminar documents do not have a unique id.");
  }

  // fixme: use treminar format
  public static Paragraph fromJson(JSONObject paragraph) {
    final String paraId = paragraph.getString("para_id");
    final JSONArray jsonArray = paragraph.getJSONArray("para_body");
    final StringBuilder text = new StringBuilder();
    for (int i = 0; i < jsonArray.length(); i++) {
      text.append(jsonArray.getJSONObject(i).getString("text"));
    }
    return new MutableParagraph(text.toString());
  }

  public static Paragraph fromText(String text) {
    return new MutableParagraph(text);
  }

  // fixme: use treminar format
  public static JSONObject toJson() {
    return new JSONObject();
  }
}
