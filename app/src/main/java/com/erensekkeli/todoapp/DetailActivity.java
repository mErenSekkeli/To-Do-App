package com.erensekkeli.todoapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.erensekkeli.todoapp.databinding.ActivityDetailBinding;
import com.erensekkeli.todoapp.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.snackbar.SnackbarContentLayout;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    private Bitmap selectedImage=null;
    private SQLiteDatabase db;
    private int todoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityDetailBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);
        try {
            db=this.openOrCreateDatabase("ToDoLists",MODE_PRIVATE,null);
            db.execSQL("CREATE TABLE IF NOT EXISTS list(id INTEGER PRIMARY KEY, title VARCHAR, contents VARCHAR, date VARCHAR, image BLOB)");
        }catch (Exception e){
            e.printStackTrace();
        }
        Intent intent=getIntent();
        todoId=intent.getIntExtra("todoId",0);
        registerLauncher();
        situation();
    }

    public void doneMission(View view){
        AlertDialog.Builder alert=new AlertDialog.Builder(this);
        alert.setTitle("Delete This Mission? ");
        alert.setMessage("Are You Sure You Have Done This Task?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            db.execSQL("DELETE FROM list WHERE id=?", new String[]{String.valueOf(todoId)});
                Intent intent=new Intent(DetailActivity.this,MainActivity.class);
                Toast.makeText(DetailActivity.this, "Task Has Been Deleted!", Toast.LENGTH_LONG).show();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            binding.isDoneSwitch.setChecked(false);
            Toast.makeText(DetailActivity.this, "Task Has Protected!", Toast.LENGTH_LONG).show();
            }
        });
        alert.show();
    }

    public void situation(){
        Intent intent=getIntent();

        String info=intent.getStringExtra("info");

        if(info.equals("new")){
            //new toDo
            binding.Title.setText("");
            binding.Contents.setText("");
            binding.isDoneSwitch.setVisibility(View.INVISIBLE);
            binding.imageView.setImageResource(R.drawable.selectimage);
            binding.saveButton.setVisibility(View.VISIBLE);
        }else{
            //Old toDo
            binding.saveButton.setVisibility(View.INVISIBLE);
            binding.isDoneSwitch.setVisibility(View.VISIBLE);
            try {

                Cursor cursor=db.rawQuery("SELECT * FROM list WHERE id=?",new String[]{String.valueOf(todoId)});
                int titleIndex=cursor.getColumnIndex("title");
                int contentsIndex=cursor.getColumnIndex("contents");
                int imageIndex=cursor.getColumnIndex("image");
                while(cursor.moveToNext()){
                    binding.Title.setText(cursor.getString(titleIndex));
                    binding.Contents.setText(cursor.getString(contentsIndex));
                    byte[] bytes=cursor.getBlob(imageIndex);
                    Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imageView.setImageBitmap(bitmap);
                }
                cursor.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            binding.imageView.setEnabled(false);
            binding.Title.setFocusableInTouchMode(false);
            binding.Contents.setFocusableInTouchMode(false);
        }
    }

    public void save(View view){
        String title=binding.Title.getText().toString();
        String text=binding.Contents.getText().toString();
        Bitmap smallImage;
        if(selectedImage!=null){
            smallImage=makeSmallerImage(selectedImage,300);
        }else{
            binding.imageView.invalidate();
            BitmapDrawable drawable=(BitmapDrawable) binding.imageView.getDrawable();
            Bitmap nullImage=drawable.getBitmap();
            smallImage=makeSmallerImage(nullImage,300);
        }

        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArr=outputStream.toByteArray();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String date = sdf.format(new Date());
        if(title.equals("")){
            Toast.makeText(DetailActivity.this, "Enter The Mission Title!",Toast.LENGTH_LONG).show();
        }else if(text.equals("")){
            Toast.makeText(DetailActivity.this, "Enter The Contents!",Toast.LENGTH_LONG).show();
        }

        try {
            String sqlString="INSERT INTO list(title, contents, date, image) VALUES(?, ?, ?, ?)";
            SQLiteStatement sqLiteStatement=db.compileStatement(sqlString);
            sqLiteStatement.bindString(1,title);
            sqLiteStatement.bindString(2,text);
            sqLiteStatement.bindString(3,date);
            sqLiteStatement.bindBlob(4,byteArr);
            sqLiteStatement.execute();
        }catch (Exception e) {
            e.printStackTrace();
        }
        //finish(); dersek bu sekmeyi kapatır maine döner
        Intent intent=new Intent(DetailActivity.this,MainActivity.class);
        Toast.makeText(DetailActivity.this, "Mission Has Saved!",Toast.LENGTH_LONG).show();
        //İkinci Yöntem
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public Bitmap makeSmallerImage(Bitmap image,int maxSize){
        int width=image.getWidth();
        int height=image.getHeight();
        float bitMapRatio=(float) width/(float) height;
        if(bitMapRatio>1){
            //landscape
            width=maxSize;
            height=(int) (width/bitMapRatio);
        }else{
            //portrait
            height=maxSize;
            width=(int) (height*bitMapRatio);
        }
        return image.createScaledBitmap(image,width,height,true);
    }

    public void selectImage(View view){

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            //request Permission
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(view, "Permission Needed If You Want To Add Image!",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //request Permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            }else{
                //request Permission
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }else{
            //Go to Gallery
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);
        }

    }

    public void registerLauncher(){
        activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode()==RESULT_OK){
                    Intent intentFromResult=result.getData();
                    if(intentFromResult!=null){
                        Uri imageData=intentFromResult.getData();
                        //binding.imageView.setImageURI(imageData);
                        //Bitmap çevirimi
                        try {
                            if(Build.VERSION.SDK_INT>=28){
                                //Bu Sadece Yüksek Seviyeli Telefonlarda Çalışır
                                ImageDecoder.Source source= ImageDecoder.createSource(getContentResolver(),imageData);
                                selectedImage=ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectedImage);
                            }else{
                                selectedImage=MediaStore.Images.Media.getBitmap(getContentResolver(),imageData);
                            }

                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        permissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    //permission granted
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                }else{
                    //permission denied
                    Toast.makeText(DetailActivity.this,"Permission Denied!",Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}