package com.example.passwordmanager;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OrgCardAdapter extends RecyclerView.Adapter<OrgCardAdapter.MyViewHolder> {



    List<RecyclerModel> model;

    OrgCardAdapter(List<RecyclerModel> populateModel) {
        this.model=populateModel;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.org_card, parent, false);

//      View On click Move to next page

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Id is"+view.getId(), Toast.LENGTH_SHORT).show();
            }
        });

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {

        holder.orgTxt.setText(model.get(position).getOrg());


        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(),DisplayActivity.class);
                intent.putExtra("org",model.get(position).getOrg());
                view.getContext().startActivity(intent);
            }
        });

    }


    @Override
    public int getItemCount() {
        return model.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView orgTxt;
        CardView cardView;

        public MyViewHolder(View itemView) {

            super(itemView);
            this.orgTxt = itemView.findViewById(R.id.org_txt);
            this.cardView=itemView.findViewById(R.id.card);

        }
    }
}
