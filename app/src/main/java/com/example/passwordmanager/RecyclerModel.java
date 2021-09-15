package com.example.passwordmanager;

class RecyclerModel {

    private String org,uName,pass,encOpt;

    RecyclerModel(String org) {

        this.org=org;

    }

    RecyclerModel(String uName,String pass)
    {
        this.uName=uName;
        this.pass=pass;
//        this.encOpt=encOpt;

    }

    public String getOrg() {
        return org;
    }

    public String getuName() {
        return uName;
    }

    public String getPass() {
        return pass;
    }

    public String getEncOpt() {
        return encOpt;
    }
}
