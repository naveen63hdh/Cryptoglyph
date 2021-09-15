package com.example.passwordmanager.Database;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.passwordmanager.R;

public class DBManager {

    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase database;

    public DBManager(Context c) {
        context = c;
    }

    public DBManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insert(String org, String uname, String pass) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.ORG, org);
        contentValues.put(DatabaseHelper.USERNAME, uname);
        contentValues.put(DatabaseHelper.PASSWORD, pass);
//        contentValues.put(DatabaseHelper.PASSWORD_OPTION, passopt);
        database.insert(DatabaseHelper.TABLE_NAME, null, contentValues);


    }

    public Cursor fetchOrg() {
        String[] columns = new String[]{DatabaseHelper.ORG};
        Cursor cursor = database.query(true, DatabaseHelper.TABLE_NAME, columns, null, null, null, null, null, null);
        if ((cursor != null)) {
            cursor.moveToFirst();
        }
        return cursor;
    }


    public Cursor fetchByOrg(String org) {
        String[] columns = new String[]{DatabaseHelper.USERNAME, DatabaseHelper.PASSWORD, DatabaseHelper.PASSWORD_OPTION};
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns, DatabaseHelper.ORG + " = '" + org + "'", null, null, null, null);
        if ((cursor != null)) {
            cursor.moveToFirst();
        }
        return cursor;
    }


    public int update(String org, String uname, String newPass) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.PASSWORD, newPass);
        int i = database.update(DatabaseHelper.TABLE_NAME, contentValues, DatabaseHelper.ORG + "='" + org + "'" + " and " + DatabaseHelper.USERNAME + "='" + uname + "'", null);
        return i;
    }

    public void delete(String uname, String org) {
        database.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper.ORG + "='" + org + "'" + " and " + DatabaseHelper.USERNAME + "='" + uname + "'", null);
    }

    public void importDb(Context context, SQLiteDatabase newDatabase) {

        try {
            String[] columns=new String[]{DatabaseHelper.ORG,DatabaseHelper.USERNAME,DatabaseHelper.PASSWORD};
            Cursor cursor = newDatabase.query(DatabaseHelper.TABLE_NAME, columns, null, null, null, null, null, null);
            database.delete(DatabaseHelper.TABLE_NAME,null,null);
            if ((cursor != null)) {
                cursor.moveToFirst();

            } else {
                Toast.makeText(context, "This is not a valid database", Toast.LENGTH_SHORT).show();
            }

            do {
                String org = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ORG));
                String uname = cursor.getString(cursor.getColumnIndex(DatabaseHelper.USERNAME));
                String pass = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PASSWORD));
                insert(org, uname, pass);
            } while (cursor.moveToNext());
            Toast.makeText(context, "Imported Successfully", Toast.LENGTH_SHORT).show();
            cursor.close();

        } catch (SQLiteException e) {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context, R.style.dialog);
            SpannableString title = new SpannableString("No Database Found");
            title.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.colorPrimary)), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            alertDialog.setTitle(title);
            alertDialog.setMessage("The Database in your device is corrupted");

            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            alertDialog.show();
        }
    }

}
