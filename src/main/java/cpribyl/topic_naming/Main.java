package cpribyl.topic_naming;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Main
 * Parses command line options and performs any class / library initialization
 */
public class Main {
  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      usage();
      return;
    }

    final String indexDir = String.format("%s/index.lucene",
      args[1].replaceAll("\\\\", "/").endsWith("/")?
      args[1].substring(0, args[1].length()-1): args[1]);
    init();

    switch (args[0]) {
      case "create-fulltext-lookup-index" -> {
        final List<String> cborFilenames = new LinkedList<>(Arrays.asList(args).subList(2, args.length));
      }
      case "kb-index" -> {
        final List<String> jsonlFilenames = new LinkedList<>(Arrays.asList(args).subList(2, args.length));
        System.out.printf("Indexing documents into %s\n", args[1]);
        Index.createNewJsonPageIndex(jsonlFilenames, indexDir);
        System.out.println("Indexing complete");
      }
      case "query" -> {
        final List<String> jsonlFilenames = new LinkedList<>(Arrays.asList(args).subList(3, args.length));
        QueryEngine.rank(jsonlFilenames.get(0), indexDir, Integer.parseInt(args[2]));
      }
      case "name-paragraphs" -> {
        final List<String> jsonlFilenames = new LinkedList<>(Arrays.asList(args).subList(3, args.length));
        for (String filename : jsonlFilenames) {
          final Scanner sc = new Scanner(new FileInputStream(filename));
          final var out = new BufferedWriter(new FileWriter(String.format("%s_headings.txt", filename)));
          while (sc.hasNextLine()) {
            try {
              final JSONArray json = new JSONArray(sc.nextLine());
              final String paragraph = Article.getParagraphText(json);
              final String paragraph_id = Article.getParagraphId(json);
              final String heading = QueryEngine.getParagraphHeading(paragraph);
              out.write(String.format(""));
            } catch (JSONException ex) {
              ex.printStackTrace();
            }
          }
        }
        QueryEngine.rank(jsonlFilenames.get(0), indexDir, Integer.parseInt(args[2]));
      }
      case "baseline" -> {
        final List<String> cborFilenames = new LinkedList<>(Arrays.asList(args).subList(3, args.length));
        QueryEngine.rankBaseline(cborFilenames.get(0), indexDir, Integer.parseInt(args[2]));
      }
      case "document" -> {
        final List<String> documentFiles = new LinkedList<>(Arrays.asList(args).subList(3, args.length));
        documentFiles.forEach(f -> QueryEngine.process(f, "_headings"));
      }
    }
  }

  private static void init() {
    Stopwords.loadStopwords();
  }

  private static void usage() {
    System.out.println("Usage: java -jar Assignment1<version>.jar (index | query)");
    System.out.println("    index <index-dir> <pages.jsonl>...");
    System.out.println("    query <index-dir>  <max-queries> <pages.jsonl>...");
  }
}