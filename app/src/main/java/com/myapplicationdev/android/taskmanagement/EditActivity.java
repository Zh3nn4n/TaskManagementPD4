package com.myapplicationdev.android.taskmanagement;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditActivity extends AppCompatActivity {

    EditText ettitle, etdesc, etdate;
    Button btnUpdate, btncancel, btnDelete;
    DatabaseReference reference;
    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private final static String default_notification_channel_id = "default" ;
    Calendar myCalendar = Calendar.getInstance();
    Date setDate;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Intent i = getIntent();
        String id = i.getStringExtra("id");

        ettitle = findViewById(R.id.ettitleEdit);
        etdesc = findViewById(R.id.etdescEdit);
        etdate = findViewById(R.id.etdateEdit);

        btnUpdate = findViewById(R.id.btnUpdate);
        btncancel = findViewById(R.id.btnEditCancel);
        btnDelete = findViewById(R.id.btndelete);

        etdate.setShowSoftInputOnFocus(false);
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        etdate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(EditActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        reference = FirebaseDatabase.getInstance().getReference().child("TaskApp").child(id);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                MyTasks tasks = dataSnapshot.getValue(MyTasks.class);
                ettitle.setText(tasks.getTitle());
                etdesc.setText(tasks.getDesc());
                etdate.setText(tasks.getDate());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reference.child("title").setValue(ettitle.getText().toString());
                reference.child("desc").setValue(etdesc.getText().toString());
                reference.child("date").setValue(etdate.getText().toString());

                long diffInMs = setDate.getTime() - System.currentTimeMillis();

                scheduleNotification(getNotification( ettitle.getText().toString()) , diffInMs);
                Toast.makeText(EditActivity.this, "Task Updated successfully", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(EditActivity.this, MainActivity.class);
                startActivity(i);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reference.removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(EditActivity.this, "Task removed successfully", Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(EditActivity.this, MainActivity.class);
                                    startActivity(i);
                                }
                            }
                        });
            }
        });
        btncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
    private void updateLabel() {
        String myFormat = "dd/MM/YYYY";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        Date date = myCalendar.getTime() ;
        setDate = date;
        etdate.setText(sdf.format(date)) ;
    }

    private void scheduleNotification (Notification notification , long delay) {
        Intent notificationIntent = new Intent( this, MyNotificationPublisher.class ) ;
        notificationIntent.putExtra(MyNotificationPublisher. NOTIFICATION_ID , 1 ) ;
        notificationIntent.putExtra(MyNotificationPublisher. NOTIFICATION , notification) ;
        PendingIntent pendingIntent = PendingIntent.getBroadcast ( this, 0 , notificationIntent , PendingIntent. FLAG_UPDATE_CURRENT ) ;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context. ALARM_SERVICE ) ;
        assert alarmManager != null;
        alarmManager.set(AlarmManager. ELAPSED_REALTIME_WAKEUP , delay , pendingIntent) ;
    }
    private Notification getNotification (String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder( this, default_notification_channel_id ) ;
        builder.setContentTitle( "You have Tasks to do" ) ;
        builder.setContentText(content) ;
        builder.setSmallIcon(R.drawable. ic_launcher_foreground ) ;
        builder.setAutoCancel( true ) ;
        builder.setChannelId( NOTIFICATION_CHANNEL_ID ) ;
        return builder.build() ;
    }
}
