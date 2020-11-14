package com.leandro.notes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;


import com.leandro.notes.utilities.Utilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class ReadAndEditNote extends AppCompatActivity implements View.OnFocusChangeListener, EditNoteBarClickListener {

    private EditText editContent;
    private EditText editTitle;
    private int id;
    private MediaPlayer  mediaPlayer = new MediaPlayer();
    private MediaRecorder recorder = null;
    private Button playButton;
    private ImageButton btnRecorder;
    int audioID;
    int noteID;
    private int audioCount = 0;
    private ArrayList<String> audioFile = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_and_edit_note);
        Toolbar toolbar = findViewById(R.id.toolbarReadEditNote);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        editContent = findViewById(R.id.editContentNote);
        editTitle = findViewById(R.id.editTitleNote);

        editContent.setOnFocusChangeListener(this);
        editTitle.setOnFocusChangeListener(this);

        boolean toShowNote = getIntent().getBooleanExtra("toShowNote", false);

        //If is to SHOW note DON'T focus text input and set data
        if (toShowNote) {

            ConstraintLayout layoutEditNote = findViewById(R.id.layoutEditNote);

            Bundle data = getIntent().getExtras();
            assert data!=null;
            editTitle.setText(data.getString("titleText"));
            editContent.setText(data.getString("contentText"));
            if (data.getStringArray("audios") != null && data.getStringArray("audios").length != 0) {
                String[] noteAudios = data.getStringArray("audios");
               audioFile.addAll(Arrays.asList(noteAudios));

                for (String ignored : audioFile) {
                    createAudioControls();
                }
            }
            layoutEditNote.setFocusable(true);
            layoutEditNote.setFocusableInTouchMode(true);

        }
    }

    @Override
    public void onBackPressed() {

        if (!editTitle.getText().toString().equals("") || !editContent.getText().toString().equals("") || audioFile.size() > 0)
            createNote();

        super.onBackPressed();
    }


    public void clickHelper(View v){
        editContent.requestFocus();
        editContent.setSelection(editContent.getText().length());

        InputMethodManager keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        keyboard.showSoftInput(editContent ,InputMethodManager.SHOW_IMPLICIT);

    }
    public void createNote(){
        boolean toShowNote = getIntent().getBooleanExtra("toShowNote", false);

        if (toShowNote){
            updateNote();
        }else {
            newNote();
        }
    }

    public void newNote(){

        SharedPreferences autoIncrement = PreferenceManager.getDefaultSharedPreferences(this);
        id = autoIncrement.getInt("ID", 1);

        SQLiteConnectionHelper connection = new SQLiteConnectionHelper(getApplicationContext(), "notes", null, 1);
        SQLiteDatabase db = connection.getWritableDatabase();
        ContentValues values  =  new ContentValues();
        values.put(Utilities.ID_FIELD, id);
        values.put(Utilities.CONTENT_FIELD, editContent.getText().toString());
        values.put(Utilities.TITLE_FIELD, editTitle.getText().toString());
        values.put(Utilities.DATE_FIELD, "01-20-2000");
        values.put(Utilities.USER_FIELD, "mike");
        if (audioFile.size() > 0) {
            String audioList = "";
            for (int i = 0; i < audioFile.size(); i++) {
                audioList += audioFile.get(i) + ",";
            }
            values.put(Utilities.AUDIOS_FIELD, audioList);
        }
        db.insert(Utilities.NOTES_TABLE, Utilities.ID_FIELD, values);
        db.close();

        //increments id +1 and save it in preference
        autoIncrement.edit().putInt("ID", ++id).apply();
    }

    public void updateNote(){
        SQLiteConnectionHelper connection = new SQLiteConnectionHelper(getApplicationContext(), "notes", null, 1);
        SQLiteDatabase db = connection.getWritableDatabase();
        int data = getIntent().getIntExtra("noteID", 1);
        String[] params = {Integer.toString(data)};
        ContentValues values  =  new ContentValues();
        values.put(Utilities.CONTENT_FIELD, editContent.getText().toString());
        values.put(Utilities.TITLE_FIELD, editTitle.getText().toString());
        values.put(Utilities.DATE_FIELD, "01-20-2000");
        if (audioFile.size() > 0) {
            String audioList = "";
            for (int i = 0; i < audioFile.size(); i++) {
                audioList += audioFile.get(i) + ",";
            }
            values.put(Utilities.AUDIOS_FIELD, audioList);
        }else {
            values.put(Utilities.AUDIOS_FIELD, (byte[]) null);
        }
        db.update(Utilities.NOTES_TABLE, values, Utilities.ID_FIELD + "=?", params);
    }

    public void deleteNote(){
        SQLiteConnectionHelper connection = new SQLiteConnectionHelper(getApplicationContext(), "notes", null, 1);
        SQLiteDatabase db = connection.getWritableDatabase();

        int data = getIntent().getIntExtra("noteID", 1);
        String[] params = {Integer.toString(data)};

        db.delete(Utilities.NOTES_TABLE,Utilities.ID_FIELD + "=?", params);

       onBackPressed();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        boolean toShowNote = getIntent().getBooleanExtra("toShowNote", false);

        if (toShowNote) {
            menu.getItem(0).setVisible(false);
            menu.getItem(2).setVisible(false);
            menu.getItem(1).setVisible(true);
        }else{
            menu.getItem(0).setVisible(false);
            menu.getItem(1).setVisible(false);
            menu.getItem(2).setVisible(false);

        }
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            //check if there is no a fragment yet
            if (((ViewGroup)findViewById(R.id.fragment_container)).getChildAt(0) == null){
                getSupportActionBar().hide();
                EditNoteBarFragment editBar = new EditNoteBarFragment();
                FragmentManager manager = getSupportFragmentManager();
                manager.beginTransaction().add(R.id.fragment_container, editBar).commit();
             }
        }
    }

    public void recorder(View view){
        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);


        if (getIntent().getBooleanExtra("toShowNote", false)){
            noteID = getIntent().getIntExtra("noteID", 1);
            audioID = shared.getInt("audioID",1);
        }else {
            noteID = shared.getInt("ID", 1);
            audioID = shared.getInt("audioID",1);
        }


        btnRecorder = findViewById(R.id.recordButton);
        shared.edit().putInt("audioID",++audioID).apply();

        if (recorder == null){
            new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"Notes audio").mkdir();
            audioFile.add(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Notes audio"+ "/record" + noteID + "_" +audioID + ".mp3");

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncodingBitRate(16);
            recorder.setAudioSamplingRate(44100);
            recorder.setOutputFile(audioFile.get(audioCount));

            try {
                recorder.prepare();
                recorder.start();
            }catch (IOException e){
                e.printStackTrace();
            }
            btnRecorder.setImageResource(R.drawable.ic_stop);
        }else{
            recorder.stop();
            recorder.release();
            recorder = null;
            btnRecorder.setImageResource(R.drawable.ic_manual_record);
            createAudioControls();
        }
    }


    private void createAudioControls() {

       final ViewGroup view = findViewById(R.id.noteLayout);
        getLayoutInflater().inflate(R.layout.media_player, view);
       // TextView time =  findViewById(R.id.audioTime);
        //time.setText("101");
        if (audioFile.size() > 0) {

           audioCount++;

            //different id to each audio controller
           final int nId = View.generateViewId();
            view.getChildAt(1 + audioCount).setId(nId);

            //get play and delete buttons
            final ImageButton playAudioButton = (ImageButton) ((ViewGroup)findViewById(nId)).getChildAt(0);
            final ImageButton deleteAudioButton = (ImageButton) ((ViewGroup)findViewById(nId)).getChildAt(1);

            //set the file to play as a tag
            playAudioButton.setTag(audioFile.get(audioCount -1));
            deleteAudioButton.setTag(audioFile.get(audioCount -1));

            playAudioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int length = mediaPlayer.getCurrentPosition();
                    if (mediaPlayer.isPlaying()) {
                        playAudioButton.setImageResource(R.drawable.ic_play_circle_outline);
                        mediaPlayer.pause();
                    }else {

                        //If is paused or has not been started, set pause image and play audio
                        playAudioButton.setImageResource(R.drawable.ic_pause_circle_outline);

                        try {
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(v.getTag().toString());
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    playAudioButton.setImageResource(R.drawable.ic_play_circle_outline);;
                                    mediaPlayer.reset();
                                }
                            });
                            mediaPlayer.prepare();
                            if (length != 0)
                            mediaPlayer.seekTo(length);
                            mediaPlayer.start();


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                }
            });
            deleteAudioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //delete file and then delete audio controller
                   new File(v.getTag().toString()).delete();

                  ViewGroup audioController = ((ViewGroup)v.getParent());
                  ((ViewGroup)audioController.getParent()).removeView(audioController);
                      audioFile.remove(v.getTag().toString());
                }
            });
        }
    }

    public void close(View v){
        findViewById(R.id.recordAudioBar).setVisibility(View.GONE);
    }

    @Override
    public void toolClicked(View btn) {

        switch (btn.getId()){

            case R.id.micButton:
                findViewById(R.id.recordAudioBar).setVisibility(View.VISIBLE);
                break;

            case R.id.drawButton:
                break;

            case R.id.checkboxButton:
                break;

            case R.id.photoButton:
                break;
            case R.id.saveButton:
                onBackPressed();
                break;
        }
    }
}
