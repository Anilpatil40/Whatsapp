package com.swayam.whatsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void signUp(View view) {
    }

    public void login(View view) {
        startActivity(new Intent(this,LoginActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        login(null);
    }
}