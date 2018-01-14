package com.android.zhxie.pittsourcejobsearch;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Study {
  public static void main(String args[]) throws Exception {
    if (args.length < 3) {
      showHelpAndExit();
      return;
    }

    if (args[0].equalsIgnoreCase("index")) {
      index(args[1], args[2]);
    } else if (args[0].equalsIgnoreCase("search")) {
      search(args[1], args[2]);
    } else if (args[0].equalsIgnoreCase("suggest")) {
      suggest(args[1], args[2]);
    }

  }

  static void showHelpAndExit() {
    System.err.println("Usage: Study [index|search|suggest] arguments...");
    System.err.println("    index <source JSON> <index path>");
    System.err.println("    search <index path> <query>");
    System.err.println("    suggest <index path> <keyword(s)>");
    System.exit(1);
  }

  static void index(String sourcePath, String indexPath) {
    File dataFile = new File(sourcePath);
    if (!dataFile.exists()) {
      System.err.println("JSON source not found: " + sourcePath);
      System.exit(1);
    }

    if (dataFile.length() > Integer.MAX_VALUE) {
      System.exit(1);
    }

    try (FileInputStream stream = new FileInputStream(sourcePath)) {
      importData(stream, indexPath, true);
    } catch (Exception e) {
      // Should not happen
      e.printStackTrace();
      System.exit(1);
    }
  }

  static public int importData(InputStream stream, String indexPath, boolean withSuggestion) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final int bufSize = 4096;
    byte[] buf = new byte[bufSize];
    int read;
    while ((read = stream.read(buf)) > 0) {
      baos.write(buf, 0, read);
    }
    String dataStr = new String(baos.toByteArray(), "UTF-8");

    List<PostingsItem> postings = new ArrayList<>();

    JSONArray jsonArray = new JSONArray(dataStr);

    for (int i = 0, len = jsonArray.length(); i < len; i++) {          //EDIT, read in the json file
      JSONObject entry = jsonArray.getJSONObject(i);
      String url = entry.getString("url");
      String title = entry.getString("title");
      String datePosted = entry.getString("datePosted");
      String text = entry.getString("text");

      PostingsItem posting = new PostingsItem(url, title, datePosted, text);
      postings.add(posting);
    }

    Indexer indexer = new Indexer(indexPath, false);
    indexer.addPostings(postings);
    indexer.close();

    if (withSuggestion) {
      Suggester.rebuild(indexPath);
    }

    return postings.size();
  }

  static void search(String indexPath, String query) throws Exception {
    Searcher searcher = new Searcher(indexPath);
    SearchResult result = searcher.search(query, null, 10);

    for (PostingsItem posting : result.postings) {
      System.out.println("title   : " + result.getHighlightedTitle(posting));
      System.out.println("url    : " + posting.url);
      System.out.println("datePosted  : " + posting.datePosted);
      System.out.println("text  : " + result.getHighlightedText(posting));
      System.out.println();
    }

    searcher.close();
  }

  static void suggest(String indexPath, String query) throws Exception {
    Suggester suggester = new Suggester(indexPath);
    List<String> suggestions = suggester.suggest(query);
    for (String text : suggestions) {
      System.out.println("Suggestion: " + text);
    }
    suggester.close();
  }
}
