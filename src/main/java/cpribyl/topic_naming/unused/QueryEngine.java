package cpribyl.topic_naming.unused;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.lang.System;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONObject;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

/**
 * QueryEngine
 * 
 * This class creates queries from wikipedia articles derived from jsonl files.
 * The queries are run, and the most common headings in the retrieved articles
 * are used to predict the headings of the query article.
 */
public class QueryEngine {
  /*
  public static void rank(final String jsonl_pages_file, final String indexPath, final int maxQueries) {
    System.setProperty("file.encoding", "UTF-8");

    try {
      double precisionSum = 0;
      double recallSum = 0;
      double jacaardSum = 0;
      double f1Sum = 0;
      int queryCount = 0;

      final Directory dir = FSDirectory.open(Paths.get(indexPath));
      final IndexReader reader = DirectoryReader.open(dir);
      final IndexSearcher searcher = new IndexSearcher(reader);

      final Scanner sc  = new Scanner(new FileInputStream(jsonl_pages_file));
      // each line is an article, which we use to create a query.
      while (sc.hasNextLine() && queryCount < maxQueries) {
        final JSONObject pageJson = new JSONObject(sc.nextLine());

        // The query text is the article name + the first paragraph.
        final String queryText = Preprocess.searchText(Objects.requireNonNull(StubQuery.fromJson(pageJson)).getQueryText());
        final Article page = Article.fromCarJson(pageJson);
        assert page != null;
        if (page.getSections().size() == 0) {
          // don't query articles with no sections
          continue;
        }

        // fixme: the standard analyzer does not filter stopwords
        QueryParser parser = new QueryParser("fulltext", new StandardAnalyzer()); 
        Query query = parser.parse(queryText);
        searcher.setSimilarity(new BM25Similarity());
        TopDocs retrieved = searcher.search(query, HyperParameters.numSearchResults);
        if (queryCount < 100) {
          System.out.println("\nquery: "+ query + "");
          System.out.println("Total Results :: " + retrieved.totalHits);
        }

        Map<Heading, Integer> sectionOccurrences = new HashMap<>();

        ScoreDoc[] scoreDocs = retrieved.scoreDocs;
        for (ScoreDoc score : scoreDocs) {
          final Document doc = searcher.doc(score.doc);
          assert (doc.getField("headings").stringValue().length() > 2);
          Stream.of(
                          doc.getField("headings")
                                  .stringValue().split(","))
                  .map(Heading::of)
                  .forEach(section -> {
                    Integer previousCount = sectionOccurrences.putIfAbsent(section, 1);
                    if (previousCount != null) {
                      sectionOccurrences.put(section, previousCount + 1);
                    }
                  });
        }
        System.out.println("Observed " + sectionOccurrences.size() + " unique section titles");
        Set<Heading> derivedSections = sectionOccurrences.entrySet().stream()
          .filter(((Entry<Heading, Integer> e) -> e.getValue() > HyperParameters.minHeadingOccurrencesForInclusion))
          .sorted(Entry.comparingByValue())
          .map(Entry::getKey)
          .limit(HyperParameters.numHeadingsToPredict)
          .collect(Collectors.toSet());
        System.out.println("Selected " + derivedSections.size() + " section titles");
        Set<Heading> actualSections = page.getSections().stream()
          .map(Heading::of)
          .collect(Collectors.toSet());
        Set<Heading> intersect = new HashSet<>(actualSections);
        intersect.retainAll(derivedSections);
        Set<Heading> union = new HashSet<>();
        union.addAll(actualSections);
        union.addAll(derivedSections);

        double precision = 0;
        double recall = 0;
        double jacaard = 0;
        double f1 = 0;
        if (!derivedSections.isEmpty()) {
          precision = intersect.size() * 1.0 / derivedSections.size();
          recall = intersect.size() * 1.0 / actualSections.size();
          jacaard = intersect.size() * 1.0 / union.size();
          if (precision + recall != 0) {
            f1 = 2 * precision * recall / (precision + recall);
          }
        }

        if (queryCount < 100) {
          System.out.println("Matched documents: " + scoreDocs.length);
          System.out.println("Predicted headings:");
          derivedSections.forEach((heading) -> System.out.print(heading.getFullText() + " : "));
          System.out.println();
          System.out.println("Actual headings:");
          actualSections.forEach((heading) -> System.out.print(heading.getFullText() + " : "));
          System.out.println();
          System.out.println("Precision: " + precision);
          System.out.println("Recall: " + recall);
          System.out.println("Jacaard: " + jacaard);
          System.out.println("F1: " + f1);
        }
        precisionSum += precision;
        recallSum += recall;
        jacaardSum += jacaard;
        f1Sum += f1;
        queryCount++;

      }
      System.out.printf("\n=====RESULTS=====\nRan %d queries on a corpus with %d documents\n", queryCount, reader.numDocs());
      System.out.printf("Mean Precision = %.2f\n", precisionSum / queryCount);
      System.out.printf("Mean Recall = %.2f\n", recallSum / queryCount);
      System.out.printf("Mean Jacaard Coefficient = %.2f\n", jacaardSum / queryCount);
      System.out.printf("Mean F1 Score = %.2f\n", f1Sum / queryCount);
      HyperParameters.print();

    }
    catch (FileNotFoundException e) {
        System.err.printf("Failed to open input file %s\n", jsonl_pages_file);
        throw new RuntimeException(e);
    } catch (IOException | ParseException e){
        e.printStackTrace();
    }
  }
*/
  /*
  public static void rankBaseline(final String jsonl_pages_file, final String indexPath, final int maxQueries) {
    System.setProperty("file.encoding", "UTF-8");

    try {
      double precisionSum = 0;
      double recallSum = 0;
      double jacaardSum = 0;
      int queryCount = 0;

      Directory dir = FSDirectory.open(Paths.get(indexPath));
      IndexReader reader = DirectoryReader.open(dir);
      IndexSearcher searcher = new IndexSearcher(reader);

      final Scanner sc  = new Scanner(new FileInputStream(jsonl_pages_file));
      while (sc.hasNextLine() && queryCount < maxQueries) {
        final JSONObject pageJson = new JSONObject(sc.nextLine());
        final String queryText = "horseshoe crabs";
        final Article page = Article.fromCarJson(pageJson);
        assert page != null;
        if (page.getSections().size() == 0) {
          // don't query articles with no sections
          continue;
        }
        QueryParser parser = new QueryParser("fulltext", new StopAnalyzer());
        Query query = parser.parse(queryText);
        searcher.setSimilarity(new BM25Similarity());
        TopDocs hits = searcher.search(query, HyperParameters.numSearchResults);
        if (queryCount < 100) {
          System.out.println("\nquery: "+ query + "");
          System.out.println("Total Results :: " + hits.totalHits);
        }

        Map<Heading, Integer> sectionOccurrences = new HashMap<>();

        ScoreDoc[] scoreDocs = hits.scoreDocs;
        for (ScoreDoc score : scoreDocs) {
          final Document doc = searcher.doc(score.doc);
          assert (doc.getField("headings").stringValue().length() > 2);
          Stream.of(
                          doc.getField("headings")
                                  .stringValue().split(","))
                  .map(Heading::of)
                  .forEach(section -> {
                    Integer previousCount = sectionOccurrences.putIfAbsent(section, 1);
                    if (previousCount != null) {
                      sectionOccurrences.put(section, previousCount + 1);
                    }
                  });
        }
        System.out.println("Observed " + sectionOccurrences.size() + " unique section titles");
        Set<Heading> derivedSections = sectionOccurrences.entrySet().stream()
          .filter(((Entry<Heading, Integer> e) -> e.getValue() > HyperParameters.minHeadingOccurrencesForInclusion))
          .sorted(Entry.comparingByValue())
          .map(Entry::getKey)
          .limit(HyperParameters.numHeadingsToPredict)
          .collect(Collectors.toSet());
        System.out.println("Chose " + derivedSections.size() + " section titles");
        Set<Heading> actualSections = page.getSections().stream()
          .map(Heading::of)
          .collect(Collectors.toSet());
        Set<Heading> intersect = new HashSet<>(actualSections);
        intersect.retainAll(derivedSections);
        Set<Heading> union = new HashSet<>();
        union.addAll(actualSections);
        union.addAll(derivedSections);

        double precision = 0;
        double recall = 0;
        double jacaard = 0;
        if (!derivedSections.isEmpty()) {
          precision = intersect.size() * 1.0 / derivedSections.size();
          recall = intersect.size() * 1.0 / actualSections.size();
          jacaard = intersect.size() * 1.0 / union.size();
        }

        if (queryCount < 100) {
          System.out.println("Matched documents: " + scoreDocs.length);
          System.out.println("Predicted headings:");
          derivedSections.forEach((heading) -> System.out.print(heading.getFullText() + " : "));
          System.out.println();
          System.out.println("Actual headings:");
          actualSections.forEach((heading) -> System.out.print(heading.getFullText() + " : "));
          System.out.println();
          System.out.println("Precision: " + precision);
          System.out.println("Recall: " + recall);
          System.out.println("Jacaard: " + jacaard);
        }
        precisionSum += precision;
        recallSum += recall;
        jacaardSum += jacaard;
        queryCount++;

      }
      System.out.printf("\n=====RESULTS=====\nRan %d queries on a corpus with %d documents\n", queryCount, reader.numDocs());
      System.out.printf("Mean Precision = %.2f\n", precisionSum / queryCount);
      System.out.printf("Mean Recall = %.2f\n", recallSum / queryCount);
      System.out.printf("Mean Jacaard Coefficient = %.2f\n", jacaardSum / queryCount);

      HyperParameters.print();
      // Include F1 scores
    }
    catch (FileNotFoundException e) {
        System.err.printf("Failed to open input file %s\n", jsonl_pages_file);
        throw new RuntimeException(e);
    } catch (IOException | ParseException e){
        e.printStackTrace();
    }
  }
  */
/*
  public static void process(String jsonFilename, String outSuffix) {

  }
*/
  public static String getParagraphHeading(String paragraph) {
    return "";
  }
}