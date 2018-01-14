package com.android.zhxie.pittsourcejobsearch;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;

import java.util.List;

public class SearchResult {
  public final int totalHits;
  public final List<PostingsItem> postings;
  final ScoreDoc lastScoreDoc;
  final Query query;
  final Sort sort;
  final HighlightingHelper highlightingHelper;

  SearchResult(int totalHits, List<PostingsItem> postings, ScoreDoc lastScoreDoc, Query query, Sort sort,
               HighlightingHelper highlightingHelper) {
    this.totalHits = totalHits;
    this.postings = postings;
    this.lastScoreDoc = lastScoreDoc;
    this.query = query;
    this.sort = sort;
    this.highlightingHelper = highlightingHelper;
  }

  public boolean hasMore() {
    return lastScoreDoc != null;
  }

  public String getHighlightedUrl(PostingsItem posting) {
    highlightingHelper.setFragmentLength(HighlightingHelper.DEFAULT_FRAGMENT_LENGTH);
    return highlightingHelper.highlightOrOriginal(Indexer.URL_FIELD_NAME, posting.url);
  }

  public String getHighlightedTitle(PostingsItem posting) {
    highlightingHelper.setFragmentLength(HighlightingHelper.DEFAULT_FRAGMENT_LENGTH);
    return highlightingHelper.highlightOrOriginal(Indexer.TITLE_FIELD_NAME, posting.title);
  }

  public String getHighlightedDatePosted(PostingsItem posting) {
    highlightingHelper.setFragmentLength(HighlightingHelper.DEFAULT_FRAGMENT_LENGTH);
    return highlightingHelper.highlightOrOriginal(Indexer.DATEPOSTED_FILED_NAME, posting.datePosted);
  }

  public String getHighlightedText(PostingsItem posting) {
    highlightingHelper.setFragmentLength(HighlightingHelper.DEFAULT_FRAGMENT_LENGTH);
    return highlightingHelper.highlightOrOriginal(Indexer.TEXT_FIELD_NAME, posting.text);

  }


}
