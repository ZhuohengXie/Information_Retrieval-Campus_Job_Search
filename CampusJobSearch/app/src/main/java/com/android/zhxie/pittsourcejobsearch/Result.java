package com.android.zhxie.pittsourcejobsearch;

import java.util.ArrayList;
import java.util.List;

public class Result {
    final SearchResult searchResult;
    final PostingsItem posting;
    final String url;
    final String title;
    final String datePosted;
    final String text;


    Result(SearchResult searchResult, PostingsItem posting) {
        this.searchResult = searchResult;
        this.posting = posting;
        url = searchResult.getHighlightedUrl(posting);
        datePosted = posting.datePosted;
        title = searchResult.getHighlightedTitle(posting);
        text = posting.text;
    }

    static List<Result> fromSearchResult(SearchResult searchResult) {
        ArrayList<Result> results = new ArrayList<>();
        for (PostingsItem posting : searchResult.postings) {
            results.add(new Result(searchResult, posting));
        }
        return results;
    }
}