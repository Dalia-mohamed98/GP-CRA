package com.cra.cra;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;


public class AddBookActivity extends AppCompatActivity {
    ImageView browse_button;
    TextView browse_text;
    EditText book_name;
    Spinner cat_choose;
    Spinner voice_choose;
    Uri File_path;
    EditText Book_describtion;
    Intent File_browser;


    String file_name;
    String category;
    String voiceof;
    String Describe;
    String bookname;



    String File_type;
    DatabaseReference firebaseReferenceaAccount;
    DatabaseReference firebaseReference;
    StorageReference storageReference;
    ProgressDialog uploadProgressDialog;
    DB_Local db_local;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.mainblue));
        }
        browse_text = findViewById(R.id.file_path_text);
        book_name = findViewById(R.id.Book_Name_Text);
        browse_button = findViewById(R.id.browse_image);
        cat_choose = findViewById(R.id.cat_spinner);
        voice_choose = findViewById(R.id.voice_spinner);
        Book_describtion = findViewById(R.id.Book_des_Text);
        browse_text.setVisibility(View.GONE);
        db_local =  new DB_Local(this);
        firebaseReferenceaAccount = FirebaseDatabase.getInstance().getReference("Accounts");
        firebaseReference = FirebaseDatabase.getInstance().getReference("Books");
        storageReference = FirebaseStorage.getInstance().getReference();
        set_spinners();
    }

    protected void set_spinners(){
        ArrayAdapter<String> cat_adapter = new ArrayAdapter<String>(AddBookActivity.this,
                android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.categories));
        cat_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cat_choose.setAdapter(cat_adapter);
        cat_choose.setPrompt("Select book category");
        ArrayAdapter<String> voice_adapter = new ArrayAdapter<String>(AddBookActivity.this,
                android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.voices));
        voice_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        voice_choose.setAdapter(voice_adapter);

    }




    public void browse_files(View view){
        String[] mimeTypes =
                {"application/pdf","text/plain"};
        File_browser = new Intent(Intent.ACTION_GET_CONTENT);
        File_browser.addCategory(Intent.CATEGORY_OPENABLE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            File_browser.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
            if (mimeTypes.length > 0) {
                File_browser.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }
        } else {
            String mimeTypesStr = "";
            for (String mimeType : mimeTypes) {
                mimeTypesStr += mimeType + "|";
            }
            File_browser.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
        }
        startActivityForResult(Intent.createChooser(File_browser,"ChooseFile"), 10);
    }

    private void show_toast(String message){
        Toast toast=Toast.makeText(AddBookActivity.this,message,Toast.LENGTH_SHORT);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 10 && resultCode == RESULT_OK) {
            File_path = data.getData();
            File_type = data.getData().getPath().substring(data.getData().getPath().length()-4,data.getData().getPath().length());
            browse_text.setText(File_path.toString());
            browse_text.setVisibility(View.VISIBLE);
            browse_button.setVisibility(View.GONE);
        }
        super.onActivityResult(requestCode, resultCode, data);
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

        public void addBook(View view){
        if (!isConnected()) {
            show_toast("No Internet Connection!");
            return;
        }
        bookname = book_name.getText().toString();
        category = cat_choose.getSelectedItem().toString();
        voiceof = voice_choose.getSelectedItem().toString();
        Describe = "    " + Book_describtion.getText().toString();
        String filepath = browse_text.getText().toString();
        if(bookname.equals("")) {
            show_toast("Please, Enter the book name!");
            return;
        }
        if(category.equals("Book Category:")) {
            show_toast("Please, Select the book category!");
            return;
        }
        if(voiceof.equals("Voice of:")) {
            show_toast("Please, Select the reader voice you desire!");
            return;
        }
        if(filepath.equals("")) {
            show_toast("Please, Select a pdf book!");
            return;
        }
        if(Describe.equals("    ")) {
            show_toast("Please, Enter the book description!");
            return;
        }


        if(bookname.length() > 20) {
            show_toast("Book name exceeds maximum number of characters(20)!");
            return;
        }
        if(Describe.length() > 255) {
            show_toast("Book description exceeds maximum number of characters(255)!");
            return;
        }


        String voc = "";
        if (voiceof.equals("Mary Nader"))
            voc = "-MY";
        else if (voiceof.equals("LJ"))
            voc = "-LJ";
        file_name = bookname.toLowerCase() + voc + File_type;
        Describe = Describe.toLowerCase();
        //see if there is a book with the same name:
        Query query = firebaseReference.orderByChild("name").equalTo(file_name);
        query.addListenerForSingleValueEvent(valueEventListener);
        //db_local.start_server();
    }


    public void upload_file_firebase(){
        uploadProgressDialog = new ProgressDialog(this);
        uploadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        uploadProgressDialog.setTitle("Uploading file...");
        uploadProgressDialog.setProgress(0);
        uploadProgressDialog.setCanceledOnTouchOutside(false);
        uploadProgressDialog.setCancelable(false);
        uploadProgressDialog.show();


        StorageReference fileReference = storageReference.child("pdf/" + file_name);
        fileReference.putFile(File_path)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                int curr_progress = (int) (100* taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                uploadProgressDialog.setProgress(curr_progress);
            }
        });
    }



    public void show_already_existing_book_post(Book book){
        Intent Same_activity = new Intent(AddBookActivity.this,BookWithSameNameActivity.class);
        Same_activity.putExtra("Book_Name",book.getName());
        Same_activity.putExtra("Book_Category",book.getCategory());
        Same_activity.putExtra("Book_Voice",book.getVoice());
        Same_activity.putExtra("Book_Description",book.getDescription());
        Same_activity.putExtra("Post_Position",1);
        startActivity(Same_activity);
    }

    private void update_account_with_book_firebase()
    {
        String id = db_local.get_curr_user_key();
        String FName = db_local.get_curr_user_fname();
        String LName = db_local.get_curr_user_lname();
        String Email = db_local.get_curr_user_email();
        String Pass = db_local.get_curr_user_pass();
        String Book_names = db_local.get_curr_user_books();
        String using_server = db_local.get_curr_user_server();
        Book_names = Book_names + file_name + "\n";
        Account a = new Account(FName,LName,Email,Pass,using_server,id,Book_names);
        firebaseReferenceaAccount.child(id).setValue(a);
    }

    public void Preview_Act_launch(View view) {
        Intent PrevIntent = new Intent(AddBookActivity.this,PreviewActivity.class);
        startActivity(PrevIntent);
        finish();
    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) { // if there is a book with the same name:
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                     Book book_with_same_Name = new Book(
                             snapshot.child("name").getValue().toString(),
                             snapshot.child("voice").getValue().toString(),
                             snapshot.child("category").getValue().toString(),
                             snapshot.child("time").getValue().toString(),
                             snapshot.child("uploaded").getValue().toString(),
                             snapshot.child("description").getValue().toString());
                     if (book_with_same_Name.getUploaded().toUpperCase().equals("TRUE"))
                     {
                         show_toast("Book already exists!");
                         show_already_existing_book_post(book_with_same_Name);
                     }
                     else
                     {
                         show_toast("Some on is uploading this book, check the book later!");
                     }


                }
            }
            else {// if the book name is unique:
                upload_file_firebase();
                String id = firebaseReference.push().getKey();
                Book b = new Book(file_name,voiceof,category,Describe);
                firebaseReference.child(id).setValue(b);
                show_toast("Your book is under conversion, You will be notified when the book is ready!");
                Intent serviceIntent = new Intent(AddBookActivity.this, CheckServerService.class);
                serviceIntent.putExtra("Book_Name",file_name);
                serviceIntent.putExtra("Book_Category",category);
                serviceIntent.putExtra("Book_Voice",voiceof);
                serviceIntent.putExtra("Book_Description",Describe);
                serviceIntent.putExtra("Post_Position",1);
                startService(serviceIntent);
                update_account_with_book_firebase();
                db_local.Add_Book_to_account(file_name);
                db_local.Add_Book(file_name,voiceof,category,"","","0");
                finish();
            }

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
}
