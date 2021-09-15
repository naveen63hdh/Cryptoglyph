package com.example.passwordmanager.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {


    //Table Name

    public static final String TABLE_NAME = "passwordStore";

    //Table Columns
    public static final String ORG = "org";
    public static final String USERNAME = "uname";
    public static final String PASSWORD = "pass";
    public static final String PASSWORD_OPTION = "passOpt";

    //DB Name
    public static final String DB_NAME = "PASSWORD.DB";

    //DB Version
    static final int DB_VERSION = 1;

    //Creating table
    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + ORG + " TEXT NOT NULL , " + USERNAME + " TEXT NOT NULL, " + PASSWORD + " TEXT, " + PASSWORD_OPTION + " TEXT,PRIMARY KEY ("+ORG+","+USERNAME+") );";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(sqLiteDatabase);
    }


}
