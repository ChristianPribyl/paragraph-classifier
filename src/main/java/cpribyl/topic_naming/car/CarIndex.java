package cpribyl.topic_naming.car;

import cpribyl.topic_naming.app.ArticleBuilder;
import cpribyl.topic_naming.index.Article;
import cpribyl.topic_naming.index.Index;
import cpribyl.topic_naming.index.Paragraph;
import cpribyl.topic_naming.index.Section;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.TopDocs;

import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import static cpribyl.topic_naming.index.Index.Mode.QUERY;

public final class CarIndex extends Index {
  private long parasAddedSinceConstructor = 0;
  private static final long maxParagraphsToAdd = 100000000;
  private final BufferedWriter logFile = new BufferedWriter(new FileWriter("CarIndex.log", true));

  private CarIndex(String location, Index.Mode mode) throws IOException {
    super(location, mode);
    log("Instantiated index in " + mode.name() + "mode");
  }

  public static Optional<Index> fromJsonls(String location, List<String> jsonlFiles) {
    Optional<Index> result = Optional.empty();
    try {
      final CarIndex index = new CarIndex(location, Mode.WRITE);
      index.clear();
      for (String jsonlFile : jsonlFiles) {
        index.includeFile(jsonlFile);
      }
      result = Optional.of(index);
      if (index.parasAddedSinceConstructor >= maxParagraphsToAdd) {
        System.err.println("Too many paragraphs.  Only " + index.parasAddedSinceConstructor + " were added before quitting.");
        index.log("Too many paragraphs.  Only " + index.parasAddedSinceConstructor + " were added before quitting.");
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    return result;
  }

  public static Optional<Index> load(String location) {
    Optional<Index> result = Optional.empty();
    try {
      result = Optional.of(new CarIndex(location, QUERY));
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    return result;
  }

  @Override
  protected synchronized void includeFile(String filename) throws IOException {
    log(String.format("Adding paragraphs from %s\n", filename));
    long paragraphsAdded = 0;
    if (parasAddedSinceConstructor >= maxParagraphsToAdd) return;
    System.setProperty("file.encoding", "UTF-8");

    final Scanner documentScanner = new Scanner(new FileInputStream(filename));
    while (documentScanner.hasNext()) {
      Optional<Article> result = ArticleBuilder.fromCarJson(documentScanner.nextLine());
      if (result.isPresent()) {
        Article article = result.get();
        for (Section section : article.getSections()) {
          for (Paragraph paragraph : section.getParagraphs()) {
            final Document doc = new Document();
            doc.add(new TextField("fulltext", paragraph.getFulltext(), Field.Store.YES));
            doc.add(new TextField("id", paragraph.getId(), Field.Store.YES));
            doc.add(new StringField("heading", section.getHeading(), Field.Store.YES));
            addDocument(doc);
            parasAddedSinceConstructor++;
            paragraphsAdded++;
            if (parasAddedSinceConstructor % 1000 == 0) {
              commit();
              System.err.println("Added " + parasAddedSinceConstructor + " documents since opening the index.");
            }
            if (parasAddedSinceConstructor >= maxParagraphsToAdd) return;
          }
        }
      }
    }
    commit();
    log(String.format("Added %d paragraphs from %s\n", paragraphsAdded, filename));
  }

  private void log(String message) {
    try {
      logFile.write(message);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  protected void logSearchResults(Paragraph paragraph, TopDocs retrieved) {
    if (paragraph instanceof CarParagraph) {
      CarParagraph carParagraph = (CarParagraph)paragraph;
      log(String.format("Search results for:\n%s\n", carParagraph.getId()));
      for (int i = 0; i < retrieved.scoreDocs.length; i++) {
        log(String.format("%d %s\n", i, paragraph.getId())); // TODO: runfile format
      }
    }
  }

  @Override
  protected Analyzer getAnalyzer() throws IOException {
    return CustomAnalyzer.builder()
              .withTokenizer("letter")
              .addTokenFilter("lowercase")
              .addTokenFilter("stop")
              .addTokenFilter("porterstem")
              .build();
  }

  public Optional<String> getFulltext(String carId) throws ParseException, IOException {
    TopDocs retrieved = switch (getMode()) {
      case QUERY -> searcher.search(new QueryParser("id", new KeywordAnalyzer()).parse(carId), 1);
      case WRITE -> throw new IllegalStateException("Index is not in QUERY mode");
    };

    return getString(retrieved.scoreDocs[0].doc, "fulltext");
  }
}
