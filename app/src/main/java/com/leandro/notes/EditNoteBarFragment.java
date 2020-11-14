package com.leandro.notes;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;

import java.io.IOException;


public class EditNoteBarFragment extends Fragment implements View.OnClickListener {

    private ImageButton micButton;
    private ImageButton drawButton;
    private ImageButton checboxButton;
    private ImageButton photoButton;
    private Button saveButton;

    public EditNoteBarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_edit_note_bar, container, false);

        micButton = view.findViewById(R.id.micButton);
        drawButton = view.findViewById(R.id.drawButton);
        checboxButton = view.findViewById(R.id.checkboxButton);
        photoButton = view.findViewById(R.id.photoButton);
        saveButton = view.findViewById(R.id.saveButton);

        micButton.setOnClickListener(this);
        drawButton.setOnClickListener(this);
        checboxButton.setOnClickListener(this);
        photoButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);

        return view;
    }

    private void noPermissionRequired(View view){

        ((EditNoteBarClickListener)getActivity()).toolClicked(view);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.micButton:

                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1000);
                }else {
                    ((EditNoteBarClickListener)getActivity()).toolClicked(v);
                }
                break;

            case R.id.drawButton:
                noPermissionRequired(v);
                break;

            case R.id.checkboxButton:
                noPermissionRequired(v);
                break;

            case R.id.photoButton:
                break;

            case R.id.saveButton:
                noPermissionRequired(v);
                break;
        }
    }
}
