package cpribyl.topic_naming.methods;

import cpribyl.topic_naming.index.Article;
import cpribyl.topic_naming.index.Index;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ClusterPrediction {
  public static List<Integer> predict(Index index, Article article) {
    return article.getSections().stream()
            .flatMap(section -> section.getParagraphs().stream())
            .map(paragraph -> ParagraphHeadingPrediction.predictHeading(index, paragraph).hashCode())
            .collect(Collectors.toList());
  }

  public static void logResult(BufferedWriter logFile, String articleId, List<Integer> predicted, List<Integer> actual) throws IOException {
    logFile.write(String.format("%s\n", articleId));
    StringBuilder predictedStr = new StringBuilder();
    predicted.forEach(clusterId -> predictedStr.append(String.format("%d,", clusterId)));
    if (!predictedStr.isEmpty()) predictedStr.delete(predictedStr.length() - 1, predictedStr.length());
    logFile.write(String.format("%s\n", predictedStr));
    StringBuilder actualStr = new StringBuilder();
    actual.forEach(clusterId -> actualStr.append(String.format("%d,", clusterId)));
    if (!actualStr.isEmpty()) actualStr.delete(actualStr.length() - 1, actualStr.length());
    logFile.write(String.format("%s\n", actualStr));
  }

  /**
   * The clustering task is evaluated by the rand_score algorithm.
   * rand_score is the probability that two paragraphs are clustered correctly relative to each other:
   * If they are in the same section, they should be clustered together, otherwise they should be in
   * separate clusters.
   * <p>
   * For every possible pair of paragraphs, we compute the ratio of those that are correctly clustered together or apart.
   *
   * @param resultsFile - file containing clustering results.  See Benchmarks class for file formats.
   * @param log - where to log results
   */
  public static void evaluateResults(FileInputStream resultsFile, BufferedWriter log) throws IOException {
    log.write("Evaluation results");
    Scanner results = new Scanner(resultsFile);
    ArrayList<Double> scores = new ArrayList<>(100000);
    var predictedClusters = new ArrayList<Integer>(40);
    var actualClusters = new ArrayList<Integer>(40);
    while (results.hasNextLine()) {
      Optional<String> articleId = getNextResult(results, predictedClusters, actualClusters);
      if (articleId.isPresent()) {
        long correctPairs = 0;
        long totalPairs = 0;
        for (int a = 0; a < actualClusters.size(); a++) {
          for (int b = 0; b < actualClusters.size(); b++) {
            if ((actualClusters.get(a).equals(actualClusters.get(b)))
            == (predictedClusters.get(a).equals(predictedClusters.get(b))))
              correctPairs++;
            totalPairs++;
          }
        }
        double score = (1.0 * correctPairs) / totalPairs;
        scores.add(score);

        log.write(String.format("%d %s %f\n", scores.size(), articleId, score));
        System.out.printf("%d %s %f\n", scores.size(), articleId, score);
      }
    }
    
    logEvalStatistics(log, scores);
  }

  private static void logEvalStatistics(BufferedWriter log, ArrayList<Double> scores) throws IOException {
    double mean = scores.stream().mapToDouble(d -> d).summaryStatistics().getAverage();
    double stdev = Math.sqrt(
            (scores.stream()
                    .mapToDouble(d -> ((d-mean) * (d-mean)))
                    .summaryStatistics().getSum())
                    / (scores.size()-1));
    double stderr = stdev / Math.sqrt(scores.size());
    log.write(String.format("\nArticles scored: %d\nrand_score\nmean: %f\nstderr: %f\n",
            scores.size(),
            mean,
            stderr));
    System.out.printf("\nArticles scored: %d\nrand_score\nmean: %f\nstderr: %f\n",
            scores.size(),
            mean,
            stderr);
  }

  private static Optional<String> getNextResult(Scanner results, ArrayList<Integer> predicted, ArrayList<Integer> actual) {
    Optional<String> articleId;
    predicted.clear();
    actual.clear();
    try {
      String line = "#";
      while (line.isBlank() || line.charAt(0) == '#') line = results.nextLine();
      articleId = Optional.of(line);
      Scanner clusterScanner = new Scanner(results.nextLine());
      clusterScanner.useDelimiter(",");
      while (clusterScanner.hasNextInt()) predicted.add(clusterScanner.nextInt());

      clusterScanner = new Scanner(results.nextLine());
      clusterScanner.useDelimiter(",");
      while (clusterScanner.hasNextInt()) actual.add(clusterScanner.nextInt());

      if (actual.size() != predicted.size()) {
        throw new IllegalArgumentException();
      }
    } catch (Exception ex) {
      articleId = Optional.empty();
      predicted.clear();
      actual.clear();
    }
    return articleId;
  }
}
