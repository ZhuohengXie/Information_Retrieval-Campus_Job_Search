package com.android.zhxie.pittsourcejobsearch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class PostingsActivity extends Activity {
    static final String TAG = MainActivity.class.getSimpleName();
    static final String DATA_SOURCE = "postings.json";
    static final String INDEX_DIR_NAME = "index";

    ArrayAdapter<Result> itemsAdapter;
    ListView listView;
    View statusOuterView;
    TextView statusText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postings);

        statusText = (TextView) findViewById(R.id.status_text);
        statusOuterView = findViewById(R.id.status_outer_view);

        itemsAdapter = new ResultAdapter(this, new ArrayList<Result>());
        listView = (ListView) findViewById(R.id.search_results_list);
        listView.setAdapter(itemsAdapter);



//        File file = itemsAdapter.getContext().getCacheDir();
//        Directory mainIndexDir = FSDirectory.open(file);

        setStatus(getString(R.string.welcome_search_text));
    }



//    public String readFileAsString() {
//        String jsonString = "";
//        try {
//            InputStream is = getResources().openRawResource(R.raw.postings);
//            Writer writer = new StringWriter();
//            char[] buffer = new char[1024];
//            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
//            int n;
//            while ((n = reader.read(buffer)) != -1) {
//                writer.write(buffer, 0, n);
//            }
//            is.close();
//            jsonString = writer.toString();
//        } catch (IOException e) {
//            Log.e("io", e.toString());
//        }
//        Log.d("jsonstring: ", jsonString);
//        return jsonString;
//    }




    @Override
    protected void onStart() {
        super.onStart();
        rebuildIndexIfNotExists();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

//        BufferedReader reader = null;
//        try {
//            reader = new BufferedReader(
//                    new InputStreamReader(getAssets().open("index.txt")));
//
//            // do reading, usually loop until end of file reading
//            String mLine;
//            while ((mLine = reader.readLine()) != null) {
//                //process line
//                ...
//            }
//        } catch (IOException e) {
//            //log the exception
//        } finally {
//            if (reader != null) {
//                try {
//                    reader.close();
//                } catch (IOException e) {
//                    //log the exception
//                }
//            }
//        }





    searchView.setQueryHint(

    getString(R.string.search_hint)

    );


    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()

    {
        @Override
        public boolean onQueryTextSubmit (String s){
        try {
            Searcher searcher = new Searcher(getIndexRootDir().getAbsolutePath());
            SearchResult result = searcher.search(s, 20);
            List<Result> results = Result.fromSearchResult(result);
            searcher.close();

            itemsAdapter.clear();
            itemsAdapter.addAll(results);
            itemsAdapter.notifyDataSetChanged();

            if (results.size() == 0) {
                setStatus(getString(R.string.query_no_results_msg));
            } else {
                setStatus(null);
            }

            searchView.clearFocus();
        } catch (ParseException e) {
            setStatus(getString(R.string.query_no_results_msg));
                    Toast.makeText(PostingsActivity.this, R.string.query_parsing_error_msg, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    setStatus(getString(R.string.query_no_results_msg));
                    Toast.makeText(PostingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_rebuild_index:
                itemsAdapter.clear();
                itemsAdapter.notifyDataSetChanged();
                rebuildIndex();
                return true;
//            case R.id.action_about:
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://zhxie.org/mobilelucene/android"));
//                startActivity(intent);
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void setStatus(String text) {
        if (text == null) {
            statusOuterView.setVisibility(View.INVISIBLE);
            statusText.setText("");
            listView.setVisibility(View.VISIBLE);
        } else {
            statusOuterView.setVisibility(View.VISIBLE);
            statusText.setText(text);
            listView.setVisibility(View.INVISIBLE);
        }
    }

    static class Result {
        final SearchResult searchResult;
        final PostingsItem posting;
        final String title;
        final String url;
        final String datePosted;
        final String text;

        Result(SearchResult searchResult, PostingsItem posting) {
            this.searchResult = searchResult;
            this.posting = posting;
            url = searchResult.getHighlightedUrl(posting);
            title = searchResult.getHighlightedTitle(posting);
            datePosted = searchResult.getHighlightedDatePosted(posting);
            text = searchResult.getHighlightedText(posting);


        }

        static List<Result> fromSearchResult(SearchResult searchResult) {
            ArrayList<Result> results = new ArrayList<>();
            for (PostingsItem posting : searchResult.postings) {
                results.add(new Result(searchResult, posting));
            }
            return results;
        }
    }

    static class ViewHolder {
        TextView title;
        TextView url;
        TextView datePosted;
        TextView text;
    }

    File getIndexRootDir() {
        return new File(getCacheDir(), INDEX_DIR_NAME);
    }

    void rebuildIndex() {
        final ProgressDialog dialog = ProgressDialog.show(this, getString(R.string.rebuild_index_progress_title), getString(R.string.rebuild_index_progress_message), true);
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    InputStream is = PostingsActivity.this.getAssets().open(DATA_SOURCE);
                    Study.importData(is, getIndexRootDir().getAbsolutePath(), false);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                dialog.dismiss();

                if (result) {
                    setStatus(getString(R.string.welcome_search_text));
                } else {
                    Toast.makeText(PostingsActivity.this, R.string.rebuild_index_failed_msg, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    void rebuildIndexIfNotExists() {
        if (!getIndexRootDir().exists()) {
            rebuildIndex();
        }
    }

    class ResultAdapter extends ArrayAdapter<Result> {
        public ResultAdapter(Context context, List<Result> results) {
            super(context, R.layout.result_item, results);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Result result = getItem(position);
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = getLayoutInflater().inflate(R.layout.result_item, parent, false);
                viewHolder.title = (TextView) convertView.findViewById(R.id.item_title);
                viewHolder.url = (TextView) convertView.findViewById(R.id.item_url);
                viewHolder.datePosted = (TextView) convertView.findViewById(R.id.item_datePosted);
                viewHolder.text = (TextView) convertView.findViewById(R.id.item_text);

                // Make source clickable.
                viewHolder.url.setMovementMethod(LinkMovementMethod.getInstance());

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.title.setText(result.title);
            viewHolder.url.setText(result.url);
            viewHolder.datePosted.setText(result.datePosted);
            viewHolder.text.setText(result.text);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(PostingsActivity.this, DetailActivity.class);
//                    intent.putExtra(DetailActivity.EXTRA_TITLE, result.title);
                    startActivity(intent);
                }
            });

            return convertView;
        }
    }
}
