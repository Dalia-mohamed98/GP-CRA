package com.cra.cra;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;


public class WelcomeActivity extends AppCompatActivity {
    Button login_button;
    TextView Register_text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.mainblue));
        }
        login_button = findViewById(R.id.openloginbutton);
        Register_text = findViewById(R.id.openregistertext);
    }
    public void openLoginActivity(View view) {
        Intent LoginIntent = new Intent(WelcomeActivity.this,LoginActivity.class);
        startActivity(LoginIntent);
        finish();
    }
    public void openRegisterActivity(View view) {
        Intent RegisterIntent = new Intent(WelcomeActivity.this,RegisterActivity.class);
        startActivity(RegisterIntent);
    }



}
