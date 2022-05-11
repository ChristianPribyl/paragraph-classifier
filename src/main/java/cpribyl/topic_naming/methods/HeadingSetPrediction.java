package cpribyl.topic_naming.methods;

import cpribyl.topic_naming.index.Article;
import cpribyl.topic_naming.index.Index;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class HeadingSetPrediction {
  public static Set<String> predict(Index index, Article article) {
    return article.getSections().stream()
            .flatMap(section -> section.getParagraphs().stream())
            .map(paragraph -> ParagraphHeadingPrediction.predictHeading(index, paragraph))
            .collect(Collectors.toSet());
  }
  public static void logResult(BufferedWriter logFile, String articleId, Set<String> predicted, Set<String> actual) throws IOException {
    logFile.write(String.format("%s\n", articleId));
    StringBuilder predictedStr = new StringBuilder();
    predicted.forEach(heading -> predictedStr.append(String.format("%s,", heading)));
    if (!predictedStr.isEmpty()) predictedStr.delete(predictedStr.length()-1, predictedStr.length());
    logFile.write(String.format("%s\n", predictedStr));
    StringBuilder actualStr = new StringBuilder();
    actual.forEach(heading -> actualStr.append(String.format("%s,", heading)));
    if (!actualStr.isEmpty()) actualStr.delete(actualStr.length()-1, actualStr.length());
    logFile.write(String.format("%s\n", actualStr));
  }

  /**
   * The heading-set prediction task is evaluated using four metrics.
   * Precision, Recall, Jacaard, and F1-measure.
   * These are computed by comparing the set of actual headings to the set of predicted headings.
   * @param resultsFile - file containing predicted and actual headings
   * @param log - output file
   * @throws IOException when failing to write to log.
   */
  public static void evaluateResults(FileInputStream resultsFile, BufferedWriter log) throws IOException {
    log.write("entry #,doc id,precision,recall,jacaard,f1\n");
    Scanner results = new Scanner(resultsFile);
    ArrayList<Double> recalls = new ArrayList<>(100000);
    ArrayList<Double> precisions = new ArrayList<>(100000);
    ArrayList<Double> f1scores = new ArrayList<>(100000);
    ArrayList<Double> jacaards = new ArrayList<>(100000);
    var predicted = new HashSet<String>(40);
    var actual = new HashSet<String>(40);
    while (results.hasNextLine()) {
      Optional<String> articleId = getNextResult(results, predicted, actual);
      if (articleId.isPresent()) {
        final Set<String> intersect = new HashSet<>(predicted.size());
        intersect.addAll(predicted);
        intersect.retainAll(actual);
        final Set<String> union = new HashSet<>(predicted.size() + actual.size());
        union.addAll(predicted);
        union.addAll(actual);

        double recall = 1.0 * intersect.size() / actual.size();
        double precision = 1.0 * intersect.size() / predicted.size();
        double jacaard = 1.0 * intersect.size() / union.size();
        double f1;
        try {
          f1 = (precision * recall) / (precision + recall);
          if (Double.isNaN(f1)) f1 = 0;
        } catch (ArithmeticException ex) {
          f1 = 0;
        }

        recalls.add(recall);
        precisions.add(precision);
        jacaards.add(jacaard);
        f1scores.add(f1);

        log.write(String.format("%d,%s,%f,%f,%f,%f\n", recalls.size(), articleId, precision, recall, jacaard, f1));
        System.out.printf("%d,%s,%f,%f,%f,%f\n", recalls.size(), articleId, precision, recall, jacaard, f1);
      }
    }

    log.write(String.format("\nArticles scored: %d\n", recalls.size()));
    System.out.printf("\nArticles scored: %d\n", recalls.size());
    logEvalStatistics(log, precisions, "precision");
    logEvalStatistics(log, recalls, "recall");
    logEvalStatistics(log, jacaards, "jacaard coefficient");
    logEvalStatistics(log, f1scores, "f1 measure");
  }

  private static void logEvalStatistics(BufferedWriter log, ArrayList<Double> scores, String metric) throws IOException {
    double mean = scores.stream().mapToDouble(d -> d).summaryStatistics().getAverage();
    double stdev = Math.sqrt(
            (scores.stream()
                    .mapToDouble(d -> ((d-mean) * (d-mean)))
                    .summaryStatistics().getSum())
                    / (scores.size()-1));
    double stderr = stdev / Math.sqrt(scores.size());
    log.write(String.format("%s\nmean: %f\nstderr: %f\n",
            metric,
            mean,
            stderr));
    System.out.printf("%s\nmean: %f\nstderr: %f\n",
            metric,
            mean,
            stderr);
  }

  private static Optional<String> getNextResult(Scanner results, Set<String> predicted, Set<String> actual) {
    Optional<String> articleId;
    predicted.clear();
    actual.clear();
    try {
      String line = "#";
      while (line.isBlank() || line.charAt(0) == '#') line = results.nextLine();
      articleId = Optional.of(line);
      Scanner sc = new Scanner(results.nextLine());
      sc.useDelimiter(",");
      while (sc.hasNext()) predicted.add(sc.next());

      sc = new Scanner(results.nextLine());
      sc.useDelimiter(",");
      while (sc.hasNext()) actual.add(sc.next());

      if (actual.isEmpty() || predicted.isEmpty()) throw new IllegalArgumentException();
    } catch (Exception ex) {
      ex.printStackTrace();
      articleId = Optional.empty();
      predicted.clear();
      actual.clear();
    }
    return articleId;
  }
}
