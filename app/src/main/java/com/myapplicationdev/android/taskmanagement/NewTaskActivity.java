package com.myapplicationdev.android.taskmanagement;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class NewTaskActivity extends AppCompatActivity {

    EditText ettitle, etdesc, etdate;
    Button btnsave, btncancel;
    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private final static String default_notification_channel_id = "default" ;

    DatabaseReference reference;
    String uniqueID = UUID.randomUUID().toString();
    Calendar myCalendar = Calendar.getInstance();

    Date setDate;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        ettitle = findViewById(R.id.ettitle);
        etdesc = findViewById(R.id.etdesc);
        etdate = findViewById(R.id.etdate);

        btnsave = findViewById(R.id.btnSaveTask);
        btncancel = findViewById(R.id.btnCancel);

        etdate.setShowSoftInputOnFocus(false);
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        etdate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(NewTaskActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });



        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reference = FirebaseDatabase.getInstance().getReference().child("TaskApp").child(uniqueID);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (TextUtils.isEmpty(ettitle.getText()) || TextUtils.isEmpty(etdesc.getText()) || TextUtils.isEmpty(etdate.getText())){
                            Toast.makeText(NewTaskActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                        }else {

                            dataSnapshot.getRef().child("title").setValue(ettitle.getText().toString());
                            dataSnapshot.getRef().child("desc").setValue(etdesc.getText().toString());
                            dataSnapshot.getRef().child("date").setValue(etdate.getText().toString());
                            dataSnapshot.getRef().child("id").setValue(uniqueID);

                            long diffInMs = setDate.getTime() - System.currentTimeMillis();
                            scheduleNotification(getNotification( ettitle.getText().toString()) , diffInMs);

                            Intent a = new Intent(NewTaskActivity.this, MainActivity.class);
                            startActivity(a);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

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
