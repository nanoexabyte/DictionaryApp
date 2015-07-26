package ua.my.dictionary;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {
    private TextView textView;
    private ListView listView;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.text);
        listView = (ListView) findViewById(R.id.listView);
        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.onActionViewExpanded();


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                showResults(query);
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                showResults(newText);
                return false;
            }

        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.btn_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showResults(String query) {

        Cursor cursor = managedQuery(DictionaryContentProvider.CONTENT_URI, null, null,
                new String[]{query}, null);
        IsEnglish.isEnglish(query);
        if (cursor == null) {
            textView.setText(getString(R.string.no_results, new Object[]{query}));
            listView.setAdapter(null);

        } else {

            int count = cursor.getCount();
            String countString = getResources().getQuantityString(R.plurals.search_results,
                    count, new Object[]{count, query});
            textView.setText(countString);

            String[] from;
            if (IsEnglish.IS_ENGLISH) from = new String[]{DictionaryDB.KEY_EN, DictionaryDB.KEY_RU};
            else from = new String[]{DictionaryDB.KEY_RU, DictionaryDB.KEY_EN};
            int[] to = new int[]{R.id.key_word, R.id.translated_word};


            SimpleCursorAdapter words =
                    new SimpleCursorAdapter(this, R.layout.result, cursor, from, to);

            listView.setAdapter(words);


            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Intent wordIntent = new Intent(MainActivity.this, TranslationActivity.class);
                    IsEnglish.isEnglish(((TextView) view.findViewById(R.id.key_word)).getText().toString());
                    wordIntent.putExtra("id", String.valueOf(id));

                    if (IsEnglish.IS_ENGLISH) wordIntent.putExtra("language", "en");
                    else wordIntent.putExtra("language", "ru");

                    startActivity(wordIntent);
                }
            });
        }
    }
}
