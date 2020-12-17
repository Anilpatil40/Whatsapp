package com.swayam.whatsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.random.customdialog.CustomDialog;

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameField,passwordField,emailField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);
        emailField = findViewById(R.id.email);

    }

    public void signUp(View view) {

        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();
        String email = emailField.getText().toString();

        if (username.equals("") || password.equals("") || email.equals("")){
            showErrorMessage("Username,Password and Email can not be empty.");
            return;
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Please wait...");
        dialog.show();

        ParseUser parseUser = new ParseUser();
        parseUser.setEmail(email);
        parseUser.setUsername(username);
        parseUser.setPassword(password);

        parseUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                dialog.dismiss();
                if (e == null){
                    showSuccessMessage("User created successfully please login");
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
        dialog.setPositiveButton("Log In", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(null);
            }
        });
        dialog.setNegativeButton("Re Try",null);
        dialog.show();
    }

    private void showSuccessMessage(String message){
        CustomDialog dialog = new CustomDialog(this,CustomDialog.SUCCESS);
        dialog.setTitle("SUCCESS");
        dialog.setContentText(message);
        dialog.setPositiveButton("Log In", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(null);
            }
        });
        dialog.setNegativeButton("Cancel",null);
        dialog.show();
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