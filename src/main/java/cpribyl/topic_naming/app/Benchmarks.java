package cpribyl.topic_naming.app;

import cpribyl.topic_naming.index.Paragraph;
import cpribyl.topic_naming.methods.ClusterPrediction;
import cpribyl.topic_naming.methods.HeadingSetPrediction;
import cpribyl.topic_naming.index.Article;
import cpribyl.topic_naming.index.Index;
import cpribyl.topic_naming.index.Section;
import cpribyl.topic_naming.methods.ParagraphHeadingPrediction;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Benchmarks runs the methods on the provided dataset.
 * The articleIterator method is coupled with the jsonl format.
 */
public class Benchmarks {
  /**
   * An article 'A' contains a list of sections S, where each section contains a single top-level heading 'h'.
   * predictHeadingSets predicts the set 'H' of headings in 'A'.
   * @param index - a corpus of knowledge used to predict headings.
   * @param jsonlFilenames - a list of jsonl files containing articles to use for evaluation.
   * @param maxToProcess - the maximum number of articles to process (if the benchmark is too large to run everything).
   * @param out - a file to log the results.
   *            Each result contains three lines.
   *            <article trec id>
   *            predicted heading 1,predicted heading 2,etc.
   *            actual article heading 1, actual article heading 2,etc.
   *
   *            Lines starting with # are ignored (comments).
   * @throws IOException when failing to write to the results file
   */
  public static void predictHeadingSets(Index index, List<String> jsonlFilenames, int maxToProcess, BufferedWriter out) throws IOException {
    out.write(String.format("# Predicting article heading sets.  N <= %d\n", maxToProcess));
    for (String filename : jsonlFilenames) out.write(String.format("# %s\n", filename));

    int articlesProcessed = 0;
    Iterator<Article> articles = articleIterator(jsonlFilenames);
    Article article = articles.next();
    while (articlesProcessed < maxToProcess && article != null) {
      final Set<String> predictedHeadingSet = HeadingSetPrediction.predict(index, article);
      final Set<String> actualHeadingSet = article.getSections().stream()
              .map(Section::getHeading)
              .collect(Collectors.toSet());
      HeadingSetPrediction.logResult(out, article.getId(), predictedHeadingSet, actualHeadingSet);

      articlesProcessed++;
      article = articles.next();
    }

    out.write(String.format("# Processed %d articles: max = %d\n", articlesProcessed, maxToProcess));
    out.close();
  }

  /**
   * An article 'A' is a set of sections.
   * Each section 'S' is a non-overlapping set of paragraphs.
   * Representing an article as the set of paragraphs 'P' contained in each section,
   * the clustering task assigns each paragraph 'p' to a section 'S' in 'A'.
   * @param index - a corpus of knowledge used to help predict clusters.
   * @param jsonlFilenames - a list of jsonl files containing articles to use for evaluation.
   * @param maxToProcess - the maximum number of articles to process (if the benchmark is too large to run everything).
   * @param out - a file to log the results.
   *            Each result contains three lines.
   *            <article trec id>
   *            paragraph-1-predicted-cluster-id,paragraph-2-predicted-cluster-id,etc.
   *            paragraph-1-actual-cluster-id,paragraph-2-actual-cluster-id,etc.
   *
   *            Lines starting with # are ignored (comments).
   * @throws IOException when failing to write to the results file
   */
  public static void predictClusters(Index index, List<String> jsonlFilenames, int maxToProcess, BufferedWriter out) throws IOException {
    out.write(String.format("# Predicting article paragraph clusters.  N <= %d\n", maxToProcess));
    for (String filename : jsonlFilenames) out.write(String.format("# %s\n", filename));

    int articlesProcessed = 0;
    Iterator<Article> articles = articleIterator(jsonlFilenames);
    Article article = articles.next();
    while (articlesProcessed < maxToProcess && article != null) {
      final List<Integer> predictedClusters = ClusterPrediction.predict(index, article);
      final List<Integer> actualClusters = article.getSections().stream()
              .flatMap(section -> section.getParagraphs().stream()
                      .map(paragraph -> section.getHeading().hashCode()))
              .collect(Collectors.toList());
      ClusterPrediction.logResult(out, article.getId(), predictedClusters, actualClusters);

      articlesProcessed++;
      article = articles.next();
    }

    out.write(String.format("# Processed %d articles: max = %d\n", articlesProcessed, maxToProcess));
    out.close();
  }

  /**
   * Every paragraph 'P' in an article belongs to a section 'S'.
   * Every section 'S' in an article contains a heading 'H'.
   * predictParagraphHeadings predicts the heading 'H' of the section 'S'
   * that 'P' belongs to without looking at the rest of 'S'.
   *
   * @param index - a corpus of knowledge used to help predict section headings.
   * @param jsonlFilenames - a list of jsonl files containing articles to use for evaluation.
   * @param maxToProcess - the maximum number of articles to process (if the benchmark is too large to run everything).
   * @param out - a file to log the results.
   *            Each result contains three lines.
   *            <article trec id>,<paragraph trec id>
   *            predicted heading
   *            actual heading
   *
   *            Lines starting with # are ignored (comments).
   * @throws IOException when failing to write to the results file
   */
  public static void predictParagraphHeadings(Index index, List<String> jsonlFilenames, int maxToProcess, BufferedWriter out) throws IOException {
    out.write(String.format("# Predicting paragraph headings.  N <= %d\n", maxToProcess));
    for (String filename : jsonlFilenames) out.write(String.format("# %s\n", filename));

    int articlesProcessed = 0;
    Iterator<Article> articles = articleIterator(jsonlFilenames);
    Article article = articles.next();
    while (articlesProcessed < maxToProcess && article != null) {
      for (Section section : article.getSections()) {
        for (Paragraph paragraph : section.getParagraphs()) {
          final String predictedHeading = ParagraphHeadingPrediction.predictHeading(index, paragraph);
          final String actualHeading = section.getHeading();
          ParagraphHeadingPrediction.logResult(out, article.getId(), paragraph.getId(), predictedHeading, actualHeading);
        }
      }

      articlesProcessed++;
      article = articles.next();
    }

    out.write(String.format("# Processed %d articles: max = %d\n", articlesProcessed, maxToProcess));
    out.close();
  }

  /**
   * An iterator over all articles in the provided list of files.
   * Individual articles within a file may be invalid, they will be skipped.
   * The iterator will stop if a file contains no valid articles (if the wrong file was provided),
   * Or if it finishes iterating over all files.
   * @param jsonlFilenames a list if jsonl files containing CAR wikipedia articles.
   * @return an iterator over all articles in the corpus.
   */
  private static Iterator<Article> articleIterator(final List<String> jsonlFilenames) {
    return new Iterator<>() {
      final Iterator<String> filenames = jsonlFilenames.iterator();
      Scanner lines = null;

      @Override
      public boolean hasNext() {
        throw new IllegalStateException("Not implemented.  Use next() instead.");
      }

      @Override
      public Article next() {
        AtomicReference<Article> result = new AtomicReference<>(null);
        try {
          if (lines == null || !lines.hasNextLine()) lines = new Scanner(new FileInputStream(filenames.next()));
          while (result.get() == null) ArticleBuilder.fromCarJson(lines.nextLine()).ifPresent(result::set);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        return result.get();
      }
    };
  }

}
