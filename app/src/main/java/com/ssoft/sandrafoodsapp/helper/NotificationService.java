package com.ssoft.sandrafoodsapp.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.ssoft.sandrafoodsapp.R;
import com.ssoft.sandrafoodsapp.activity.TelaIdentificacao;
import com.ssoft.sandrafoodsapp.model.Andamento;

import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {

    Timer timer;
    TimerTask timerTask;
    String TAG = "Timers";
    int Your_X_SECS = 5;
    String chavePedido = "";
    DatabaseReference firebaseBanco = FirebaseDatabase.getInstance().getReference();
    DatabaseReference andamentoDB;
    Andamento recuperadoFB = new Andamento();
    int notificacaoStatus = -1;


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        SQLiteDatabase bancoDados;

        try{
            String id = "Erro";
            String nome = "Erro";
            String celular = "Erro";
            bancoDados = this.openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
            //Recuperar pessoas
            Cursor cursor = bancoDados.rawQuery("SELECT id, nome, celular FROM cliente LIMIT 1", null);
            //Recupera indices
            int indiceId = cursor.getColumnIndex("id");
            int indiceNome = cursor.getColumnIndex("nome");
            int indiceCelular = cursor.getColumnIndex("celular");
            if(cursor.moveToFirst()){
                id = cursor.getString(indiceId);
                nome = cursor.getString(indiceNome);
                celular = cursor.getString(indiceCelular);
            }
            String celularSoNumeros = celular.replace("(", "").replace(")","").replace(" ", "").replace("-", "");
            String nomeSoLetras = nome.replace("/", "-").replace("\\", "-").replace(" ", "");
            chavePedido = celularSoNumeros + "-" + nomeSoLetras;
        }
        catch(Exception e){
            String erro = e.toString();
            Log.e(TAG, "erro: " + erro);
        }

        startTimer();
        return START_STICKY;
    }


    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        stoptimertask();
        super.onDestroy();


    }

    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();


    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 5000, Your_X_SECS * 1000); //
        //timer.schedule(timerTask, 5000,1000); //
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {

                        //TODO CALL NOTIFICATION FUNC
                        verificaStatusPedido();

                    }
                });
            }
        };
    }

    public void verificaStatusPedido()
    {
        DatabaseReference andamentoDB = firebaseBanco.child("andamento");
        andamentoDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    if(ds.getValue(Andamento.class).getId().equals(chavePedido)) //se for 0, é adicional
                    {
                        recuperadoFB = ds.getValue(Andamento.class);
                        break;
                    }
                }
                recuperaStatusNotificacaoFB();
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public void recuperaStatusNotificacaoFB()
    {
        DatabaseReference andamentoDB = firebaseBanco.child("notificacao").child(chavePedido);
        andamentoDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null)
                    notificacaoStatus = snapshot.getValue(Integer.class);

                if(recuperadoFB.getStatus() == "SAIU" && notificacaoStatus == 0)
                    gerarNotificacao("Seu pedido saiu para entrega!");
                if(recuperadoFB.getStatus() == "PRONTO" && notificacaoStatus == 0)
                    gerarNotificacao("Seu pedido está pronto! Basta retirá-lo no balcão.");

            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    String CHANNEL_ID = "sandra_foods";
    public void gerarNotificacao(String notificacao)
    {
        DatabaseReference notificacaoDB = firebaseBanco.child("notificacao");
        notificacaoDB.child(chavePedido).setValue(1);

        Intent notificationIntent = new Intent(this, TelaIdentificacao.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] pattern = {500,500,500,500,500,500,500,500,500};

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_fastfood_24)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icone_sf_bmp))
                .setContentTitle("Pedido em Andamento")
                .setContentText(notificacao)
                .setContentIntent(contentIntent)
                .setLights(Color.WHITE, 500, 500)
                .setVibrate(pattern)
                .setSound(alarmSound)
                .setStyle(new NotificationCompat.InboxStyle())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "meu_canal";
            String description = "minha_descricao";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            notificationManager.notify(1, builder.build());
        }
        else
        {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(1, builder.build());
        }

        /*NotificationManager mNotificationManager = (NotificationManager) getSystemService( NOTIFICATION_SERVICE ) ;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext() , CHANNEL_ID ) ;
        mBuilder.setContentTitle( "My Notification" ) ;
        mBuilder.setContentText( "Notification Listener Service Example" ) ;
        mBuilder.setTicker( "Notification Listener Service Example" ) ;
        mBuilder.setSmallIcon(R.drawable. ic_launcher_foreground ) ;
        mBuilder.setAutoCancel( true ) ;
        if (android.os.Build.VERSION. SDK_INT >= android.os.Build.VERSION_CODES. O ) {
            int importance = NotificationManager. IMPORTANCE_HIGH ;
            NotificationChannel notificationChannel = new NotificationChannel( CHANNEL_ID , "sandra_foods" , importance) ;
            mBuilder.setChannelId( CHANNEL_ID ) ;
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel(notificationChannel) ;
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(( int ) System. currentTimeMillis () , mBuilder.build()) ;*/
    }
}
