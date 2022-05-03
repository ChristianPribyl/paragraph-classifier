package cpribyl.topic_naming.index;

public abstract class Paragraph {
  private final String fulltext;

  public Paragraph(String fulltext) {
    this.fulltext = fulltext;
  }

  public String getFulltext() {
    return fulltext.replaceAll("[^\\w\\d]", " ");
  }

  public abstract String getId();
}
