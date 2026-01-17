package com.example.plantdisease;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class NotepadActivity extends AppCompatActivity {

    private EditText notepad;
    private Button saveBtn, clearBtn, backBtn;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "AcreIntelligenceNotes";
    private static final String NOTE_KEY = "saved_notes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notepad);

        notepad = findViewById(R.id.notepad);
        saveBtn = findViewById(R.id.saveBtn);
        clearBtn = findViewById(R.id.clearBtn);
        backBtn = findViewById(R.id.backBtn);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Load saved notes
        loadNotes();

        saveBtn.setOnClickListener(v -> saveNotes());
        clearBtn.setOnClickListener(v -> clearNotes());
        backBtn.setOnClickListener(v -> finish());
    }

    private void loadNotes() {
        String savedNotes = prefs.getString(NOTE_KEY, "");
        notepad.setText(savedNotes);
        notepad.setSelection(savedNotes.length()); // Move cursor to end
    }

    private void saveNotes() {
        String notes = notepad.getText().toString();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(NOTE_KEY, notes);
        editor.apply();

        Toast.makeText(this, "✓ Notes saved!", Toast.LENGTH_SHORT).show();
    }

    private void clearNotes() {
        notepad.setText("");
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(NOTE_KEY, "");
        editor.apply();

        Toast.makeText(this, "Notes cleared", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Auto-save when leaving
        saveNotes();
    }
}