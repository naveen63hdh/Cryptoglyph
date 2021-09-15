package com.example.passwordmanager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.passwordmanager.Database.DBManager;
import com.example.passwordmanager.Database.DatabaseHelper;
import com.example.passwordmanager.Utils.AESUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Result;


public class DisplayActivity extends AppCompatActivity {

    static String org;
    static RecyclerView recyclerView;
    DBManager dbManager;
    Cursor cursor;
    static boolean valid = false;
    DisplayAdapter displayAdapter;

    static List<RecyclerModel> listModel;

    static int selectedItemPosition = 0;
    static CheckBox checkBoxView = null;
    static TextView passTextView = null;


    public static int SHOW_PASS_REQ_CODE = 123;
    public static int COPY_PASS_REQ_CODE = 321;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        recyclerView = findViewById(R.id.contentRecycler);

//        Get org name from intent
        org = getIntent().getExtras().getString("org");

//        Recycler view initialization
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());


//      Database Init
        dbManager = new DBManager(this);
        dbManager.open();

        cursor = dbManager.fetchByOrg(org);

        if (!cursor.isBeforeFirst()) {
            listModel = populateModel();
            displayAdapter = new DisplayAdapter(this, listModel, org);
            recyclerView.setAdapter(displayAdapter);
        } else {
            Toast.makeText(this, "Add New Password", Toast.LENGTH_SHORT).show();
        }
    }

    private List<RecyclerModel> populateModel() {
        List<RecyclerModel> model = new ArrayList<>();

        do {
            String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.USERNAME));
            String pass = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PASSWORD));
//            String passOpt=cursor.getString(cursor.getColumnIndex(DatabaseHelper.PASSWORD_OPTION));
//            RecyclerModel mod =new RecyclerModel(name,pass,passOpt);
            RecyclerModel mod = new RecyclerModel(name, pass);
            model.add(mod);

        } while (cursor.moveToNext());

        return model;

    }


    public void addNewToOrg(View view) {
        Intent addIntent = new Intent(DisplayActivity.this, AddActivity.class);
        addIntent.putExtra("org", org);
        finish();
        startActivity(addIntent);
    }


//    ============================ Below Code Flow is for show password and copy password ============================

    public static void securityActivity(Context context, int pos, CheckBox checkBox, TextView passTxt) {
        selectedItemPosition = pos;
        checkBoxView = checkBox;
        passTextView = passTxt;
        KeyguardManager kl = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);
        if (kl.isKeyguardSecure()) {
            Intent i = kl.createConfirmDeviceCredentialIntent("Authentication required", "PASSWORD");
            ((Activity) context).startActivityForResult(i, SHOW_PASS_REQ_CODE);
        }
    }

    public static void securityActivity(Context context, int pos) {
        selectedItemPosition = pos;
        KeyguardManager kl = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);
        if (kl.isKeyguardSecure()) {
            Intent i = kl.createConfirmDeviceCredentialIntent("Authentication required", "PASSWORD");
            ((Activity) context).startActivityForResult(i, COPY_PASS_REQ_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SHOW_PASS_REQ_CODE) {

            if (resultCode == RESULT_OK) {
                passwordResult(true, selectedItemPosition, checkBoxView, passTextView);
            } else {
                passwordResult(false, selectedItemPosition, checkBoxView, passTextView);
                Toast.makeText(this, "User Authentication Required", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == COPY_PASS_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                copyPassword(true,selectedItemPosition);
            } else {
                copyPassword(false,selectedItemPosition);
                Toast.makeText(this, "User Authentication Required", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void copyPassword(boolean b, int selectedItemPosition) {
        if (b) {
            String pass = listModel.get(selectedItemPosition).getPass();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("pass", decode(pass));
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Text Copied", Toast.LENGTH_SHORT).show();
        }

    }

    private static void passwordResult(boolean b, int selectedItemPosition, CheckBox child, TextView passText) {

        if (b) {
            String pass = listModel.get(selectedItemPosition).getPass();
            child.setChecked(true);
            passText.setText(decode(pass));
        } else {
            child.setChecked(false);
        }

    }


    private static String decode(String decryptString) {

        String decrypted = null;
        try {
            decrypted = AESUtils.decrypt(decryptString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decrypted;
    }

}
