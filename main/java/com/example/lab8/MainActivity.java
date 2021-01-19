package com.example.lab8;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lab8.model.MonthlyExpenses;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.android.*;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import static com.google.android.gms.common.util.ArrayUtils.*;

public class MainActivity extends AppCompatActivity {

    //UI
    private TextView tStatus;
    private EditText eSearch, eIncome, eExpenses;

    //firebase

    private DatabaseReference databaseReference;
    ValueEventListener databaseListener;

    private final String[] months = {"January","February","March","April","May","June","July","August","September","October","November","December"};
    private String preSelectedMonth;
    private String selectedMonth;
    MonthlyExpenses monthlyExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tStatus=(TextView)findViewById(R.id.statusText);
        //eSearch=(EditText)findViewById(R.id.eSearch);
        eIncome=(EditText)findViewById(R.id.income);
        eExpenses=(EditText)findViewById(R.id.expenses);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference=database.getReference();

        SharedPreferences sharedPreferences = getSharedPreferences("lastMonth", MODE_PRIVATE);
        //eSearch.setText(sharedPreferences.getString("month", ""));
        preSelectedMonth = sharedPreferences.getString("month","");
        getMonths(new FirebaseCallback() {
            @Override
            public void onCallBack(ArrayList<String> m) {

                Spinner spinner = findViewById(R.id.spinnerMonths);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, m);
                spinner.setAdapter(adapter);
                //spinner.setSelection(0, false);
                //spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                int index = -1;
                if(!preSelectedMonth.equals("")){
                    for(int i = 0; i< m.size();i++){
                        if(m.get(i).equals(preSelectedMonth)){
                            index = i;
                            break;
                        }
                    }
                    createNewDBListener(preSelectedMonth);
                }
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedMonth = parent.getItemAtPosition(position).toString().toLowerCase();
                        SharedPreferences.Editor sharedPreferencesEditor = getSharedPreferences("lastMonth", MODE_PRIVATE).edit();
                        sharedPreferencesEditor.putString("month", selectedMonth);
                        sharedPreferencesEditor.apply();

                        tStatus.setText("Searching ...");
                        createNewDBListener(selectedMonth);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });



            }
        });

    }


    public void clicked(View view) {
        String currentMonth;
        switch (view.getId()) {
            /*case R.id.search:
                {
                    currentMonth = eSearch.getText().toString();
                    SharedPreferences.Editor sharedPreferencesEditor = getSharedPreferences("lastMonth", MODE_PRIVATE).edit();
                    sharedPreferencesEditor.putString("month", currentMonth);
                    sharedPreferencesEditor.apply();
                    createNewDBListener(currentMonth);
            }*/

            case R.id.bUpdate:
            {
                if (!(eExpenses.getText().toString().isEmpty()) && !(eIncome.getText().toString().isEmpty()) ){
                    float income = Float.parseFloat(eIncome.getText().toString());
                    float expenses = Float.parseFloat(eExpenses.getText().toString());
                    if(selectedMonth != null)
                        updateDb(selectedMonth, income, expenses);
                    else if(preSelectedMonth != null)
                        updateDb(preSelectedMonth, income, expenses);

                }
            }
        }

    }
    protected void onResume() {
        super.onResume();

    }

    private void createNewDBListener(String currentMonth) {

        if (databaseReference != null && currentMonth != null && databaseListener != null)
            databaseReference.child("Calender").child(currentMonth).removeEventListener(databaseListener);

        databaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    MonthlyExpenses monthlyExpense = dataSnapshot.getValue(MonthlyExpenses.class);
                    monthlyExpense.month = dataSnapshot.getKey();

                    eIncome.setText(String.valueOf(monthlyExpense.getIncome()));
                    eExpenses.setText(String.valueOf(monthlyExpense.getExpenses()));
                    tStatus.setText("Data for " + currentMonth);
                } catch (NullPointerException e) {
                    eIncome.setText(String.valueOf(0));
                    eExpenses.setText(String.valueOf(0));
                    tStatus.setText("No data for " + currentMonth);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        };

        // set new databaseListener
        databaseReference.child("Calender").child(currentMonth).addValueEventListener(databaseListener);
    }



    private void updateDb(String month, float income, float expenses) {
        MonthlyExpenses monthlyExpenses = new MonthlyExpenses(month, income, expenses);
        databaseReference.child("Calender").child(month).child("income").setValue(income);
        databaseReference.child("Calender").child(month).child("expenses").setValue(expenses);
    }

    private interface FirebaseCallback {
        void onCallBack(ArrayList<String> m);
    }

    private void getMonths(FirebaseCallback callback) {
        ArrayList<String> firebaseMonths = new ArrayList<String>();
        Query query = databaseReference.child("Calender").orderByValue();
        ((Query) query).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot month : snapshot.getChildren()) {
                        firebaseMonths.add(month.getKey());
                    }
                    callback.onCallBack(firebaseMonths);
                }
                callback.onCallBack(firebaseMonths);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Spinner spinner = findViewById(R.id.spinnerMonths);
        String[] items = firebaseMonths.toArray(new String[0]);
        System.out.println(items.toString());

    }

}