package com.android.zhxie.pittsourcejobsearch;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.lukhnos.portmobile.file.Path;
import org.lukhnos.portmobile.file.Paths;
//import org.lukhnos.portmobile.file.Path;
//import org.lukhnos.portmobile.file.Paths;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Searcher implements AutoCloseable {
  final Analyzer analyzer;
  final IndexReader indexReader;

  public Searcher(String indexRoot) throws IOException {
    Path indexRootPath = Paths.get(indexRoot);

//    File path = new File("/Users/zhxie/GitHub/PittSourceJobSearch/app/src/main/assets/index");

    analyzer = Indexer.getAnalyzer();
    Directory mainIndexDir = FSDirectory.open(Indexer.getMainIndexPath(indexRootPath));
    indexReader = DirectoryReader.open(mainIndexDir);
  }

  public SearchResult search(String queryStr, int maxCount) throws ParseException, IOException {
    return search(queryStr, null, maxCount);
  }

  public SearchResult search(String queryStr, SortBy sortBy, int maxCount)
      throws ParseException, IOException {
    String[] fields = { Indexer.URL_FIELD_NAME, Indexer.TEXT_FIELD_NAME };   //EDIT
    QueryParser parser = new MultiFieldQueryParser(fields, analyzer);
    Query query = parser.parse(queryStr);

    Sort sort = null;
    if (sortBy != null) {
      sort = sortBy.sort;
    }

    return searchAfter(null, query, sort, maxCount);
  }

  public SearchResult searchAfter(SearchResult result, int maxCount) throws IOException {
    if (!result.hasMore()) {
      throw new AssertionError("No more search results to be fetched after this");
    }

    return searchAfter(result.lastScoreDoc, result.query, result.sort, maxCount);
  }

  @Override
  public void close() throws Exception {
    indexReader.close();
  }

  SearchResult searchAfter(ScoreDoc lastScoreDoc, Query query, Sort sort, int maxCount)
      throws IOException {
    if (maxCount < 1) {
      throw new AssertionError("maxCount must be at least 1, but instead: " + maxCount);
    }

    IndexSearcher searcher = new IndexSearcher(indexReader);

    TopDocs topDocs;
    int actualMaxCount = maxCount + 1;
    if (lastScoreDoc == null) {
      if (sort == null) {
        topDocs = searcher.search(query, actualMaxCount);
      } else {
        topDocs = searcher.search(query, actualMaxCount, sort);
      }
    } else {
      if (sort == null) {
        topDocs = searcher.searchAfter(lastScoreDoc, query, actualMaxCount);
      } else {
        topDocs = searcher.searchAfter(lastScoreDoc, query, actualMaxCount, sort);
      }
    }

    ScoreDoc nextSearchAfterDoc = null;
    int topDocsLen;
    if (topDocs.scoreDocs.length > maxCount) {
      nextSearchAfterDoc = topDocs.scoreDocs[maxCount - 1];
      topDocsLen = maxCount;
    } else {
      topDocsLen = topDocs.scoreDocs.length;
    }

    HighlightingHelper highlightingHelper = new HighlightingHelper(query, analyzer);

    List<PostingsItem> postings = new ArrayList<>();
    for (int i = 0; i < topDocsLen; i++) {
      org.apache.lucene.document.Document luceneDoc = indexReader.document(topDocs.scoreDocs[i].doc);
      PostingsItem posting = Indexer.fromLuceneDocument(luceneDoc);
      postings.add(posting);
    }

    return new SearchResult(topDocs.totalHits, postings, nextSearchAfterDoc, query, sort, highlightingHelper);
  }

  public enum SortBy {
    RELEVANCE(Sort.RELEVANCE),
    DOCUMENT_ORDER(Sort.INDEXORDER),
    URL(new Sort(new SortField(Indexer.URL_FIELD_NAME, SortField.Type.STRING))),
    DATEPOSTED(new Sort(new SortField(Indexer.DATEPOSTED_FILED_NAME, SortField.Type.STRING)));

    final Sort sort;
    SortBy(Sort sort) {
      this.sort = sort;
    }
  }
}
