package com.vamshi1107.indentifier;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabelerOptionsBase;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView im;
    Bitmap bm;
    LinearLayout layout;
    BottomSheetBehavior bs;
    ListView view;

    ImageLabeler labeler;

    ArrayList<String> al=new ArrayList<String>();

    ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(checkPermissions()){
            requestPermissions();
        }
        initvars();
        initsheet();

        im.setOnClickListener(view -> {
            Content.launch("image/*");
        });
    }

    void  initvars()
    {
        im=findViewById(R.id.imageView);
        layout=findViewById(R.id.sheet);
        view=findViewById(R.id.list);
        labeler= ImageLabeling.getClient(new ImageLabelerOptions.Builder().setConfidenceThreshold(0.7f).build());
        arrayAdapter =new ArrayAdapter(this, android.R.layout.simple_list_item_1,al);
        view.setAdapter(arrayAdapter);
    }

    void initsheet(){
        bs= BottomSheetBehavior.from(layout);
        bs.setPeekHeight(500,true);
        bs.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    ActivityResultLauncher<String> Content = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri->{
                try{
                   bm = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                   im.setImageBitmap(bm);
                   identify();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
    );


    void identify(){
        labeler.process(InputImage.fromBitmap(bm,0)).addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
            @Override
            public void onSuccess(List<ImageLabel> imageLabels) {
                al.clear();
                for(ImageLabel i : imageLabels){
                    al.add(i.getText()+"  ("+i.getConfidence()+")");
                }
                arrayAdapter.notifyDataSetChanged();
                bs.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("data5",e.toString());

            }
        });
    }

    boolean checkPermissions(){
        return ContextCompat
                .checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED;
    }

    void requestPermissions(){
        if(ContextCompat
                .checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},007);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
       if(requestCode==007){
           if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
               Toast.makeText(this,"Permission Granted",Toast.LENGTH_LONG).show();
           }
           else{
               requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},007);
           }
       }
    }
}