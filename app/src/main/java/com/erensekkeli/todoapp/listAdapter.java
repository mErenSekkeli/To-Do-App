package com.erensekkeli.todoapp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.erensekkeli.todoapp.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class listAdapter extends RecyclerView.Adapter<listAdapter.ListHolder> {

    ArrayList<list> todoList;

    public listAdapter(ArrayList<list> todoList) {
        this.todoList = todoList;
    }

    @NonNull
    @Override
    public ListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ListHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ListHolder holder, int position) {
        holder.binding.recyclerViewTextView.setText("Mission: "+todoList.get(position).getTitle()+"\nDate: "+todoList.get(position).getDate());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(holder.itemView.getContext(),DetailActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("todoId",todoList.get(position).getId());
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public class ListHolder extends RecyclerView.ViewHolder{
        private RecyclerRowBinding binding;
        public ListHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }
    }
}
