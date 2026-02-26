package com.example.secondstoryproject.screens;

import android.os.Bundle;

import com.example.secondstoryproject.R;

public class navpageActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // טוען רק את התוכן הייחודי של המסך
        setContentView(R.layout.activity_navpage);
    }
}