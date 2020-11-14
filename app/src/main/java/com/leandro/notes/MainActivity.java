package com.leandro.notes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.leandro.notes.entity.Note;
import com.leandro.notes.utilities.Utilities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RecyclerViewClickListener {

    private ArrayList<Note> listNotes;
    private RecyclerView recyclerNotes;
    private AdapterNotes adapterNotes;
    private SharedPreferences sharedPreferences;
    private SQLiteConnectionHelper connection;
    private boolean isToSelectFlag = false;
    private ArrayList<Note> selectedNotes = new ArrayList<>();
    private ArrayList<View>  selectedViews = new ArrayList<>();
    private View logo;
    private Menu myMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //tell to Adapter if is grid
        Utilities.IS_GRID_VIEW =  sharedPreferences.getBoolean("isGrid", true);

       logo = toolbar.getChildAt(0);
       logo.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if (isToSelectFlag){

                   for (View view : selectedViews) {
                           view.setBackgroundResource(R.drawable.straggered_recyclerview_item_bg);
                   }

                   //Set toolbar and helpers to initial state
                   getSupportActionBar().setDisplayShowTitleEnabled(false);
                   getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                   logo.setVisibility(View.GONE);
                   myMenu.getItem(0).setVisible(true);
                   myMenu.getItem(1).setVisible(false);
                   myMenu.getItem(2).setVisible(true);


                   selectedViews.clear();
                   selectedNotes.clear();

                   isToSelectFlag = false;
               }
           }
       });
        logo.setVisibility(View.GONE);

        getSupportActionBar().setElevation(0);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createNote = new Intent(getApplicationContext(), ReadAndEditNote.class);
                startActivity(createNote);
            }
        });

        //Create inner data base
        connection = new SQLiteConnectionHelper(this, "notes", null, 1);
    }

    @Override
    protected void onStart() {
        super.onStart();

        createRecyclerView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        myMenu = menu;
        myMenu.getItem(1).setVisible(false);
        MenuItem search =  menu.findItem(R.id.app_bar_search);
        final SearchView searchView = (SearchView) search.getActionView();

        //get pixels of device screen
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        searchView.setMaxWidth(width / 2);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapterNotes.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.delete) {
            deleteNote();
            return true;
        }
        if (id == R.id.toggleView){



            if (sharedPreferences.getBoolean("isGrid", true)) {
                item.setIcon(R.drawable.ic_list_view_black);
                sharedPreferences.edit().putBoolean("isGrid",false).apply();
                Utilities.IS_GRID_VIEW = false;

            }else {
                item.setIcon(R.drawable.ic_grid_view_black);
                sharedPreferences.edit().putBoolean("isGrid",true).apply();
                Utilities.IS_GRID_VIEW = true;
            }

            createRecyclerView();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createRecyclerView() {

        recyclerNotes = findViewById(R.id.recycleNotes);
        //toggle between grid and list view
        if (sharedPreferences.getBoolean("isGrid", true)){
            recyclerNotes.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        }else {
            recyclerNotes.setLayoutManager(new LinearLayoutManager(this));
        }

        listNotes = new ArrayList<>();

        getNotes();

        adapterNotes = new AdapterNotes(listNotes, this);

        recyclerNotes.setAdapter(adapterNotes);
    }

    @Override
    public void onBackPressed(){

        if (isToSelectFlag){

            for (View view : selectedViews) {
                    view.setBackgroundResource(R.drawable.straggered_recyclerview_item_bg);
            }

            //Set toolbar and helpers to initial state
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            logo.setVisibility(View.GONE);
            myMenu.getItem(0).setVisible(true);
            myMenu.getItem(1).setVisible(false);
            myMenu.getItem(2).setVisible(true);


            selectedViews.clear();
            selectedNotes.clear();

            isToSelectFlag = false;
        }else{
            super.onBackPressed();
        }

    }


    public void getNotes(){
        SQLiteDatabase db = connection.getReadableDatabase();

        Note note;

        try {

            Cursor cursor = db.rawQuery("SELECT * FROM " + Utilities.NOTES_TABLE, null);
            //sure that there is a note
           if(cursor.moveToFirst()) {

               do {
                   String[] audios = null;
                   if (cursor.getString(3) != null) {
                       //set in array all audio paths
                       String audiosToSplit = cursor.getString(3);
                       audios = audiosToSplit.split(",");
                   }

                   note = new Note(cursor.getInt(0), cursor.getString(1), cursor.getString(2),
                          audios , cursor.getString(4), cursor.getString(5));

                   listNotes.add(note);
               } while (cursor.moveToNext());
           }
            cursor.close();

           //if there is no notes, show empty notes text
            if (listNotes.size() > 0){
                findViewById(R.id.textEmpty1).setVisibility(View.GONE);
                findViewById(R.id.textEmpty2).setVisibility(View.GONE);
            }else {
                findViewById(R.id.textEmpty1).setVisibility(View.VISIBLE);
                findViewById(R.id.textEmpty2).setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Error getting notes", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void recyclerViewItemClicked(View view, int position) {

        //Click listener after a Long Click
        if (isToSelectFlag){

            if (selectedViews.contains(view)){

                    selectedViews.remove(view);
                    selectedNotes.remove(listNotes.get(position));

                    view.setBackgroundResource(R.drawable.straggered_recyclerview_item_bg);

                getSupportActionBar().setTitle("  " + Integer.toString(selectedViews.size()));

            }else{

                selectedViews.add(view);
                getSupportActionBar().setTitle("  " +Integer.toString(selectedViews.size()));

                //Change color of Linear Layout since Constraint doesn't work
               view.setBackgroundColor(getColor(R.color.selection));

                Note note = listNotes.get(position);
                selectedNotes.add(note);
            }


         //Default Click listener
        }else {

            Intent open = new Intent(getApplicationContext(), ReadAndEditNote.class);
            open.putExtra("toShowNote", true);

            Note note = listNotes.get(position);

            open.putExtra("titleText", note.getTitle());
            open.putExtra("contentText", note.getContent());
            open.putExtra("audios", note.getAudios());
            open.putExtra("noteID", note.getId());

            startActivity(open);
        }
    }

    @Override
    public void recyclerViewItemLongClicked(boolean isToSelect) {
        isToSelectFlag = isToSelect;

        //reset title count
        getSupportActionBar().setTitle("0");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        logo.setVisibility(View.VISIBLE);

        myMenu.getItem(0).setVisible(false);
        myMenu.getItem(1).setVisible(true);
        myMenu.getItem(2).setVisible(false);


    }

    public void deleteNote(){

        SQLiteDatabase db = connection.getWritableDatabase();

        for (Note note: selectedNotes) {
            String[] params = {Integer.toString(note.getId())};

            db.delete(Utilities.NOTES_TABLE,Utilities.ID_FIELD + "=?", params);
            listNotes.remove(note);
        }
        //set toolbar and helpers to initial state
        db.close();
        logo.setVisibility(View.GONE);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myMenu.getItem(0).setVisible(true);
        myMenu.getItem(1).setVisible(false);
        myMenu.getItem(2).setVisible(true);


        selectedViews.clear();
        selectedNotes.clear();

        isToSelectFlag = false;

        createRecyclerView();

    }

    public void showPopUpProfile(View view){
        //inflate layout of pop up window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popUpView = inflater.inflate(R.layout.pop_up_profile, null);

        //create pop up window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // taps outside popup dismiss it
        final PopupWindow popupWindow = new PopupWindow(popUpView, width,height,focusable);
        popupWindow.setElevation(20);

        //show popup window
        popupWindow.showAtLocation(recyclerNotes, Gravity.TOP,0,0);
    }

    public void openLogin(View view){
        Intent open = new Intent(getApplicationContext(), Login.class);
        startActivity(open);
    }
}
