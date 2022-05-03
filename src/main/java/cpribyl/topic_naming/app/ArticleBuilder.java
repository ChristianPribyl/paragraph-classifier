package cpribyl.topic_naming.app;

import cpribyl.topic_naming.index.Article;
import cpribyl.topic_naming.car.CarArticle;
import cpribyl.topic_naming.index.Index;
import cpribyl.topic_naming.index.Paragraph;
import cpribyl.topic_naming.index.Section;
import cpribyl.topic_naming.methods.ParagraphHeadingPrediction;
import cpribyl.topic_naming.treminar.MutableArticle;

import java.util.*;
import java.util.stream.Collectors;

public class ArticleBuilder {
  public static Optional<Article> fromCarJson(String jsonString) {
    return CarArticle.fromJson(jsonString);
  }

  public static Optional<Article> fromTremaJson(String jsonString) {
    return Optional.empty();
  }

  public static Article completeFromText(Index index, String title, String text) {
    MutableArticle article = MutableArticle.fromTextWithoutHeadings(title, text);
    for (Section section : article.getSections()) {
      Map<String, Integer> candidates = new HashMap<>();
      for (Paragraph paragraph : section.getParagraphs()) {
        final String heading = ParagraphHeadingPrediction.predictHeading(index, paragraph);
        candidates.putIfAbsent(heading, 0);
        candidates.put(heading, candidates.get(heading) + 1);
      }
      final String heading = candidates.entrySet().stream()
              .sorted((l, r) -> -l.getValue().compareTo(r.getValue())).limit(1)
              .collect(Collectors.toList()).get(0).getKey();
      article.setSectionHeading(section, heading);
    }
    return article;
  }
}
