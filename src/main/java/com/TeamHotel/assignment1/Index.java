package com.TeamHotel.assignment1;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.Data.Page;
import edu.unh.cs.treccar_v2.Data.Section;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.nio.file.FileSystems;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONObject;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;


public class Index {
    public static boolean createNewOutlineIndex(final List<String> cborFiles, final String index) {
        // adapted from https://github.com/TREMA-UNH/trec-car-tools/blob/master/trec-car-tools-example/src/main/java/edu/unh/cs/TrecCarBuildLuceneIndex.java
        System.setProperty("file.encoding", "UTF-8");
        try {
            final IndexWriter indexWriter = new IndexWriter(
                FSDirectory.open(
                    FileSystems.getDefault().getPath(index)), 
                new IndexWriterConfig(new StandardAnalyzer()));
            int i = 0;
            for (String documentCBOR: cborFiles) {
                final FileInputStream documentIStream  = new FileInputStream(new File(documentCBOR));
                for (final Iterator<Data.Page> pageIterator = DeserializeData.iterAnnotations(documentIStream); pageIterator.hasNext();) {
                    final Page page = pageIterator.next();

                    final Document doc = new Document();
                    
                    final StringBuilder categories = new StringBuilder();
                    for (String category: page.getPageMetadata().getCategoryNames()) {
                        categories.append(category).append(" ");
                    }
                    doc.add(new TextField("rawtext", categories.toString(), Field.Store.YES));
                    doc.add(new StringField("pageid", page.getPageId(), Field.Store.YES));
                    StringBuilder sections = new StringBuilder();
                    for (Section section: page.getChildSections()) {
                        sections.append(section.getHeading()).append(",");
                    }
                    final String sectionText = Preprocess.indexSections(sections.toString());
                    doc.add(new StringField("sections", sectionText, Field.Store.YES));

                    indexWriter.addDocument(doc);
                    if (i % 10000 == 0) {
                        indexWriter.commit();
                    }
                    i++;
                }
                indexWriter.commit();
            }
            indexWriter.commit();
            indexWriter.close();

            System.out.println("Created index at " + index);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean createNewJsonPageIndex(final List<String> cborFiles, final String index) {
        // adapted from https://github.com/TREMA-UNH/trec-car-tools/blob/master/trec-car-tools-example/src/main/java/edu/unh/cs/TrecCarBuildLuceneIndex.java
        System.setProperty("file.encoding", "UTF-8");
        try {
            final IndexWriter indexWriter = new IndexWriter(
                FSDirectory.open(
                    FileSystems.getDefault().getPath(index)), 
                new IndexWriterConfig(new StandardAnalyzer()));
            int i = 0;
            for (String documentJson: cborFiles) {
                final Scanner documentScanner = new Scanner(new FileInputStream(new File(documentJson)));
                while (documentScanner.hasNext()) {
                    final JSONObject json = new JSONObject(documentScanner.nextLine()); 
                    Article article = Article.fromJson(json);

                    if (article == null) {
                        continue;
                    }

                    final Document doc = new Document();

                    doc.add(new TextField("fulltext", article.getFulltext(), Field.Store.YES));
                    doc.add(new StringField("pageid", article.getPageId(), Field.Store.YES));
                    doc.add(new StringField("headings", article.getSectionsAsString(), Field.Store.YES));

                    indexWriter.addDocument(doc);
                    if (i % 500 == 0) {
                        indexWriter.commit();
                        System.err.println("Parsed " + i + " documents");
                    }
                    i++;
                }
                indexWriter.commit();
            }
            indexWriter.close();

            System.out.println("Created index at " + index);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
