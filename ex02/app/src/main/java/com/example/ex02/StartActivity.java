package com.example.ex02;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_home_24);
    }

    public void onClick(View view){
        Intent intent = new Intent(this, MainActivity.class);
        if(view.getId() == R.id.btn1){
            intent = new Intent(this, MainActivity.class);
        } else if (view.getId() == R.id.btn2) {
            intent = new Intent(this, MainActivity2.class);
        }
        startActivity(intent);
    }
}