package com.example.passwordmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.passwordmanager.Database.DBManager;
import com.example.passwordmanager.Utils.AESUtils;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class AddActivity extends AppCompatActivity {

    DBManager dbManager;
    EditText orgTxt,unameTxt;
    TextInputEditText passTxt;
    RadioGroup encryptOpt;
    Button saveToDb;
    String intentOrg,organization,uname,pass,passOpt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        orgTxt=findViewById(R.id.org);
        unameTxt=findViewById(R.id.uname);
        passTxt=findViewById(R.id.password);
//        encryptOpt=findViewById(R.id.encrypt_options);
        saveToDb=findViewById(R.id.save_btn);

        intentOrg= Objects.requireNonNull(getIntent().getExtras()).getString("org");
        if (intentOrg != null && !intentOrg.equals("new")) {
            orgTxt.setText(intentOrg);
            orgTxt.setEnabled(false);
        }

        dbManager=new DBManager(this);
        dbManager.open();

        saveToDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int radioId;
                organization=orgTxt.getText().toString();
                uname=unameTxt.getText().toString();
                pass=passTxt.getText().toString();
//                radioId=encryptOpt.getCheckedRadioButtonId();
//                if(radioId!=-1)
//                {
//                    switch (radioId) {
//                        case R.id.pass_only:
//                            passOpt = "P-O";
                            crypt(pass);
//                            break;
//                        case R.id.uname_and_pass:
//                            passOpt = "U-P";
//                            crypt(uname,pass);
//                            break;
//                    }
//                }
                organization = organization.trim();
                dbManager.insert(organization,uname,pass);
                Toast.makeText(AddActivity.this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AddActivity.this,DisplayActivity.class);
                intent.putExtra("org",organization);
                finish();
                startActivity(intent);

            }
        });
    }

    private void crypt(String name, String password) {

        try {
            uname = AESUtils.encrypt(name);
            pass = AESUtils.encrypt(password);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void crypt(String password) {

        try {
            pass = AESUtils.encrypt(password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
