package com.will.sxlib.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Will on 2016/5/20.
 */
public class Book implements Serializable{
    private String title;
    private String titleHref;
    private List<Gcxx> gcxx;
    private String publisher;
    private String publicationDate;
    private String author;
    private String index;
    private String isbn;
    private String type;
    private String coverUrl;
    private String bookNumber;
    public void setTitle(String title){
        this.title = title;
    }
    public String getTitle(){
        return title;
    }
    public void setTitleHref(String titleHref){
        this.titleHref = titleHref;
    }
    public String getTitleHref(){
        return titleHref;
    }
    public void setGcxx(List<Gcxx> gcxx){
        this.gcxx = gcxx;
    }
    public List<Gcxx> getGcxx(){
        return gcxx;
    }
    public void setPublisher(String publisher){
        this.publisher = publisher;
    }
    public String getPublisher(){
        return publisher;
    }
    public void setPublicationDate(String publicationDate){
        this.publicationDate = publicationDate;
    }
    public String getPublicationDate(){
        return publicationDate;
    }
    public void setAuthor(String author){
        this.author = author;
    }
    public String getAuthor(){
        return author;
    }
    public void setIndex(String index){
        this.index = index;
    }
    public String getIndex(){
        return index;
    }
    public void setIsbn(String isbn){
        this.isbn = isbn;
    }
    public String getIsbn(){
        return isbn;
    }
    public void setType(String type){
        this.type = type;
    }
    public String getType(){
        return type;
    }
    public void setCoverUrl(String coverUrl){
        this.coverUrl = coverUrl;
    }
    public String getCoverUrl(){
        return coverUrl;
    }
    public void setBookNumber(String bookNumber){
        this.bookNumber = bookNumber;
    }
    public String getBookNumber(){
        return bookNumber;
    }
}
