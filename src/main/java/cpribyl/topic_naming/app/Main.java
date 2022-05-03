package cpribyl.topic_naming.app;

import cpribyl.topic_naming.car.CarIndex;
import cpribyl.topic_naming.index.Article;
import cpribyl.topic_naming.index.Index;
import cpribyl.topic_naming.index.Paragraph;
import cpribyl.topic_naming.methods.ClusterPrediction;
import cpribyl.topic_naming.methods.HeadingSetPrediction;
import cpribyl.topic_naming.methods.ParagraphHeadingPrediction;
import cpribyl.topic_naming.treminar.MutableArticle;
import cpribyl.topic_naming.treminar.MutableParagraph;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Main
 * Parses command line options and performs any class / library initialization
 */
public class Main {
  public static void main(String[] args) throws IOException, ParseException {
    if (args.length < 2) {
      usage();
      return;
    }

    switch (args[0]) {
      case "index-kb" -> {
        final String indexDir = String.format("%s/index.lucene",
                args[1].replaceAll("\\\\", "/").endsWith("/")?
                        args[1].substring(0, args[1].length()-1): args[1]);
        final List<String> jsonlFilenames = new LinkedList<>(Arrays.asList(args).subList(2, args.length));
        final Index index = IndexBuilder.createCar(indexDir, jsonlFilenames);
        System.out.println("Created index at " + indexDir + " with " + index.size() + " paragraphs");
      }
      case "benchmark" -> {
        final String indexDir = String.format("%s/index.lucene",
                args[1].replaceAll("\\\\", "/").endsWith("/")?
                        args[1].substring(0, args[1].length()-1): args[1]);
        final List<String> jsonlFilenames = new LinkedList<>(Arrays.asList(args).subList(3, args.length));
        final Index index = IndexBuilder.loadCar(indexDir);
        final int maxToProcess = Integer.parseInt(args[2]);
        Benchmarks.predictHeadingSets(index, jsonlFilenames, maxToProcess,
                new BufferedWriter(new FileWriter("results/predictedHeadingSets.log")));

        Benchmarks.predictClusters(index, jsonlFilenames, maxToProcess,
                new BufferedWriter(new FileWriter("results/predictedClusters.log")));
        Benchmarks.predictParagraphHeadings(index, jsonlFilenames, maxToProcess,
                new BufferedWriter(new FileWriter("results/predictedParagraphHeadings.log")));
      }
      case "json-insert-headings" -> {
        // TODO: insert headings into 953 article
      }
      case "raw-insert-headings" -> {
        final String indexDir = String.format("%s/index.lucene",
                args[1].replaceAll("\\\\", "/").endsWith("/")?
                        args[1].substring(0, args[1].length()-1): args[1]);
        final Index index = IndexBuilder.loadCar(indexDir);
        Article article = ArticleBuilder.completeFromText(index, args[2], Files.readString(Path.of(args[2])));
        System.out.println(article.getFulltext());
      }
      case "heading-set-eval" -> {
        final FileInputStream resultsFile = new FileInputStream(args[1]);
        HeadingSetPrediction.evaluateResults(resultsFile,
                new BufferedWriter(new FileWriter("results/heading_set_eval.log")));
      }
      case "paragraph-heading-eval" -> {
        final FileInputStream resultsFile = new FileInputStream(args[1]);
        ParagraphHeadingPrediction.evaluateResults(resultsFile,
                new BufferedWriter(new FileWriter("results/paragraph_heading_eval.log")));
      }
      case "cluster-eval" -> {
        final FileInputStream resultsFile = new FileInputStream(args[1]);
        ClusterPrediction.evaluateResults(resultsFile,
                new BufferedWriter(new FileWriter("results/paragraph_clustering_eval.log")));
      }
      case "single-query" -> {
        final String indexDir = String.format("%s/index.lucene",
                args[1].replaceAll("\\\\", "/").endsWith("/")?
                        args[1].substring(0, args[1].length()-1): args[1]);
        final Index index = IndexBuilder.loadCar(indexDir);
        final String heading = ParagraphHeadingPrediction.predictHeading(index, MutableParagraph.fromText(args[2]));
        System.out.println(heading);
      }
      case "para-fulltext" -> {
        final String indexDir = String.format("%s/index.lucene",
                args[1].replaceAll("\\\\", "/").endsWith("/")?
                        args[1].substring(0, args[1].length()-1): args[1]);
        CarIndex index = (CarIndex) IndexBuilder.loadCar(indexDir);
        System.out.println(index.getFulltext(args[2]).get());

      }
    }
  }

  private static void usage() {
    throw new NotImplementedException("TODO: write usage.  Reference README.md for usage instructions.");
  }
}