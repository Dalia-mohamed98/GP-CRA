package com.cra.cra;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.util.Currency;

import androidx.annotation.Nullable;

import java.util.Arrays;

public class DB_Local extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "CRA_Local.db";

    public static final String User_Data_Table = "User_Account";
    public static final String User_Email_Col = "Email";
    public static final String User_Password_Col = "Password";
    public static final String User_Book_Names_Col = "Book_Names";
    public static final String User_Using_Server_Col = "Using_Server";
    public static final String User_key = "User_key";
    public static final String User_F_name = "User_First_Name";
    public static final String User_L_Name = "User_Last_Name";


    public static final String User_Book_Table = "User_Books";
    public static final String Book_Names_Col = "Book_Name";
    public static final String Voice_Col = "Voice_Of";
    public static final String Category_Col = "Category";
    public static final String Text_Col = "Text";
    public static final String Audio_Files_String = "Audio_String";
    public static final String Current_page_index = "Page_Index";





    public DB_Local(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + User_Data_Table + " ("+
                User_Email_Col + " TEXT ," + User_Password_Col + " TEXT ," +
                User_Book_Names_Col + " TEXT ," + User_Using_Server_Col + " TEXT ,"
                + User_key + " TEXT ,"+ User_F_name + " TEXT ,"+ User_L_Name + " TEXT)");

        db.execSQL("create table " + User_Book_Table + " ("+
                Book_Names_Col + " TEXT ," + Voice_Col + " TEXT ," +
                Category_Col + " TEXT ,"  +
                Text_Col + " TEXT ," + Audio_Files_String + " TEXT ," + Current_page_index + " TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + User_Data_Table);
        db.execSQL("DROP TABLE IF EXISTS " + User_Book_Table);
        onCreate(db);
    }

    public Boolean Login_db(String User_Email_Col, String User_Password_Col ,
                                       String User_Book_Names_Col,String User_Using_Server_Col,
                            String User_key,String User_F_name,String User_L_Name){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + User_Data_Table);
        db.execSQL("DROP TABLE IF EXISTS " + User_Book_Table);
        onCreate(db);

        ContentValues contentValues = new ContentValues();

        contentValues.put(this.User_Email_Col, User_Email_Col);
        contentValues.put(this.User_Password_Col, User_Password_Col);
        contentValues.put(this.User_Book_Names_Col, User_Book_Names_Col);
        contentValues.put(this.User_Using_Server_Col, User_Using_Server_Col);
        contentValues.put(this.User_key, User_key);
        contentValues.put(this.User_F_name, User_F_name);
        contentValues.put(this.User_L_Name, User_L_Name);

        long result = db.insert(this.User_Data_Table, null, contentValues);
        if (result == -1){
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public Boolean Logout_db(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(User_Data_Table, String.valueOf(1), null);
        db.delete(User_Book_Table, String.valueOf(1), null);
        return Boolean.TRUE;
    }


    public String get_curr_user(){
        SQLiteDatabase db = this.getWritableDatabase();
        String user = "";
        Cursor cursor = db.rawQuery("select * from " + User_Data_Table, null);
        if (cursor.getCount() == 0)
            return user;
        cursor.moveToFirst();
        user = cursor.getString(0);
        return user;
    }


    public String get_curr_user_email(){
        SQLiteDatabase db = this.getWritableDatabase();
        String key = "";
        Cursor cursor = db.rawQuery("select * from " + User_Data_Table, null);
        if (cursor.getCount() == 0)
            return key;
        cursor.moveToFirst();
        key = cursor.getString(0);
        return key;
    }
    public String get_curr_user_pass(){
        SQLiteDatabase db = this.getWritableDatabase();
        String key = "";
        Cursor cursor = db.rawQuery("select * from " + User_Data_Table, null);
        if (cursor.getCount() == 0)
            return key;
        cursor.moveToFirst();
        key = cursor.getString(1);
        return key;
    }
    public String get_curr_user_books(){
        SQLiteDatabase db = this.getWritableDatabase();
        String key = "";
        Cursor cursor = db.rawQuery("select * from " + User_Data_Table, null);
        if (cursor.getCount() == 0)
            return key;
        cursor.moveToFirst();
        key = cursor.getString(2);
        return key;
    }
    public String get_curr_user_server(){
        SQLiteDatabase db = this.getWritableDatabase();
        String key = "";
        Cursor cursor = db.rawQuery("select * from " + User_Data_Table, null);
        if (cursor.getCount() == 0)
            return key;
        cursor.moveToFirst();
        key = cursor.getString(3);
        return key;
    }
    public String get_curr_user_key(){
        SQLiteDatabase db = this.getWritableDatabase();
        String key = "";
        Cursor cursor = db.rawQuery("select * from " + User_Data_Table, null);
        if (cursor.getCount() == 0)
            return key;
        cursor.moveToFirst();
        key = cursor.getString(4);
        return key;
    }
    public String get_curr_user_fname(){
        SQLiteDatabase db = this.getWritableDatabase();
        String key = "";
        Cursor cursor = db.rawQuery("select * from " + User_Data_Table, null);
        if (cursor.getCount() == 0)
            return key;
        cursor.moveToFirst();
        key = cursor.getString(5);
        return key;
    }
    public String get_curr_user_lname(){
        SQLiteDatabase db = this.getWritableDatabase();
        String key = "";
        Cursor cursor = db.rawQuery("select * from " + User_Data_Table, null);
        if (cursor.getCount() == 0)
            return key;
        cursor.moveToFirst();
        key = cursor.getString(6);
        return key;
    }





    public Boolean Add_Book(String Book_Names_Col, String Voice_Col,
                            String Category_Col, String Text_Col,
                            String Audio_Files_String, String Current_page_index)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(this.Book_Names_Col, Book_Names_Col);
        contentValues.put(this.Voice_Col, Voice_Col);
        contentValues.put(this.Category_Col, Category_Col);
        contentValues.put(this.Text_Col, Text_Col);
        contentValues.put(this.Audio_Files_String, Audio_Files_String);
        contentValues.put(this.Current_page_index, Current_page_index);
        long result = db.insert(this.User_Book_Table, null, contentValues);
        if (result == -1){
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }



    public Boolean Add_Book_to_account(String Book_Names_Col)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + User_Data_Table, null);
        if (cursor.getCount() == 0)
            return Boolean.FALSE;
        cursor.moveToFirst();
        String User_book_names = cursor.getString(2);
        User_book_names = User_book_names  + Book_Names_Col + "\n";
        ContentValues content = new ContentValues();
        content.put(this.User_Email_Col, cursor.getString(0));
        content.put(this.User_Password_Col, cursor.getString(1));
        content.put(this.User_Book_Names_Col, User_book_names);
        content.put(this.User_Using_Server_Col, cursor.getString(3));
        content.put(this.User_key, cursor.getString(4));
        content.put(this.User_F_name, cursor.getString(5));
        content.put(this.User_L_Name, cursor.getString(6));
        db.update(this.User_Data_Table,content,this.User_Email_Col + " = ?" , new String[]{cursor.getString(0)});
        return Boolean.TRUE;
    }





    public Boolean download_Book(String Book_Names_Col, String Voice_Col,
                            String Category_Col, String Text_Col,
                            String Audio_Files_String, String Current_page_index){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(this.Book_Names_Col, Book_Names_Col);
        contentValues.put(this.Voice_Col, Voice_Col);
        contentValues.put(this.Category_Col, Category_Col);
        contentValues.put(this.Text_Col, Text_Col);
        contentValues.put(this.Audio_Files_String, Audio_Files_String);
        contentValues.put(this.Current_page_index, Current_page_index);
        db.update(this.User_Book_Table,contentValues,this.Book_Names_Col + " = ?" , new String[]{Book_Names_Col});
        return Boolean.TRUE;
    }


    public String get_book_content(String Book_Names_Col){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + User_Book_Table, null);
        if (cursor.getCount() == 0)
            return "";
        cursor.moveToFirst();
        do
        {
            if(cursor.getString(0).equals(Book_Names_Col))
            {
                return cursor.getString(3);
            }
        } while (cursor.moveToNext());
        return "";
    }
    public String get_book_Audio(String Book_Names_Col){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + User_Book_Table, null);
        if (cursor.getCount() == 0)
            return "";
        cursor.moveToFirst();
        do
        {
            if(cursor.getString(0).equals(Book_Names_Col))
            {
                return cursor.getString(4);
            }
        }while (cursor.moveToNext());
        return "";

    }

    public String get_book_page_index(String Book_Names_Col){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + User_Book_Table, null);
        if (cursor.getCount() == 0)
            return "";
        cursor.moveToFirst();
        do
        {
            if(cursor.getString(0).equals(Book_Names_Col))
            {
                return cursor.getString(5);
            }
        }while (cursor.moveToNext());
        return "";
    }

    public Boolean save_book_page_index(String Book_Names_Col, String Current_page_index){

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + User_Book_Table, null);
        if (cursor.getCount() == 0)
            return Boolean.FALSE;
        cursor.moveToFirst();
        ContentValues contentValues = new ContentValues();
        do
        {
            if(cursor.getString(0).equals(Book_Names_Col))
            {
                contentValues.put(this.Book_Names_Col, cursor.getString(0));
                contentValues.put(this.Voice_Col, cursor.getString(1));
                contentValues.put(this.Category_Col, cursor.getString(2));
                contentValues.put(this.Text_Col, cursor.getString(3));
                contentValues.put(this.Audio_Files_String, cursor.getString(4));
                contentValues.put(this.Current_page_index, Current_page_index);
                break;
            }
        }while (cursor.moveToNext());
        db.update(this.User_Book_Table,contentValues,this.Book_Names_Col + " = ?" , new String[]{Book_Names_Col});
        return Boolean.TRUE;
    }

    public Boolean book_is_in_db(String Book_Names_Col){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + User_Book_Table, null);
        if (cursor.getCount() == 0)
            return Boolean.FALSE;
        cursor.moveToFirst();
        do
        {
            if(cursor.getString(0).equals(Book_Names_Col))
            {
                return Boolean.TRUE;
            }
        } while (cursor.moveToNext());
        return Boolean.FALSE;
    }

    public Boolean book_is_downloaded(String Book_Names_Col){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + User_Book_Table, null);
        if (cursor.getCount() == 0)
            return Boolean.FALSE;
        cursor.moveToFirst();
        do
        {
            if(cursor.getString(0).equals(Book_Names_Col) && !cursor.getString(4).equals(""))
            {
                return Boolean.TRUE;
            }
        } while (cursor.moveToNext());
        return Boolean.FALSE;
    }

    public Boolean book_is_owned(String Book_Names_Col){
        SQLiteDatabase db = this.getWritableDatabase();
        String book_nms = "";
        Cursor cursor = db.rawQuery("select * from " + User_Data_Table, null);
        if (cursor.getCount() == 0)
            return Boolean.FALSE;
        cursor.moveToFirst();
        book_nms = cursor.getString(2);
        String[] book_names = book_nms.split("\n");
        for(String name:book_names)
        {
            if(name.equals(Book_Names_Col))
            {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }




    public Boolean start_server(){
        SQLiteDatabase db = this.getWritableDatabase();
        String key = "";
        Cursor cursor = db.rawQuery("select * from " + User_Data_Table, null);
        if (cursor.getCount() == 0)
            return Boolean.FALSE;
        cursor.moveToFirst();
        String email = cursor.getString(0);
        String pass = cursor.getString(1);
        String books = cursor.getString(2);
        String us = cursor.getString(3);
        String u_ky = cursor.getString(4);
        String f_nme = cursor.getString(5);
        String l_nme = cursor.getString(6);



        ContentValues content = new ContentValues();

        content.put(this.User_Email_Col, email);
        content.put(this.User_Password_Col, pass);
        content.put(this.User_Book_Names_Col, books);
        content.put(this.User_Using_Server_Col, "TRUE");
        content.put(this.User_key, u_ky);
        content.put(this.User_F_name, f_nme);
        content.put(this.User_L_Name, l_nme);

        db.update(this.User_Data_Table,content,this.User_Email_Col + " = ?" , new String[]{cursor.getString(0)});
        return Boolean.TRUE;
    }

    public Boolean end_server(){
        SQLiteDatabase db = this.getWritableDatabase();
        String key = "";
        Cursor cursor = db.rawQuery("select * from " + User_Data_Table, null);
        if (cursor.getCount() == 0)
            return Boolean.FALSE;
        cursor.moveToFirst();
        String email = cursor.getString(0);
        String pass = cursor.getString(1);
        String books = cursor.getString(2);
        String us = cursor.getString(3);
        String u_ky = cursor.getString(4);
        String f_nme = cursor.getString(5);
        String l_nme = cursor.getString(6);



        ContentValues content = new ContentValues();

        content.put(this.User_Email_Col, email);
        content.put(this.User_Password_Col, pass);
        content.put(this.User_Book_Names_Col, books);
        content.put(this.User_Using_Server_Col, "FALSE");
        content.put(this.User_key, u_ky);
        content.put(this.User_F_name, f_nme);
        content.put(this.User_L_Name, l_nme);

        db.update(this.User_Data_Table,content,this.User_Email_Col + " = ?" , new String[]{cursor.getString(0)});
        return Boolean.TRUE;
    }

}
