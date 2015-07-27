package ua.my.dictionary;

/**
 * Created by Andrew Ponomarev on 7/26/2015.
 */
public class TranslateWordModel {
    private String text;
    private String translate;

    public TranslateWordModel(String text, String tr) {
        this.translate = tr;
        this.text=text;
    }
    public TranslateWordModel() {

    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTranslate() {
        return translate;
    }

    public void setTranslate(String translate) {
        this.translate = translate;
    }



}
