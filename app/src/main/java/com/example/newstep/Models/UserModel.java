package com.example.newstep.Models;


public class UserModel {
    private String username;
    private String token;
    private String  cat;

    public boolean isBanned() {
        return isBanned;
    }

    public void setBanned(boolean banned) {
        isBanned = banned;
    }

    boolean isBanned;

    private String registerDate;
    private String id;

    private int availability;

    public int getNb_reports() {
        return nb_reports;
    }

    public void setNb_reports(int nb_reports) {
        this.nb_reports = nb_reports;
    }

    private int nb_reports;

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public UserModel(String username, String token, String cat, String registerDate, String id, int availability) {
        this.username = username;
        this.token = token;
        this.cat = cat;
        this.registerDate = registerDate;
        this.id = id;
        this.availability = availability;
    }

    public void setRegisterDate(String registerDate) {
        this.registerDate = registerDate;
    }

    public UserModel() {
    }



    public int getAvailability() {
        return availability;
    }

    public void setAvailability(int availability) {
        this.availability = availability;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    public UserModel(String name, String date,String id) {
        this.username = name;
        this.registerDate = date;
        this.id=id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String name) {
        this.username = name;
    }

    public String getRegisterDate() {
        return registerDate;
    }


}



