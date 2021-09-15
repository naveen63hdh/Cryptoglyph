package com.example.passwordmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.passwordmanager.Database.DBManager;
import com.example.passwordmanager.Database.DatabaseHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static RecyclerView recyclerView;
    DBManager dbManager;
    Cursor cursor;
    OrgCardAdapter cardAdapter;
    private static final int STORAGE_REQUEST_CODE = 100;
    String[] storagePermission;
    File myDir;

    String LOG_TAG = "Problem Check @MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Recycler view initialization
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

//      Database Init
        dbManager = new DBManager(this);
        dbManager.open();

        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    @Override
    protected void onStart() {
        super.onStart();
        cursor = dbManager.fetchOrg();

        if (!cursor.isBeforeFirst()) {
            cardAdapter = new OrgCardAdapter(populateModel());
            recyclerView.setAdapter(cardAdapter);
        } else {
            recyclerView.setAdapter(null);
            Toast.makeText(this, "Add New Password", Toast.LENGTH_SHORT).show();
        }
    }

    private List<RecyclerModel> populateModel() {

        List<RecyclerModel> model = new ArrayList<>();

        do {
            String org = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ORG));
            RecyclerModel mod = new RecyclerModel(org);
            model.add(mod);
        } while (cursor.moveToNext());

        return model;
    }

    //    Three dots menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu_item, menu);
        return true;
    }

    //    Three dots menu option selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.add_new:
                Intent addIntent = new Intent(this, AddActivity.class);
                addIntent.putExtra("org", "new");
                startActivity(addIntent);
                return true;
            case R.id.imports:
                if (!checkStoragePermission())
                    requestStoragePermission();
                if(checkStoragePermission()) {
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.dialog2);
                    SpannableString title = new SpannableString("Important Message");
                    title.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimary)), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    alertDialog.setTitle(title);
                    alertDialog.setMessage("By importing database all your current passwords will be deleted.");

                    alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            importDB();
                        }
                    });
                    alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    alertDialog.show();

                }
                else
                    Toast.makeText(this, "Permission is Denied", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.export:
                if (!checkStoragePermission())
                    requestStoragePermission();

                if(checkStoragePermission()) {
                    myDir = new File(Environment.getExternalStorageDirectory() + "/Cryptoglyph");
                    if (!myDir.exists()) {
                        myDir.mkdir();
                    }
                    exportDB();
                }
                else
                    Toast.makeText(this, "Permission is Denied", Toast.LENGTH_SHORT).show();
                return true;
//            case R.id.pin:
//                Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
//                return true;
//            case R.id.emote:
//                Toast.makeText(this, "s", Toast.LENGTH_SHORT).show();
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }

    private void importDB() {
        ProgressDialog pg = ProgressDialog.show(MainActivity.this, "Importing", "Please wait until import complete");
        DBManager dbManager = new DBManager(this);
        dbManager.open();
        File sd = Environment.getExternalStorageDirectory();
        String path = Environment.getExternalStorageDirectory() + "/Cryptoglyph/" + DatabaseHelper.DB_NAME;
        File backupDB = new File(sd, path);
        try {
            SQLiteDatabase database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
            dbManager.importDb(this, database);
            pg.cancel();
            onStart();

        } catch (SQLiteCantOpenDatabaseException e) {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.dialog);
            SpannableString title = new SpannableString("No Database Found");
            title.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimary)), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            alertDialog.setTitle(title);
            alertDialog.setMessage("You either didn't export any database or deleted the exported database from phone");

            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            alertDialog.show();
        }
    }
    private void exportDB() {

        Log.i(LOG_TAG, "Came to Method");

        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String packageName = "com.example.passwordmanager";
                String currentDBPath = "//data//" + packageName
                        + "//databases//" + DatabaseHelper.DB_NAME;

                String backupDBPath ="/Cryptoglyph/" + DatabaseHelper.DB_NAME;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);


                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Log.i(LOG_TAG, "Success");
                Toast.makeText(getBaseContext(), "Exported Successfully",
                        Toast.LENGTH_LONG).show();

            }
        } catch (Exception e) {

            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG)
                    .show();

        }
    }
}

