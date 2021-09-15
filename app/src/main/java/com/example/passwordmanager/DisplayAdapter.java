package com.example.passwordmanager;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.passwordmanager.Database.DBManager;
import com.example.passwordmanager.Utils.AESUtils;

import java.util.List;

import static android.content.Context.KEYGUARD_SERVICE;

public class DisplayAdapter extends RecyclerView.Adapter<DisplayAdapter.MyViewHolder> {

    List<RecyclerModel> model;
    String org;
    Context context;
    DBManager dbManager;

    DisplayAdapter(Context context, List<RecyclerModel> model, String org) {
        this.model = model;
        this.org = org;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.display_card, parent, false);

        dbManager = new DBManager(parent.getContext());
        dbManager.open();

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        final String uname = model.get(position).getuName();
        final String pass = model.get(position).getPass();
//        final String passOpt = model.get(position).getEncOpt();
        String passTxt = model.get(position).getPass();

        final StringBuilder passEmote = new StringBuilder();

        for (int i = 0; i < passTxt.length(); i++) {

            String unicode = "1F60" + passTxt.charAt(i);
            passEmote.append(String.valueOf(Character.toChars(Integer.parseInt(unicode, 16))));
        }

        holder.unameTxt.setText(model.get(position).getuName());
        holder.passTxt.setText(passEmote);

        holder.changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ChangePassSheet changePassSheet = new ChangePassSheet();
                Bundle bundle = new Bundle();
                bundle.putString("org", org);
                bundle.putString("uname", uname);
                bundle.putString("pass", decode(pass));
                changePassSheet.setArguments(bundle);
                changePassSheet.show(((DisplayActivity) context).getSupportFragmentManager(), "bottom_sheet");

            }
        });

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dbManager.delete(uname, org);
                model.remove(position);
                notifyDataSetChanged();

            }
        });

//        if (passOpt.equals("P-O")) {
//            holder.passDec.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                    ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
//                    ClipData clip = ClipData.newPlainText("pass", decode(pass));
//                    clipboard.setPrimaryClip(clip);
//                    Toast.makeText(view.getContext(), "Text Copied"+passEmote, Toast.LENGTH_SHORT).show();
//
//
//                }
//            });
//        } else if (passOpt.equals("U-P")) {
//            holder.unameDec.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
//                    ClipData clip = ClipData.newPlainText("uname", decode(uname));
//                    clipboard.setPrimaryClip(clip);
//                    Toast.makeText(view.getContext(), "Text Copied", Toast.LENGTH_SHORT).show();
//                }
//            });
//
//            holder.passDec.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
//                    ClipData clip = ClipData.newPlainText("pass", decode(pass));
//                    clipboard.setPrimaryClip(clip);
//                    Toast.makeText(view.getContext(), "Text Copied", Toast.LENGTH_SHORT).show();
//                }
//            });


        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!holder.showPass.isChecked()) {

                    DisplayActivity.securityActivity(context, position,holder.showPass,holder.passTxt);
                }
                else
                {
                    holder.showPass.setChecked(false);
                    holder.passTxt.setText(encode(holder.passTxt.getText().toString()));
                }
            }
        });



//        holder.showPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if(b)
//                {
//                    KeyguardManager kl = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);
//                    if (kl.isKeyguardSecure()) {
//                        Intent i = kl.createConfirmDeviceCredentialIntent("Authentication required", "PASSWORD");
//                        ((Activity)context).startActivityForResult(i, 123);
//                    } else {
//                        Toast.makeText(context, "No any security setup done by user(pattern or password or pin or fingerprint", Toast.LENGTH_SHORT).show();
//                    }
//                    holder.passTxt.setText(decode(pass));
//                }
//                else
//                {
//                    String pass = holder.passTxt.getText().toString();
//
//                    holder.passTxt.setText(encode(pass));
//                }
//            }
//        });

        holder.copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DisplayActivity.securityActivity(context,position);
//                ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
//                ClipData clip = ClipData.newPlainText("pass", decode(pass));
//                clipboard.setPrimaryClip(clip);
//                Toast.makeText(view.getContext(), "Text Copied", Toast.LENGTH_SHORT).show();
            }
        });


    }




    private String decode(String decryptString) {

        String decrypted = null;
        try {
            decrypted = AESUtils.decrypt(decryptString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decrypted;
    }

    private StringBuilder encode(String password) {

        String pass = null;
        final StringBuilder passEmote = new StringBuilder();
        try {
            pass = AESUtils.encrypt(password);

            for (int i = 0; i < pass.length(); i++) {

                String unicode = "1F60" + pass.charAt(i);
                passEmote.append(String.valueOf(Character.toChars(Integer.parseInt(unicode, 16))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return passEmote;
    }


    @Override
    public int getItemCount() {
        return model.size();
    }

static class MyViewHolder extends RecyclerView.ViewHolder {

    TextView unameTxt, passTxt, unameDec, passDec;
    Button changeBtn, deleteBtn;
    ImageButton copyBtn;
    LinearLayout checkBox;
    CheckBox showPass;

    MyViewHolder(@NonNull View itemView) {
        super(itemView);
        unameTxt = itemView.findViewById(R.id.uname_txt);
        passTxt = itemView.findViewById(R.id.pass_txt);
        unameDec = itemView.findViewById(R.id.uname_label);
        passDec = itemView.findViewById(R.id.pass_label);
        changeBtn = itemView.findViewById(R.id.changeBtn);
        deleteBtn = itemView.findViewById(R.id.deleteBtn);
        copyBtn = itemView.findViewById(R.id.cpy_btn);
        checkBox = itemView.findViewById(R.id.check_box);
        showPass = itemView.findViewById(R.id.showPass);

    }
}
}
