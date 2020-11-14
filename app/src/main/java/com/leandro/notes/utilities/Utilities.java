package com.leandro.notes.utilities;

public class Utilities {

    //toogle button global variable
    public static boolean IS_GRID_VIEW;


    //Constants notes table
    public static final String NOTES_TABLE = "notes";
    public static final String ID_FIELD = "id";
    public static final String TITLE_FIELD = "title";
    public static final String CONTENT_FIELD = "content";
    public static final String AUDIOS_FIELD = "audios";
    public static final String DATE_FIELD = "date";
    public static final String USER_FIELD = "user";




    public static final String CREATE_TABLE_NOTES = "CREATE TABLE "+NOTES_TABLE+" ("+ID_FIELD+" INTEGER, "+TITLE_FIELD+" TEXT, "+CONTENT_FIELD+" TEXT, "+AUDIOS_FIELD+" TEXT, "+DATE_FIELD+" DATE, "+USER_FIELD+" TEXT)";



}
