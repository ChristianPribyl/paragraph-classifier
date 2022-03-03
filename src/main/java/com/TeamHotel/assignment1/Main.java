package com.TeamHotel.assignment1;

import java.util.LinkedList;
import java.util.List;

/**
 * Main
 * Parses command line options and performs any class / library initialization
 */
public class Main {
  public static void main(String[] args) {
    if (args.length < 2) {
      usage();
      return;
    }

    final String indexDir = String.format("%s/index.lucene",
      args[1].replaceAll("\\\\", "/").endsWith("/")?
      args[1].substring(0, args[1].length()-1): args[1]);
    if (args[0].equals("index")) {
      
      final List<String> cborFilenames = new LinkedList<>();
      for (int i = 2; i < args.length; i++) {
        cborFilenames.add(args[i]);
      }
      init();
      System.out.printf("Indexing documents under %s\n", args[2]);
      Index.createNewJsonPageIndex(cborFilenames, indexDir);
      System.out.println("Indexing complete");
    } else if (args[0].equals("query")) {
      final List<String> cborFilenames = new LinkedList<>();
      for (int i = 3; i < args.length; i++) {
        cborFilenames.add(args[i]);
      }
      init();
      QueryEngine.rank(cborFilenames.get(0), indexDir, Integer.parseInt(args[2]));
    } else if (args[0].equals("baseline")) {
      final List<String> cborFilenames = new LinkedList<>();
      for (int i = 3; i < args.length; i++) {
        cborFilenames.add(args[i]);
      }
      init();
      QueryEngine.rankBaseline(cborFilenames.get(0), indexDir, Integer.parseInt(args[2]));
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