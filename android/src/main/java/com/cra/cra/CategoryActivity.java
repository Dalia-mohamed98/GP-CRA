package com.cra.cra;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class CategoryActivity extends AppCompatActivity {

    private String category;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.grey));
        }
    }
    public void Business_cat(View view){
        category = "Business";
        open_posts_activity();
    }
    public void History_cat(View view){
        category = "History";
        open_posts_activity();
    }
    public void Literature_cat(View view){
        category = "Literature";
        open_posts_activity();
    }
    public void Health_cat(View view){
        category = "Health";
        open_posts_activity();
    }
    public void Story_cat(View view){
        category = "Story";
        open_posts_activity();
    }
    public void Science_cat(View view){
        category = "Science";
        open_posts_activity();
    }
    public void My_cat(View view){
        category = "My_Books";
        open_posts_activity();
    }
    private void open_posts_activity(){
        Intent Posts_activity = new Intent(CategoryActivity.this,PostsRecycleActivity.class);
        Posts_activity.putExtra("category", category);
        startActivity(Posts_activity);
    }
}
