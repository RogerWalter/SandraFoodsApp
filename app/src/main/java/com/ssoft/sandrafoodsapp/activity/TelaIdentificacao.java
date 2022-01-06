package com.ssoft.sandrafoodsapp.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.instacart.library.truetime.TrueTime;
import com.ssoft.sandrafoodsapp.R;
import com.ssoft.sandrafoodsapp.model.Andamento;
import com.ssoft.sandrafoodsapp.model.Parametros;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.thekhaeng.pushdownanim.PushDownAnim.MODE_STATIC_DP;

public class TelaIdentificacao extends AppCompatActivity {

    private ConstraintLayout loIdentificacao;
    private EditText etNome;
    private EditText etCelular;
    private Button btEntrar, btRecarregar;
    private ImageView btWhats, ivSemConexao, ivLogoSandraFoods;
    private TextView texto1, texto2, texto3;
    private SQLiteDatabase bancoDados;
    private ArrayList<Andamento> listaAndamento = new ArrayList<>();
    private String chavePedido;
    DatabaseReference firebaseBanco = FirebaseDatabase.getInstance().getReference();
    DatabaseReference andamentoDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_identificacao);
        getSupportActionBar().hide();
        //Inicializa os componentes do layout
        inicializaComponentes();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    TrueTime.build().initialize();
                    horaLib = TrueTime.now();
                }
                catch(ExceptionInInitializerError | IOException e)
                {
                    Date currentTime = Calendar.getInstance().getTime();
                    horaLib = currentTime;
                    String erro = e.toString();
                }
            }
        });
        //cadastraCupom();
        //popularFirebase();
        //popularFirebaseTaxa();
        try{
            //Instancia o banco de dados
            bancoDados = openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
            //Criar tabela
            bancoDados.execSQL("CREATE TABLE IF NOT EXISTS cliente (id INTEGER PRIMARY KEY AUTOINCREMENT, nome VARCHAR, celular VARCHAR, rua VARCHAR, numero VARCHAR, bairro VARCHAR, referencia VARCHAR)");
        }
        catch(Exception e){
            SuperActivityToast.create(TelaIdentificacao.this, new Style(), Style.TYPE_STANDARD)
                    .setText("Entre em contato pelo Whats. Ocorreu um erro ao abrir o aplicativo: " + e.toString())
                    .setDuration(Style.DURATION_SHORT)
                    .setColor(getResources().getColor(R.color.marrom))
                    .setAnimations(Style.ANIMATIONS_POP)
                    .show();
        }
        //buscaDados();
        //deletarDados();
        //Insere máscara no campo celular
        //etCelular.addTextChangedListener(Mask.insert("(##)#####-####", etCelular));
        //Instancia o listener do botão do WhatsApp
        PushDownAnim.setPushDownAnimTo(btWhats)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clickWhats();
                    }
                });
        PushDownAnim.setPushDownAnimTo(btEntrar)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        botaoEntrarClick();
                    }
                });
        PushDownAnim.setPushDownAnimTo(btRecarregar)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!possuiConexao())
                        {
                            alterarTelaSemConexao();
                        }
                        else
                        {
                            recuperaParametros();

                            if(verificaCadastro())
                            {
                                recuperarPedidosEmAndamento();
                            }
                            else
                            {
                                alterarTelaIdentificacao();
                            }
                        }
                    }
                });
        if(!possuiConexao())
        {
            loIdentificacao.setVisibility(View.GONE);
            alterarTelaSemConexao();
        }
        else
        {
            recuperaParametros();
            //Verifica se é o primeiro acesso do usuário no app
            if(verificaCadastro())
            {
                recuperarPedidosEmAndamento();
            }
            else
            {
                alterarTelaIdentificacao();
            }
        }
    }

    public boolean verificaCadastro() //verificamos se o cliente já acessou o app alguma vez
    {
        long numOfEntries = DatabaseUtils.queryNumEntries(bancoDados, "cliente");

        if(numOfEntries == 0) {
            // Tabela vazia
            return false;
        } else {
            // Tabela ja contem dados.
            return true;
        }
    }
    private void inicializaComponentes(){
        etNome = findViewById(R.id.etNomeClienteIdentificacao);
        etCelular   = findViewById(R.id.etCelularClienteIdentificacao);
        btEntrar   = findViewById(R.id.btEntrarIdentificacao);
        btWhats = findViewById(R.id.ivWhatsTelaIdentificacao);
        btRecarregar = findViewById(R.id.btTelaIdentVerificarConexao);
        ivSemConexao = findViewById(R.id.ivSemConexao);
        ivLogoSandraFoods = findViewById(R.id.ivBemVindoIdentificacao);
        texto1 = findViewById(R.id.tvTexto1Identificacao);
        texto2 = findViewById(R.id.tvTexto2Identificacao);
        texto3 = findViewById(R.id.tvTexto4Identificacao);
        loIdentificacao = findViewById(R.id.loTelaIdentificacao);
        loIdentificacao.setVisibility(View.INVISIBLE);
    }

    private void alterarTelaSemConexao(){

        etNome.setVisibility(View.INVISIBLE);
        etCelular.setVisibility(View.INVISIBLE);
        btEntrar.setVisibility(View.INVISIBLE);
        btWhats.setVisibility(View.INVISIBLE);
        ivLogoSandraFoods.setVisibility(View.INVISIBLE);
        texto1.setVisibility(View.INVISIBLE);
        texto2.setVisibility(View.INVISIBLE);
        texto3.setVisibility(View.INVISIBLE);
        btRecarregar.setVisibility(View.INVISIBLE);
        ivSemConexao.setVisibility(View.INVISIBLE);

        loIdentificacao.setVisibility(View.VISIBLE);

        ivSemConexao.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ivSemConexao.animate().translationY(0).alpha(1.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        ivSemConexao.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                btRecarregar.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        btRecarregar.animate().translationY(0).alpha(1.0f).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                btRecarregar.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
            }
        }, 250);
    }
    private void alterarTelaIdentificacao(){

        btRecarregar.setVisibility(View.INVISIBLE);
        ivSemConexao.setVisibility(View.INVISIBLE);

        etNome.setVisibility(View.INVISIBLE);
        etCelular.setVisibility(View.INVISIBLE);
        btEntrar.setVisibility(View.INVISIBLE);
        btWhats.setVisibility(View.INVISIBLE);
        ivLogoSandraFoods.setVisibility(View.INVISIBLE);
        texto1.setVisibility(View.INVISIBLE);
        texto2.setVisibility(View.INVISIBLE);
        texto3.setVisibility(View.INVISIBLE);

        loIdentificacao.setVisibility(View.VISIBLE);

        ivLogoSandraFoods.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ivLogoSandraFoods.animate().translationY(0).alpha(1.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        ivLogoSandraFoods.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
        texto1.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                texto1.animate().translationY(0).alpha(1.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        texto1.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                etNome.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        etNome.animate().translationY(0).alpha(1.0f).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                etNome.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
                texto2.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        texto2.animate().translationY(0).alpha(1.0f).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                texto2.setVisibility(View.VISIBLE);
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
                etCelular.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        etCelular.animate().translationY(0).alpha(1.0f).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                etCelular.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
                texto3.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        texto3.animate().translationY(0).alpha(1.0f).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                texto3.setVisibility(View.VISIBLE);
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
                btWhats.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        btWhats.animate().translationY(0).alpha(1.0f).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                btWhats.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
            }
        }, 750);

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
                                SuperActivityToast.create(TelaIdentificacao.this, new Style(), Style.TYPE_STANDARD)
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
    public void botaoEntrarClick()
    {
        SimpleDateFormat dataFormatada = new SimpleDateFormat("dd/MM/yyyy");
        Date currentTime = new Date();
        String dataAtual = dataFormatada.format(currentTime);

        String textoNome = etNome.getText().toString();
        String textoCelular = etCelular.getText().toString();

        if(!textoNome.isEmpty()) {
            if(!textoCelular.isEmpty()) {
                if(textoCelular.length() == 14) {

                    try{
                        //SQLiteDatabase bancoDados = openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
                        bancoDados = openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);

                        //Criar tabela
                        bancoDados.execSQL("CREATE TABLE IF NOT EXISTS cliente (id INTEGER PRIMARY KEY AUTOINCREMENT, nome VARCHAR, celular VARCHAR, rua VARCHAR, numero VARCHAR, bairro VARCHAR, referencia VARCHAR)");
                        bancoDados.execSQL("CREATE TABLE IF NOT EXISTS primeiroacesso (parametro INTEGER)");
                        bancoDados.execSQL("CREATE TABLE IF NOT EXISTS ultimoacesso (data VARCHAR)");

                        //Insere dados pessoa
                        bancoDados.execSQL("INSERT INTO cliente (nome, celular, rua, numero, bairro, referencia) VALUES ('" + textoNome + "', '" + textoCelular + "', '', '', '', '')");
                        bancoDados.execSQL("INSERT INTO primeiroacesso (parametro) VALUES (0)");
                        bancoDados.execSQL("INSERT INTO ultimoacesso (data) VALUES ('" + dataAtual + "')");


                        String celularSoNumeros = textoCelular.replace("(", "").replace(")","").replace(" ", "").replace("-", "");
                        String nomeSoLetras = textoNome.replace("/", "-").replace("\\", "-").replace(" ", "");
                        chavePedido = celularSoNumeros + "-" + nomeSoLetras;

                        int parametroFor = 0;
                        for(Andamento item: listaAndamento)
                        {
                            parametroFor = 1;
                            if(item.getId().equals(chavePedido))
                            {
                                //ESTE AMIGO, COM ESTE NOME E ESTE TELEFONE POSSUI UM PEDIDO EM ANDAMENTO


                                //VAMOS SALVAR NO BANCO DE DADOS DO CELULAR O PEDIDO QUE RECUPERAMOS
                                //bancoDados = TelaIdentificacao.this.openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
                                //bancoDados.execSQL("CREATE TABLE IF NOT EXISTS andamento (id VARCHAR, data VARCHAR, status INTEGER)");
                                //bancoDados.execSQL("INSERT INTO andamento (id, data, status) VALUES ('" + item.getId() + "', '" + item.getData() + "', '" + item.getStatus() + "')");
                                //animação p sair dessa tela...
                                loIdentificacao.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        //Então, abriremos a tela de pedido em andamento...
                                        Intent intent = new Intent(getApplicationContext(), TelaAcompanhamentoPedido.class);
                                        intent.putExtra("dataPedido", item.getData());
                                        intent.putExtra("chavePedido", item.getId());
                                        startActivity(intent);
                                    }
                                });
                            }
                            parametroFor = 0;
                        }
                        if(parametroFor == 0)
                        {
                            //animação p sair dessa tela...
                            loIdentificacao.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    startActivity(new Intent(getApplicationContext(), TelaPrincipal.class));
                                    //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                    //startActivity(new Intent(getApplicationContext(), TelaSlider.class));
                                    //finish();
                                }
                            });
                        }
                    }
                    catch(Exception e){
                        String erro = e.toString();
                        SuperActivityToast.create(TelaIdentificacao.this, new Style(), Style.TYPE_STANDARD)
                                .setText("Ocorreu um erro ao salvar os dados: " + erro)
                                .setDuration(Style.DURATION_SHORT)
                                .setColor(getResources().getColor(R.color.marrom))
                                .setAnimations(Style.ANIMATIONS_POP)
                                .show();
                    }

                }
                else{
                    SuperActivityToast.create(TelaIdentificacao.this, new Style(), Style.TYPE_STANDARD)
                            .setText("O numero de celular não está completo. Verifique e tente novamente.")
                            .setDuration(Style.DURATION_SHORT)
                            .setColor(getResources().getColor(R.color.marrom))
                            .setAnimations(Style.ANIMATIONS_POP)
                            .show();
                    etCelular.requestFocus();
                }
            }
            else{
                SuperActivityToast.create(TelaIdentificacao.this, new Style(), Style.TYPE_STANDARD)
                        .setText("Por gentileza, informe seu celular.")
                        .setDuration(Style.DURATION_SHORT)
                        .setColor(getResources().getColor(R.color.marrom))
                        .setAnimations(Style.ANIMATIONS_POP)
                        .show();
                etCelular.requestFocus();
            }
        }
        else{
            SuperActivityToast.create(TelaIdentificacao.this, new Style(), Style.TYPE_STANDARD)
                    .setText("Por gentileza, informe seu nome.")
                    .setDuration(Style.DURATION_SHORT)
                    .setColor(getResources().getColor(R.color.marrom))
                    .setAnimations(Style.ANIMATIONS_POP)
                    .show();
            etNome.requestFocus();
        }
    }

    private boolean possuiConexao()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if((wifiConn != null && wifiConn.isConnected()) || (mobileConn != null && mobileConn.isConnected()))
            return true;
        else
            return false;
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

            bancoDados = TelaIdentificacao.this.openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
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

            int parametroFor = 0;
            for(Andamento item: listaAndamento)
            {
                parametroFor = 1;
                if(item.getId().equals(chavePedido))
                {
                    /*//ESTE AMIGO, COM ESTE NOME E ESTE TELEFONE POSSUI UM PEDIDO EM ANDAMENTO
                    //VAMOS SALVAR NO BANCO DE DADOS DO CELULAR O PEDIDO QUE RECUPERAMOS
                    bancoDados = TelaIdentificacao.this.openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
                    bancoDados.execSQL("CREATE TABLE IF NOT EXISTS andamento (id VARCHAR, data VARCHAR, status INTEGER)");
                    bancoDados.execSQL("INSERT INTO andamento (id, data, status) VALUES ('" + item.getId() + "', '" + item.getData() + "', '" + item.getStatus() + "')");
                    //Então, abriremos a tela de pedido em andamento...*/
                    Intent intent = new Intent(getApplicationContext(), TelaAcompanhamentoPedido.class);
                    intent.putExtra("dataPedido", item.getData());
                    intent.putExtra("chavePedido", item.getId());
                    intent.putExtra("parametroAbertoOuFechado", parametroAbertoOuFechado);
                    startActivity(intent);
                    parametroFor = 1;
                    break;
                }
                parametroFor = 0;
            }
            if(parametroFor == 0)
            {
                if(parametroAbertoOuFechado == 1)
                {
                    loIdentificacao.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        startActivity(new Intent(getApplicationContext(), TelaBemVindo.class));
                    }
                });
                }
                else {
                    loIdentificacao.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        startActivity(new Intent(getApplicationContext(), TelaEstabelecimentoFechado.class));
                    }
                });
                }
            }
        }
        catch(Exception e){
            String erro = e.toString();
            SuperActivityToast.create(TelaIdentificacao.this, new Style(), Style.TYPE_STANDARD)
                    .setText("Ocorreu um erro ao verificar seus dados: " + erro)
                    .setDuration(Style.DURATION_SHORT)
                    .setColor(getResources().getColor(R.color.marrom))
                    .setAnimations(Style.ANIMATIONS_POP)
                    .show();
        }
    }

    private Parametros parametros = new Parametros();

    public void recuperaParametros()
    {
        DatabaseReference andamentoDB = firebaseBanco.child("parametro");
        andamentoDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                parametros = snapshot.getValue(Parametros.class);
                if(parametros.getManutencao() == 1)
                {
                    startActivity(new Intent(getApplicationContext(), TelaAplicativoOffline.class));
                    return;
                }
                verificaFuncionamentoRestaurante();
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private int parametroAbertoOuFechado = 0;
    Date horaLib = null;

    public void verificaFuncionamentoRestaurante()
    {
        //0 - FECHADO | 1 - ABERTO
        //FUNÇÃO USADA PARA VERIFICAR SE O RESTAURANTE ESTÁ ABERTO OU NÃO
        String horaAbreString = parametros.getInicio() + ":00";
        String horaFechaString = parametros.getFim() + ":00";

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        final SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
        //String hora = formatoHora.format(new Date());
        String hora = formatoHora.format(horaLib);

        Date horaAbre = null, horaFecha = null, horaAtual = null;
        try {
            horaAtual = formatoHora.parse(hora);
            horaAbre = formatoHora.parse(horaAbreString);
            horaFecha = formatoHora.parse(horaFechaString);
        }catch(ParseException e){
            String novo = "erro";
        }

        switch (day) {
            case Calendar.SUNDAY:
                // DOMINGO
                if(parametros.getDom() == 0)
                {
                    //FECHADO
                    parametroAbertoOuFechado = 0;
                }
                else
                {
                    //DIA OK. VERIFICAMOS O HORÁRIO
                    if((horaAtual.equals(horaAbre) || horaAtual.after(horaAbre)) && horaAtual.before(horaFecha))
                    {
                        //ESTABELECIMENTO ABERTO
                        parametroAbertoOuFechado = 1;
                    }
                    else
                    {
                        //ESTABELECIMENTO FECHADO
                        parametroAbertoOuFechado = 0;
                    }
                }
                break;
            case Calendar.MONDAY:
                // SEGUNDA
                if(parametros.getSeg() == 0)
                {
                    //FECHADO
                    parametroAbertoOuFechado = 0;
                }
                else
                {
                    //DIA OK. VERIFICAMOS O HORÁRIO
                    if((horaAtual.equals(horaAbre) || horaAtual.after(horaAbre)) && horaAtual.before(horaFecha))
                    {
                        //ESTABELECIMENTO ABERTO
                        parametroAbertoOuFechado = 1;
                    }
                    else
                    {
                        //ESTABELECIMENTO FECHADO
                        parametroAbertoOuFechado = 0;
                    }
                }
                break;
            case Calendar.TUESDAY:
                // TERÇA
                if(parametros.getTer() == 0)
                {
                    //FECHADO
                    parametroAbertoOuFechado = 0;
                }
                else
                {
                    //DIA OK. VERIFICAMOS O HORÁRIO
                    if((horaAtual.equals(horaAbre) || horaAtual.after(horaAbre)) && horaAtual.before(horaFecha))
                    {
                        //ESTABELECIMENTO ABERTO
                        parametroAbertoOuFechado = 1;
                    }
                    else
                    {
                        //ESTABELECIMENTO FECHADO
                        parametroAbertoOuFechado = 0;
                    }
                }
                break;
            case Calendar.WEDNESDAY:
                // QUARTA
                if(parametros.getQua() == 0)
                {
                    //FECHADO
                    parametroAbertoOuFechado = 0;
                }
                else
                {
                    //DIA OK. VERIFICAMOS O HORÁRIO
                    if((horaAtual.equals(horaAbre) || horaAtual.after(horaAbre)) && horaAtual.before(horaFecha))
                    {
                        //ESTABELECIMENTO ABERTO
                        parametroAbertoOuFechado = 1;
                    }
                    else
                    {
                        //ESTABELECIMENTO FECHADO
                        parametroAbertoOuFechado = 0;
                    }
                }
                break;
            case Calendar.THURSDAY:
                // QUINTA
                if(parametros.getQui() == 0)
                {
                    //FECHADO
                    parametroAbertoOuFechado = 0;
                }
                else
                {
                    //DIA OK. VERIFICAMOS O HORÁRIO
                    if((horaAtual.equals(horaAbre) || horaAtual.after(horaAbre)) && horaAtual.before(horaFecha))
                    {
                        //ESTABELECIMENTO ABERTO
                        parametroAbertoOuFechado = 1;
                    }
                    else
                    {
                        //ESTABELECIMENTO FECHADO
                        parametroAbertoOuFechado = 0;
                    }
                }
                break;
            case Calendar.FRIDAY:
                // SEXTA
                if(parametros.getSex() == 0)
                {
                    //FECHADO
                    parametroAbertoOuFechado = 0;
                }
                else
                {
                    //DIA OK. VERIFICAMOS O HORÁRIO
                    if((horaAtual.equals(horaAbre) || horaAtual.after(horaAbre)) && horaAtual.before(horaFecha))
                    {
                        //ESTABELECIMENTO ABERTO
                        parametroAbertoOuFechado = 1;
                    }
                    else
                    {
                        //ESTABELECIMENTO FECHADO
                        parametroAbertoOuFechado = 0;
                    }
                }
                break;
            case Calendar.SATURDAY:
                // SABADO
                if(parametros.getSab() == 0)
                {
                    //FECHADO
                    parametroAbertoOuFechado = 0;
                }
                else
                {
                    //DIA OK. VERIFICAMOS O HORÁRIO
                    if((horaAtual.equals(horaAbre) || horaAtual.after(horaAbre)) && horaAtual.before(horaFecha))
                    {
                        //ESTABELECIMENTO ABERTO
                        parametroAbertoOuFechado = 1;
                    }
                    else
                    {
                        //ESTABELECIMENTO FECHADO
                        parametroAbertoOuFechado = 0;
                    }
                }
                break;
        }

        if(parametroAbertoOuFechado == 0)
        {
            //FECHADO - VERIFICAMOS SE CLIENTE POSSUI PEDIDO EM ANDAMENTO
            if(verificaCadastro())
            {
                recuperarPedidosEmAndamento();
            }
            else
            {
                //CLIENTE SEM PEDIDO EM ANDAMENTO E ESTABELECIMENTO FECHADO. ABRIMOS A TELA DE ESTABELECIMENTO FECHADO.
                loIdentificacao.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        startActivity(new Intent(getApplicationContext(), TelaEstabelecimentoFechado.class));
                    }
                });
            }


        }
        else
        {
            //ABERTO - SEGUIMOS A VIDA NORMALMENTE
        }

    }

}