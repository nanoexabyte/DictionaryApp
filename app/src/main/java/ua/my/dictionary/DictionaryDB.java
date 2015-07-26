package ua.my.dictionary;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by Andrew Ponomarev on 7/25/2015.
 */
public class DictionaryDB {
    public static final String KEY_EN = SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String KEY_RU = SearchManager.SUGGEST_COLUMN_TEXT_2;
    private static final String DB_TAG = "DictionaryDB";
    private static final String DB_NAME = "DictionaryDB";
    private static final String FTS_VIRTUAL_TABLE = "FTS_DB";
    private static final int DB_VERSION = 1;
    private static final HashMap<String, String> columnMap = buildColumnMap();
    private final DictionaryDBOpenHelper dbOpenHelper;


    public DictionaryDB(Context context) {
        dbOpenHelper = new DictionaryDBOpenHelper(context);
    }


    private static HashMap<String, String> buildColumnMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(KEY_EN, KEY_EN);
        map.put(KEY_RU, KEY_RU);
        map.put(BaseColumns._ID, "rowid AS " +
                BaseColumns._ID);
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        return map;
    }


    public Cursor getWord(String rowId, String[] columns) {
        String selection = "rowid = ?";
        String[] selectionArgs = new String[]{rowId};

        return query(selection, selectionArgs, columns);
    }


    public Cursor getWordMatches(String query, String[] columns, boolean isEn) {
        String selection = KEY_RU + " MATCH ?";
        if (isEn) selection = KEY_EN + " MATCH ?";
        String[] selectionArgs = new String[]{query + "*"};
        return query(selection, selectionArgs, columns);


    }


    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);
        builder.setProjectionMap(columnMap);
        Cursor cursor = builder.query(dbOpenHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    private static class DictionaryDBOpenHelper extends SQLiteOpenHelper {

        private final Context helperContext;
        private SQLiteDatabase database;

        DictionaryDBOpenHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            helperContext = context;
        }

        private static final String FTS_TABLE_CREATE =
                "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                        " USING fts3 (" +
                        KEY_EN + ", " +
                        KEY_RU + ");";

        @Override
        public void onCreate(SQLiteDatabase db) {
            database = db;
            database.execSQL(FTS_TABLE_CREATE);
            loadDictionary();
        }

        private void loadDictionary() {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        loadWordsFromFile();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }

        private void loadWordsFromFile() throws IOException {
            Log.d(DB_TAG, "Загрузка слов из файла...");
            final Resources resources = helperContext.getResources();
            InputStream inputStream = resources.openRawResource(R.raw.translate);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] strings = TextUtils.split(line, "-");
                    if (strings.length < 2) continue;
                    long id = addWord(strings[0].trim(), strings[1].trim());
                    if (id < 0) {
                        Log.e(DB_TAG, "Невозможно добавить слово: " + strings[0].trim());
                    }
                }
            } finally {
                reader.close();
            }
            Log.d(DB_TAG, "Загрузка слов завершена.");
        }

        public long addWord(String en, String ru) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(KEY_EN, en);
            initialValues.put(KEY_RU, ru);

            return database.insert(FTS_VIRTUAL_TABLE, null, initialValues);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            onCreate(db);
        }


    }

}
