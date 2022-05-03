package cpribyl.topic_naming.index;

import java.util.LinkedList;
import java.util.List;

public class Section {
  protected String heading;
  private final List<Paragraph> paragraphs = new LinkedList<>();
  public Section(final String heading, final List<Paragraph> paragraphs) {
    this.heading = heading;
    this.paragraphs.addAll(paragraphs);
  }

  public String getHeading() {
    return heading;
  }

  public List<Paragraph> getParagraphs() {
    return new LinkedList<>(paragraphs);
  }

  public String getFulltext() {
    StringBuilder s = new StringBuilder(getHeading());
    getParagraphs().forEach(para -> s.append(para.getFulltext()).append('\n'));
    return s.toString();
  }
}
