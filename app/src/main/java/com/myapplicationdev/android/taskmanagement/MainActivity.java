package com.myapplicationdev.android.taskmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    TextView title, desc, date, noMore;
    Button btnAddNew;
    DatabaseReference reference;
    RecyclerView recyclerView;

    Spinner spinner;
    String selected = "";

    FirebaseRecyclerOptions<MyTasks> options;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = findViewById(R.id.spinner);

        title = findViewById(R.id.taskTitle);
        desc = findViewById(R.id.taskDesc);
        date = findViewById(R.id.taskDate);
        noMore = findViewById(R.id.noMore);
        btnAddNew = findViewById(R.id.btnAddNew);

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        reference = FirebaseDatabase.getInstance().getReference().child("TaskApp");

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selected = spinner.getSelectedItem().toString();
                onStart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        btnAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, NewTaskActivity.class);
                startActivity(i);
            }
        });
    }

    public void onStart() {
        super.onStart();
        if (selected.equals("Show all")){
            options = new FirebaseRecyclerOptions.Builder<MyTasks>()
                            .setQuery(reference, MyTasks.class)
                            .build();
        }else{
            Query postsQuery = reference.orderByChild("date").equalTo(getDate());

            options = new FirebaseRecyclerOptions.Builder<MyTasks>()
                            .setQuery(postsQuery, MyTasks.class)
                            .build();
        }


        FirebaseRecyclerAdapter<MyTasks, TaskViewHolder> adapter =
                new FirebaseRecyclerAdapter<MyTasks, TaskViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull TaskViewHolder holder, int position, @NonNull final MyTasks model) {

                        holder.title.setText(model.getTitle());
                        holder.desc.setText(model.getDesc());
                        holder.date.setText(model.getDate());

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                                intent.putExtra("id",model.getId());
                                startActivity(intent);
                            }
                        });

                    }

                    @NonNull
                    @Override
                    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent,false);
                        TaskViewHolder holder = new TaskViewHolder(view);
                        return holder;

                    }
                };
        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    protected String getDate(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/YYYY");
        String formattedDate = df.format(c);
        return formattedDate;
    }
}
