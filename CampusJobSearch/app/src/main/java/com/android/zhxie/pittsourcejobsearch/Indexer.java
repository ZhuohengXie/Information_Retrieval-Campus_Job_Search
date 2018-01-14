package com.android.zhxie.pittsourcejobsearch;


import android.content.res.AssetManager;
import android.util.Log;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.json.JSONObject;
import org.lukhnos.portmobile.file.Path;
import org.lukhnos.portmobile.file.Paths;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

/**
 * Indexer for the document index. This class also defines the "schema" of the document index: It
 * defines field names and decides the Lucene field type to use when indexing. It also provides
 * an internal converter from Lucene documents to our Document objects.
 */
public class Indexer implements AutoCloseable {
  static final String URL_FIELD_NAME = "url";
  static final String DATEPOSTED_FILED_NAME = "datePosted";
  static final String TITLE_FIELD_NAME = "title";
  static final String TEXT_FIELD_NAME = "text";

  static final String INDEX_NAME = "index";

  final IndexWriter indexWriter;

  /**
   * Create a new document index.
   *
   * @param indexRoot The parent directory inside which the index lives.
   * @throws IOException
   */
  public Indexer(String indexRoot) throws IOException {
    this(indexRoot, false);
  }

  /**
   * Create or open a document index
   *
   * @param indexRoot The parent directory inside which the index lives.
   * @param appendIfExists If true, the index will be opened for appending new documents.
   * @throws IOException
   */
  public Indexer(String indexRoot, boolean appendIfExists) throws IOException {







//    File filesDir = getFilesDir();
//    Scanner input = new Scanner(new File(filesDir, "index"));
//    File path = new File("/Users/zhxie/GitHub/PittSourceJobSearch/app/src/main/assets/index");
//    Analyzer analyzer = getAnalyzer();

//    try {
//      // Create a URL for the desired page
//      URL url = new URL("https://www.dropbox.com/s/fqis2n3j2ff9teh/postings.json?dl=0");
//
//
//      // Read all the text returned by the server
//      BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
//      String str;
//      while ((str = in.readLine()) != null) {
//        // str is one line of text; readLine() strips the newline character(s)
//      }
//      in.close();
//    } catch (MalformedURLException e) {
//    } catch (IOException e) {
//    }
    Path indexRootPath = Paths.get(indexRoot);
    Analyzer analyzer = getAnalyzer();



    Directory mainIndexDir = FSDirectory.open(getMainIndexPath(indexRootPath));

    IndexWriterConfig mainIndexWriterConfig = new IndexWriterConfig(analyzer);

    if (appendIfExists) {
      mainIndexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
    } else {
      mainIndexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    }

    indexWriter = new IndexWriter(mainIndexDir, mainIndexWriterConfig);
  }

  public static Analyzer getAnalyzer() {
    return new EnglishAnalyzer();
  }

  static Integer getInteger(org.apache.lucene.document.Document luceneDoc, String fieldName) {
    IndexableField field = luceneDoc.getField(fieldName);
    if (field != null) {
      Number number = field.numericValue();
      if (number != null) {
        return number.intValue();
      }
    }
    return null;
  }

  static PostingsItem fromLuceneDocument(org.apache.lucene.document.Document luceneDoc) {
    String url = luceneDoc.get(URL_FIELD_NAME);
    String datePosted = luceneDoc.get(DATEPOSTED_FILED_NAME);
    String title = luceneDoc.get(TITLE_FIELD_NAME);
    String text = luceneDoc.get(TEXT_FIELD_NAME);

    return new PostingsItem(url, title, datePosted, text);
  }

  static Path getMainIndexPath(Path indexRoot) {
    return indexRoot.resolve(INDEX_NAME);
  }

  @Override
  public void close() throws Exception {
    indexWriter.close();
  }

  public void addPostings(List<PostingsItem> postings) throws IOException {
    // Reuse doc and field instances. See http://wiki.apache.org/lucene-java/ImproveIndexingSpeed
    Field urlField = new TextField(URL_FIELD_NAME, "", Field.Store.YES);
    Field urlDocsValueField = new SortedDocValuesField(URL_FIELD_NAME, new BytesRef(0));

    Field titleField = new TextField(TITLE_FIELD_NAME, "", Field.Store.YES);
    Field datePostedField = new TextField(DATEPOSTED_FILED_NAME, "", Field.Store.YES);
    Field textField = new StringField(TEXT_FIELD_NAME, "", Field.Store.YES);

    for (PostingsItem posting : postings) {
      org.apache.lucene.document.Document luceneDoc = new org.apache.lucene.document.Document();

      if (posting.url != null && !posting.url.isEmpty()) {
        urlField.setStringValue(posting.url);
        luceneDoc.add(urlField);

        urlDocsValueField.setBytesValue(new BytesRef(posting.url));
        luceneDoc.add(urlDocsValueField);
      }

      if (posting.title != null && !posting.title.isEmpty()) {
        titleField.setStringValue(posting.title);
        luceneDoc.add(titleField);
      }

      if (posting.datePosted != null && !posting.datePosted.isEmpty()) {
        datePostedField.setStringValue(posting.datePosted);
        luceneDoc.add(datePostedField);
      }

      if (posting.text != null && !posting.text.isEmpty()) {
        textField.setStringValue(posting.text);
        luceneDoc.add(textField);
      }

      indexWriter.addDocument(luceneDoc);
    }

    indexWriter.commit();
  }
}
