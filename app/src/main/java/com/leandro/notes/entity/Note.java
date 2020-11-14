package com.leandro.notes.entity;

public class Note {

    private int id;
    private String title;
    private String content;
    private String date;
    private String user;
    private String[] audios;

    public Note(int id, String title, String content,  String[] audios, String date, String user) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.audios = audios;
        this.date = date;
        this.user = user;
    }

    public String[] getAudios() {
        return audios;
    }

    public void setAudios(String[] audios) {
        this.audios = audios;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
