package com.swayam.whatsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.random.customdialog.CustomDialog;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameField,passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ParseInstallation.getCurrentInstallation().saveInBackground();

        usernameField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);

    }

    public void login(View view) {
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        if (username.equals("") || password.equals("")){
            showErrorMessage("Username and Password can not be empty.");
            return;
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Please wait...");
        dialog.show();

        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                dialog.dismiss();
                if (e == null){
                    showSuccessMessage("login credentials are correct");
                }else {
                    showErrorMessage(e.getMessage());
                }
            }
        });

    }

    private void showErrorMessage(String message){
        CustomDialog dialog = new CustomDialog(this,CustomDialog.FAILURE);
        dialog.setTitle("FAILED");
        dialog.setContentText(message);
        dialog.show();
    }

    private void showSuccessMessage(String message){
        CustomDialog dialog = new CustomDialog(this,CustomDialog.SUCCESS);
        dialog.setTitle("SUCCESS");
        dialog.setContentText(message);
        dialog.setPositiveButton("Ok", null);
        dialog.setNegativeButton("Cancel",null);
        dialog.show();
    }

    public void signUp(View view) {
        startActivity(new Intent(this,RegisterActivity.class));
        finish();
    }
}