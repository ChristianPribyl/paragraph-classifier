package cpribyl.topic_naming.index;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;


public abstract class Index {
  public enum Mode {QUERY, WRITE}
  private final String location;
  private final Directory dir;
  private Mode mode = Mode.QUERY;
  private IndexWriter writer;
  protected IndexReader reader;
  protected IndexSearcher searcher;

  protected Index(String location, Mode mode) throws IOException {
    synchronized (this) {
      this.location = location;
      dir = FSDirectory.open(Paths.get(location));
      switch (mode) {
        case QUERY -> configureQueryMode();
        case WRITE -> configureWriteMode();
      }
    }
  }

  private void configureQueryMode() throws IOException {
    reader = DirectoryReader.open(dir);
    searcher = new IndexSearcher(reader);
    searcher.setSimilarity(getSimilarity());
    writer = null;
    mode = Mode.QUERY;
  }

  private void configureWriteMode() throws IOException {
    reader = null;
    searcher = null;
    writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()));
    mode = Mode.WRITE;
  }

  public final synchronized void clear() throws IOException {
    switch (mode) {
      case QUERY -> throw new IllegalStateException("Index is not in WRITE mode");
      case  WRITE -> {
        writer.deleteAll();
        writer.commit();
      }
    }
  }

  public final synchronized long size() {
    return switch (mode) {
      case QUERY -> reader.numDocs();
      case WRITE -> writer.getPendingNumDocs();
    };
  }

  protected final synchronized void setReadMode() throws IOException {
    if (mode == Mode.WRITE) {
      writer.commit();
      writer.close();
      configureQueryMode();
    }
  }

  protected final synchronized void setWriteMode() throws IOException {
    if (mode == Mode.QUERY) configureWriteMode();
  }

  protected void addDocument(Document document) throws IOException {
    switch (mode) {
      case QUERY -> throw new IllegalStateException("Index is not in WRITE mode");
      case WRITE -> writer.addDocument(document);
    }
  }

  protected void commit() throws IOException {
    switch (mode) {
      case QUERY -> throw new IllegalStateException("Index is not in WRITE mode");
      case WRITE -> writer.commit();
    }
  }

  public TopDocs search(Paragraph query, String field, int maxToRetrieve) throws IOException, ParseException {
    TopDocs retrieved = switch (mode) {
      case QUERY -> searcher.search(new QueryParser(field, getAnalyzer()).parse(query.getFulltext()), maxToRetrieve);
      case WRITE -> throw new IllegalStateException("Index is not in QUERY mode");
    };
    logSearchResults(query, retrieved);
    return retrieved;
  }

  protected abstract void logSearchResults(Paragraph paragraph, TopDocs retrieved);

  public Optional<String> getString(int docId, String field) throws IOException {
    Optional<String> result = Optional.empty();
    String s = searcher.doc(docId).getField(field).stringValue();
    if (s != null) {
      result = Optional.of(s);
    }
    return result;
  }

  protected abstract void includeFile(String filename) throws IOException;

  protected Analyzer getAnalyzer() throws IOException {
    return new StandardAnalyzer();
  }

  protected Similarity getSimilarity() {
    return new BM25Similarity();
  }

  protected Mode getMode() {
    return mode;
  }
}
