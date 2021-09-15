package com.example.passwordmanager;

import android.app.Activity;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.passwordmanager.Database.DBManager;
import com.example.passwordmanager.Utils.AESUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.KEYGUARD_SERVICE;

public class ChangePassSheet extends BottomSheetDialogFragment {

    private String oldPass, newPass, retypePass;
    private String uname, org, pass;
    private DBManager dbManager;

    EditText oldPassTxt, newPassTxt, retypePassTxt;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        org = bundle.getString("org");
        uname = bundle.getString("uname");
        pass = bundle.getString("pass");

        dbManager = new DBManager(getContext());
        dbManager.open();

        View v = inflater.inflate(R.layout.change_pass_sheet, container, false);
        oldPassTxt = v.findViewById(R.id.oldPass);
        newPassTxt = v.findViewById(R.id.newPass);
        retypePassTxt = v.findViewById(R.id.retypePass);
        Button changeBtn = v.findViewById(R.id.change_btn);

        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                oldPass = oldPassTxt.getText().toString();
                newPass = newPassTxt.getText().toString();
                retypePass = retypePassTxt.getText().toString();
                if (validation()) {
                    KeyguardManager kl = (KeyguardManager) getContext().getSystemService(KEYGUARD_SERVICE);
                    if (kl.isKeyguardSecure()) {
                        Intent i = kl.createConfirmDeviceCredentialIntent("Authentication required", "PASSWORD");
                        startActivityForResult(i, 123);
                    } else {
                        Toast.makeText(getContext(), "No any security setup done by user(pattern or password or pin or fingerprint", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123 && resultCode == RESULT_OK) {
            changePass();
            Toast.makeText(getContext(), "Unlocked", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validation() {

        if (oldPass.equals("")) {
            oldPassTxt.setError("Please enter the password");
            return false;
        }

        else if (newPass.equals("")) {
            newPassTxt.setError("Please enter the password");
            return false;
        }
        else if (retypePass.equals("")) {
            retypePassTxt.setError("Please enter the password");
            return false;
        }

        else if (!newPass.equals(retypePass)) {
            newPassTxt.setText("");
            newPassTxt.setError("Password Mismatch");
            retypePassTxt.setText("");
            retypePassTxt.setError("Password Mismatch");
            return false;
        }

        else if (!pass.equals(oldPass)) {

            oldPassTxt.setError("Invalid Password");
            return false;
        }

        else
        return true;
    }

    public void changePass() {

        try {
            newPass = AESUtils.encrypt(newPass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dbManager.update(org, uname, newPass);
        this.dismiss();
        getActivity().finish();
        Intent intent = new Intent(getContext(), DisplayActivity.class);
        intent.putExtra("org", org);
        startActivity(intent);
    }

}
