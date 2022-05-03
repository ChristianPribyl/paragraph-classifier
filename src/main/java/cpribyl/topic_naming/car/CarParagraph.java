package cpribyl.topic_naming.car;

import cpribyl.topic_naming.index.Paragraph;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * CarParagraph represents a paragraph in the TREC CAR dataset.
 * @ThreadSafe
 */
public class CarParagraph extends Paragraph {
  private final String id;
  private CarParagraph(final String id, final String fulltext) {
    super(fulltext);
    this.id = id;
  }

  /**
   * fromJson is used to extract a paragraph from a json object
   * representing a paragraph from the TREC CAR dataset.
   * @param paragraph
   * @return
   */
  public static Paragraph fromJson(JSONObject paragraph) {
    final String paraId = paragraph.getString("para_id");
    final JSONArray jsonArray = paragraph.getJSONArray("para_body");
    final StringBuilder text = new StringBuilder();
    for (int i = 0; i < jsonArray.length(); i++) {
      text.append(jsonArray.getJSONObject(i).getString("text"));
    }
    return new CarParagraph(paraId, text.toString());
  }

  public String getId() {
    return id;
  }
}
