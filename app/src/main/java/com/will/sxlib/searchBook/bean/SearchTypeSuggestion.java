package com.will.sxlib.searchBook.bean;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

/**
 * Created by Will on 2016/5/29.
 */
public class SearchTypeSuggestion implements SearchSuggestion {
    private String typeName = "";
    public static final Creator<SearchTypeSuggestion> CREATOR = new Creator<SearchTypeSuggestion>() {
        @Override
        public SearchTypeSuggestion createFromParcel(Parcel source) {
            return new SearchTypeSuggestion(source);
        }

        @Override
        public SearchTypeSuggestion[] newArray(int size) {
            return new SearchTypeSuggestion[size];
        }
    };
    public SearchTypeSuggestion(String typeName){
        this.typeName = typeName;
    }
    public SearchTypeSuggestion(Parcel source){
        this.typeName = source.readString();
    }
    public void setTypeName(String typeName){
        this.typeName = typeName;
    }
    public String getTypeName(){
        return typeName;
    }
    @Override
    public String getBody(){
        return typeName;
    }
    @Override
    public Creator getCreator(){
        return CREATOR;
    }
    @Override
    public void writeToParcel(Parcel dest,int flags){
        dest.writeString(typeName);
    }
    @Override
    public int describeContents(){
        return 0;
    }
}
