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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class BookInfoAndDownloadActivity extends AppCompatActivity {
    String Book_Name;
    String Book_Category;
    String Book_Voice;
    String Book_Description;
    int Post_Position;

    TextView Book_Name_TEXT;
    TextView Book_Category_TEXT;
    TextView Book_Voice_TEXT;
    TextView Book_Description_TEXT;
    TextView Upper_Background;
    Button Download_Book_btn;

    StorageReference storageReference;
    ProgressDialog uploadProgressDialog;

    String Book_Content;
    List<byte[]> Audio_list;
    int num_of_audio_files;
    int num_of_downloaded_audio_files;

    DB_Local db_local;
    DatabaseReference firebaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_info_and_download);
        Bundle extras = getIntent().getExtras();
        Book_Name = extras.getString("Book_Name");
        Book_Category = extras.getString("Book_Category");
        Book_Voice = extras.getString("Book_Voice");
        Book_Description = extras.getString("Book_Description");
        Post_Position = extras.getInt("Post_Position");
        firebaseReference = FirebaseDatabase.getInstance().getReference("Accounts");

        num_of_audio_files = -1;
        num_of_downloaded_audio_files = 0;

        Book_Name_TEXT = findViewById(R.id.B_Name__d_act);
        Book_Category_TEXT = findViewById(R.id.b_categ__d_act);
        Book_Voice_TEXT = findViewById(R.id.b_voice__d_act);
        Book_Description_TEXT = findViewById(R.id.b_Desc__d_act);
        Upper_Background = findViewById(R.id.upper_background);
        Download_Book_btn = findViewById(R.id.download_book_button);
        storageReference = FirebaseStorage.getInstance().getReference();

        db_local = new DB_Local(this);
        Book_Content = "";
        Audio_list = new ArrayList<byte[]>();
        set_colors();
        set_values();
    }
    private void set_colors()
    {
        int back_col;
        int text_col;
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Post_Position % 2 == 0)
        {
            back_col = Color.parseColor("#A94543");
            text_col = Color.parseColor("#2A373D");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(ContextCompat.getColor(this,R.color.mainorange));
            }
            Download_Book_btn.setBackgroundResource(R.drawable.button);
        }
        else
        {
            back_col = Color.parseColor("#2A373D");
            text_col = Color.parseColor("#A94543");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(ContextCompat.getColor(this,R.color.mainblue));
            }
            Download_Book_btn.setBackgroundResource(R.drawable.button1);
        }
        Book_Name_TEXT.setTextColor(text_col);
        Book_Category_TEXT.setTextColor(text_col);
        Book_Voice_TEXT.setTextColor(text_col);
        Book_Description_TEXT.setTextColor(text_col);
        Upper_Background.setBackgroundColor(back_col);
        Download_Book_btn.setTextColor(text_col);
    }
    private void set_values()
    {
        Book_Name_TEXT.setText(Book_Name.substring(0,Book_Name.length()-7));
        Book_Category_TEXT.setText("Category: " + Book_Category);
        Book_Voice_TEXT.setText("Voice: " + Book_Voice);
        Book_Description_TEXT.setText(Book_Description);
        if (db_local.book_is_downloaded(Book_Name))
            Download_Book_btn.setText("Open");
        else
            Download_Book_btn.setText("Download");
    }

    private void show_toast(String message){
        Toast toast=Toast.makeText(BookInfoAndDownloadActivity.this,message,Toast.LENGTH_SHORT);
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




    private void get_audio_files_count(String Folder_Name){
        StorageReference listRef = storageReference.child(Folder_Name);
        listRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        for (StorageReference prefix : listResult.getPrefixes()) {
                            // All the prefixes under listRef.
                            // You may call listAll() recursively on them.
                        }

                        for (StorageReference item : listResult.getItems()) {
                            // All the items under listRef.
                            num_of_audio_files = num_of_audio_files + 1;
                        }
                        download_audio_folder();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Uh-oh, an error occurred!
                        num_of_audio_files = 0;
                    }
                });
    }


    private void download_pdf_file(){
        uploadProgressDialog = new ProgressDialog(this);
        uploadProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        uploadProgressDialog.setTitle("downloading pdf...");
        uploadProgressDialog.setProgress(0);
        uploadProgressDialog.setCanceledOnTouchOutside(false);
        uploadProgressDialog.setCancelable(false);
        uploadProgressDialog.show();


        String Folder_Name = Book_Name.substring(0,(Book_Name.length()-4));
        String pdf_file_Name = Folder_Name + ".txt";


        StorageReference fileRef = storageReference.child(Folder_Name + "/" + pdf_file_Name);

        final long ONE_GIGABYTE = 1024 * 1024 * 1000;
        fileRef.getBytes(ONE_GIGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                uploadProgressDialog.setProgress(100);
                Book_Content = new String(bytes);
                uploadProgressDialog.dismiss();
                get_audio_files_count(Book_Name.substring(0,(Book_Name.length()-4)));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                uploadProgressDialog.setProgress(100);
            }
        });
    }

    private void download_audio_folder(){
        uploadProgressDialog = new ProgressDialog(this);
        uploadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        uploadProgressDialog.setTitle("downloading audio...");
        uploadProgressDialog.setProgress(0);
        uploadProgressDialog.setCanceledOnTouchOutside(false);
        uploadProgressDialog.setCancelable(false);
        uploadProgressDialog.show();
        for (int i = 0;i<num_of_audio_files;i++){
            Audio_list.add(null);
        }

        String Folder_Name = Book_Name.substring(0,(Book_Name.length()-4));
        for (int current_audio_file_index = 0;current_audio_file_index<num_of_audio_files;current_audio_file_index++){
            String current_audio_file_name = current_audio_file_index + ".mp3";
            final StorageReference fileRef = storageReference.child(Folder_Name + "/" + current_audio_file_name);
            final long ONE_GIGABYTE = 1024 * 1024 * 1000;
            fileRef.getBytes(ONE_GIGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    //file i downloaded
                    int id = Integer.parseInt(fileRef.getName().substring(0,fileRef.getName().length()-4));
                    Audio_list.set(id,bytes);
                    num_of_downloaded_audio_files = num_of_downloaded_audio_files + 1;
                    int curr_progress = (int) (100* num_of_downloaded_audio_files /num_of_audio_files);
                    uploadProgressDialog.setProgress(curr_progress);
                    if (curr_progress >= 100)
                    {
                        //all files downloaded
                        save_book_to_all_db();
                        uploadProgressDialog.dismiss();
                        show_toast("The book has been successfully downloaded, check your books!");
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    int x = 0;
                }
            });
        }
    }
    private void update_account_with_book_firebase()
    {
        String id = db_local.get_curr_user_key();
        String FName = db_local.get_curr_user_fname();
        String LName = db_local.get_curr_user_lname();
        String Email = db_local.get_curr_user_email();
        String Pass = db_local.get_curr_user_pass();
        String Book_names = db_local.get_curr_user_books();
        Book_names = Book_names + Book_Name + "\n";
        Account a = new Account(FName,LName,Email,Pass,"FALSE",id,Book_names);
        firebaseReference.child(id).setValue(a);
    }
    public void save_book_to_all_db(){
        byte[] EOF_flag = new byte[4];
        EOF_flag[0] = (byte)Integer.parseInt("11111111", 2);
        EOF_flag[1] = (byte)Integer.parseInt("01010101", 2);
        EOF_flag[2] = (byte)Integer.parseInt("10101010", 2);
        EOF_flag[3] = (byte)Integer.parseInt("00000000", 2);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            for (byte[] b_arr : Audio_list) {
                outputStream.write(b_arr);
                outputStream.write(EOF_flag);
            }
        }catch (Exception ignored){}
        byte audio_files[] = outputStream.toByteArray( );
        if(!db_local.book_is_owned(Book_Name)) {
            update_account_with_book_firebase();
            db_local.Add_Book_to_account(Book_Name);
        }
        if(!db_local.book_is_in_db(Book_Name)){
            String f = this.getFilesDir().toString() + "#@!" + Book_Name;
            db_local.Add_Book(Book_Name,Book_Voice,Book_Category,Book_Content, f,"0");
            save_file_to_internal_storage(audio_files);
        }
        else{
            String f = this.getFilesDir().toString() + "#@!" + Book_Name;
            db_local.download_Book(Book_Name,Book_Voice,Book_Category,Book_Content,f,"0");
            save_file_to_internal_storage(audio_files);
        }
    }

    public void save_file_to_internal_storage(byte[] audio_content)
    {
        File file = new File(this.getFilesDir(), Book_Name);
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(Book_Name, this.MODE_PRIVATE);
            outputStream.write(audio_content);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
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
    public void Download_files_firebase(View view){
        if (db_local.get_curr_user_email().equals("")) {
            show_toast("No user logged in, cannot process the book!");
            return;
        }

        if (db_local.book_is_downloaded(Book_Name) == Boolean.TRUE)
        {
            Intent BookIntent = new Intent(BookInfoAndDownloadActivity.this,BookViewActivity.class);
            BookIntent.putExtra("Book_Name",Book_Name);
            startActivity(BookIntent);
            finish();
        }
        else {
            if (!isConnected()) {
                show_toast("No Internet Connection!");
                return;
            }
            download_pdf_file();
            Download_Book_btn.setText("Open");
        }
    }
}
