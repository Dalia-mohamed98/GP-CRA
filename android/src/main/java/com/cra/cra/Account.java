package com.cra.cra;

public class Account {
    String first_name;
    String last_name;
    String email;
    String password;
    String using_server;
    String key;
    String book_names;


    public Account(String first_name, String last_name, String email, String password,String key) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.password = password;
        this.using_server = "FALSE";
        this.key = key;
        this.book_names = "";
    }

    public Account(String first_name, String last_name, String email, String password, String using_server,String key, String book_names) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.password = password;
        this.using_server = using_server;
        this.key = key;
        this.book_names = book_names;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsing_server() {
        return using_server;
    }

    public void setUsing_server(String using_server) {
        this.using_server = using_server;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getBook_names() {
        return book_names;
    }

    public void setBook_names(String book_names) {
        this.book_names = book_names;
    }
}
