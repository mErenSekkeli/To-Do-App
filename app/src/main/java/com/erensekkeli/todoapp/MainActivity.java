package com.erensekkeli.todoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.erensekkeli.todoapp.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Currency;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ArrayList<list> todoList;
    private listAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);

        todoList=new ArrayList<>();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listAdapter=new listAdapter(todoList);
        binding.recyclerView.setAdapter(listAdapter);

        getData();
        if(listAdapter.getItemCount()!=0){
        binding.noTaskMessage.setVisibility(View.INVISIBLE);
        }

    }

    @Override//Menuyu Koda Bağlama
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.todo_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }
    public void newToDo(View view){
        Intent intent=new Intent(this,DetailActivity.class);
        intent.putExtra("info","new");
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //add todo seçildiğinde ne olacak
        if(item.getItemId()==R.id.addTodo){
            Intent intent=new Intent(this,DetailActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);

        }else if(item.getItemId()==R.id.infoPage){
            Intent intent=new Intent(this,InfoActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void getData(){
        try {
            SQLiteDatabase db=this.openOrCreateDatabase("ToDoLists",MODE_PRIVATE,null);
            Cursor cursor=db.rawQuery("SELECT * FROM list",null);
            int titleIndex=cursor.getColumnIndex("title");
            int dateIndex=cursor.getColumnIndex("date");
            int idIndex=cursor.getColumnIndex("id");

            while(cursor.moveToNext()){
                String title=cursor.getString(titleIndex);
                String date=cursor.getString(dateIndex);
                int id=cursor.getInt(idIndex);
                list list=new list(title,date,id);
                todoList.add(list);
            }

            listAdapter.notifyDataSetChanged();
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}