package com.cra.cra;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    TextView Login_text;
    EditText FName_text;
    EditText LName_text;
    EditText Email_Text;
    EditText Pass_Text;
    EditText confirm_Pass_text;
    DatabaseReference firebaseReference;

    String FName;
    String LName;
    String Email;
    String Pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Login_text = findViewById(R.id.openlogintext);
        FName_text = findViewById(R.id.regFName_text);
        LName_text = findViewById(R.id.regLname_text);
        Email_Text = findViewById(R.id.regEmail_text);
        Pass_Text = findViewById(R.id.regpass_text);
        confirm_Pass_text = findViewById(R.id.regconfirm_pass_text);
        firebaseReference = FirebaseDatabase.getInstance().getReference("Accounts");

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.mainblue));
        }
    }
    public void openLogin_Activity(View view) {
        Intent LoginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(LoginIntent);
        finish();
    }
    private void show_toast(String message){
        Toast toast=Toast.makeText(RegisterActivity.this,message,Toast.LENGTH_SHORT);
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
    public void Register(View view){
        if (!isConnected())
        {
            show_toast("No Internet Connection!");
            return;
        }
        FName = FName_text.getText().toString();
        LName = LName_text.getText().toString();
        Email = Email_Text.getText().toString();
        Pass = Pass_Text.getText().toString();
        String Confirm_Pass = confirm_Pass_text.getText().toString();

        if (FName.equals(""))
        {
            show_toast("Please, Enter your first name!");
            return;
        }
        if (!FName.matches("[a-zA-Z]+"))
        {
            show_toast("First name can only contain letters!");
            return;
        }
        if (FName.length() > 12)
        {
            show_toast("First name exceeds maximum number of characters(12)!");
            return;
        }



        if (LName.equals(""))
        {
            show_toast("Please, Enter your last name!");
            return;
        }
        if (!LName.matches("[a-zA-Z]+"))
        {
            show_toast("Last name can only contain letters!");
            return;
        }
        if (FName.length() > 12)
        {
            show_toast("Last name exceeds maximum number of characters(12)!");
            return;
        }



        if (Email.equals(""))
        {
            show_toast("Please, Enter your email!");
            return;
        }
        if (!Email.matches("^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$"))
        {
            show_toast("Invalid email address!");
            return;
        }
        if (Email.length() > 25)
        {
            show_toast("Email exceeds maximum number of characters(25)!");
            return;
        }


        if (Pass.equals(""))
        {
            show_toast("Please, Enter your password!");
            return;
        }
        if (!Pass.matches("((?=.*[a-z])(?=.*[A-Z]).{6,16})"))
        {
            show_toast("Password is weak!");
            return;
        }

        if (!Pass.matches(".*\\d.*"))
        {
            show_toast("Password is weak!");
            return;
        }
        if (Confirm_Pass.equals(""))
        {
            show_toast("Please, Confirm your password!");
            return;
        }
        if (!Confirm_Pass.equals(Pass))
        {
            show_toast("Please, Password confirmation is not the same as password!");
            return;
        }
        Pass = encrypt(Pass);

        FName = FName.toLowerCase();
        LName = LName.toLowerCase();
        Email = Email.toLowerCase();
        //see if there is a book with the same name:
        Query query = firebaseReference.orderByChild("email").equalTo(Email);
        query.addListenerForSingleValueEvent(valueEventListener);
    }
    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) { // if there is an account with the same email:
                show_toast("Email is already taken by another user!");
            }
            else {// if the account email is unique:
                show_toast("Congratulation, Your account is ready, Login to go!");
                String id = firebaseReference.push().getKey();
                Account a = new Account(FName,LName,Email,Pass,id);
                firebaseReference.child(id).setValue(a);
                finish();
            }

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
}
