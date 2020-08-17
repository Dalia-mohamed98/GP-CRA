package com.cra.cra;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
public class BookViewActivity extends AppCompatActivity {
    DB_Local db_local;
    String Book_Name;
    List<String> Book_Content;
    List<byte[]> Book_Audio;
    TextView Book_Name_Text;
    TextView Book_Content_Text;
    TextView speedtext;

    ImageView Next_Button;
    ImageView Prev_Button;
    TextView page_index_text;
    int curr_page_index;
    int num_of_pages;
    MediaPlayer mediaPlayer;
    boolean Back;
    float play_speed;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_view);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.grey));
        }
        Back = false;
        Bundle extras = getIntent().getExtras();
        Book_Name = extras.getString("Book_Name");
        db_local = new DB_Local(this);
        Book_Name_Text = findViewById(R.id.Reader_Book_name_text);
        Book_Content_Text = findViewById(R.id.Reader_Book_content_text);
        Next_Button = findViewById(R.id.next_page_button);
        Prev_Button = findViewById(R.id.prev_page_button);
        page_index_text = findViewById(R.id.page_index_text);
        speedtext = findViewById(R.id.speedtext);
        curr_page_index = Integer.parseInt(db_local.get_book_page_index(Book_Name));
        num_of_pages = 0;
        Book_Name_Text.setText(Book_Name.substring(0,Book_Name.length()-7));
        play_speed = 1f;
        speedtext.setText(String.valueOf(play_speed)+"X");
        set_strings();
        set_page_view();
    }
    public byte[] getFileBytes(String FPath) {
        String[] f = FPath.split("#@!");

        File file = new File(f[0],f[1]);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bytes;
    }
    private void set_strings()
    {
        Book_Content = new ArrayList<>();
        Book_Audio = new ArrayList<>();

        String[] t1 = db_local.get_book_content(Book_Name).split("\n");
        for (String t : t1){
            Book_Content.add(t);
            num_of_pages = num_of_pages + 1;
        }

        int file_number = 0;
        String Audio_file_name = db_local.get_book_Audio(Book_Name);

        byte[] full_book_audio= getFileBytes(Audio_file_name);
        byte[] curr_file = new byte[1024*50];
        int j = 0; // loop on current file
        for (int i=0;i<full_book_audio.length;i++)
        {
            byte b = full_book_audio[i];
            if (b == (byte)Integer.parseInt("11111111", 2))
            {
                if
                    (
                        full_book_audio[i+1] == (byte)Integer.parseInt("01010101", 2) &&
                        full_book_audio[i+2] == (byte)Integer.parseInt("10101010", 2) &&
                        full_book_audio[i+3] == (byte)Integer.parseInt("00000000", 2)
                    )
                {
                    Book_Audio.add(file_number,curr_file);
                    file_number = file_number + 1;
                    curr_file = new byte[1024*50];
                    i = i + 4;
                    j = 0;
                    continue;
                }
            }
            curr_file[j] = b;
            j = j + 1;
            if (file_number > 948)
                break;

        }
    }
    private void set_page_view()
    {
        page_index_text.setText(String.valueOf(curr_page_index + 1) + "/" + String.valueOf(num_of_pages));
        Book_Content_Text.setText(Book_Content.get(curr_page_index));
        play_voice_of_curr_page();
    }
    private void play_voice_of_curr_page()
    {
        byte[] curr_audio = Book_Audio.get(curr_page_index);
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
                    if(curr_page_index + 1 == num_of_pages)//last page
                    {
                        stop_voice_of_curr_page();
                        show_toast("This is the last page!");
                    }
                    else
                    {
                        curr_page_index = curr_page_index + 1;
                        set_page_view();
                    }
                }
            });
            mediaPlayer.start();

        } catch (IOException ex) {
            String s = ex.toString();
            ex.printStackTrace();
        }
    }
    private void stop_voice_of_curr_page()
    {

    }

    public void click_next(View view) {
        if (curr_page_index + 1 == num_of_pages)//last page
        {
            stop_voice_of_curr_page();
            show_toast("This is the last page!");
        } else {
            curr_page_index = curr_page_index + 1;
            set_page_view();
        }
    }

    public void click_prev(View view){
        if(curr_page_index == 0)//first page
        {
            stop_voice_of_curr_page();
            show_toast("This is the first page!");
        }
        else
        {
            curr_page_index = curr_page_index - 1;
            set_page_view();
        }
    }

    public void click_faster(View view) {
        if (play_speed >= 2)
        {
            show_toast("This is the maximum speed");
        } else {
            play_speed = play_speed + .05f;
            speedtext.setText(String.valueOf(play_speed)+"X");
        }
    }

    public void click_slower(View view) {
        if (play_speed <= .25)
        {
            show_toast("This is the minimum speed");
        } else {
            play_speed = play_speed - .05f;
            speedtext.setText(String.valueOf(play_speed)+"X");
        }
    }
    private void show_toast(String message){
        Toast toast=Toast.makeText(BookViewActivity.this,message,Toast.LENGTH_SHORT);
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
    public void onDestroy() {
        db_local.save_book_page_index(Book_Name,String.valueOf(curr_page_index));
        Back = true;
        try {
            mediaPlayer.stop();
        }catch (Exception ignored){}
        super.onDestroy();
    }

}
