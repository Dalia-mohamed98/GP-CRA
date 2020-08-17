package com.cra.cra;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class BookWithSameNameActivity extends AppCompatActivity {
    String Book_Name;
    String Book_Category;
    String Book_Voice;
    String Book_Description;

    TextView Book_Name_TEXT;
    TextView Book_Desc_TEXT;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_with_same_name);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.grey));
        }
        Bundle extras = getIntent().getExtras();
        Book_Name = extras.getString("Book_Name");
        Book_Category = extras.getString("Book_Category");
        Book_Voice = extras.getString("Book_Voice");
        Book_Description = extras.getString("Book_Description");

        Book_Name_TEXT = findViewById(R.id.Same_Book_Name);
        Book_Desc_TEXT = findViewById(R.id.Same_Book_Desc);

        Book_Name_TEXT.setText(Book_Name.substring(0,Book_Name.length()-7));
        String b_desc = Book_Description;
        if (b_desc.length() > 100)
            b_desc = b_desc.substring(0,100) + "...";
        Book_Desc_TEXT.setText(b_desc);
    }

    public void on_click_same(View view){
        Intent InfoIntent = new Intent(BookWithSameNameActivity.this,BookInfoAndDownloadActivity.class);
        InfoIntent.putExtra("Book_Name",Book_Name);
        InfoIntent.putExtra("Book_Category",Book_Category);
        InfoIntent.putExtra("Book_Voice",Book_Voice);
        InfoIntent.putExtra("Book_Description",Book_Description);
        InfoIntent.putExtra("Post_Position",1);
        startActivity(InfoIntent);
    }
}
