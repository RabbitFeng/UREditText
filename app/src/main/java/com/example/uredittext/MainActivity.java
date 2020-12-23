package com.example.uredittext;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            final UREditText urEditText = findViewById(R.id.et_ur);
            findViewById(R.id.btn_redo).setOnClickListener(v -> urEditText.redo());
            findViewById(R.id.btn_undo).setOnClickListener(v -> urEditText.undo());
            urEditText.setOnUpdateEnableListener((canUndo, canRedo) -> {
                findViewById(R.id.btn_undo).setEnabled(canUndo);
                findViewById(R.id.btn_redo).setEnabled(canRedo);
            });
        }
    }
}
