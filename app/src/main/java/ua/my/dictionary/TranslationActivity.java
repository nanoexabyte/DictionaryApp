package ua.my.dictionary;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;


public class TranslationActivity extends ActionBarActivity {
    final static String FILENAME="LastWords";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation);
        Uri uri = Uri.withAppendedPath(DictionaryContentProvider.CONTENT_URI, getIntent().getStringExtra("id"));

        Cursor cursor = managedQuery(uri, null, null, null, null);

        if (cursor == null) {
            finish();
        } else {
            cursor.moveToFirst();

            TextView tvTranslate = (TextView) findViewById(R.id.tvTranslate);

            int key_word_index = cursor.getColumnIndexOrThrow(DictionaryDB.KEY_RU);
            int translated_word_index = cursor.getColumnIndexOrThrow(DictionaryDB.KEY_EN);

            if (getIntent().getStringExtra("language").equals("en")) {
                key_word_index = cursor.getColumnIndexOrThrow(DictionaryDB.KEY_EN);
                translated_word_index = cursor.getColumnIndexOrThrow(DictionaryDB.KEY_RU);
            }


            try {

                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                        openFileOutput(FILENAME, MODE_APPEND)));

                bw.write(cursor.getString(key_word_index)+" ");

                bw.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            tvTranslate.setText(cursor.getString(key_word_index) + " - " + cursor.getString(translated_word_index));

        }
    }



}
