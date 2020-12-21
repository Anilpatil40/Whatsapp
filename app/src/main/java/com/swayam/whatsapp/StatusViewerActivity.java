package com.swayam.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class StatusViewerActivity extends AppCompatActivity {
    private PhotoView photoView;
    private TextView textView;
    private ProgressBar progressBar;
    private String objectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_viewer);

        objectId = getIntent().getStringExtra("status");

        photoView = findViewById(R.id.photoView);
        textView = findViewById(R.id.tag);
        progressBar = findViewById(R.id.progressBar);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Statuses");
        query.whereEqualTo("objectId",objectId);

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("loading Status...");
        dialog.show();

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                dialog.dismiss();
                if (e == null){
                    try {
                        ParseObject parseObject = objects.get(0);
                        textView.setText(parseObject.getString("tag"));
                        byte[] data = parseObject.getParseFile("image").getData();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        photoView.setImageBitmap(bitmap);
                        startStatusViewing();
                    }catch (Exception exception){
                        onBackPressed();
                    }
                }else {
                    onBackPressed();
                }
            }
        });

    }

    public void startStatusViewing(){
        progressBar.setMax(6000);

        CountDownTimer timer = new CountDownTimer(6000,10) {

            @Override
            public void onTick(long millisUntilFinished) {
                progressBar.setProgress(6000 - (int) millisUntilFinished);
            }

            @Override
            public void onFinish() {
                onBackPressed();
            }
        };
        timer.start();

        ParseUser parseUser = ParseUser.getCurrentUser();
        parseUser.add("status_viewed",objectId);

        parseUser.saveInBackground();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
