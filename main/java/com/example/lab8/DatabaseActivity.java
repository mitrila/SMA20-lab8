package com.example.lab8;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lab8.ui.Payment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseActivity extends AppCompatActivity {

    private TextView name, type, cost;
    String time;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        name = findViewById(R.id.nameView);
        type = findViewById(R.id.typeView);
        cost = findViewById(R.id.costView);
        Button button = findViewById(R.id.saveButton);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            //on click update the database
            public void onClick(View view) {
                Double c = Double.parseDouble(cost.getText().toString());
                String n = name.getText().toString();
                String t = type.getText().toString();
                time = getCurrentTimeDate();
                databaseReference.child("wallet").child(time).setValue(new Payment(c, n, t));
                finish();
            }

            private void finish() {
                this.finish();
            }
        });
    }


    private static String getCurrentTimeDate() {
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        return date.format(now);
    }



}


