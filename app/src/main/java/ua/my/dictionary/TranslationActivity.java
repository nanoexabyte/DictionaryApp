package ua.my.dictionary;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;


public class TranslationActivity extends ActionBarActivity {

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


            tvTranslate.setText(cursor.getString(key_word_index) + " - " + cursor.getString(translated_word_index));

        }
    }


}
