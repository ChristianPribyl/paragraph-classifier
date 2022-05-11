package cpribyl.topic_naming.methods;

import cpribyl.topic_naming.index.Index;
import cpribyl.topic_naming.index.Paragraph;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.TopDocs;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ParagraphHeadingPrediction {
  public static String predictHeading(Index index, Paragraph paragraph) {
    String result = "";
    try {
      TopDocs retrieved = index.search(paragraph, "fulltext", 1);
      result = index.getString(retrieved.scoreDocs[0].doc, "heading").orElse("");
      String docid = index.getString(retrieved.scoreDocs[0].doc, "id").orElse("");
      System.out.println("para-id: " + docid);
      System.out.println("heading: " + result);
    } catch (IOException | ParseException | ArrayIndexOutOfBoundsException ex) {
      ex.printStackTrace();
    }
    return result;
  }

  public static void logResult(BufferedWriter logFile, String articleId, String paragraphId, String predicted, String actual) throws IOException {
    logFile.write(String.format("%s,%s\n%s\n%s\n", articleId, paragraphId, predicted, actual));
    System.out.printf("%s,%s\n%s\n%s\n", articleId, paragraphId, predicted, actual);
  }

  public static void evaluateResults(FileInputStream resultsFile, BufferedWriter log) throws IOException {
    log.write("Evaluation results");
    int numScored = 0;
    Scanner results = new Scanner(resultsFile);
    HashMap<String, Integer> correctHeadings = new HashMap<>(100000);
    HashMap<String, Integer> headings = new HashMap<>(100000);
    while (results.hasNextLine()) {
      try {
        String line = "#";
        while (line.charAt(0) == '#') line = results.nextLine();
        Scanner sc = new Scanner(line);
        sc.useDelimiter(",");
        final String articleId = sc.next();
        final String paraId = sc.next();
        final String predicted = results.nextLine();
        final String actual = results.nextLine();
        if (closeEnough(predicted, actual)) {
          correctHeadings.putIfAbsent(articleId, 0);
          correctHeadings.put(articleId, correctHeadings.get(articleId) + 1);
        }
        headings.putIfAbsent(articleId, 0);
        headings.put(articleId, headings.get(articleId) + 1);
        numScored++;
      } catch (Exception ex) {
        ex.printStackTrace();
        System.err.println("num scored: " + numScored);
      }
    }

    var scores = headings.keySet().stream()
            .map(article -> 1.0 * correctHeadings.getOrDefault(article, 0) / headings.get(article))
            .collect(Collectors.toList());
    logEvalStatistics(log, scores);
  }

  private static boolean closeEnough(String predicted, String actual) {
    return predicted.equalsIgnoreCase(actual);
  }

  private static void logEvalStatistics(BufferedWriter log, List<Double> scores) throws IOException {
    double mean = scores.stream().mapToDouble(d -> d).summaryStatistics().getAverage();
    double stdev = Math.sqrt(
            (scores.stream()
                    .mapToDouble(d -> ((d - mean) * (d - mean)))
                    .summaryStatistics().getSum())
                    / (scores.size() - 1));
    double stderr = stdev / Math.sqrt(scores.size());
    log.write(String.format("\nArticles scored: %d\ncorrect predictions ratio\nmean: %f\nstderr: %f\n",
            scores.size(), mean, stderr));
    System.out.printf("\nArticles scored: %d\ncorrect predictions ratio\nmean: %f\nstderr: %f\n",
            scores.size(), mean, stderr);
  }
}