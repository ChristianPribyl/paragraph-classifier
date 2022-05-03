package cpribyl.topic_naming.index;

import java.util.LinkedList;
import java.util.List;

public abstract class Article {
  private final String title;
  protected final List<Section> sections = new LinkedList<>();

  protected Article(String title, List<? extends Section> sections) {
    this.title = title;
    this.sections.addAll(sections);
  }

  public List<Section> getSections() {
    return new LinkedList<>(sections);
  }

  protected String getTitle() {
    return title;
  }

  public abstract String getId();

  public String getFulltext() {
    StringBuilder s = new StringBuilder();
    for (Section section : getSections()) {
      s.append(section.getFulltext()).append('\n');
    }
    return s.toString();
  }
}
