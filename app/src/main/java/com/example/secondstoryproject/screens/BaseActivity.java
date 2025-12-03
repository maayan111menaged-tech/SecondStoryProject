package com.example.secondstoryproject.screens;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;


public class BaseActivity extends AppCompatActivity {
    protected DatabaseService databaseService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        ///get the instance of the database service
        databaseService = DatabaseService.getInstance();
    }
}
