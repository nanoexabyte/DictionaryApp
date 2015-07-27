package ua.my.dictionary;


import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by Andrew Ponomarev on 7/26/2015.
 */

public interface IYandexDictionaryAPI {


    @GET("/dicservice.json/lookup")
    public void getWordTranslate(@Query("key") String KEY_API,
                                 @Query("lang") String lang,
                                 @Query("text") String text,
                                 Callback<TranslateWordModel> response);
}
