package com.ssoft.sandrafoodsapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.ssoft.sandrafoodsapp.R;
import com.ssoft.sandrafoodsapp.helper.NotificationService;
import com.ssoft.sandrafoodsapp.model.Andamento;
import com.ssoft.sandrafoodsapp.model.ItemPedido;
import com.ssoft.sandrafoodsapp.model.Pedido;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.thekhaeng.pushdownanim.PushDownAnim.MODE_STATIC_DP;

public class TelaAcompanhamentoPedido extends AppCompatActivity {


    DatabaseReference firebaseBanco = FirebaseDatabase.getInstance().getReference();
    DatabaseReference andamentoDB;
    private String chavePedido; //chave do pedido gerado. Pode vir da tela de confirmação ou do banco de dados do celular
    private String dataPedido; //se a data for diferente da atual, apenas mandamos a merda
    private SQLiteDatabase bancoDados;
    private Andamento recuperadoDB = new Andamento();
    private Andamento recuperadoFB = new Andamento();
    private int parametroPrimeiroAcesso = 0;
    private ImageView btWhats;
    private Button btLocalizacaoPedido, btInformacoesPedido;
    private ImageView ivStatus;
    private TextView tvStatus;
    private ConstraintLayout loTelaAcompPedido;
    private Pedido pedBanco = new Pedido();
    private Pedido pedidoFB = new Pedido();
    private List<ItemPedido> listaPedido = new ArrayList<>();
    private int NOTIFICATION_ID = 459302938;
    private int parametroAbertoOuFechado = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_acompanhamento_pedido);
        getSupportActionBar().hide();

        btWhats = findViewById(R.id.ivAcompPedidoWhats);
        btLocalizacaoPedido = findViewById(R.id.btAcompPedidoAcompanharEntrega);
        btInformacoesPedido = findViewById(R.id.btAcompPedidoDadosDoPedido);
        ivStatus = findViewById(R.id.ivAcompPedidoImagemStatus);
        tvStatus = findViewById(R.id.ivAcompPedidoTextoStatus);
        loTelaAcompPedido = findViewById(R.id.loTelaAcompanhamentoPedido);

        loTelaAcompPedido.setVisibility(View.INVISIBLE);

        btLocalizacaoPedido.setVisibility(View.GONE);

        try{
            Bundle dados = getIntent().getExtras();
            recuperadoDB.setData(dados.getString("dataPedido"));
            recuperadoDB.setId(dados.getString("chavePedido"));
            //String parametroString = dados.getString("parametroAbertoOuFechado");
            parametroAbertoOuFechado = dados.getInt("parametroAbertoOuFechado");
            chavePedido = recuperadoDB.getId();
            dataPedido = recuperadoDB.getData();

            if(TextUtils.isEmpty(chavePedido))
            {
                recuperaChaveDoBanco();
            }

            criaListenerParaAndamentoDoPedido();
        }catch (Exception e)
        {
            SuperActivityToast.create(this, new Style(), Style.TYPE_STANDARD)
                    .setText("Erro ao abrir tela de acompanhamento.")
                    .setDuration(Style.DURATION_SHORT)
                    .setColor(getResources().getColor(R.color.marrom))
                    .setAnimations(Style.ANIMATIONS_POP)
                    .show();
            Intent intent = new Intent(getApplicationContext(), TelaBemVindo.class);
            startActivity(intent);
        }

        PushDownAnim.setPushDownAnimTo(btWhats)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clickWhats();
                    }
                });

        PushDownAnim.setPushDownAnimTo(btLocalizacaoPedido)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        abrirTelaAcompanharEntrega();
                    }
                });
        PushDownAnim.setPushDownAnimTo(btInformacoesPedido)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        gerarDialogoApresentarDadosPedido();
                    }
                });

        recuperarDadosPedido();
        recuperarItensPedido();

        loTelaAcompPedido.animate().translationY(-250).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                loTelaAcompPedido.setVisibility(View.VISIBLE);
                loTelaAcompPedido.animate().translationY(0).alpha(1.0f).setDuration(250);
            }
        });
    }

    @Override
    public void onBackPressed() {
        return;
    }

    public void criaListenerParaAndamentoDoPedido()
    {
        DatabaseReference andamentoDB = firebaseBanco.child("andamento").child(chavePedido).child("status");
        andamentoDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null)
                    recuperaRecuperadoFB();
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public void clickWhats()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Abrir WhatsApp")
                .setMessage("O aplicativo deseja abrir o WhatsApp. Deseja permitir?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Uri uri = Uri.parse("smsto:" + "47997838305");
                            Intent i = new Intent(Intent.ACTION_SENDTO, uri);
                            //i.putExtra(Intent.EXTRA_TEXT, "Olá, gostaria de fazer um pedido!");
                            i.setPackage("com.whatsapp");
                            startActivity(i);
                        }catch(Exception e)
                        {
                            try{
                                Uri uri = Uri.parse("smsto:" + "47997838305");
                                Intent j = new Intent(Intent.ACTION_SENDTO, uri);
                                //j.putExtra(Intent.EXTRA_TEXT, "Olá, gostaria de fazer um pedido!");
                                j.setPackage("com.whatsapp.w4b");
                                startActivity(j);
                            }catch (Exception f)
                            {
                                Toast.makeText(TelaAcompanhamentoPedido.this, "Ocorreu um erro ao abrir o aplicativo: " + f.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
        builder.show();
    }
    public void recuperaRecuperadoFB()
    {
        DatabaseReference andamentoFirebase = firebaseBanco.child("andamento").child(chavePedido);
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null)
                {
                    recuperadoFB = dataSnapshot.getValue(Andamento.class);
                    if(recuperadoFB.getStatus() != "FINALIZADO")
                        recuperaStatusNotificacaoFB();
                    configuraTela();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        andamentoFirebase.addListenerForSingleValueEvent(eventListener);

        /*DatabaseReference andamentoDB = firebaseBanco.child("andamento");
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
                if(recuperadoFB.getStatus() != "FINALIZADO")
                    recuperaStatusNotificacaoFB();
                configuraTela();
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });*/
    }

    private int notificacaoStatus = -1;

    public void recuperaStatusNotificacaoFB()
    {
        DatabaseReference notificacaoFirebase = firebaseBanco.child("notificacao").child(chavePedido);
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null)
                {
                    notificacaoStatus = dataSnapshot.getValue(Integer.class);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        notificacaoFirebase.addListenerForSingleValueEvent(eventListener);


        /*DatabaseReference andamentoDB = firebaseBanco.child("notificacao").child(chavePedido);
        andamentoDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null)
                {
                    notificacaoStatus = snapshot.getValue(Integer.class);
                }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });*/
    }
    public void configuraTela()
    {
        //verificamos o status do pedido para mostrar a imagem e frase correta
        if(recuperadoFB.getStatus().equals("EM PREPARACAO"))
        {
            Glide.with(this)
                    .asGif()
                    .load(R.drawable.status_em_preparacao_sem_fundo)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                    .centerInside()
                    .into(ivStatus);
            tvStatus.setText("Seu pedido está sendo preparado!");
            btLocalizacaoPedido.setVisibility(View.GONE);
        }
        if(recuperadoFB.getStatus().equals("SAIU"))
        {
            Glide.with(this)
                    .asGif()
                    .load(R.drawable.status_saiu_sem_fundo)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                    .centerInside()
                    .into(ivStatus);
            tvStatus.setText("Seu pedido saiu para entrega!");
            btLocalizacaoPedido.setVisibility(View.VISIBLE);
            if(notificacaoStatus == 0)
                gerarNotificacao("Seu pedido saiu para entrega!");
            else
                limpaNotificacao();
        }
        if(recuperadoFB.getStatus().equals("PRONTO"))
        {
            Glide.with(this)
                    .asGif()
                    .load(R.drawable.status_pronto_sem_fundo)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                    .centerInside()
                    .into(ivStatus);
            tvStatus.setText("Seu pedido está pronto! Basta retirá-lo no balcão.");
            btLocalizacaoPedido.setVisibility(View.GONE);
            if(notificacaoStatus == 0)
                gerarNotificacao("Seu pedido está pronto! Basta retirá-lo no balcão.");
            else
                limpaNotificacao();
        }
        if(recuperadoFB.getStatus().equals("FINALIZADO"))
        {
            int emojiUnicode = 0x1F970;
            String emojiToast = getEmojiByUnicode(emojiUnicode);
            SuperActivityToast.create(TelaAcompanhamentoPedido.this, new Style(), Style.TYPE_STANDARD)
                    .setText("Pedido Finalizado! Muito obrigada! " + emojiToast)
                    .setTextColor(getResources().getColor(R.color.black))
                    .setDuration(Style.DURATION_MEDIUM)
                    .setColor(getResources().getColor(R.color.verde))
                    .setAnimations(Style.ANIMATIONS_POP)
                    .show();
            //APAGAMOS DO FIREBASE FINALIZANDO POR COMPLETO O PEDIDO
            firebaseBanco.child("pedido").child(chavePedido).removeValue();
            firebaseBanco.child("pedido-itens").child(chavePedido).removeValue();
            firebaseBanco.child("andamento").child(chavePedido).removeValue();
            firebaseBanco.child("notificacao").child(chavePedido).removeValue();

            SuperActivityToast.create(this, new Style(), Style.TYPE_STANDARD)
                    .setText("Pedido Finalizado! Muito obrigada! <3")
                    .setTextColor(getResources().getColor(R.color.black))
                    .setDuration(Style.DURATION_MEDIUM)
                    .setColor(getResources().getColor(R.color.verde))
                    .setAnimations(Style.ANIMATIONS_POP)
                    .show();

            if(parametroAbertoOuFechado == 0)
            {
                loTelaAcompPedido.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        Intent intent = new Intent(getApplicationContext(), TelaEstabelecimentoFechado.class);
                        startActivity(intent);
                    }
                });
            }
            else {
                loTelaAcompPedido.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        Intent intent = new Intent(getApplicationContext(), TelaPrincipal.class);
                        startActivity(intent);
                    }
                });
            }
        }
    }

    public String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

    public void limpaNotificacao()
    {
        NotificationManager notificationManager = (NotificationManager)getSystemService(this.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public void gerarDialogoApresentarDadosPedido()
    {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(TelaAcompanhamentoPedido.this);
        bottomSheetDialog.setContentView(R.layout.layout_dialogo_mostrar_informacoes_pedido);

        bottomSheetDialog.setCanceledOnTouchOutside(false);

        TextView tvDataPedido = bottomSheetDialog.findViewById(R.id.tvDialogoMostrarPedData);
        TextView tvTipoPedido = bottomSheetDialog.findViewById(R.id.tvDialogoMostrarPedTipoPedido);
        TextView tvEndEntrega = bottomSheetDialog.findViewById(R.id.tvDialogoMostrarPedAuxiliarTipoPedido);
        TextView tvMeuPedido = bottomSheetDialog.findViewById(R.id.tvDialogoMostrarPedItensPedido);
        TextView tvObsPedido = bottomSheetDialog.findViewById(R.id.tvDialogoMostrarPedObsPedido);
        TextView tvTotalPedido = bottomSheetDialog.findViewById(R.id.tvDialogoMostrarPedValorPedido);
        TextView tvDescPag = bottomSheetDialog.findViewById(R.id.tvDialogoMostrarPedPagamentoPedido);
        Button btCancelar = bottomSheetDialog.findViewById(R.id.btDialogoMostrarPedFechar);
        ConstraintLayout loEndereco = bottomSheetDialog.findViewById(R.id.loDialogoConfPedidoEndereco);
        ConstraintLayout loPagamento = bottomSheetDialog.findViewById(R.id.loDialogoConfPedidoPagamento);

        loEndereco.setVisibility(View.GONE);
        loPagamento.setVisibility(View.GONE);

        try {
            //PREENCHEMOS A TELA DOS DADOS DO PEDIDO AGORA QUE RECUPERAMOS AS INFORMAÇÕES DO BANCO
            tvDataPedido.setText(pedidoFB.getData());
            tvTipoPedido.setText(pedidoFB.getTipo());
            if(pedidoFB.getTipo().equals("ENTREGA"))
            {
                loEndereco.setVisibility(View.VISIBLE);
                loPagamento.setVisibility(View.VISIBLE);
                String enderecoFinal = pedidoFB.getClt_nome() + " - " + pedidoFB.getClt_celular() + " - " + pedidoFB.getClt_rua() + " - " + pedidoFB.getClt_numero() + " - " + pedidoFB.getClt_bairro() + " - " + pedidoFB.getClt_referencia();
                tvEndEntrega.setText(enderecoFinal);

                if(pedidoFB.getPagamento().equals("DINHEIRO"))
                {
                    String valorTrocoMostrarConfirmacao;
                    if(pedidoFB.getTroco().equals("R$0,00"))
                        valorTrocoMostrarConfirmacao = "(Não preciso de troco)";
                    else
                        valorTrocoMostrarConfirmacao = "(Troco para " + pedidoFB.getTroco() + ")";
                    tvDescPag.setText("DINHEIRO " + valorTrocoMostrarConfirmacao);
                }
                else
                {
                    tvDescPag.setText(pedidoFB.getPagamento());
                }
            }
        }
        catch (Exception e)
        {
            SuperActivityToast.create(this, new Style(), Style.TYPE_STANDARD)
                    .setText("Ocorreu um erro ao exibir o seu pedido")
                    .setDuration(Style.DURATION_SHORT)
                    .setColor(getResources().getColor(R.color.marrom))
                    .setAnimations(Style.ANIMATIONS_POP)
                    .show();
            return;
        }

        //Preenchemos os itens do pedido
        String pedidoMostrar = "";
        int cont = 0;
        int tamLista = listaPedido.size() - 1;
        for(ItemPedido item : listaPedido)
        {
            pedidoMostrar = pedidoMostrar + String.valueOf(item.getQtd_item()) + " x " + item.getDesc_item();
            if(cont < tamLista)
                pedidoMostrar = pedidoMostrar  + "\n";
            cont++;
        }
        tvMeuPedido.setText(pedidoMostrar);

        tvObsPedido.setText(pedidoFB.getObservacao());

        Locale ptBr = new Locale("pt", "BR");
        String valorMostrar = NumberFormat.getCurrencyInstance(ptBr).format(pedidoFB.getValor());
        tvTotalPedido.setText(valorMostrar);

        PushDownAnim.setPushDownAnimTo(btCancelar)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss();
                    }
                });

        bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        bottomSheetDialog.show();
    }

    public void abrirTelaAcompanharEntrega()
    {
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            new AlertDialog.Builder(this)
                    .setMessage("Para acessar esta tela, é necessário que a sua localização esteja ativa.")
                    .setPositiveButton("Ativar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            //getApplicationContext().startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Voltar",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            return;
                        }
                    })
                    .show();
        }
        else
        {
            Intent intent = new Intent(getApplicationContext(), TelaAcompanharMotoboy.class);
            intent.putExtra("chavePedido", chavePedido);
            intent.putExtra("dataPedido", dataPedido);
            startActivity(intent);
        }
    }

    public void recuperarDadosPedido()
    {
        DatabaseReference andamentoDB = firebaseBanco.child("pedido").child(chavePedido);
        andamentoDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                pedidoFB = snapshot.getValue(Pedido.class);
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public void recuperarItensPedido()
    {
        DatabaseReference andamentoDB = firebaseBanco.child("pedido-itens").child(chavePedido);
        andamentoDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                listaPedido.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    listaPedido.add(ds.getValue(ItemPedido.class));
                }
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
                .setSmallIcon(R.drawable.icone_sf_bmp)
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
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
        else
        {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
        //wl.release();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {

            this.setTurnScreenOn(true);
        } else {
            final Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        startService( new Intent( this, NotificationService.class )) ;
    }

    public void recuperaChaveDoBanco()
    {
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
            String nomeSoLetras = nome.replace(" ", "").replace("/", "-").replace("\\", "-");
            chavePedido = celularSoNumeros + "-" + nomeSoLetras;
        }
        catch(Exception e){
            String erro = e.toString();
            Log.e("SQLITE", "erro: " + erro);
        }
    }
}