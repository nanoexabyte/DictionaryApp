package ua.my.dictionary;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import retrofit.RestAdapter;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;

/**
 * Created by Andrew Ponomarev on 7/26/2015.
 */
public class DictionaryRestClient {

   final String END_POINT_URL = "https://dictionary.yandex.net/api/v1";
   public final String KEY_API="dict.1.1.20150726T075245Z.9286952278bb7822.ca59f5a209a08ff0a0c46cda6edca96a173892a9";

    public class TranslateModelDeserializer implements JsonDeserializer<TranslateWordModel>
    {
        @Override
        public TranslateWordModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            TranslateWordModel word = new TranslateWordModel();
            JsonObject jsonObject = json.getAsJsonObject();
            JsonArray jsonArray= jsonObject.getAsJsonArray("def");
            jsonObject=jsonArray.get(0).getAsJsonObject();

            word.setText(jsonObject.get("text").getAsString());
            jsonArray = jsonObject.getAsJsonArray("tr");
            word.setTranslate(jsonArray.get(0).getAsJsonObject().get("text").getAsString());

            return word;
        }
    }
    public Converter getGsonConverter() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(TranslateWordModel.class, new TranslateModelDeserializer())
                .create();
        return  new GsonConverter(gson);
    }
    RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint(END_POINT_URL)
            .setConverter(getGsonConverter())
            .build();

    public IYandexDictionaryAPI service = restAdapter.create(IYandexDictionaryAPI.class);




}
