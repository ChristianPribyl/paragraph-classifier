package cpribyl.topic_naming.treminar;

import cpribyl.topic_naming.index.Article;
import cpribyl.topic_naming.index.Paragraph;
import cpribyl.topic_naming.index.Section;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class MutableArticle extends Article {
  protected MutableArticle(String title, List<MutableSection> sections) {
    super(title, sections);
  }

  @Override
  public String getId() {
    throw new IllegalStateException("RawFulltextArticle has no id.");
  }

  public static MutableArticle fromTextWithoutHeadings(String title, String article) {
    Scanner sc = new Scanner(article);
    StringBuilder sectionText = new StringBuilder();
    List<MutableSection> sections = new LinkedList<>();
    while (sc.hasNext() && sc.hasNextLine()) {
      String line = sc.nextLine();
      while (!line.isEmpty()) {
        sectionText.append(line).append('\n');
        line = sc.nextLine();
      }
      if (!sectionText.isEmpty()) {
        Paragraph paragraph = MutableParagraph.fromText(sectionText.toString());
        MutableSection section = MutableSection.createUnnamed(List.of(paragraph));
        sectionText = new StringBuilder();
        sections.add(section);
      }
    }
    return new MutableArticle(title, sections);
  }

  public void setSectionHeading(Section section, String heading) {
    if (section instanceof MutableSection && getSections().contains(section)) {
      ((MutableSection)section).setHeading(heading);
    } else {
      throw new IllegalArgumentException("Can only change a section from this article.");
    }
  }
}
