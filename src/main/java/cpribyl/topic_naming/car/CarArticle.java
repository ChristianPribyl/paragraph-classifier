package cpribyl.topic_naming.car;

import cpribyl.topic_naming.index.Article;
import cpribyl.topic_naming.index.Paragraph;
import cpribyl.topic_naming.index.Section;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Article
 * Represents an article.
 */
public class CarArticle extends Article {
  final String id; // unique id in corpus

  private CarArticle(String id, String name, List<Section> sections) {
    super(name, sections);
    this.id = id;
  }

  public static Optional<Article> fromJson(String jsonString) {
    Optional<Article> result = Optional.empty();
    try {
      final JSONObject json = new JSONObject(jsonString);
      if (json.getString("page_type").equals("article")) {
        final String id = json.getString("page_id");
        final String name = json.getString("page_name");
        final List<Section> sections = processSections(json.getJSONArray("content"));
        if (!sections.isEmpty()) {
          result = Optional.of(new CarArticle(id, name, sections));
        }
      }
    } catch (JSONException ex) {
      ex.printStackTrace();
    }
    return result;
  }

  private static List<Section> processSections(final JSONArray content) {
    String currentHeading = "";
    final List<Section> sections = new LinkedList<>();
    final List<Paragraph> currentSection = new LinkedList<>();
    for (int i = 0; i < content.length(); i++) {
      final JSONObject item = content.getJSONObject(i);
      final String contentType = item.getString("type");
      switch (contentType) {
        case "section" -> {
          if (!currentSection.isEmpty()) {
            sections.add(new Section(currentHeading, currentSection));
            currentSection.clear();
          }
          currentHeading = item.getString("heading_text");
          currentSection.addAll(extractParagraphs(item.getJSONArray("content")));
        }
        case "paragraph" -> currentSection.add(CarParagraph.fromJson(item.getJSONObject("paragraph")));
      }
    }
    return sections;
  }

  private static List<Paragraph> extractParagraphs(JSONArray content) {
    List<Paragraph> paragraphs = new LinkedList<>();
    for (int i = 0; i < content.length(); i++) {
      final JSONObject item = content.getJSONObject(i);
      final String contentType = item.getString("type");
      switch (contentType) {
        case "section" -> paragraphs.addAll(extractParagraphs(item.getJSONArray("content")));
        case "paragraph" -> paragraphs.add(CarParagraph.fromJson(item.getJSONObject("paragraph")));
      }
    }
    return paragraphs;
  }

  @Override
  public String getId() {
    return id;
  }


}
