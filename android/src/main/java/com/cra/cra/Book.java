package com.cra.cra;

import java.util.List;

public class Book {
    String name;
    String voice;
    String category;
    String time;
    String uploaded;
    String description;

    public Book(String name, String voice, String category, String time, String uploaded, String description) {
        this.name = name;
        this.voice = voice;
        this.category = category;
        this.time = time;
        this.uploaded = uploaded;
        this.description = description;
    }

    public Book(String name, String voice, String category,String description) {
        this.name = name;
        this.voice = voice;
        this.category = category;
        this.description = description;
        this.time = "";
        this.uploaded = "FALSE";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUploaded() {
        return uploaded;
    }

    public void setUploaded(String uploaded) {
        this.uploaded = uploaded;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



}
