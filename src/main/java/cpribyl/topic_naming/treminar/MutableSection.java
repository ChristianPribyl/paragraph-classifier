package cpribyl.topic_naming.treminar;

import cpribyl.topic_naming.index.Paragraph;
import cpribyl.topic_naming.index.Section;

import java.util.List;

public class MutableSection extends Section {
  private MutableSection(List<Paragraph> paragraphs) {
    super("", paragraphs);
  }

  public static MutableSection createUnnamed(List<Paragraph> paragraphs) {
    return new MutableSection(paragraphs);
  }

  public void setHeading(String heading) {
    this.heading = heading;
  }


}
