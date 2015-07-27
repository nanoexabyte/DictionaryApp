package ua.my.dictionary;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private SearchView searchView;
    MenuItem actionProgressItem;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        listView = (ListView) findViewById(R.id.listView);

        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.onActionViewExpanded();
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                showResults(query);
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.trim().length() == 0) hideProgressBar();
                showResults(newText);
                return false;
            }

        });

        showHistory();
    }


    public void showProgressBar() {
        actionProgressItem.setVisible(true);
    }

    public void hideProgressBar() {
        actionProgressItem.setVisible(false);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        actionProgressItem = menu.findItem(R.id.miActionProgress);
        ProgressBar v =  (ProgressBar) MenuItemCompat.getActionView(actionProgressItem);
        return super.onPrepareOptionsMenu(menu);
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

   private  void loadWordFromAPI(String word){

       DictionaryRestClient api = new DictionaryRestClient();
       String lang="ru-en";
       if(IsEnglish.IS_ENGLISH) lang="en-ru";
       api.service.getWordTranslate(api.KEY_API, lang, word, new Callback<TranslateWordModel>() {
           public void success(TranslateWordModel arg0, Response arg1) {


               ContentValues cv = new ContentValues();
               if (IsEnglish.IS_ENGLISH) {
                   cv.put(DictionaryDB.KEY_EN, arg0.getText());
                   cv.put(DictionaryDB.KEY_RU, arg0.getTranslate());
               } else {
                   cv.put(DictionaryDB.KEY_EN, arg0.getTranslate());
                   cv.put(DictionaryDB.KEY_RU, arg0.getText());
               }

               Uri newUri = getContentResolver().insert(DictionaryContentProvider.CONTENT_URI, cv);
               hideProgressBar();
               showResults(searchView.getQuery().toString());

           }

           public void failure(RetrofitError arg0) {
               hideProgressBar();
               arg0.printStackTrace();
           }
       });
     ;

   }


    private  String[] reverse(String[] input) {


        for (int i = 0; i < input.length / 2; i++) {
            String temp = input[i];

            input[i] = input[input.length - 1 - i];
            input[input.length - 1 - i] = temp;
        }
        return input;

    }

    private void showHistory(){
        StringBuilder sb = new StringBuilder();
        try
        {

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    openFileInput(TranslationActivity.FILENAME)));
            String str = "";

            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            String listHistory[] =  sb.toString().split(" ");

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1,reverse(listHistory));
            if(sb.toString().split(" ").length!=0)
                listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                   showResults(((TextView) view).getText().toString());
                }
            });



        } catch (Exception e) {
             e.printStackTrace();
        }






}
    private void showResults(String query) {

        Cursor cursor = managedQuery(DictionaryContentProvider.CONTENT_URI, null, null,
                new String[]{query}, null);
        IsEnglish.isEnglish(query);
        if (cursor == null) {

            listView.setAdapter(null);
            if(query.trim().length()!=0){
                showProgressBar();

            }
            loadWordFromAPI(query);



        } else {



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
                    Log.d("id:", String.valueOf(id));
                    if (IsEnglish.IS_ENGLISH) wordIntent.putExtra("language", "en");
                    else wordIntent.putExtra("language", "ru");

                    startActivity(wordIntent);
                }
            });
        }
    }
}
