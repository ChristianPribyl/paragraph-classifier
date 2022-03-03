package com.TeamHotel.assignment1;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.lang.System;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;

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
  public static int rank(final String jsonl_pages_file, final String indexPath, final int maxQueries) {
    System.setProperty("file.encoding", "UTF-8");

    try {
      double precisionSum = 0;
      double recallSum = 0;
      double jacaardSum = 0;
      double f1Sum = 0;
      int queryCount = 0;

      Directory dir = FSDirectory.open(Paths.get(indexPath));
      IndexReader reader = DirectoryReader.open(dir);
      IndexSearcher searcher = new IndexSearcher(reader);

      final Scanner sc  = new Scanner(new FileInputStream(new File(jsonl_pages_file)));
      while (sc.hasNextLine() && queryCount < maxQueries) {
        final JSONObject pageJson = new JSONObject(sc.nextLine());
        final String queryText = Preprocess.searchtext(StubQuery.fromJson(pageJson).getQueryText());
        final Article page = Article.fromJson(pageJson);
        if (page.getSections().size() == 0) {
          // don't query articles with no sections
          continue;
        }
        QueryParser parser = new QueryParser("fulltext", new StandardAnalyzer());
        Query query = parser.parse(queryText);
        searcher.setSimilarity(new BM25Similarity());
        TopDocs hits = searcher.search(query, HyperParameters.numSearchResults);
        if (queryCount < 100) {
          System.out.println("\nquery: "+ query + "");
          System.out.println("Total Results :: " + hits.totalHits);
        }

        Map<Heading, Integer> sectionOccurrances = new HashMap<>();

        ScoreDoc[] scoreDocs = hits.scoreDocs;
        for (int i = 0; i < scoreDocs.length; i++) {
          ScoreDoc score = scoreDocs[i];
          final Document doc = searcher.doc(score.doc);
          assert(doc.getField("headings").stringValue().length() > 2);
          List.of(
            doc.getField("headings")
            .stringValue().split(","))
          .stream()
          .map((String section) -> Heading.of(section))
          .forEach(section -> {
          Integer previousCount = sectionOccurrances.putIfAbsent(section, 1);
            if (previousCount != null) {
              sectionOccurrances.put(section, previousCount + 1);
            }
          });
        }
        System.out.println("Observed " + sectionOccurrances.size() + " unique section titles");
        Set<Heading> derivedSections = sectionOccurrances.entrySet().stream()
          .filter(((Entry<Heading, Integer> e) -> e.getValue() > HyperParameters.minHeadingOccurrancesForInclusion))
          .sorted((Entry<Heading, Integer> el, Entry<Heading, Integer> er) -> el.getValue().compareTo(er.getValue()))
          .map(entry -> entry.getKey())
          .limit(HyperParameters.numHeadingsToPredict)
          .collect(Collectors.toSet());
        System.out.println("Chose " + derivedSections.size() + " section titles");
        Set<Heading> actualSections = page.getSections().stream()
          .map((String section) -> Heading.of(section))
          .collect(Collectors.toSet());
        Set<Heading> intersect = new HashSet<>();
        intersect.addAll(actualSections);
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
          derivedSections.forEach((heading) -> {
            System.out.print(heading.getFullText() + " : ");
          });
          System.out.println();
          System.out.println("Actual headings:");
          actualSections.forEach((heading) -> {
            System.out.print(heading.getFullText() + " : ");
          });
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
        return 1;
    } catch (IOException e){
        e.printStackTrace();
    } catch (ParseException e) {
        e.printStackTrace();
    }
    return 0;
  }

  public static int rankBaseline(final String jsonl_pages_file, final String indexPath, final int maxQueries) {
    System.setProperty("file.encoding", "UTF-8");

    try {
      double precisionSum = 0;
      double recallSum = 0;
      double jacaardSum = 0;
      int queryCount = 0;

      Directory dir = FSDirectory.open(Paths.get(indexPath));
      IndexReader reader = DirectoryReader.open(dir);
      IndexSearcher searcher = new IndexSearcher(reader);

      final Scanner sc  = new Scanner(new FileInputStream(new File(jsonl_pages_file)));
      while (sc.hasNextLine() && queryCount < maxQueries) {
        final JSONObject pageJson = new JSONObject(sc.nextLine());
        final String queryText = "horseshoe crabs";
        final Article page = Article.fromJson(pageJson);
        if (page.getSections().size() == 0) {
          // don't query articles with no sections
          continue;
        }
        QueryParser parser = new QueryParser("fulltext", new StandardAnalyzer());
        Query query = parser.parse(queryText);
        searcher.setSimilarity(new BM25Similarity());
        TopDocs hits = searcher.search(query, HyperParameters.numSearchResults);
        if (queryCount < 100) {
          System.out.println("\nquery: "+ query + "");
          System.out.println("Total Results :: " + hits.totalHits);
        }

        Map<Heading, Integer> sectionOccurrances = new HashMap<>();

        ScoreDoc[] scoreDocs = hits.scoreDocs;
        for (int i = 0; i < scoreDocs.length; i++) {
          ScoreDoc score = scoreDocs[i];
          final Document doc = searcher.doc(score.doc);
          //final String paragraphid = doc.getField("pageid").stringValue();
          //final float searchScore = score.score;
          //final int searchRank = i+1;
          assert(doc.getField("headings").stringValue().length() > 2);
          List.of(
            doc.getField("headings")
            .stringValue().split(","))
          .stream()
          .map((String section) -> Heading.of(section))
          .forEach(section -> {
          Integer previousCount = sectionOccurrances.putIfAbsent(section, 1);
            if (previousCount != null) {
              sectionOccurrances.put(section, previousCount + 1);
            }
          });
        }
        System.out.println("Observed " + sectionOccurrances.size() + " unique section titles");
        Set<Heading> derivedSections = sectionOccurrances.entrySet().stream()
          .filter(((Entry<Heading, Integer> e) -> e.getValue() > HyperParameters.minHeadingOccurrancesForInclusion))
          .sorted((Entry<Heading, Integer> el, Entry<Heading, Integer> er) -> el.getValue().compareTo(er.getValue()))
          .map(entry -> entry.getKey())
          .limit(HyperParameters.numHeadingsToPredict)
          .collect(Collectors.toSet());
        System.out.println("Chose " + derivedSections.size() + " section titles");
        Set<Heading> actualSections = page.getSections().stream()
          .map((String section) -> Heading.of(section))
          .collect(Collectors.toSet());
        Set<Heading> intersect = new HashSet<>();
        intersect.addAll(actualSections);
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
          derivedSections.forEach((heading) -> {
              System.out.print(heading.getFullText() + " : ");
          });
          System.out.println();
          System.out.println("Actual headings:");
          actualSections.forEach((heading) -> {
              System.out.print(heading.getFullText() + " : ");
          });
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
      // Incldue F1 scores
    }
    catch (FileNotFoundException e) {
        System.err.printf("Failed to open input file %s\n", jsonl_pages_file);
        return 1;
    } catch (IOException e){
        e.printStackTrace();
    } catch (ParseException e) {
        e.printStackTrace();
    }
    return 0;
  }
}