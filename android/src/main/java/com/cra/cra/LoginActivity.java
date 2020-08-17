package com.cra.cra;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    TextView Register_text;
    EditText Email_Text;
    EditText Pass_Text;
    String Email;
    String Pass;
    Button Login_Btn;
    DatabaseReference firebaseReference;
    DB_Local db_local;
    ProgressDialog ProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.mainblue));
        }
        Register_text = findViewById(R.id.openregistertext1);
        Email_Text = findViewById(R.id.login_email_text);
        Pass_Text = findViewById(R.id.login_pass_text);
        Login_Btn = findViewById(R.id.Login_BTN);
        firebaseReference = FirebaseDatabase.getInstance().getReference("Accounts");
        db_local = new DB_Local(this);
    }
    public void openRegister_Activity(View view) {
        Intent RegisterIntent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(RegisterIntent);
        finish();
    }
    private String encrypt(String p){
        String e = "";
        int k = p.length()-1;
        for (int i=0;i<p.length();i++)
        {
            e = e + "!@SD$3" + p.charAt(i) + "G" + p.charAt(k) + "/8_" + e +
                    "!@SD$3" + p.charAt(k) + "l;e" + p.charAt(i) + "!";
            k--;
        }
        return e;
    }
    private void show_toast(String message){
        Toast toast=Toast.makeText(LoginActivity.this,message,Toast.LENGTH_SHORT);
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
    public void Login_click(View view){
        if (!isConnected()) {
            show_toast("No Internet Connection!");
            return;
        }
        ProgressDialog = new ProgressDialog(this);
        ProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        ProgressDialog.setTitle("Logging in...");
        ProgressDialog.setProgress(0);
        ProgressDialog.setCanceledOnTouchOutside(false);
        ProgressDialog.setCancelable(false);
        ProgressDialog.show();
        Email = Email_Text.getText().toString().toLowerCase();
        Pass = Pass_Text.getText().toString();
        Pass = encrypt(Pass);
        //see if there is a book with the same name:
        Query query = firebaseReference.orderByChild("email").equalTo(Email);
        query.addListenerForSingleValueEvent(valueEventListener);
    }
    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) { // if there is an account with the same email:
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String p = snapshot.child("password").getValue().toString();
                    if (!p.equals(Pass))
                    {
                        ProgressDialog.dismiss();
                        show_toast("Password is incorrect!");
                        return;
                    }
                    Login_Btn.setEnabled(false);
                    Intent CategoryIntent = new Intent(LoginActivity.this,CategoryActivity.class);
                    startActivity(CategoryIntent);
                    db_local.Login_db(Email,Pass,snapshot.child("book_names").getValue().toString(),
                            snapshot.child("using_server").getValue().toString(),
                            snapshot.child("key").getValue().toString(),snapshot.child("first_name").getValue().toString(),
                            snapshot.child("last_name").getValue().toString());
                    finishAffinity();
                    show_toast("Login success!");
                    finish();
                }
            }
            else {
                ProgressDialog.dismiss();
                show_toast("Email is not registered!");
            }

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    @Override
    protected void onDestroy() {
        try {
            ProgressDialog.dismiss();
        }catch (Exception e){int x = 0;}
        super.onDestroy();
    }
}
