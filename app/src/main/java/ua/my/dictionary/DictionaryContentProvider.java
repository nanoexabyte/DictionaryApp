package ua.my.dictionary;

/**
 * Created by Andrew Ponomarev on 7/25/2015.
 */

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class DictionaryContentProvider extends ContentProvider {

    public static final String WORDS_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
            "/vnd.my.dictionary";
    public static final String WORD_TRANSLATE_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd.my.dictionary";
    private static final int SEARCH_WORDS = 0;
    private static final int GET_WORD = 1;
    private static final int SEARCH_SUGGEST = 2;
    private static final int REFRESH_SHORTCUT = 3;
    private static final UriMatcher sURIMatcher = buildUriMatcher();
    public static String AUTHORITY = "ua.my.dictionary.DictionaryContentProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/DictionaryDB");
    private DictionaryDB dictionaryDB;

    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(AUTHORITY, "DictionaryDB", SEARCH_WORDS);
        matcher.addURI(AUTHORITY, "DictionaryDB/#", GET_WORD);

        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);

        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", REFRESH_SHORTCUT);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        dictionaryDB = new DictionaryDB(getContext());
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        if (selectionArgs != null) IsEnglish.isEnglish(selectionArgs[0]);
        switch (sURIMatcher.match(uri)) {
            case SEARCH_SUGGEST:
                if (selectionArgs == null) {
                    throw new IllegalArgumentException(
                            "Аргументы для выборки отсутсвуют в Uri: " + uri);
                }
                return getSuggestions(selectionArgs[0]);
            case SEARCH_WORDS:
                if (selectionArgs == null) {
                    throw new IllegalArgumentException(
                            "Аргументы для выборки отсутсвуют в Uri: " + uri);
                }
                return search(selectionArgs[0]);
            case GET_WORD:
                return getWord(uri);
            case REFRESH_SHORTCUT:
                return refreshShortcut(uri);
            default:
                throw new IllegalArgumentException("Неизвестный Uri: " + uri);
        }
    }

    private Cursor getSuggestions(String query) {
        query = query.toLowerCase();
        String[] columns;

        if (IsEnglish.IS_ENGLISH) {
            columns = new String[]{
                    BaseColumns._ID,
                    DictionaryDB.KEY_EN,
                    DictionaryDB.KEY_RU,
                    SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID};
        } else {
            columns = new String[]{
                    BaseColumns._ID,
                    DictionaryDB.KEY_RU,
                    DictionaryDB.KEY_EN,
                    SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID};

        }
        return dictionaryDB.getWordMatches(query, columns, IsEnglish.IS_ENGLISH);
    }

    private Cursor search(String query) {
        query = query.toLowerCase();
        String[] columns;

        if (IsEnglish.IS_ENGLISH) {
            columns = new String[]{
                    BaseColumns._ID,
                    DictionaryDB.KEY_EN,
                    DictionaryDB.KEY_RU};

        } else {
            columns = new String[]{
                    BaseColumns._ID,
                    DictionaryDB.KEY_RU,
                    DictionaryDB.KEY_EN};

        }

        return dictionaryDB.getWordMatches(query, columns, IsEnglish.IS_ENGLISH);
    }

    private Cursor getWord(Uri uri) {
        String rowId = uri.getLastPathSegment();
        String[] columns;
        if (IsEnglish.IS_ENGLISH) {
            columns = new String[]{
                    BaseColumns._ID,
                    DictionaryDB.KEY_EN,
                    DictionaryDB.KEY_RU};
        } else {
            columns = new String[]{
                    BaseColumns._ID,
                    DictionaryDB.KEY_RU,
                    DictionaryDB.KEY_EN};
        }

        return dictionaryDB.getWord(rowId, columns);
    }

    private Cursor refreshShortcut(Uri uri) {
        String rowId = uri.getLastPathSegment();
        String[] columns;
        if (IsEnglish.IS_ENGLISH) {
            columns = new String[]{
                    BaseColumns._ID,
                    DictionaryDB.KEY_EN,
                    DictionaryDB.KEY_RU,
                    SearchManager.SUGGEST_COLUMN_SHORTCUT_ID,
                    SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID};
        } else {
            columns = new String[]{
                    BaseColumns._ID,
                    DictionaryDB.KEY_RU,
                    DictionaryDB.KEY_EN,
                    SearchManager.SUGGEST_COLUMN_SHORTCUT_ID,
                    SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID};
        }

        return dictionaryDB.getWord(rowId, columns);
    }


    @Override
    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case SEARCH_WORDS:
                return WORDS_MIME_TYPE;
            case GET_WORD:
                return WORD_TRANSLATE_MIME_TYPE;
            case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
            case REFRESH_SHORTCUT:
                return SearchManager.SHORTCUT_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Неизвестный URL " + uri);
        }
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

}