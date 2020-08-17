package com.cra.cra;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class PreviewActivity extends AppCompatActivity {
    EditText Preview_Text;
    Button Preview_Button;
    Uri File_path;

    String text;
    String file_name;
    String category;
    String voiceof;
    String Describe;
    String bookname;

    int curr_page_index;

    DatabaseReference firebaseReferenceaAccount;
    DatabaseReference firebaseReference;
    StorageReference storageReference;
    ProgressDialog uploadProgressDialog;
    Spinner voice_choose;
    MediaPlayer mediaPlayer;
    byte[] curr_audio;


    List<byte[]> Audio_list;
    int num_of_audio_files;
    int num_of_downloaded_audio_files;
    int num_of_delete_files;
    float play_speed;
    TextView speedtext;
    Boolean Back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.grey));
        }

        Preview_Text = findViewById(R.id.Text_prev);
        Preview_Button = findViewById(R.id.Preview_BTN);
        text = "";
        firebaseReferenceaAccount = FirebaseDatabase.getInstance().getReference("Accounts");
        firebaseReference = FirebaseDatabase.getInstance().getReference("Books");
        storageReference = FirebaseStorage.getInstance().getReference();
        voice_choose = findViewById(R.id.voice_spinner_);
        speedtext = findViewById(R.id.speedtext2);

        Back = false;
        curr_page_index = 0;
        num_of_delete_files = -1;
        play_speed = 1f;
        speedtext.setText(String.valueOf(play_speed)+"X");
        set_spinners();
        Audio_list = new ArrayList<byte[]>();
    }
    protected void set_spinners(){
        ArrayAdapter<String> voice_adapter = new ArrayAdapter<String>(PreviewActivity.this,
                android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.voices));
        voice_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        voice_choose.setAdapter(voice_adapter);
    }

    private void show_toast(String message){
        Toast toast=Toast.makeText(PreviewActivity.this,message,Toast.LENGTH_SHORT);
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

    public void save_file_to_internal_storage() {
        File file = new File(PreviewActivity.this.getFilesDir(), "text");
        if (!file.exists()) {
            file.mkdir();
        }
        try {
            File gpxfile = new File(file, "sample.txt");
            File_path = Uri.fromFile(gpxfile);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(text);
            writer.flush();
            writer.close();
        } catch (Exception ignored) {
        }

    }

    private void deleteFstorage() {
        StorageReference ref = storageReference.child("temp-LJ");
        ref.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                num_of_delete_files = -1;
                for (StorageReference item : listResult.getItems()) {
                    num_of_delete_files = num_of_delete_files + 1;
                    deleteFile("temp-LJ", num_of_delete_files + ".mp3");
                }

                curr_page_index = 0;
                Audio_list = new ArrayList<byte[]>();
                Audio_list.clear();
                num_of_audio_files = -1;
                num_of_delete_files = -1;
                num_of_downloaded_audio_files = 0;
                text = Preview_Text.getText().toString();
                bookname = "temp";
                category = "Prev";
                voiceof = voice_choose.getSelectedItem().toString();
                Describe = "";
                if (text.equals(""))
                {
                    show_toast("Please, Enter text to convert!");
                    return;
                }
                if (voiceof.equals("Voice of:"))
                {
                    show_toast("Please, Select acting voice!");
                    return;
                }

                String voc = "";
                if (voiceof.equals("Mary Nader"))
                    voc = "-MY";
                else if (voiceof.equals("LJ"))
                    voc = "-LJ";
                file_name = bookname.toLowerCase() + voc + ".txt";
                Describe = Describe.toLowerCase();

                save_file_to_internal_storage();
                uploadfile_firebase();
                String id = firebaseReference.push().getKey();
                Book b = new Book(file_name,voiceof,category,Describe);
                firebaseReference.child(id).setValue(b);

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Uh-oh, an error occurred!
                        num_of_delete_files = 0;
                    }
                });
        deleteFile("temp-LJ","temp-LJ.txt");



        ref = storageReference.child("temp-MY");
        ref.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                num_of_delete_files = -1;
                for (StorageReference item : listResult.getItems()) {
                    num_of_delete_files = num_of_delete_files + 1;
                    deleteFile("temp-MY", num_of_delete_files + ".mp3");
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Uh-oh, an error occurred!
                        num_of_delete_files = 0;
                    }
                });
        deleteFile("temp-MY","temp-MY.txt");

    }

    private void deleteFile(String path,String fname) {
        StorageReference ref = storageReference.child(path);
        StorageReference childRef = ref.child(fname);
        childRef.delete();
    }
    public void Preview_click(View view){
        if (!isConnected()) {
            show_toast("No Internet Connection!");
            return;
        }
        deleteFstorage();
    }

    private void get_audio_files_count(){
        StorageReference listRef = storageReference.child(file_name.substring(0,file_name.length()-4));
        listRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        for (StorageReference prefix : listResult.getPrefixes()) {
                            // All the prefixes under listRef.
                            // You may call listAll() recursively on them.
                        }
                        num_of_audio_files = -1;
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
    private void play_voice()
    {
        curr_audio = Audio_list.get(curr_page_index);
        try {
            mediaPlayer.stop();
        }catch (Exception ignored){}
        try {
            mediaPlayer = new MediaPlayer();
            File Mytemp = File.createTempFile("TCL", "mp3", getCacheDir());
            Mytemp.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(Mytemp);
            fos.write(curr_audio);
            fos.close();
            FileInputStream MyFile = new FileInputStream(Mytemp);
            mediaPlayer.setDataSource(MyFile.getFD());
            mediaPlayer.prepare();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(play_speed));
            }
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer player) {
                    // next audio file
                    if (Back){
                        return;
                    }
                    if (curr_page_index < num_of_downloaded_audio_files-1) {
                        curr_page_index = curr_page_index + 1;
                        play_voice();
                    }

                }
            });
            mediaPlayer.start();

        } catch (IOException ex) {
            String s = ex.toString();
            ex.printStackTrace();
        }
    }
    private void uploadfile_firebase(){
        uploadProgressDialog = new ProgressDialog(this);
        uploadProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        uploadProgressDialog.setTitle("Converting text...");
        uploadProgressDialog.setProgress(0);
        uploadProgressDialog.setCanceledOnTouchOutside(false);
        uploadProgressDialog.setCancelable(false);
        uploadProgressDialog.show();


        StorageReference fileReference = storageReference.child("pdf/" + file_name);
        fileReference.putFile(File_path)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Query query = firebaseReference.orderByChild("name").equalTo(file_name);
                            query.addListenerForSingleValueEvent(valueEventListener);
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

    private void download_audio_folder(){
        for (int i = 0;i<num_of_audio_files;i++){
            Audio_list.add(null);
        }

        String Folder_Name = file_name.substring(0,(file_name.length()-4));
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
                    if (curr_progress >= 100)
                    {
                        //all files downloaded
                        uploadProgressDialog.dismiss();
                        play_voice();
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


    public void click_faster_(View view) {
        if (play_speed >= 2)
        {
            show_toast("This is the maximum speed");
        } else {
            play_speed = play_speed + .05f;
            speedtext.setText(String.valueOf(play_speed)+"X");
        }
    }

    public void click_slower_(View view) {
        if (play_speed <= .25)
        {
            show_toast("This is the minimum speed");
        } else {
            play_speed = play_speed - .05f;
            speedtext.setText(String.valueOf(play_speed)+"X");
        }
    }


    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String up = snapshot.child("uploaded").getValue().toString();
                    if (up.equals("TRUE"))
                    {
                        uploadProgressDialog.dismiss();
                        Query applesQuery = firebaseReference.orderByChild("name").equalTo(file_name);
                        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                    appleSnapshot.getRef().removeValue();
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                        get_audio_files_count();
                    }
                    else
                    {
                        Query query = firebaseReference.orderByChild("name").equalTo(file_name);
                        query.addListenerForSingleValueEvent(valueEventListener);
                    }

                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    @Override
    public void onDestroy() {
        Back = true;
        try {
            mediaPlayer.stop();
        }catch (Exception ignored){}
        super.onDestroy();
    }
}
