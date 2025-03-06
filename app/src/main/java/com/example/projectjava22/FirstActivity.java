package com.example.projectjava22;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class FirstActivity extends AppCompatActivity implements View.OnClickListener{
    private Button button1,button2,button3;
    private AlertDialog.Builder alertDialogbuilder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        button1=findViewById(R.id.buttonId1);
        button2= findViewById(R.id.buttonId2);
        //button3=findViewById(R.id.buttonId3);


        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        //button3.setOnClickListener(this);

        //button3 = findViewById(R.id.buttonId3);


    }
    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.buttonId1)
        {
            Intent intent = new Intent(FirstActivity.this,MainActivity.class);
            startActivity(intent);
        }
        if (v.getId()==R.id.buttonId2)
        {
            alertDialogbuilder = new AlertDialog.Builder(FirstActivity.this);
            alertDialogbuilder.setTitle("Warning");
            alertDialogbuilder.setMessage("Do you sure?");
            alertDialogbuilder.setIcon(R.drawable.ttcon);
            alertDialogbuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alertDialogbuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            AlertDialog alertDialog = alertDialogbuilder.create();
            alertDialog.show();
        }

    }
}