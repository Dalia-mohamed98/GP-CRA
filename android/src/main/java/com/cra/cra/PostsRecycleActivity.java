package com.cra.cra;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PostsRecycleActivity extends AppCompatActivity implements Posts_Adapter.OnPostListener{
    private RecyclerView recyclerView;
    private Posts_Adapter posts_adapter;
    private ArrayList<Book>posts;
    private DB_Local db_local;
    private DatabaseReference firebaseReference;
    private String category;
    private EditText Search_TextBOX;
    private String Search_Text;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_recycle);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.mainblue));
        }
        db_local = new DB_Local(this);
        firebaseReference = FirebaseDatabase.getInstance().getReference("Books");
        progressBar = findViewById(R.id.loading_posts_progress);
        progressBar.setVisibility(View.VISIBLE);
        Bundle extras = getIntent().getExtras();
        category = extras.getString("category");


        recyclerView = findViewById(R.id.recycleview1);
        posts = new ArrayList<>();
        //Add posts to the list:
        add_posts();
        Search_TextBOX = findViewById(R.id.search_text);
        Search_Text = Search_TextBOX.getText().toString();
        Search_TextBOX.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Search_Text = Search_TextBOX.getText().toString().toLowerCase();
                add_posts();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        if (!isConnected())
        {
            show_toast("No Internet Connection!");
            progressBar.setVisibility(View.GONE);
        }
    }
    private boolean isConnected() {
        final String command = "ping -c 1 google.com";
        try {
            return Runtime.getRuntime().exec(command).waitFor() == 0;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public void openAddBook_Activity(View view) {
        if (db_local.get_curr_user_server().equals("TRUE"))
            show_toast("Cannot convert more than one book at the same time!");
        else
            {
            Intent AddBookIntent = new Intent(PostsRecycleActivity.this, AddBookActivity.class);
            startActivity(AddBookIntent);
            }
    }

    private void show_toast(String message){
        Toast toast=Toast.makeText(PostsRecycleActivity.this,message,Toast.LENGTH_SHORT);
        View view =toast.getView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C69E9D")));
        }
        TextView toastMessage = (TextView) toast.getView().findViewById(android.R.id.message);
        toastMessage.setTextColor(Color.parseColor("#485E64"));
        toastMessage.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM,
                0, 50);
        toast.show();
    }
    public void logout_click(View view) {
        if (db_local.get_curr_user_server().equals("TRUE"))
            show_toast("Cannot logout while converting a book!");
        else
            {
            show_toast("Logged out!");
            Intent WelcomeIntent = new Intent(PostsRecycleActivity.this, WelcomeActivity.class);
            startActivity(WelcomeIntent);
            db_local.Logout_db();
            finishAffinity();
            finish();
        }
    }

    //sorting
    public Boolean B1_older_than_B2(Book B1,Book B2) {
        Double B1_time = Double.parseDouble(B1.getTime());
        Double B2_time = Double.parseDouble(B2.getTime());
        if (B1_time < B2_time)
            return true;
        return false;
    }
    int partition(List<Book> arr, int low, int high)
    {
        Book pivot = arr.get(high);
        int i = (low-1); // index of smaller element
        for (int j=low; j<high; j++)
        {
            // If current element is smaller than the pivot
            if (!B1_older_than_B2(arr.get(j), pivot))
            {
                i++;
                // swap arr[i] and arr[j]
                Book temp = arr.get(i);
                arr.set(i,arr.get(j));
                arr.set(j,temp);
            }
        }

        // swap arr[i+1] and arr[high] (or pivot)
        Book temp = arr.get(i+1);
        arr.set(i+1,arr.get(high));
        arr.set(high,temp);
        return i+1;
    }
    public void sort(List<Book> arr, int low, int high)
    {
        if (low < high)
        {
            int pi = partition(arr, low, high);
            // Recursively sort elements before
            // partition and after partition
            sort(arr, low, pi-1);
            sort(arr, pi+1, high);
        }
    }
    private void add_posts(){
        posts.clear();
        if (category.equals("My_Books"))
        {
            String[] book_names = db_local.get_curr_user_books().split("\n");
            for (String b_nme : book_names){
                Query query = firebaseReference.orderByChild("name").equalTo(b_nme);
                query.addListenerForSingleValueEvent(valueEventListener);
            }
            Query query = firebaseReference.orderByChild("uploaded").equalTo("Maf4_7aga_htrg3 mn hna");
            query.addListenerForSingleValueEvent(valueEventListener);
        }
        else
        {
            Query query = firebaseReference.orderByChild("uploaded").equalTo("TRUE");
            query.addListenerForSingleValueEvent(valueEventListener);
        }
    }
    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(category.equals("My_Books"))
            {
                if (dataSnapshot.exists()) { // if there is a book with the same name:
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.child("uploaded").getValue().toString().equals("TRUE")){
                            Book p = new Book(
                                    snapshot.child("name").getValue().toString(),
                                    snapshot.child("voice").getValue().toString(),
                                    snapshot.child("category").getValue().toString(),
                                    snapshot.child("time").getValue().toString(),
                                    snapshot.child("uploaded").getValue().toString(),
                                    snapshot.child("description").getValue().toString());
                            if (Search_Text.equals(""))
                            {
                                boolean exists = false;
                                for (Book post : posts){
                                    if (post.getName().equals(p.getName()))
                                    {
                                        exists = true;
                                        break;
                                    }
                                }
                                if (!exists)
                                {
                                    posts.add(p);
                                }
                            }
                            else if (p.getName().substring(0,p.getName().length()-7).contains(Search_Text) ||
                                    p.getDescription().contains(Search_Text)) {
                                boolean exists = false;
                                for (Book post : posts) {
                                    if (post.getName().equals(p.getName())) {
                                        exists = true;
                                        break;
                                    }
                                }
                                if (!exists) {
                                    posts.add(p);
                                }
                            }
                        }
                    }
                }
                else {//if snap shot empty (no more books)
                    sort(posts,0,posts.size()-1);
                    set_Adapt();
                    recyclerView.setLayoutManager(new LinearLayoutManager(PostsRecycleActivity.this));
                    recyclerView.setAdapter(posts_adapter);
                    progressBar.setVisibility(View.GONE);
                }
                return;
            }
            else
            {
                posts.clear();
            }
            if (dataSnapshot.exists()) { // if there is a book with the same name:
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("category").getValue().toString().equals(category)){
                        Book p = new Book(
                                snapshot.child("name").getValue().toString(),
                                snapshot.child("voice").getValue().toString(),
                                snapshot.child("category").getValue().toString(),
                                snapshot.child("time").getValue().toString(),
                                snapshot.child("uploaded").getValue().toString(),
                                snapshot.child("description").getValue().toString());
                        if (Search_Text.equals("") ||
                                p.getName().substring(0,p.getName().length()-7).contains(Search_Text) ||
                                p.getDescription().contains(Search_Text))
                                    posts.add(p);
                    }
                }
            }
            sort(posts,0,posts.size()-1);
            set_Adapt();
            recyclerView.setLayoutManager(new LinearLayoutManager(PostsRecycleActivity.this));
            recyclerView.setAdapter(posts_adapter);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    public void set_Adapt(){
        posts_adapter = new Posts_Adapter(PostsRecycleActivity.this,posts,this);
    }
    @Override
    public void OnPostClick(int position) {
        Book b = posts.get(position);
        Intent InfoIntent = new Intent(PostsRecycleActivity.this,BookInfoAndDownloadActivity.class);
        InfoIntent.putExtra("Book_Name",b.getName());
        InfoIntent.putExtra("Book_Category",b.getCategory());
        InfoIntent.putExtra("Book_Voice",b.getVoice());
        InfoIntent.putExtra("Book_Description",b.getDescription());
        InfoIntent.putExtra("Post_Position",position);
        startActivity(InfoIntent);
    }
}
