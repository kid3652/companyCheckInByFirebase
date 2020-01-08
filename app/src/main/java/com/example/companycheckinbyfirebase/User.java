package com.example.companycheckinbyfirebase;

//this is very simple class and it only contains the user attributes, a constructor and the getters
// you can easily do this by right click -> generate -> constructor and getters
public class User {

    private String username, password,account;

    public User(String username,String account, String password) {
        this.username = username;
        this.account=account;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getAccount() {
        return account;
    }

    public String getPassword() {
        return password;
    }

}