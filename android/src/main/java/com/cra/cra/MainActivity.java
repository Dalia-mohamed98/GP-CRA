package com.cra.cra;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 2000;
    private DB_Local db_local;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.mainorange));
        }
        db_local = new DB_Local(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String curr_user = db_local.get_curr_user();
                if (curr_user.equals("")){
                    Intent welcomeIntent = new Intent(MainActivity.this,WelcomeActivity.class);
                    startActivity(welcomeIntent);
                    finish();
                }
                else {
                    Intent categoryIntent = new Intent(MainActivity.this,CategoryActivity.class);
                    startActivity(categoryIntent);
                    finish();
                }

            }
        },SPLASH_TIME_OUT);
    }
}
