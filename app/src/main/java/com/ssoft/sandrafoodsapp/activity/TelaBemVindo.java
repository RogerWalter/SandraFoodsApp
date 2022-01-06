package com.ssoft.sandrafoodsapp.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.ssoft.sandrafoodsapp.R;
import com.ssoft.sandrafoodsapp.model.Andamento;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import static com.thekhaeng.pushdownanim.PushDownAnim.MODE_STATIC_DP;

public class TelaBemVindo extends AppCompatActivity {

    DatabaseReference firebaseBanco = FirebaseDatabase.getInstance().getReference();
    private SQLiteDatabase bancoDados;
    private TextView tvFrase;
    private Button btEntrar;
    private ImageView bteWhats;
    private ImageView ivLogo;
    private ConstraintLayout loTelaBemVindo;
    private ArrayList<Andamento> listaAndamento = new ArrayList<>();

    private Andamento recuperado = new Andamento();
    private String chavePedido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_bem_vindo);
        getSupportActionBar().hide();
        //Instancia o banco de dados
        bancoDados = openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
        //Inicializa os componentes
        inicializaComponentes();
        //busca o nome do cliente no banco de dcados
        String nome = recuperaNome();
        //verificar ultimo acesso e limpar tabela de itens do pedido, se necessário
        verificarUltimoAcesso();
        //gera a frase aleatoria
        gerarFrase(nome);

        //Instancia o listener do botão do WhatsApp

        PushDownAnim.setPushDownAnimTo(bteWhats)
                .setScale( MODE_STATIC_DP, 16  )
                .setInterpolatorPush( PushDownAnim.DEFAULT_INTERPOLATOR )
                .setInterpolatorRelease( PushDownAnim.DEFAULT_INTERPOLATOR )
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickWhats();
            }
        });

        PushDownAnim.setPushDownAnimTo(btEntrar)
                .setScale( MODE_STATIC_DP, 16  )
                .setInterpolatorPush( PushDownAnim.DEFAULT_INTERPOLATOR )
                .setInterpolatorRelease( PushDownAnim.DEFAULT_INTERPOLATOR )
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickBotaoEntrar();
            }
        });
        animarComponentes();
    }
    public void inicializaComponentes()
    {
        ivLogo = findViewById(R.id.ivBemVindo);
        tvFrase = findViewById(R.id.tvBemVindo);
        btEntrar = findViewById(R.id.btEntrar);
        bteWhats = findViewById(R.id.ivWhatsTelaBemVindo);
        loTelaBemVindo = findViewById(R.id.loTelaBemVindo);
        loTelaBemVindo.setVisibility(View.INVISIBLE);
    }

    public void animarComponentes()
    {
        ivLogo.setVisibility(View.INVISIBLE);
        tvFrase.setVisibility(View.INVISIBLE);
        btEntrar.setVisibility(View.INVISIBLE);
        bteWhats.setVisibility(View.INVISIBLE);

        loTelaBemVindo.setVisibility(View.VISIBLE);

        ivLogo.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ivLogo.animate().translationY(0).alpha(1.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        ivLogo.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvFrase.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        tvFrase.animate().translationY(0).alpha(1.0f).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                tvFrase.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
            }
        }, 250);

        final Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                btEntrar.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        btEntrar.animate().translationY(0).alpha(1.0f).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                btEntrar.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
            }
        }, 500);

        final Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                bteWhats.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        bteWhats.animate().translationY(0).alpha(1.0f).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                bteWhats.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
            }
        }, 750);
    }

    public String recuperaNome()
    {
        String nome = "Erro";

        //bancoDados.execSQL("DROP TABLE item_pedido");

        //Recuperar pessoas
        Cursor cursor = bancoDados.rawQuery("SELECT id, nome, celular FROM cliente LIMIT 1", null);

        //Recupera indices
        int indiceId = cursor.getColumnIndex("id");
        int indiceNome = cursor.getColumnIndex("nome");
        int indiceCelular = cursor.getColumnIndex("celular");

        if(cursor.moveToFirst()){
            cursor.getString(indiceId);
            nome = cursor.getString(indiceNome);
            cursor.getString(indiceCelular);
        }
        return nome;
    }

    public String recuperaUltimoAcesso()
    {
        String dataSalva = "Erro";

        //bancoDados.execSQL("DROP TABLE item_pedido");

        //Recuperar pessoas
        Cursor cursor = bancoDados.rawQuery("SELECT data FROM ultimoacesso LIMIT 1", null);

        //Recupera indices
        int indiceData = cursor.getColumnIndex("data");

        if(cursor.moveToFirst()){
            dataSalva = cursor.getString(indiceData);
        }
        return dataSalva;
    }

    public void verificarUltimoAcesso()
    {
        String dataUlimoAcesso;
        try{
            dataUlimoAcesso = recuperaUltimoAcesso();
        }
        catch(Exception e)
        {
            return;
        }
        final SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy");
        Date currentTime = new Date();
        String dataAtual = formatoData.format(currentTime);

        Date dateAtual = null, dateUltAcesso = null;
        try {
            dateAtual = formatoData.parse(dataAtual);
            dateUltAcesso = formatoData.parse(dataUlimoAcesso);
        }catch(ParseException e){
            String novo = "erro";
        }

        if((dateAtual.after(dateUltAcesso)))
        {
            //ULTIMO ACESSO DO CLIENTE FOI ANTES DE HOJE, ENTÃO OS ITENS DE SEU PEDIDO DEVEM SER EXCLUÍDOS
            //PARA EVITAR INCONSISTÊNCIA DE VALORES E TAXAS
            bancoDados = this.openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
            if(verificaTabelaItemPedido())
                bancoDados.execSQL("DELETE FROM item_pedido");
            bancoDados.execSQL("UPDATE ultimoacesso SET data = '" + dataAtual + "'");
        }
    }
    public boolean verificaTabelaItemPedido() //verificamos se o cliente já acessou o app alguma vez
    {
        try {
            long numOfEntries = DatabaseUtils.queryNumEntries(bancoDados, "item_pedido");
            if(numOfEntries == 0) {
                // Tabela vazia
                return false;
            } else {
                // Tabela ja contem dados.
                return true;
            }
        }catch (Exception e)
        {
            return false;
        }

    }

    public void gerarFrase(String nome)
    {
        String[] frases = {
                "Olá " + nome + "! Deu aquela fome? Bora pedir algo incrível para comer!",
                "É muito bom ter você novamente aqui " + nome + "! Já estavamos com saudades!",
                "Quando a fome bate, não tem jeito " + nome + "... O melhor a ser feito é pedir uma delícia da Sandra!",
                "Não sabe se está com fome " + nome + "? Na dúvida, peça algo para comer, só por garantia.",
                "Tá sentindo um vazio hoje " + nome + "? Coma que é fome!",
                "Na vida, temos duas escolhas: estar no peso correto ou ser feliz. O que vai escolher hoje " + nome + "?",
                "Hummmm... Sentiu esse cheiro " + nome + "? Não? Então pede logo que eu te garanto: o sabor é fantástico!",
                "Olá novamente " + nome + "! Estávamos esperando você para saborear mais uma de nossas delícias!",
                "Batatinha quando nasce, se esparrama pelo chão... O Sandra Foods quando abre, faz tudo com amor e dedicação. Olá novamente " + nome + "!",
                "Batatinha quando nasce, se esparrama pelo chão... Se pedir um hambúrguer de costela, vai gostar de montão! Olá novamente " + nome + "!",
                "Batatinha quando nasce, se esparrama pelo chão... Lanche, crepes, tapioca, pastel... E ainda tem porção. Olá novamente " + nome + "!",
                "Batatinha quando nasce, se esparrama pelo chão... Coma um Sandra Foods hoje e encha de alegria o seu coração. Olá novamente " + nome + "!",
                "Batatinha quando nasce, se esparrama pelo chão... Só no Sandra Foods você pede e come rapidão. Olá novamente " + nome + "!",
                "Como que faz pra parar de sentir fome de meia em meia hora? Também não sabemos " + nome + ", mas pede algo aqui que com certeza já vai melhorar!",
                "Já dizia o poeta: \"Comer comer, comer comer, é o melhor para poder ser feliz\". O que vai querer hoje " + nome + "? "};

        int x = new Random().nextInt(10);
        tvFrase.setText(frases[x]);
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
                                    SuperActivityToast.create(TelaBemVindo.this, new Style(), Style.TYPE_STANDARD)
                                        .setText("Ocorreu um erro ao abrir o aplicativo: " + f.toString())
                                        .setDuration(Style.DURATION_SHORT)
                                        .setColor(getResources().getColor(R.color.marrom))
                                        .setAnimations(Style.ANIMATIONS_POP)
                                        .show();
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
    public void clickBotaoEntrar()
    {
        recuperarPedidosEmAndamento();
    }

    public void recuperarPedidosEmAndamento()
    {
        DatabaseReference andamentoDB = firebaseBanco.child("andamento");
        andamentoDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                listaAndamento.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    listaAndamento.add(ds.getValue(Andamento.class));
                }
                verificaDadosAndamento();
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public void verificaDadosAndamento()
    {
        try{
            String id = "Erro";
            String nome = "Erro";
            String celular = "Erro";

            bancoDados = TelaBemVindo.this.openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
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

            int parametroFor = 0;
            for(Andamento item: listaAndamento)
            {
                parametroFor = 1;
                if(item.getId().equals(chavePedido))
                {
                    loTelaBemVindo.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            Intent intent = new Intent(getApplicationContext(), TelaAcompanhamentoPedido.class);
                            intent.putExtra("dataPedido", recuperado.getData());
                            intent.putExtra("chavePedido", recuperado.getId());
                            startActivity(intent);
                        }
                    });
                    parametroFor = 1;
                    break;
                }
                parametroFor = 0;
            }
            if(parametroFor == 0)
            {
                loTelaBemVindo.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        Intent intent = new Intent(getApplicationContext(), TelaPrincipal.class);
                        startActivity(intent);
                    }
                });
            }
        }
        catch(Exception e){
            String erro = e.toString();
            SuperActivityToast.create(TelaBemVindo.this, new Style(), Style.TYPE_STANDARD)
                    .setText("Ocorreu um erro ao abrir o aplicativo. Por gentileza, no envie mensagem via Whats informando o problema.")
                    .setDuration(Style.DURATION_SHORT)
                    .setColor(getResources().getColor(R.color.marrom))
                    .setAnimations(Style.ANIMATIONS_POP)
                    .show();
        }
    }
}