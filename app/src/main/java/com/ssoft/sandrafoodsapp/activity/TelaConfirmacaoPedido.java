package com.ssoft.sandrafoodsapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

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
import com.instacart.library.truetime.TrueTime;
import com.ssoft.sandrafoodsapp.R;
import com.ssoft.sandrafoodsapp.model.Andamento;
import com.ssoft.sandrafoodsapp.model.Cliente;
import com.ssoft.sandrafoodsapp.model.CupomDesconto;
import com.ssoft.sandrafoodsapp.model.ItemPedido;
import com.ssoft.sandrafoodsapp.model.Parametros;
import com.ssoft.sandrafoodsapp.model.Pedido;
import com.ssoft.sandrafoodsapp.model.TaxaEntrega;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Retrofit;

import static com.thekhaeng.pushdownanim.PushDownAnim.MODE_STATIC_DP;

public class TelaConfirmacaoPedido extends AppCompatActivity {

    DatabaseReference firebaseBanco = FirebaseDatabase.getInstance().getReference();
    DatabaseReference taxasDB;

    private Retrofit retrofit;
    private SQLiteDatabase bancoDados;
    private Cliente cliente = new Cliente();
    private int idUsuario = 0;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng meuLocal;

    private TextView taxaEntregaDesc, taxaEntregaValor, tvTotalItens, tvTotalPedido;
    private TextView tvDescontoDesc, tvDescontoValor, tvInformarDescontoDesc;
    private EditText etDesconto;
    private ImageButton ibVerificarDesconto;
    private ProgressBar pbBuscaCupom;

    private ImageButton ibVoltar;

    private String nomeRetiraOuConsumo;

    private Button btTipoEntrega, btTipoRetirada, btTipoConsNoLocal;
    private int parametroTipoPedido = -1; // 0 - Entrega | 1 - Retirada | 2 - Consumo no Local

    private ConstraintLayout loTipoPedidoEnt, loTipoPedidoRet, loTipoPedidoLoc, loTelaConfirmacaoPedido;
    private ConstraintLayout loTipoPagamentoDin, loTipoPagamentoCar, loTipoPagamentoPix;
    private ConstraintLayout loTipoPedido, loPagamento, loObservacao, loCupom, loTotalizador, loMostraTipoPedido, loTipoPagamento;
    private ScrollView mScrollView;

    private List<ItemPedido> listaPedido = new ArrayList<>();
    private Double totalItensPedido, valorTaxa, valorTotalPedido;

    private List<TaxaEntrega> listaTaxasEntrega = new ArrayList<>();
    private List<CupomDesconto> listaCuponsDesconto = new ArrayList<>();
    private Double valorDoPedidoComDesconto;
    private Double valorDoDescontoNoPedido;
    private int parametroCupomAplicado = 0; //serve para informar se existe um cupom aplicado ao trocar o tipo do pedido;
    private String cupomAplicado;

    private TextView tvFormaPagamento, tvObservacao;
    private Button btTipoPagDinheiro, btTipoPagCartao, btTipoPagPix;
    private ImageButton btAjudaPagamento;
    private int parametroTipoPagamento = -1; // 0 - DINHEIRO | 1 - CARTAO | 2 - PIX
    private int parametroPrecisaDeTroco = -1; // 0 - NÃO PRECISA | 1 - PRECISA
    private EditText etObservacao;

    private Button btConfirmarPedido;
    private String trocoFinal;
    private String enderecoEntregaFinal;

    private String identificadorUnicoParaCupom;

    Date horaLib = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_confirmacao_pedido);
        getSupportActionBar().hide();

        identificadorUnicoParaCupom = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);

        recuperaTaxas();
        recuperaCupons();
        recuperaParametros();

        btConfirmarPedido = findViewById(R.id.btTelaConfPedidoConfirmarPedido);

        tvFormaPagamento = findViewById(R.id.textView71);
        tvObservacao = findViewById(R.id.textView72);

        etObservacao = findViewById(R.id.etConfirmacaoPedidoObservacoes);

        btAjudaPagamento = findViewById(R.id.ibConfPedAjudaPagamento);
        btTipoPagDinheiro = findViewById(R.id.btTelaConfPedidoOpcDinheiro);
        btTipoPagCartao = findViewById(R.id.btTelaConfPedidoOpcCartao);
        btTipoPagPix = findViewById(R.id.btTelaConfPedidoOpcPix);

        ibVoltar = findViewById(R.id.ibVoltarTelaConfPedido);
        tvTotalItens = findViewById(R.id.tvConfPedidoTotalItens);
        tvTotalPedido = findViewById(R.id.tvConfPedidoTotalPedido);

        btTipoEntrega = findViewById(R.id.btTelaConfPedidoOpcEntrega);
        btTipoRetirada = findViewById(R.id.btTelaConfPedidoOpcRetirada);
        btTipoConsNoLocal = findViewById(R.id.btTelaConfPedidoOpcConsNoLocal);

        taxaEntregaDesc = findViewById(R.id.tvDescTaxaEntrega);
        taxaEntregaValor = findViewById(R.id.tvTaxaDeEntregaValor);

        loTipoPedidoEnt = findViewById(R.id.layoutTipoDePedidoEntrega);
        loTipoPedidoRet = findViewById(R.id.layoutTipoDePedidoRetirada);
        loTipoPedidoLoc = findViewById(R.id.layoutTipoDePedidoConsNoLocal);
        loTelaConfirmacaoPedido = findViewById(R.id.layout_confirmacao_pedido);


        loTipoPagamentoDin = findViewById(R.id.layoutTipoDePagamentoDinheiro);
        loTipoPagamentoCar = findViewById(R.id.layoutTipoDePagamentoCartao);
        loTipoPagamentoPix = findViewById(R.id.layoutTipoDePagamentoPix);

        tvDescontoDesc = findViewById(R.id.tvConfPedidoDescontoDesc);
        tvDescontoValor = findViewById(R.id.tvConfPedidoTotalDesconto);
        tvInformarDescontoDesc = findViewById(R.id.tvConfirmaoPedidoCupomDesconto);
        etDesconto = findViewById(R.id.etConfirmaoPedidoCupomDesconto);
        ibVerificarDesconto = findViewById(R.id.ibConfirmacaoPedidoConfirmarCupom);
        pbBuscaCupom = findViewById(R.id.progressBarBuscaCupom);

        loTipoPedido = findViewById(R.id.loTelaConfPedidoTipoPedido);
        loPagamento = findViewById(R.id.loTelaConfPedidoPagamento);
        loTipoPagamento = findViewById(R.id.layoutPrincipalTipoPagamento);
        loObservacao = findViewById(R.id.loTelaConfPedidoObservacao);
        loCupom = findViewById(R.id.loConfirmacaoPedidoCupomDesc);
        loTotalizador = findViewById(R.id.loTelaConfPedidoTotalizador);
        loMostraTipoPedido = findViewById(R.id.layoutPrincipalTipoPedido);

        mScrollView = findViewById(R.id.scrollViewTelaConfPedido);

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

        etDesconto.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

        esconderPagamento();

        etDesconto.setText("");

        tvDescontoDesc.setVisibility(View.GONE);
        tvDescontoValor.setVisibility(View.GONE);

        getLayoutInflater().inflate(R.layout.layout_tipo_pedido_entrega, loTipoPedidoEnt);
        getLayoutInflater().inflate(R.layout.layout_tipo_pedido_retirada, loTipoPedidoRet);
        getLayoutInflater().inflate(R.layout.layout_tipo_pedido_consumo_no_local, loTipoPedidoLoc);

        getLayoutInflater().inflate(R.layout.layout_tipo_pagamento_dinheiro, loTipoPagamentoDin);
        getLayoutInflater().inflate(R.layout.layout_tipo_pagamento_cartao, loTipoPagamentoCar);
        getLayoutInflater().inflate(R.layout.layout_tipo_pagamento_pix, loTipoPagamentoPix);

        taxaEntregaDesc.setVisibility(View.GONE);
        taxaEntregaValor.setVisibility(View.GONE);

        loTipoPedidoEnt.setVisibility(View.GONE);
        loTipoPedidoRet.setVisibility(View.GONE);
        loTipoPedidoLoc.setVisibility(View.GONE);

        loTipoPagamentoCar.setVisibility(View.GONE);
        loTipoPagamentoDin.setVisibility(View.GONE);
        loTipoPagamentoPix.setVisibility(View.GONE);

        pbBuscaCupom.setVisibility(View.GONE);

        loTipoPedido.setVisibility(View.INVISIBLE);
        loPagamento.setVisibility(View.INVISIBLE);
        loTipoPagamento.setVisibility(View.INVISIBLE);
        loObservacao.setVisibility(View.INVISIBLE);
        loCupom.setVisibility(View.INVISIBLE);
        loTotalizador.setVisibility(View.INVISIBLE);
        loMostraTipoPedido.setVisibility(View.INVISIBLE);
        btConfirmarPedido.setVisibility(View.INVISIBLE);

        PushDownAnim.setPushDownAnimTo(ibVoltar)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loTelaConfirmacaoPedido.animate().translationY(-400).alpha(0.0f).setDuration(250);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                onBackPressed();
                            }
                        }, 250);
                    }
                });
        PushDownAnim.setPushDownAnimTo(btTipoEntrega)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        parametroTipoPedido = 0;
                        mostrarPagamento();
                        limparBotoesPagamento();
                        recuperaDados();
                    }
                });
        PushDownAnim.setPushDownAnimTo(btTipoRetirada)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        parametroTipoPedido = 1;
                        esconderPagamento();
                        limparBotoesPagamento();
                        recuperaDados();
                    }
                });
        PushDownAnim.setPushDownAnimTo(btTipoConsNoLocal)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        parametroTipoPedido = 2;
                        esconderPagamento();
                        limparBotoesPagamento();
                        recuperaDados();
                    }
                });
        PushDownAnim.setPushDownAnimTo(ibVerificarDesconto)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        verificaCupomDesconto();
                    }
                });
        PushDownAnim.setPushDownAnimTo(btAjudaPagamento)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        gerarDialogoIntrucoesPagamento();
                    }
                });
        PushDownAnim.setPushDownAnimTo(btTipoPagDinheiro)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        parametroTipoPagamento = 0;
                        preencherLayoutPagamentoDin();
                    }
                });
        PushDownAnim.setPushDownAnimTo(btTipoPagCartao)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        parametroTipoPagamento = 1;
                        preencherLayoutPagamentoCar();
                    }
                });
        PushDownAnim.setPushDownAnimTo(btTipoPagPix)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        parametroTipoPagamento = 2;
                        preencherLayoutPagamentoPix();
                    }
                });
        PushDownAnim.setPushDownAnimTo(btConfirmarPedido)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        validarInformacoes();
                    }
                });
        etDesconto.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                verificaCupomDesconto();
                return false;
            }
        });
        try{
            recuperarPedido();
        }catch(Exception e)
        {
            SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                    .setText("Ocorreu um erro ao recuperar seu pedido. Tente finalizá-lo novamente. Se o problema persistir, entre em contato pelo nosso Whats")
                    .setDuration(Style.DURATION_MEDIUM)
                    .setColor(getResources().getColor(R.color.marrom))
                    .setAnimations(Style.ANIMATIONS_POP)
                    .show();
            return;
        }

        loTipoPedido.setVisibility(View.INVISIBLE);
        loTotalizador.setVisibility(View.INVISIBLE);
        loObservacao.setVisibility(View.INVISIBLE);
        loCupom.setVisibility(View.INVISIBLE);
        loTipoPedido.animate().translationY(-250).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                loTipoPedido.setVisibility(View.VISIBLE);
                loTipoPedido.animate().translationY(0).alpha(1.0f).setDuration(250);
            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loObservacao.animate().translationY(-250).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        loObservacao.setVisibility(View.VISIBLE);
                        loObservacao.animate().translationY(0).alpha(1.0f).setDuration(250);
                    }
                });
            }
        }, 250);

        final Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                loCupom.animate().translationY(-250).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        loCupom.setVisibility(View.VISIBLE);
                        loCupom.animate().translationY(0).alpha(1.0f).setDuration(250);
                    }
                });
            }
        }, 500);

        final Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                loTotalizador.animate().translationY(-250).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        loTotalizador.setVisibility(View.VISIBLE);
                        loTotalizador.animate().translationY(0).alpha(1.0f).setDuration(250);
                    }
                });
            }
        }, 750);

        final Handler handler3 = new Handler();
        handler3.postDelayed(new Runnable() {
            @Override
            public void run() {
                btConfirmarPedido.animate().translationY(-250).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        btConfirmarPedido.setVisibility(View.VISIBLE);
                        btConfirmarPedido.animate().translationY(0).alpha(1.0f).setDuration(250);
                    }
                });
            }
        }, 1000);
    }

    public void animFadeoutTipoPedido()
    {
        loMostraTipoPedido.animate().translationY(-250).alpha(0.0f).setDuration(100).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                loMostraTipoPedido.setVisibility(View.INVISIBLE);
            }
        });
    }
    public void animFadeinTipoPedido()
    {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loMostraTipoPedido.animate().translationY(0).alpha(1.0f).setDuration(100).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    loMostraTipoPedido.setVisibility(View.VISIBLE);
                    }
                });
            }
        }, 250);
    }

    public void animFadeoutTipoPagamento()
    {
        loTipoPagamento.animate().translationY(-250).alpha(0.0f).setDuration(100).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                loTipoPagamento.setVisibility(View.INVISIBLE);
            }
        });
    }
    public void animFadeinTipoPagamento()
    {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loTipoPagamento.animate().translationY(0).alpha(1.0f).setDuration(100).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        loTipoPagamento.setVisibility(View.VISIBLE);
                    }
                });
            }
        }, 250);
    }

    public void configuraBotoesTipoDePedido() //usado para alterar a cor dos botões ao selecionar diferentes valores
    {
        if(parametroTipoPedido == 0)
        {
            //ALTERA COR DOS BOTÕES
            btTipoEntrega.setBackgroundResource(R.drawable.background_botao_arredondado);
            btTipoEntrega.setTextColor(getResources().getColor(R.color.branco));
            btTipoRetirada.setBackgroundResource(R.drawable.background_botao_arredondado_branco);
            btTipoRetirada.setTextColor(getResources().getColor(R.color.marrom));
            btTipoConsNoLocal.setBackgroundResource(R.drawable.background_botao_arredondado_branco);
            btTipoConsNoLocal.setTextColor(getResources().getColor(R.color.marrom));
            //ALTERA OS CAMPOS DE TAXA DE ENTREGA
            taxaEntregaDesc.setVisibility(View.VISIBLE);
            taxaEntregaValor.setVisibility(View.VISIBLE);
            //ALTERA VISIBILIDADE DO LAYOUT
            loTipoPedidoEnt.setVisibility(View.VISIBLE);
            loTipoPedidoRet.setVisibility(View.GONE);
            loTipoPedidoLoc.setVisibility(View.GONE);
        }
        if(parametroTipoPedido == 1)
        {
            //ALTERA COR DOS BOTÕES
            btTipoEntrega.setBackgroundResource(R.drawable.background_botao_arredondado_branco);
            btTipoEntrega.setTextColor(getResources().getColor(R.color.marrom));
            btTipoRetirada.setBackgroundResource(R.drawable.background_botao_arredondado);
            btTipoRetirada.setTextColor(getResources().getColor(R.color.branco));
            btTipoConsNoLocal.setBackgroundResource(R.drawable.background_botao_arredondado_branco);
            btTipoConsNoLocal.setTextColor(getResources().getColor(R.color.marrom));
            //ALTERA OS CAMPOS DE TAXA DE ENTREGA
            taxaEntregaDesc.setVisibility(View.GONE);
            taxaEntregaValor.setVisibility(View.GONE);
            //ALTERA VISIBILIDADE DO LAYOUT
            loTipoPedidoEnt.setVisibility(View.GONE);
            loTipoPedidoRet.setVisibility(View.VISIBLE);
            loTipoPedidoLoc.setVisibility(View.GONE);
        }
        if(parametroTipoPedido == 2)
        {
            //ALTERA COR DOS BOTÕES
            btTipoEntrega.setBackgroundResource(R.drawable.background_botao_arredondado_branco);
            btTipoEntrega.setTextColor(getResources().getColor(R.color.marrom));
            btTipoRetirada.setBackgroundResource(R.drawable.background_botao_arredondado_branco);
            btTipoRetirada.setTextColor(getResources().getColor(R.color.marrom));
            btTipoConsNoLocal.setBackgroundResource(R.drawable.background_botao_arredondado);
            btTipoConsNoLocal.setTextColor(getResources().getColor(R.color.branco));
            //ALTERA OS CAMPOS DE TAXA DE ENTREGA
            taxaEntregaDesc.setVisibility(View.GONE);
            taxaEntregaValor.setVisibility(View.GONE);
            //ALTERA VISIBILIDADE DO LAYOUT
            loTipoPedidoEnt.setVisibility(View.GONE);
            loTipoPedidoRet.setVisibility(View.GONE);
            loTipoPedidoLoc.setVisibility(View.VISIBLE);
        }
    }

    public void recuperaDados()
    {
        String id = "Erro";
        String nome = "Erro";
        String celular = "Erro";
        String rua = "Erro";
        String numero = "Erro";
        String bairro = "Erro";
        String referencia = "Erro";

        bancoDados = TelaConfirmacaoPedido.this.openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
        //Recuperar pessoas
        Cursor cursor = bancoDados.rawQuery("SELECT id, nome, celular, rua, numero, bairro, referencia FROM cliente LIMIT 1", null);

        //Recupera indices
        int indiceId = cursor.getColumnIndex("id");
        int indiceNome = cursor.getColumnIndex("nome");
        int indiceCelular = cursor.getColumnIndex("celular");
        int indiceRua = cursor.getColumnIndex("rua");
        int indiceNumero = cursor.getColumnIndex("numero");
        int indiceBairro = cursor.getColumnIndex("bairro");
        int indiceReferencia = cursor.getColumnIndex("referencia");

        if(cursor.moveToFirst()){
            id = cursor.getString(indiceId);
            nome = cursor.getString(indiceNome);
            celular = cursor.getString(indiceCelular);
            rua = cursor.getString(indiceRua);
            numero = cursor.getString(indiceNumero);
            bairro = cursor.getString(indiceBairro);
            referencia = cursor.getString(indiceReferencia);

            cliente.setId(Integer.parseInt(id));
            cliente.setNome(nome);
            cliente.setCelular(celular);
            cliente.setRua(rua);
            cliente.setNumero(numero);
            cliente.setBairro(bairro);
            cliente.setReferencia(referencia);

            idUsuario = Integer.parseInt(id.trim());

            nomeRetiraOuConsumo = cliente.getNome();
        }
        animFadeoutTipoPedido();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(parametroTipoPedido == 0)
                    testaDados();
                if(parametroTipoPedido == 1)
                    preencherLayoutRetirada();
                if(parametroTipoPedido == 2)
                    preencherLayoutConsumoNoLocal();
            }
        }, 150);
        animFadeinTipoPedido();
    }
    public void testaDados()
    {
        if(!TextUtils.isEmpty(cliente.getRua()))
        {
            if(!TextUtils.isEmpty(cliente.getBairro()))
            {
                if(!TextUtils.isEmpty(cliente.getNumero()))
                {
                    try {
                        int testaNumero = Integer.parseInt(cliente.getNumero());
                        if(testaNumero > 0)
                        {
                            preencherLayoutEntrega();
                        }
                        else
                        {
                            if(!TextUtils.isEmpty(cliente.getReferencia()))
                            {
                                preencherLayoutEntrega();
                            }
                            else
                            {
                                gerarAlertaDadosIncompletosEspecifico();
                            }
                        }
                    }catch (Exception e)
                    {
                        gerarAlertaDadosIncompletos();
                    }
                }
                else
                {
                    gerarAlertaDadosIncompletos();
                }
            }
            else
            {
                gerarAlertaDadosIncompletos();
            }
        }
        else
        {
            gerarAlertaDadosIncompletos();
        }
    }

    public void gerarAlertaDadosIncompletos()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(TelaConfirmacaoPedido.this);
        builder.setCancelable(true);
        builder.setTitle("Dados Incompletos");
        builder.setMessage("Parece que existem dados incompletos do seu endereço. Deseja preenchê-los agora?");
        builder.setPositiveButton("Sim",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gerarDialogoAlterarDadosEntrega();
                    }
                });
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                parametroTipoPedido = 1;
                esconderPagamento();
                configuraBotoesTipoDePedido();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void gerarAlertaDadosIncompletosEspecifico()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(TelaConfirmacaoPedido.this);
        builder.setCancelable(true);
        builder.setTitle("Dados Incompletos");
        builder.setMessage("Parece que sua casa não possui número. Nesse caso, precisamos que explique no campo COMPLEMENTO como encontramos sua casa. Deseja fazer isso agora?");
        builder.setPositiveButton("Sim",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gerarDialogoAlterarDadosEntrega();
                    }
                });
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                parametroTipoPedido = 1;
                configuraBotoesTipoDePedido();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void preencherLayoutEntrega()
    {
        TextView tvDadosEntrega;
        ImageButton btAlterarDadosEntrega;
        tvDadosEntrega = loTipoPedidoEnt.findViewById(R.id.tvlDadosEntrega);
        btAlterarDadosEntrega = loTipoPedidoEnt.findViewById(R.id.btlEntregaAlterarDados);

        tvDadosEntrega.setText(cliente.getNome() + " - " + cliente.getCelular() + " - " + cliente.getRua() + " - " + cliente.getNumero() + " - " + cliente.getBairro() + " - " + cliente.getReferencia());

        enderecoEntregaFinal = tvDadosEntrega.getText().toString().trim();

        PushDownAnim.setPushDownAnimTo(btAlterarDadosEntrega)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        gerarDialogoAlterarDadosEntrega();
                    }
                });

        parametroTipoPedido = 0;
        calcularValorTaxa();
        configuraBotoesTipoDePedido();
        if(parametroCupomAplicado == 1)
        {
            verificaCupomDesconto();
        }
    }

    public void preencherLayoutRetirada()
    {
        TextView tvNome;
        ImageButton btAlterarNomeRetirada;
        tvNome = loTipoPedidoRet.findViewById(R.id.tvlNomeRetirada);
        btAlterarNomeRetirada = loTipoPedidoRet.findViewById(R.id.btlRetiradaAlterarDados);

        tvNome.setText(nomeRetiraOuConsumo);

        PushDownAnim.setPushDownAnimTo(btAlterarNomeRetirada)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        gerarDialogoAlterarDadosRetirada();
                    }
                });

        parametroTipoPedido = 1;
        calulcarTotalDoPedido();
        configuraBotoesTipoDePedido();
        if(parametroCupomAplicado == 1)
        {
            verificaCupomDesconto();
        }
    }

    public void preencherLayoutConsumoNoLocal()
    {
        TextView tvNome;
        ImageButton btAlterarNomeConsumoNoLocal;
        tvNome = loTipoPedidoLoc.findViewById(R.id.tvlNomeConsumoNoLocal);
        btAlterarNomeConsumoNoLocal = loTipoPedidoLoc.findViewById(R.id.btlConsumoNoLocalAlterarDados);

        tvNome.setText(nomeRetiraOuConsumo);

        PushDownAnim.setPushDownAnimTo(btAlterarNomeConsumoNoLocal)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        gerarDialogoAlterarDadosConsumoNoLocal();
                    }
                });

        parametroTipoPedido = 2;
        calulcarTotalDoPedido();
        configuraBotoesTipoDePedido();
        if(parametroCupomAplicado == 1)
        {
            verificaCupomDesconto();
        }
    }

    public void gerarDialogoAlterarDadosEntrega()
    {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(TelaConfirmacaoPedido.this);
        bottomSheetDialog.setContentView(R.layout.dialogo_alterar_dados_cliente);

        bottomSheetDialog.setCanceledOnTouchOutside(false);

        String[] ITENS = {
                "25 De Julho", "Alpestre", "Alpino", "Bela Aliança", "Bohemerwald", "Brasilia", "Centro", "Colonial", "Cruzeiro", "Dona Francisca", "Fundão",
                "Industrial Sudoeste", "Lençol", "Mato Preto", "Oxford", "Progresso", "Rio Negro", "Rio Vermelho", "Rio Vermelho Estação", "Rio Vermelho Povoado",
                "Schramm", "Serra Alta", "Urca", "Vila Centenário", "Vila São Paulo",
        };

        ProgressBar pbBuscarCep = bottomSheetDialog.findViewById(R.id.progressBarDialogoEditarDados);
        EditText etNome= bottomSheetDialog.findViewById(R.id.etDialogoDadosNome);
        EditText etCelular = bottomSheetDialog.findViewById(R.id.etDialogoDadosCelular);
        EditText etRua = bottomSheetDialog.findViewById(R.id.etDialogoDadosRua);
        EditText etNumero = bottomSheetDialog.findViewById(R.id.etDialogoDadosNumero);
        AutoCompleteTextView etBairro = bottomSheetDialog.findViewById(R.id.etDialogoDadosBairro);
        EditText etReferencia = bottomSheetDialog.findViewById(R.id.etDialogoDadosReferencia);
        Button btBuscaLocal = bottomSheetDialog.findViewById(R.id.btDialogoDadosBuscaLocal);
        Button btSalvar = bottomSheetDialog.findViewById(R.id.btDialogoDadosSalvar);
        Button btCancelar = bottomSheetDialog.findViewById(R.id.btDialogoDadosCancelar);

        //Insere máscara no campo celular
        //etCelular.addTextChangedListener(Mask.insert("(##)#####-####", etCelular));
        //etBuscaCep.addTextChangedListener(Mask.insert("#####-###", etBuscaCep));

        pbBuscarCep.setVisibility(View.GONE);

        ArrayAdapter<String> adapterAutoComplete = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, ITENS);
        etBairro.setAdapter(adapterAutoComplete);

        etNome.setText(cliente.getNome());
        etCelular.setText(cliente.getCelular());
        etRua.setText(cliente.getRua());
        etNumero.setText(cliente.getNumero());
        etBairro.setText(cliente.getBairro());
        etReferencia.setText(cliente.getReferencia());

        PushDownAnim.setPushDownAnimTo(btCancelar)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(TextUtils.isEmpty(cliente.getRua()) || TextUtils.isEmpty(cliente.getBairro()))
                        {
                            parametroTipoPedido = 1;
                            configuraBotoesTipoDePedido();
                            SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                    .setText("Os dados para entrega não foram preenchidos.")
                                    .setDuration(Style.DURATION_SHORT)
                                    .setColor(getResources().getColor(R.color.marrom))
                                    .setAnimations(Style.ANIMATIONS_POP)
                                    .show();
                        }
                        bottomSheetDialog.dismiss();
                    }
                });
        PushDownAnim.setPushDownAnimTo(btSalvar)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Salvamos alterações e validamos
                        String nomeSalvar = etNome.getText().toString();
                        String celularSalvar = etCelular.getText().toString();
                        String ruaSalvar = etRua.getText().toString();
                        String numeroSalvar = etNumero.getText().toString();
                        String bairroSalvar = etBairro.getText().toString();
                        String referenciaSalvar = etReferencia.getText().toString();

                        if(!nomeSalvar.isEmpty()) {
                            if(!celularSalvar.isEmpty()) {
                                if(celularSalvar.length() == 14) {
                                    if(!TextUtils.isEmpty(ruaSalvar)) {
                                        if(!TextUtils.isEmpty(numeroSalvar)) {
                                            if(!TextUtils.isEmpty(bairroSalvar)) {
                                                if(Arrays.asList(ITENS).contains(bairroSalvar)) {
                                                    if(TextUtils.isEmpty(referenciaSalvar))
                                                    {
                                                        try {
                                                            int testaNumero = Integer.parseInt(numeroSalvar);
                                                            if(testaNumero > 0)
                                                            {
                                                                try{
                                                                    //SQLiteDatabase bancoDados = openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
                                                                    bancoDados = TelaConfirmacaoPedido.this.openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
                                                                    //Executa o update do cliente
                                                                    bancoDados.execSQL("UPDATE cliente SET nome = '"+ nomeSalvar +"', celular = '"+ celularSalvar +"', rua = '"+ ruaSalvar +"', numero = '"+ numeroSalvar +"', bairro = '"+ bairroSalvar +"', referencia = '"+ referenciaSalvar +"' WHERE id = " + idUsuario);
                                                                    SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                                                            .setText("Dados salvos com sucesso!")
                                                                            .setDuration(Style.DURATION_SHORT)
                                                                            .setColor(getResources().getColor(R.color.marrom))
                                                                            .setAnimations(Style.ANIMATIONS_POP)
                                                                            .show();
                                                                    cliente.setNome(nomeSalvar);
                                                                    cliente.setCelular(celularSalvar);
                                                                    cliente.setRua(ruaSalvar);
                                                                    cliente.setNumero(numeroSalvar);
                                                                    cliente.setBairro(bairroSalvar);
                                                                    cliente.setReferencia(referenciaSalvar);
                                                                    preencherLayoutEntrega();
                                                                    bottomSheetDialog.dismiss();
                                                                }
                                                                catch(Exception e){
                                                                    String erro = e.toString();
                                                                    SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                                                            .setText("Ocorreu um erro ao salvar os dados: " + erro)
                                                                            .setDuration(Style.DURATION_SHORT)
                                                                            .setColor(getResources().getColor(R.color.marrom))
                                                                            .setAnimations(Style.ANIMATIONS_POP)
                                                                            .show();
                                                                    Log.i("ERROBANCO", erro);
                                                                }
                                                            }
                                                            else
                                                            {
                                                                if(!TextUtils.isEmpty(referenciaSalvar))
                                                                {
                                                                    try{
                                                                        //SQLiteDatabase bancoDados = openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
                                                                        bancoDados = TelaConfirmacaoPedido.this.openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
                                                                        //Executa o update do cliente
                                                                        bancoDados.execSQL("UPDATE cliente SET nome = '"+ nomeSalvar +"', celular = '"+ celularSalvar +"', rua = '"+ ruaSalvar +"', numero = '"+ numeroSalvar +"', bairro = '"+ bairroSalvar +"', referencia = '"+ referenciaSalvar +"' WHERE id = " + idUsuario);
                                                                        SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                                                                .setText("Dados salvos com sucesso!")
                                                                                .setDuration(Style.DURATION_SHORT)
                                                                                .setColor(getResources().getColor(R.color.marrom))
                                                                                .setAnimations(Style.ANIMATIONS_POP)
                                                                                .show();
                                                                        cliente.setNome(nomeSalvar);
                                                                        cliente.setCelular(celularSalvar);
                                                                        cliente.setRua(ruaSalvar);
                                                                        cliente.setNumero(numeroSalvar);
                                                                        cliente.setBairro(bairroSalvar);
                                                                        cliente.setReferencia(referenciaSalvar);
                                                                        preencherLayoutEntrega();
                                                                        bottomSheetDialog.dismiss();
                                                                    }
                                                                    catch(Exception e){
                                                                        String erro = e.toString();
                                                                        SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                                                                .setText("Ocorreu um erro ao salvar os dados: " + erro)
                                                                                .setDuration(Style.DURATION_SHORT)
                                                                                .setColor(getResources().getColor(R.color.marrom))
                                                                                .setAnimations(Style.ANIMATIONS_POP)
                                                                                .show();
                                                                        Log.i("ERROBANCO", erro);
                                                                    }
                                                                }
                                                                else
                                                                {
                                                                    SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                                                            .setText("Se sua casa não possuir número, é necessário informar detalhes para que seja possível encontrar a sua residência. Neste caso, preencha o campo COMPLEMENTO")
                                                                            .setDuration(Style.DURATION_SHORT)
                                                                            .setColor(getResources().getColor(R.color.marrom))
                                                                            .setAnimations(Style.ANIMATIONS_POP)
                                                                            .show();
                                                                    etReferencia.requestFocus();
                                                                }
                                                            }
                                                        }catch (Exception e)
                                                        {
                                                            SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                                                    .setText("O número informado é inválido. Verifique e tente novamente")
                                                                    .setDuration(Style.DURATION_SHORT)
                                                                    .setColor(getResources().getColor(R.color.marrom))
                                                                    .setAnimations(Style.ANIMATIONS_POP)
                                                                    .show();
                                                            etNumero.requestFocus();
                                                        }
                                                    }
                                                    else
                                                    {
                                                        try{
                                                            //SQLiteDatabase bancoDados = openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
                                                            bancoDados = TelaConfirmacaoPedido.this.openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
                                                            //Executa o update do cliente
                                                            bancoDados.execSQL("UPDATE cliente SET nome = '"+ nomeSalvar +"', celular = '"+ celularSalvar +"', rua = '"+ ruaSalvar +"', numero = '"+ numeroSalvar +"', bairro = '"+ bairroSalvar +"', referencia = '"+ referenciaSalvar +"' WHERE id = " + idUsuario);
                                                            SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                                                    .setText("Dados salvos com sucesso!")
                                                                    .setDuration(Style.DURATION_SHORT)
                                                                    .setColor(getResources().getColor(R.color.marrom))
                                                                    .setAnimations(Style.ANIMATIONS_POP)
                                                                    .show();
                                                            cliente.setNome(nomeSalvar);
                                                            cliente.setCelular(celularSalvar);
                                                            cliente.setRua(ruaSalvar);
                                                            cliente.setNumero(numeroSalvar);
                                                            cliente.setBairro(bairroSalvar);
                                                            cliente.setReferencia(referenciaSalvar);
                                                            preencherLayoutEntrega();
                                                            bottomSheetDialog.dismiss();
                                                        }
                                                        catch(Exception e){
                                                            String erro = e.toString();
                                                            SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                                                    .setText("Ocorreu um erro ao salvar os dados: " + erro)
                                                                    .setDuration(Style.DURATION_SHORT)
                                                                    .setColor(getResources().getColor(R.color.marrom))
                                                                    .setAnimations(Style.ANIMATIONS_POP)
                                                                    .show();
                                                            Log.i("ERROBANCO", erro);
                                                        }
                                                    }
                                                }
                                                else {
                                                    SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                                            .setText("Bairro inválido. Selecione um bairro da lista")
                                                            .setDuration(Style.DURATION_SHORT)
                                                            .setColor(getResources().getColor(R.color.marrom))
                                                            .setAnimations(Style.ANIMATIONS_POP)
                                                            .show();
                                                    etBairro.requestFocus();
                                                }
                                            }
                                            else {
                                                SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                                        .setText("Por gentileza, informe o bairro. Verifique ou digite novamente")
                                                        .setDuration(Style.DURATION_SHORT)
                                                        .setColor(getResources().getColor(R.color.marrom))
                                                        .setAnimations(Style.ANIMATIONS_POP)
                                                        .show();
                                                etBairro.requestFocus();
                                            }
                                        }
                                        else
                                        {
                                            SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                                    .setText("Por gentileza, informe o numero da casa. Se a casa não possuir número, informe 0")
                                                    .setDuration(Style.DURATION_SHORT)
                                                    .setColor(getResources().getColor(R.color.marrom))
                                                    .setAnimations(Style.ANIMATIONS_POP)
                                                    .show();
                                            etNumero.requestFocus();
                                        }
                                    }
                                    else {
                                        SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                                .setText("Por gentileza, informe a rua. Verifique ou digite novamente")
                                                .setDuration(Style.DURATION_SHORT)
                                                .setColor(getResources().getColor(R.color.marrom))
                                                .setAnimations(Style.ANIMATIONS_POP)
                                                .show();
                                        etRua.requestFocus();
                                    }
                                }
                                else{
                                    SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                            .setText("O numero de celular não está completo. Verifique ou digite novamente")
                                            .setDuration(Style.DURATION_SHORT)
                                            .setColor(getResources().getColor(R.color.marrom))
                                            .setAnimations(Style.ANIMATIONS_POP)
                                            .show();
                                    etCelular.requestFocus();
                                }
                            }
                            else{
                                SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                        .setText("Por gentileza, informe seu celular.")
                                        .setDuration(Style.DURATION_SHORT)
                                        .setColor(getResources().getColor(R.color.marrom))
                                        .setAnimations(Style.ANIMATIONS_POP)
                                        .show();
                                etCelular.requestFocus();
                            }
                        }
                        else{
                            SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                    .setText("Por gentileza, informe seu nome.")
                                    .setDuration(Style.DURATION_SHORT)
                                    .setColor(getResources().getColor(R.color.marrom))
                                    .setAnimations(Style.ANIMATIONS_POP)
                                    .show();
                            etNome.requestFocus();
                        }
                    }
                });
        PushDownAnim.setPushDownAnimTo(btBuscaLocal)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Recupera local do cliente
                        if(checkLocationPermission())
                        {
                            LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
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
                                bottomSheetDialog.dismiss();
                                gerarDialogoLocalizacao();
                            }
                            else
                            {
                                pbBuscarCep.setVisibility(View.VISIBLE);
                                if (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                                {
                                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                    locationListener = new LocationListener() {
                                        @Override
                                        public void onLocationChanged(@NonNull Location location) {
                                            double latitude = location.getLatitude();
                                            double longitude = location.getLongitude();
                                            meuLocal = new LatLng(latitude, longitude);
                                            Cliente recebido = new Cliente();
                                            recebido = recuperaLocalCliente();

                                            if(recebido.getRua().equals("ERRO NA BUSCA"))
                                            {
                                                pbBuscarCep.setVisibility(View.GONE);
                                                return;
                                            }

                                            etRua.setText(recebido.getRua().trim());
                                            etBairro.setText(recebido.getBairro().trim());
                                            etNumero.setText(recebido.getNumero().trim());
                                            etReferencia.setText("");

                                            etNumero.selectAll();
                                            etNumero.requestFocus();

                                            pbBuscarCep.setVisibility(View.GONE);
                                        }

                                        @Override
                                        public void onStatusChanged(String provider, int status, Bundle extras) {

                                        }

                                        @Override
                                        public void onProviderEnabled(String provider) {

                                        }

                                        @Override
                                        public void onProviderDisabled(String provider) {

                                        }
                                    };
                                    Criteria criteria = new Criteria();
                                    criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                                    criteria.setPowerRequirement(Criteria.POWER_LOW);
                                    criteria.setAltitudeRequired(false);
                                    criteria.setBearingRequired(false);
                                    criteria.setSpeedRequired(false);
                                    criteria.setCostAllowed(true);
                                    criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
                                    criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

                                    final Looper looper = null;

                                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                        locationManager.requestSingleUpdate(criteria, locationListener, looper);
                                    }
                                }
                            }
                        }
                    }
                });
        bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        bottomSheetDialog.show();
    }

    public void gerarDialogoLocalizacao()
    {
        new AlertDialog.Builder(this)
                .setMessage("Para usar essa função, é necessário que a sua localização esteja ativa.")
                .setPositiveButton("Ativar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
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

    public Cliente recuperaLocalCliente()
    {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        Cliente retorno = new Cliente();

        try {
            addresses = geocoder.getFromLocation(meuLocal.latitude, meuLocal.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String rua = addresses.get(0).getThoroughfare();
            String bairro = addresses.get(0).getSubLocality();
            String numero = addresses.get(0).getSubThoroughfare();

            retorno.setRua(rua.trim());
            retorno.setBairro(bairro.trim());
            retorno.setNumero(numero.trim());

        } catch (IOException e) {
            e.printStackTrace();
            retorno.setRua("ERRO NA BUSCA");
            SuperActivityToast.create(getApplicationContext(), new Style(), Style.TYPE_STANDARD)
                    .setText("Ocorreu um erro ao recuperar a sua localização. Verifique se sua localização está ativa e tente novamente.")
                    .setDuration(Style.DURATION_SHORT)
                    .setColor(getResources().getColor(R.color.marrom))
                    .setAnimations(Style.ANIMATIONS_POP)
                    .show();
        }
        return retorno;
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 189;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    public void gerarDialogoAlterarDadosRetirada()
    {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(TelaConfirmacaoPedido.this);
        bottomSheetDialog.setContentView(R.layout.dialogo_alterar_nome_cliente);

        bottomSheetDialog.setCanceledOnTouchOutside(false);

        EditText etNome= bottomSheetDialog.findViewById(R.id.etDialogoAlterarNome);
        Button btSalvar = bottomSheetDialog.findViewById(R.id.btDialogoAlterarNomeSalvar);
        Button btCancelar = bottomSheetDialog.findViewById(R.id.btDialogoAlterarNomeCancelar);

        PushDownAnim.setPushDownAnimTo(btCancelar)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss();
                    }
                });

        PushDownAnim.setPushDownAnimTo(btSalvar)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String nomeSalvar = etNome.getText().toString().trim();
                        if(!TextUtils.isEmpty(nomeSalvar))
                        {
                            nomeRetiraOuConsumo = nomeSalvar;
                            preencherLayoutRetirada();
                            bottomSheetDialog.dismiss();
                        }
                        else
                        {
                            SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                    .setText("Não é possível salvar o nome em branco")
                                    .setDuration(Style.DURATION_SHORT)
                                    .setColor(getResources().getColor(R.color.marrom))
                                    .setAnimations(Style.ANIMATIONS_POP)
                                    .show();
                            etNome.requestFocus();
                        }
                    }
                });
        bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        bottomSheetDialog.show();
    }

    public void gerarDialogoAlterarDadosConsumoNoLocal()
    {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(TelaConfirmacaoPedido.this);
        bottomSheetDialog.setContentView(R.layout.dialogo_alterar_nome_cliente);

        bottomSheetDialog.setCanceledOnTouchOutside(false);

        EditText etNome= bottomSheetDialog.findViewById(R.id.etDialogoAlterarNome);
        Button btSalvar = bottomSheetDialog.findViewById(R.id.btDialogoAlterarNomeSalvar);
        Button btCancelar = bottomSheetDialog.findViewById(R.id.btDialogoAlterarNomeCancelar);

        PushDownAnim.setPushDownAnimTo(btCancelar)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss();
                    }
                });

        PushDownAnim.setPushDownAnimTo(btSalvar)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String nomeSalvar = etNome.getText().toString().trim();
                        if(!TextUtils.isEmpty(nomeSalvar))
                        {
                            nomeRetiraOuConsumo = nomeSalvar;
                            preencherLayoutConsumoNoLocal();
                            bottomSheetDialog.dismiss();
                        }
                        else
                        {
                            SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                    .setText("Não é possível salvar o nome em branco")
                                    .setDuration(Style.DURATION_SHORT)
                                    .setColor(getResources().getColor(R.color.marrom))
                                    .setAnimations(Style.ANIMATIONS_POP)
                                    .show();
                            etNome.requestFocus();
                        }
                    }
                });
        bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        bottomSheetDialog.show();
    }

    public void recuperarPedido()
    {
        listaPedido.clear();

        ItemPedido buscado = new ItemPedido();

        bancoDados = TelaConfirmacaoPedido.this.openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
        //Recuperar pessoas
        Cursor cursor = bancoDados.rawQuery("SELECT id_item, id_pedido, desc_item, adicionais_item, obs_item, valor_item, qtd_item, grupo_item FROM item_pedido", null);

        //Recupera indices
        int indiceIdItem = cursor.getColumnIndex("id_item");
        int indiceIdPedido = cursor.getColumnIndex("id_pedido");
        int indiceDescItem = cursor.getColumnIndex("desc_item");
        int indiceAdicionaisItem = cursor.getColumnIndex("adicionais_item");
        int indiceObsItem = cursor.getColumnIndex("obs_item");
        int indiceValorItem = cursor.getColumnIndex("valor_item");
        int indiceQtdItem = cursor.getColumnIndex("qtd_item");
        int indiceGrupoItem = cursor.getColumnIndex("grupo_item");

        if(cursor.moveToFirst()){
            do {
                buscado = new ItemPedido();

                buscado.setId_item(cursor.getInt(indiceIdItem));
                buscado.setId_pedido(cursor.getString(indiceIdPedido));
                buscado.setDesc_item(cursor.getString(indiceDescItem));
                buscado.setAdicionais_item(cursor.getString(indiceAdicionaisItem));
                buscado.setObs_item(cursor.getString(indiceObsItem));
                buscado.setValor_item(cursor.getDouble(indiceValorItem));
                buscado.setQtd_item(cursor.getInt(indiceQtdItem));
                buscado.setGrupo_item(cursor.getInt(indiceGrupoItem));

                String ajuste = buscado.getAdicionais_item().trim();
                buscado.setAdicionais_item(ajuste);

                listaPedido.add(buscado);

            } while (cursor.moveToNext());
        }
        calculaTotalItens();
    }

    public void calculaTotalItens()
    {
        totalItensPedido = 0.0;
        for(ItemPedido item : listaPedido)
        {
            totalItensPedido = item.getValor_item() + totalItensPedido;
        }
        Locale ptBr = new Locale("pt", "BR");
        String valorMostrar = NumberFormat.getCurrencyInstance(ptBr).format(totalItensPedido);
        tvTotalItens.setText(valorMostrar);
    }

    public void calcularValorTaxa()
    {
        valorTaxa = 0.0;
        String bairroCliente = cliente.getBairro().toUpperCase();
        String bairroLista;
        for(TaxaEntrega item : listaTaxasEntrega)
        {
            bairroLista = item.getBairro().toUpperCase();
            if(bairroLista.equals(bairroCliente))
            {
                valorTaxa = item.getValor();
                Locale ptBr = new Locale("pt", "BR");
                String valorMostrar = NumberFormat.getCurrencyInstance(ptBr).format(valorTaxa);
                taxaEntregaValor.setText(valorMostrar);
                calulcarTotalDoPedido();
                break;
            }
        }
    }

    public void calulcarTotalDoPedido()
    {
        valorTotalPedido = 0.0;
        if(parametroTipoPedido == 0)
        {
            valorTotalPedido = totalItensPedido + valorTaxa;
        }
        else
        {
            valorTotalPedido = totalItensPedido;
        }
        Locale ptBr = new Locale("pt", "BR");
        String valorMostrar = NumberFormat.getCurrencyInstance(ptBr).format(valorTotalPedido);
        tvTotalPedido.setText(valorMostrar);
    }

    public void recuperaTaxas()
    {
        DatabaseReference taxaDB = firebaseBanco.child("taxa");
        taxaDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                listaTaxasEntrega.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    listaTaxasEntrega.add(ds.getValue(TaxaEntrega.class));
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    public void recuperaCupons()
    {
        DatabaseReference cupomDB = firebaseBanco.child("cupom");
        cupomDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                listaCuponsDesconto.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    listaCuponsDesconto.add(ds.getValue(CupomDesconto.class));
                }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private int parametroRetornadoUsoCupom = 0;
    private int parametroCupomUnicoEmAnalise = 0;
    public void verificaCupomDesconto()
    {
        parametroCupomUnicoEmAnalise = 0;
        pbBuscaCupom.setVisibility(View.VISIBLE);
        ibVerificarDesconto.setVisibility(View.GONE);

        String cupomInformado = etDesconto.getText().toString();

        if(listaCuponsDesconto.size() <= 0)
        {
            SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                    .setText("Cupom inválido :(")
                    .setDuration(Style.DURATION_SHORT)
                    .setColor(getResources().getColor(R.color.marrom))
                    .setAnimations(Style.ANIMATIONS_POP)
                    .show();
            pbBuscaCupom.setVisibility(View.GONE);
            ibVerificarDesconto.setVisibility(View.VISIBLE);
            return;
        }

        if(parametroCupomAplicado == 1)
        {
            cupomInformado = cupomAplicado;
        }

        String cupomLista;
        int contador = 0; //usado pra saber se já percorrimos a lista toda
        for(CupomDesconto item : listaCuponsDesconto)
        {
            contador++;
            cupomLista = item.getDescricao();
            if(cupomLista.equals(cupomInformado)) //O CUPOM EXISTE PARA SER APLICADO
            {
                SimpleDateFormat dataFormatada = new SimpleDateFormat("dd/MM/yyyy");
                Date currentTime = new Date();
                String dataAtual = dataFormatada.format(currentTime);
                Date dataAtualConvertida;

                String dataValidade = item.getValidade();
                Date dataValidadelConvertida;
                SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
                try{
                    dataValidadelConvertida = formato.parse(dataValidade);
                    dataAtualConvertida = formato.parse(dataAtual);
                }catch(ParseException e)
                {
                    e.printStackTrace();
                    SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                            .setText("Ocorreu um erro ao verificar o cupom")
                            .setDuration(Style.DURATION_SHORT)
                            .setColor(getResources().getColor(R.color.marrom))
                            .setAnimations(Style.ANIMATIONS_POP)
                            .show();
                    pbBuscaCupom.setVisibility(View.GONE);
                    ibVerificarDesconto.setVisibility(View.VISIBLE);
                    return;
                }
                if(dataAtualConvertida.before(dataValidadelConvertida) || dataAtualConvertida.equals(dataValidadelConvertida))//O CUPOM ESTÁ NA VALIDADE
                {
                    String cupomRepassar = cupomInformado;
                    Double valorMinimo = item.getMinimo();
                    if(valorTotalPedido > valorMinimo) //O CUPOM TEM SEU VALOR ACIMA DO MÍNIMO ESTIPULADO
                    {

                        if(item.getCupomUnico() == 1) //SIGNIGICA QUE É UM CUPOM POR PESSOA. SEM FOLIA
                        {
                            //VERIFICAMOS SE ESSA PESSOA JÁ NÃO USOU ESSE CUPOM
                            DatabaseReference cupomUsadoDB = firebaseBanco.child("cupom-uso").child(identificadorUnicoParaCupom).child(item.getDescricao());
                            parametroCupomUnicoEmAnalise = 1;
                            cupomUsadoDB.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                    if(snapshot.getValue() != null)
                                        parametroRetornadoUsoCupom = snapshot.getValue(Integer.class);
                                    verificaCupomUnico(cupomRepassar, item);
                                }
                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                }
                            });
                        }
                        else
                        {
                            CupomDesconto cupomAplicar = item;
                            cupomAplicado = cupomInformado;
                            if(cupomAplicar.getTipo() == 1) //cupom de porcentagem
                            {
                                Double valorComDesconto = 0.0;
                                Double valorDoDesconto = 0.0;
                                Double multiplicador = 0.0;
                                multiplicador = cupomAplicar.getValor()/100;
                                valorDoDesconto = valorTotalPedido * multiplicador;
                                valorComDesconto = valorTotalPedido - valorDoDesconto;
                                valorDoPedidoComDesconto = valorComDesconto;
                                valorDoDescontoNoPedido = valorDoDesconto;
                                Locale ptBr = new Locale("pt", "BR");
                                String valorMostrar = NumberFormat.getCurrencyInstance(ptBr).format(valorDoDesconto);
                                tvDescontoValor.setText(valorMostrar);
                                String valorTotalMostrar = NumberFormat.getCurrencyInstance(ptBr).format(valorComDesconto);
                                tvTotalPedido.setText(valorTotalMostrar);
                                tvDescontoDesc.setVisibility(View.VISIBLE);
                                tvDescontoValor.setVisibility(View.VISIBLE);
                                tvInformarDescontoDesc.setVisibility(View.GONE);
                                etDesconto.setVisibility(View.GONE);
                                etDesconto.setText("");
                                ibVerificarDesconto.setVisibility(View.GONE);
                                if(parametroCupomAplicado == 0)
                                {
                                    SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                            .setText("Cupom aplicado! :)")
                                            .setDuration(Style.DURATION_SHORT)
                                            .setColor(getResources().getColor(R.color.marrom))
                                            .setAnimations(Style.ANIMATIONS_POP)
                                            .show();
                                }
                                parametroCupomAplicado = 1;
                                break;
                            }
                            else //cupom de valor em reais
                            {
                                Double valorComDesconto = 0.0;
                                Double valorDoDesconto = 0.0;
                                valorDoDesconto = cupomAplicar.getValor();
                                valorComDesconto = valorTotalPedido - valorDoDesconto;
                                valorDoPedidoComDesconto = valorComDesconto;
                                valorDoDescontoNoPedido = valorDoDesconto;
                                Locale ptBr = new Locale("pt", "BR");
                                String valorMostrar = NumberFormat.getCurrencyInstance(ptBr).format(valorDoDesconto);
                                tvDescontoValor.setText(valorMostrar);
                                String valorTotalMostrar = NumberFormat.getCurrencyInstance(ptBr).format(valorComDesconto);
                                tvTotalPedido.setText(valorTotalMostrar);
                                tvDescontoDesc.setVisibility(View.VISIBLE);
                                tvDescontoValor.setVisibility(View.VISIBLE);
                                tvInformarDescontoDesc.setVisibility(View.GONE);
                                etDesconto.setText("");
                                etDesconto.setVisibility(View.GONE);
                                ibVerificarDesconto.setVisibility(View.GONE);
                                if(parametroCupomAplicado == 0)
                                {
                                    SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                            .setText("Cupom aplicado! :)")
                                            .setDuration(Style.DURATION_SHORT)
                                            .setColor(getResources().getColor(R.color.marrom))
                                            .setAnimations(Style.ANIMATIONS_POP)
                                            .show();
                                }
                                parametroCupomAplicado = 1;
                                break;
                            }
                        }
                    }
                    else
                    {
                        SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                .setText("Seu pedido não atende o valor mínimo para este cupom. :(")
                                .setDuration(Style.DURATION_SHORT)
                                .setColor(getResources().getColor(R.color.marrom))
                                .setAnimations(Style.ANIMATIONS_POP)
                                .show();
                        pbBuscaCupom.setVisibility(View.VISIBLE);
                        ibVerificarDesconto.setVisibility(View.GONE);
                        tvDescontoValor.setText("R$0,00");
                        tvDescontoDesc.setVisibility(View.GONE);
                        tvDescontoValor.setVisibility(View.GONE);
                        tvInformarDescontoDesc.setVisibility(View.VISIBLE);
                        etDesconto.setVisibility(View.VISIBLE);
                        ibVerificarDesconto.setVisibility(View.VISIBLE);
                        Locale ptBr = new Locale("pt", "BR");
                        String valorMostrar = NumberFormat.getCurrencyInstance(ptBr).format(valorTotalPedido);
                        tvTotalPedido.setText(valorMostrar);
                        parametroCupomAplicado = 0;
                        return;
                    }
                }
                else
                {
                    SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                            .setText("Este cupom expirou. :(")
                            .setDuration(Style.DURATION_SHORT)
                            .setColor(getResources().getColor(R.color.marrom))
                            .setAnimations(Style.ANIMATIONS_POP)
                            .show();
                    pbBuscaCupom.setVisibility(View.VISIBLE);
                    ibVerificarDesconto.setVisibility(View.GONE);
                    return;
                }
            }
            else
            {
                if(contador == listaCuponsDesconto.size() && parametroCupomUnicoEmAnalise == 0)//significa que chegamos no final
                {
                    SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                            .setText("Cupom inválido! :(")
                            .setDuration(Style.DURATION_SHORT)
                            .setColor(getResources().getColor(R.color.marrom))
                            .setAnimations(Style.ANIMATIONS_POP)
                            .show();
                    pbBuscaCupom.setVisibility(View.GONE);
                    ibVerificarDesconto.setVisibility(View.VISIBLE);
                    return;
                }
            }
        }
    }
    public void verificaCupomUnico(String cupomInformado, CupomDesconto item)
    {
        parametroCupomUnicoEmAnalise = 0;
        if(parametroRetornadoUsoCupom == 1) // SIGNIGICA QUE ESSE CLIENTE JÁ USOU ESSE CUPOM
        {
            SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                    .setText("Você já utilizou este cupom uma vez. Não é possível utilizar novamente. :/")
                    .setDuration(Style.DURATION_SHORT)
                    .setColor(getResources().getColor(R.color.marrom))
                    .setAnimations(Style.ANIMATIONS_POP)
                    .show();
            pbBuscaCupom.setVisibility(View.GONE);
            ibVerificarDesconto.setVisibility(View.VISIBLE);
            return;
        }
        else //ESSE CUPOM AINDA NÃO FOI USADO POR ESSE CLIENTE
        {
            CupomDesconto cupomAplicar = item;
            cupomAplicado = cupomInformado;
            if(cupomAplicar.getTipo() == 1) //cupom de porcentagem
            {
                Double valorComDesconto = 0.0;
                Double valorDoDesconto = 0.0;
                Double multiplicador = 0.0;
                multiplicador = cupomAplicar.getValor()/100;
                valorDoDesconto = valorTotalPedido * multiplicador;
                valorComDesconto = valorTotalPedido - valorDoDesconto;
                valorDoPedidoComDesconto = valorComDesconto;
                valorDoDescontoNoPedido = valorDoDesconto;
                Locale ptBr = new Locale("pt", "BR");
                String valorMostrar = NumberFormat.getCurrencyInstance(ptBr).format(valorDoDesconto);
                tvDescontoValor.setText(valorMostrar);
                String valorTotalMostrar = NumberFormat.getCurrencyInstance(ptBr).format(valorComDesconto);
                tvTotalPedido.setText(valorTotalMostrar);
                tvDescontoDesc.setVisibility(View.VISIBLE);
                tvDescontoValor.setVisibility(View.VISIBLE);
                tvInformarDescontoDesc.setVisibility(View.GONE);
                etDesconto.setVisibility(View.GONE);
                etDesconto.setText("");
                ibVerificarDesconto.setVisibility(View.GONE);
                if(parametroCupomAplicado == 0)
                {
                    SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                            .setText("Cupom aplicado! :)")
                            .setDuration(Style.DURATION_SHORT)
                            .setColor(getResources().getColor(R.color.marrom))
                            .setAnimations(Style.ANIMATIONS_POP)
                            .show();
                }
                parametroCupomAplicado = 1;
            }
            else //cupom de valor em reais
            {
                Double valorComDesconto = 0.0;
                Double valorDoDesconto = 0.0;
                valorDoDesconto = cupomAplicar.getValor();
                valorComDesconto = valorTotalPedido - valorDoDesconto;
                valorDoPedidoComDesconto = valorComDesconto;
                valorDoDescontoNoPedido = valorDoDesconto;
                Locale ptBr = new Locale("pt", "BR");
                String valorMostrar = NumberFormat.getCurrencyInstance(ptBr).format(valorDoDesconto);
                tvDescontoValor.setText(valorMostrar);
                String valorTotalMostrar = NumberFormat.getCurrencyInstance(ptBr).format(valorComDesconto);
                tvTotalPedido.setText(valorTotalMostrar);
                tvDescontoDesc.setVisibility(View.VISIBLE);
                tvDescontoValor.setVisibility(View.VISIBLE);
                tvInformarDescontoDesc.setVisibility(View.GONE);
                etDesconto.setText("");
                etDesconto.setVisibility(View.GONE);
                ibVerificarDesconto.setVisibility(View.GONE);
                if(parametroCupomAplicado == 0)
                {
                    SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                            .setText("Cupom aplicado! :)")
                            .setDuration(Style.DURATION_SHORT)
                            .setColor(getResources().getColor(R.color.marrom))
                            .setAnimations(Style.ANIMATIONS_POP)
                            .show();
                }
                parametroCupomAplicado = 1;
            }
        }
    }

    public void gerarDialogoIntrucoesPagamento()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(TelaConfirmacaoPedido.this);
        builder.setCancelable(true);
        builder.setTitle("Informações para Pagamento");
        builder.setMessage("Selecione uma das opções para efetuar o pagamento.\nSe deseja pagar de alguma outra forma ou com mais de uma forma, por gentileza informe no campo de observação do pedido.");
        builder.setPositiveButton("Entendido!",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void configuraBotoesTipoDePagamento() //usado para alterar a cor dos botões ao selecionar diferentes valores
    {
        if(parametroTipoPagamento == 0) //DINHEIRO
        {
            parametroPrecisaDeTroco = -1;
            //ALTERA COR DOS BOTÕES
            btTipoPagDinheiro.setBackgroundResource(R.drawable.background_botao_arredondado);
            btTipoPagDinheiro.setTextColor(getResources().getColor(R.color.branco));
            btTipoPagCartao.setBackgroundResource(R.drawable.background_botao_arredondado_branco);
            btTipoPagCartao.setTextColor(getResources().getColor(R.color.marrom));
            btTipoPagPix.setBackgroundResource(R.drawable.background_botao_arredondado_branco);
            btTipoPagPix.setTextColor(getResources().getColor(R.color.marrom));
            //ALTERA VISIBILIDADE DO LAYOUT
            animFadeoutTipoPagamento();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loTipoPagamentoDin.setVisibility(View.VISIBLE);
                    loTipoPagamentoCar.setVisibility(View.GONE);
                    loTipoPagamentoPix.setVisibility(View.GONE);
                }
            }, 150);
            animFadeinTipoPagamento();
            final Handler handler1 = new Handler();
            handler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            mScrollView.smoothScrollTo(0, loTipoPagamento.getBottom());
                        }
                    });
                }
            }, 500);
        }
        if(parametroTipoPagamento == 1)//CARTÃO
        {
            parametroPrecisaDeTroco = -1;
            //ALTERA COR DOS BOTÕES
            btTipoPagCartao.setBackgroundResource(R.drawable.background_botao_arredondado);
            btTipoPagCartao.setTextColor(getResources().getColor(R.color.branco));
            btTipoPagDinheiro.setBackgroundResource(R.drawable.background_botao_arredondado_branco);
            btTipoPagDinheiro.setTextColor(getResources().getColor(R.color.marrom));
            btTipoPagPix.setBackgroundResource(R.drawable.background_botao_arredondado_branco);
            btTipoPagPix.setTextColor(getResources().getColor(R.color.marrom));
            //ALTERA VISIBILIDADE DO LAYOUT
            animFadeoutTipoPagamento();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loTipoPagamentoDin.setVisibility(View.GONE);
                    loTipoPagamentoCar.setVisibility(View.VISIBLE);
                    loTipoPagamentoPix.setVisibility(View.GONE);
                }
            }, 150);
            animFadeinTipoPagamento();
            final Handler handler1 = new Handler();
            handler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            mScrollView.smoothScrollTo(0, loTipoPagamento.getBottom());
                        }
                    });
                }
            }, 500);
        }
        if(parametroTipoPagamento == 2)//PIX
        {
            parametroPrecisaDeTroco = -1;
            //ALTERA COR DOS BOTÕES
            btTipoPagPix.setBackgroundResource(R.drawable.background_botao_arredondado);
            btTipoPagPix.setTextColor(getResources().getColor(R.color.branco));
            btTipoPagDinheiro.setBackgroundResource(R.drawable.background_botao_arredondado_branco);
            btTipoPagDinheiro.setTextColor(getResources().getColor(R.color.marrom));
            btTipoPagCartao.setBackgroundResource(R.drawable.background_botao_arredondado_branco);
            btTipoPagCartao.setTextColor(getResources().getColor(R.color.marrom));
            //ALTERA VISIBILIDADE DO LAYOUT
            animFadeoutTipoPagamento();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loTipoPagamentoDin.setVisibility(View.GONE);
                    loTipoPagamentoCar.setVisibility(View.GONE);
                    loTipoPagamentoPix.setVisibility(View.VISIBLE);
                }
            }, 150);
            animFadeinTipoPagamento();
            final Handler handler1 = new Handler();
            handler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            mScrollView.smoothScrollTo(0, loTipoPagamento.getBottom());
                        }
                    });
                }
            }, 500);
        }
    }
    private EditText etTrocoPara;
    public void preencherLayoutPagamentoDin()
    {
        TextView descTrocoSim, descTrocoNao, descTrocoSim1;
        Button btlPagDinTrocoSim, btlPagDinTrocoNao;

        btlPagDinTrocoSim = loTipoPagamentoDin.findViewById(R.id.btlPagDinSim);
        btlPagDinTrocoNao = loTipoPagamentoDin.findViewById(R.id.btlPagDinNao);
        etTrocoPara = loTipoPagamentoDin.findViewById(R.id.etPagamentoDinTrocoPara);
        descTrocoSim = loTipoPagamentoDin.findViewById(R.id.textView151);
        descTrocoNao = loTipoPagamentoDin.findViewById(R.id.textView152);
        descTrocoSim1 = loTipoPagamentoDin.findViewById(R.id.textView9);

        btlPagDinTrocoSim.setBackgroundResource(R.drawable.background_botao_arredondado_branco);
        btlPagDinTrocoSim.setTextColor(getResources().getColor(R.color.marrom));
        btlPagDinTrocoNao.setBackgroundResource(R.drawable.background_botao_arredondado_branco);
        btlPagDinTrocoNao.setTextColor(getResources().getColor(R.color.marrom));

        etTrocoPara.setVisibility(View.GONE);
        descTrocoSim.setVisibility(View.GONE);
        descTrocoSim1.setVisibility(View.GONE);
        descTrocoNao.setVisibility(View.GONE);

        etTrocoPara.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus)
                {
                    View loDinheiro = TelaConfirmacaoPedido.this.getCurrentFocus();
                    if (loDinheiro != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(loDinheiro.getWindowToken(), 0);
                    }
                }
                else
                {

                }
            }
        });

        PushDownAnim.setPushDownAnimTo(btlPagDinTrocoSim)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        btlPagDinTrocoSim.setBackgroundResource(R.drawable.background_botao_arredondado);
                        btlPagDinTrocoSim.setTextColor(getResources().getColor(R.color.branco));
                        btlPagDinTrocoNao.setBackgroundResource(R.drawable.background_botao_arredondado_branco);
                        btlPagDinTrocoNao.setTextColor(getResources().getColor(R.color.marrom));
                        etTrocoPara.setVisibility(View.VISIBLE);
                        descTrocoSim.setVisibility(View.VISIBLE);
                        descTrocoSim1.setVisibility(View.VISIBLE);
                        descTrocoNao.setVisibility(View.GONE);
                        etTrocoPara.setText("");
                        parametroPrecisaDeTroco = 1;
                        mScrollView.post(new Runnable() {
                            @Override
                            public void run() {
                                mScrollView.smoothScrollTo(0, loTipoPagamento.getBottom());
                            }
                        });
                    }
                });
        PushDownAnim.setPushDownAnimTo(btlPagDinTrocoNao)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        btlPagDinTrocoNao.setBackgroundResource(R.drawable.background_botao_arredondado);
                        btlPagDinTrocoNao.setTextColor(getResources().getColor(R.color.branco));
                        btlPagDinTrocoSim.setBackgroundResource(R.drawable.background_botao_arredondado_branco);
                        btlPagDinTrocoSim.setTextColor(getResources().getColor(R.color.marrom));
                        etTrocoPara.setVisibility(View.GONE);
                        descTrocoSim.setVisibility(View.GONE);
                        descTrocoSim1.setVisibility(View.GONE);
                        descTrocoNao.setVisibility(View.VISIBLE);
                        etTrocoPara.setText("");
                        parametroPrecisaDeTroco = 0;
                        mScrollView.post(new Runnable() {
                            @Override
                            public void run() {
                                mScrollView.smoothScrollTo(0, loTipoPagamento.getBottom());
                            }
                        });
                    }
                });
        configuraBotoesTipoDePagamento();
    }
    public void preencherLayoutPagamentoCar()
    {
        configuraBotoesTipoDePagamento();
    }
    public void preencherLayoutPagamentoPix()
    {
        ImageButton btCopiaChavePix;
        btCopiaChavePix = loTipoPagamentoPix.findViewById(R.id.btlPagamentoPìxCopiarChave);
        PushDownAnim.setPushDownAnimTo(btCopiaChavePix)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("chave_pix", "sandramariastiz@gmail.com");
                        clipboard.setPrimaryClip(clip);

                        SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                .setText("Chave PIX copiada para a área de transferência!")
                                .setDuration(Style.DURATION_SHORT)
                                .setColor(getResources().getColor(R.color.marrom))
                                .setAnimations(Style.ANIMATIONS_POP)
                                .show();
                    }
                });
        configuraBotoesTipoDePagamento();
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.smoothScrollTo(0, loTipoPagamento.getBottom());
            }
        });
    }

    public void esconderPagamento()
    {
        loPagamento.animate().translationY(-250).alpha(0.0f).setDuration(100).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                loPagamento.setVisibility(View.INVISIBLE);
            }
        });
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvFormaPagamento.setVisibility(View.GONE);
                btTipoPagDinheiro.setVisibility(View.GONE);
                btTipoPagCartao.setVisibility(View.GONE);
                btTipoPagPix.setVisibility(View.GONE);
                btAjudaPagamento.setVisibility(View.GONE);
            }
        }, 250);
    }

    public void mostrarPagamento()
    {
        loPagamento.animate().translationY(-250).alpha(0.0f).setDuration(100).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                loPagamento.setVisibility(View.INVISIBLE);
            }
        });
        tvFormaPagamento.setVisibility(View.VISIBLE);
        btTipoPagDinheiro.setVisibility(View.VISIBLE);
        btTipoPagCartao.setVisibility(View.VISIBLE);
        btTipoPagPix.setVisibility(View.VISIBLE);
        btAjudaPagamento.setVisibility(View.VISIBLE);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loPagamento.animate().translationY(0).alpha(1.0f).setDuration(100).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        loPagamento.setVisibility(View.VISIBLE);
                    }
                });
            }
        }, 500);

    }

    public void limparBotoesPagamento()
    {
        parametroTipoPagamento = -1;
        parametroPrecisaDeTroco = -1;
        loTipoPagamentoCar.setVisibility(View.GONE);
        loTipoPagamentoDin.setVisibility(View.GONE);
        loTipoPagamentoPix.setVisibility(View.GONE);
        btTipoPagCartao.setBackgroundResource(R.drawable.background_botao_arredondado_branco);
        btTipoPagCartao.setTextColor(getResources().getColor(R.color.marrom));
        btTipoPagDinheiro.setBackgroundResource(R.drawable.background_botao_arredondado_branco);
        btTipoPagDinheiro.setTextColor(getResources().getColor(R.color.marrom));
        btTipoPagPix.setBackgroundResource(R.drawable.background_botao_arredondado_branco);
        btTipoPagPix.setTextColor(getResources().getColor(R.color.marrom));
    }

    public void validarInformacoes()
    {
        if(parametroTipoPedido == -1)
        {
            SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                    .setText("Não foi informado se o pedido será para entrega, retirada ou consumo no local")
                    .setDuration(Style.DURATION_SHORT)
                    .setColor(getResources().getColor(R.color.marrom))
                    .setAnimations(Style.ANIMATIONS_POP)
                    .show();
            return;
        }
        if(parametroTipoPedido == 0)
        {
            if(parametroTipoPagamento == -1)
            {
                SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                        .setText("Não foi informado a forma de pagamento")
                        .setDuration(Style.DURATION_SHORT)
                        .setColor(getResources().getColor(R.color.marrom))
                        .setAnimations(Style.ANIMATIONS_POP)
                        .show();
                return;
            }
            else
            {
                switch(parametroTipoPagamento)
                {
                    case 0:
                            if(parametroTipoPagamento == 0 && parametroPrecisaDeTroco == -1)
                            {
                                SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                        .setText("Não foi informado se devemos enviar troco ou não")
                                        .setDuration(Style.DURATION_SHORT)
                                        .setColor(getResources().getColor(R.color.marrom))
                                        .setAnimations(Style.ANIMATIONS_POP)
                                        .show();
                                return;
                            }
                            else
                            {
                                if(parametroTipoPagamento == 0 && parametroPrecisaDeTroco == 1 && TextUtils.isEmpty(etTrocoPara.getText().toString()))
                                {
                                    SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                            .setText("Não foi informado o valor para qual devemos levar troco")
                                            .setDuration(Style.DURATION_SHORT)
                                            .setColor(getResources().getColor(R.color.marrom))
                                            .setAnimations(Style.ANIMATIONS_POP)
                                            .show();
                                    return;
                                }
                                if(parametroTipoPagamento == 0 && parametroPrecisaDeTroco == 1 && !TextUtils.isEmpty(etTrocoPara.getText().toString()))
                                {
                                    try{
                                        String trocoString = etTrocoPara.getText().toString();
                                        Double trocoDouble = Double.parseDouble(trocoString);
                                        Locale ptBr = new Locale("pt", "BR");
                                        if(trocoDouble <= valorTotalPedido)
                                        {
                                            SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                                    .setText("O valor do pedido é maior do que o valor informado para troco. Não haverá troco.")
                                                    .setDuration(Style.DURATION_SHORT)
                                                    .setColor(getResources().getColor(R.color.marrom))
                                                    .setAnimations(Style.ANIMATIONS_POP)
                                                    .show();
                                            return;
                                        }
                                        String valorMostrar = NumberFormat.getCurrencyInstance(ptBr).format(trocoDouble);
                                        trocoFinal = valorMostrar;

                                    }catch(Exception e)
                                    {
                                        SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                                .setText("O valor de troco informado é inválido.")
                                                .setDuration(Style.DURATION_SHORT)
                                                .setColor(getResources().getColor(R.color.marrom))
                                                .setAnimations(Style.ANIMATIONS_POP)
                                                .show();
                                        return;
                                    }
                                    abrirTelaConfirmacaoPedido();
                                }
                                if(parametroTipoPagamento == 0 && parametroPrecisaDeTroco == 0)
                                {
                                    trocoFinal = "R$0,00";
                                    abrirTelaConfirmacaoPedido();
                                }
                            }
                        break;
                    case 1:
                        abrirTelaConfirmacaoPedido();
                        break;
                    case 2:
                        abrirTelaConfirmacaoPedido();
                        break;
                }
            }
        }
        if(parametroTipoPedido == 1 || parametroTipoPedido == 2)
        {
            abrirTelaConfirmacaoPedido();
        }
    }

    public void abrirTelaConfirmacaoPedido()
    {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(TelaConfirmacaoPedido.this);
        bottomSheetDialog.setContentView(R.layout.dialogo_confirmar_pedido);

        bottomSheetDialog.setCanceledOnTouchOutside(false);

        TextView tvPrazoPedido = bottomSheetDialog.findViewById(R.id.tvDialogoConfPedPrazoPedido);
        TextView tvTipoPedido = bottomSheetDialog.findViewById(R.id.tvDialogoConfPedTipoPedido);
        TextView tvDescPag = bottomSheetDialog.findViewById(R.id.tvDialogoConfPedDescPag);
        TextView tvTipoPag = bottomSheetDialog.findViewById(R.id.tvDialogoConfPedTipoPag);
        TextView tvObsPedido = bottomSheetDialog.findViewById(R.id.tvDialogoConfPedObservacao);
        TextView tvMeuPedido = bottomSheetDialog.findViewById(R.id.tvDialogoConfPedMeuPedido);
        TextView tvTotalPedido = bottomSheetDialog.findViewById(R.id.tvDialogoConfPedValorPedido);
        TextView tvEndereco = bottomSheetDialog.findViewById(R.id.tvDialogoConfPedEnderecoEntrega);
        Button btConfirmar = bottomSheetDialog.findViewById(R.id.btDialogoConfPedConfirmar);
        Button btCancelar = bottomSheetDialog.findViewById(R.id.btDialogoConfPedCancelar);
        ConstraintLayout loEndereco = bottomSheetDialog.findViewById(R.id.loDialogoConfPedidoEndereco);
        ConstraintLayout loPagamento = bottomSheetDialog.findViewById(R.id.loDialogoConfPedidoPagamento);

        //Preenchemos o tipo do pedido e a forma de pagamento
        if(parametroTipoPedido == 0)
        {
            tvTipoPedido.setText("ENTREGA");
            if(parametroTipoPagamento == 0)
            {
                String valorTrocoMostrarConfirmacao;
                if(trocoFinal.equals("R$0,00"))
                    valorTrocoMostrarConfirmacao = "(Não preciso de troco)";
                else
                    valorTrocoMostrarConfirmacao = "(Troco para " + trocoFinal + ")";
                tvTipoPag.setText("DINHEIRO " + valorTrocoMostrarConfirmacao);
            }
            if(parametroTipoPagamento == 1)
                tvTipoPag.setText("CARTÃO");
            if(parametroTipoPagamento == 2)
                tvTipoPag.setText("PIX");
            tvEndereco.setText(enderecoEntregaFinal);
            tvPrazoPedido.setText(String.valueOf(parametros.getEntrega()) + " minutos");
        }
        if(parametroTipoPedido == 1)
        {
            tvTipoPedido.setText("RETIRADA");
            loEndereco.setVisibility(View.GONE);
            loPagamento.setVisibility(View.GONE);
            tvPrazoPedido.setText(String.valueOf(parametros.getRetirada())  + " minutos");
        }
        if(parametroTipoPedido == 2)
        {
            tvTipoPedido.setText("CONSUMIR NO LOCAL");
            loEndereco.setVisibility(View.GONE);
            loPagamento.setVisibility(View.GONE);
            tvPrazoPedido.setText(String.valueOf(parametros.getRetirada())  + " minutos");
        }
       //Preenchemos a observação
        if(TextUtils.isEmpty(etObservacao.getText().toString()))
        {
            tvObsPedido.setText("Nenhuma observação. :)");
        }
        else
        {
            tvObsPedido.setText(etObservacao.getText().toString());
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

        if(parametroCupomAplicado == 1)
        {
            Locale ptBr = new Locale("pt", "BR");
            String valorMostrar = NumberFormat.getCurrencyInstance(ptBr).format(valorDoPedidoComDesconto);
            tvTotalPedido.setText(valorMostrar);
        }
        else
        {
            Locale ptBr = new Locale("pt", "BR");
            String valorMostrar = NumberFormat.getCurrencyInstance(ptBr).format(valorTotalPedido);
            tvTotalPedido.setText(valorMostrar);
        }

        PushDownAnim.setPushDownAnimTo(btCancelar)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss();
                    }
                });

        PushDownAnim.setPushDownAnimTo(btConfirmar)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SimpleDateFormat dataFormatada = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        Date currentTime = new Date();
                        String dataAtual = dataFormatada.format(currentTime);

                        //Pedido é salvo na nuvem e impresso no restaurante.
                        Pedido novo = new Pedido();
                        //novo.setId(1);
                        //PREENCHEMOS O ENDEREÇO SE FOR ENTREGA - CASO NÃO SEJA, NÃO PREENCHEMOS
                        if(parametroTipoPedido == 0)
                        {
                            novo.setClt_nome(cliente.getNome());
                            novo.setClt_celular(cliente.getCelular());
                            novo.setClt_rua(cliente.getRua());
                            novo.setClt_numero(cliente.getNumero());
                            novo.setClt_bairro(cliente.getBairro());
                            novo.setClt_referencia(cliente.getReferencia());
                        }
                        else
                        {
                            novo.setClt_nome(nomeRetiraOuConsumo);
                            novo.setClt_celular(cliente.getCelular());
                            novo.setClt_rua("-");
                            novo.setClt_numero("-");
                            novo.setClt_bairro("-");
                            novo.setClt_referencia("-");
                        }
                        //O VALOR É PREENCHIDO DEPENDENDO SE EXISTE UM CUPOM APLICADO OU NÃO
                        if(parametroCupomAplicado == 1)
                        {
                            novo.setValor(valorDoPedidoComDesconto);
                        }
                        else
                        {
                            novo.setValor(valorTotalPedido);
                        }
                        //DATA ATUAL GERADA NO INICIO DO BLOCO - DIA-MES-ANO HORA:MINUTOS
                        novo.setData(dataAtual);
                        if(TextUtils.isEmpty(etObservacao.getText().toString()))
                        {
                            novo.setObservacao("-");
                        }
                        else
                        {
                            novo.setObservacao(etObservacao.getText().toString());
                        }
                        //DEFINIMOS O TIPO DE PAGAMENTO
                        if(parametroTipoPagamento == 0)
                            novo.setPagamento("DINHEIRO");
                        if(parametroTipoPagamento == 1)
                            novo.setPagamento("CARTÃO");
                        if(parametroTipoPagamento == 2)
                            novo.setPagamento("PIX");
                        //DEFINIMOS O TIPO DO PEDIDO
                        if(parametroTipoPedido == 0)
                        {
                            novo.setTipo("ENTREGA");
                        }
                        if(parametroTipoPedido == 1)
                        {
                            novo.setTipo("RETIRADA");
                            novo.setPagamento("-");
                        }
                        if(parametroTipoPedido == 2)
                        {
                            novo.setTipo("CONSUMIR NO LOCAL");
                            novo.setPagamento("-");
                        }
                        //DEFINIMOS O VALOR DO DESCONTO
                        if(parametroCupomAplicado == 1)
                        {
                            novo.setDesconto(valorDoDescontoNoPedido);
                        }
                        else
                        {
                            novo.setDesconto(0.0);
                        }
                        if(parametroTipoPedido == 0 && parametroPrecisaDeTroco == 1)
                        {
                            novo.setTroco(trocoFinal);
                        }
                        else
                        {
                            novo.setTroco("-");
                        }
                        //CRIAR IDENTIFICADOR DO FIREBASE PARA O PEDIDO
                        String chaveFinalFB;
                        String celularSoNumeros = cliente.getCelular().replace("(", "").replace(")","").replace(" ", "").replace("-", "");
                        String nomeSoLetras = cliente.getNome().replace("/", "-").replace("\\", "-").replace(" ", "");
                        chaveFinalFB = celularSoNumeros + "-" + nomeSoLetras;

                        novo.setId(chaveFinalFB);

                        //SALVAR O PEDIDO NO FIREBASE
                        DatabaseReference firebaseBanco = FirebaseDatabase.getInstance().getReference();
                        DatabaseReference pedidoDB = firebaseBanco.child("pedido");
                        pedidoDB.child(chaveFinalFB).setValue(novo);
                        //ADICIONAR OS ITENS DO PEDIDO NO PEDIDO
                        DatabaseReference pedidoDB1 = firebaseBanco.child("pedido-itens").child(chaveFinalFB);
                        int cont = 1; //CONTADOR PARA CRIAR O ID CORRETO DO ITEM NO PEDIDO
                        for(ItemPedido item: listaPedido)
                        {
                            String identificador = String.format("%05d", cont);

                            String ajusteAdicionais = item.getAdicionais_item().replace(" ", "").trim();

                            item.setAdicionais_item(ajusteAdicionais);

                            item.setId_pedido(chaveFinalFB);

                            pedidoDB1.child(identificador).setValue(item);
                            cont++;
                        }
                        //ADICIONAR PEDIDO NA TABELA PEDIDO_ANDAMENTO NO FIREBASE
                        Andamento adicionar = new Andamento();
                        adicionar.setId(chaveFinalFB);
                        adicionar.setStatus("EM PREPARACAO");
                        adicionar.setData(dataAtual);
                        adicionar.setCliente(novo.getClt_nome());
                        String valorAndamentoSalvar = tvTotalPedido.getText().toString();
                        adicionar.setValor(valorAndamentoSalvar);
                        if(novo.getTipo().equals("ENTREGA"))
                        {
                            adicionar.setEndereco(cliente.getRua() + " - " + cliente.getNumero() + " - " + cliente.getBairro() + " - " + cliente.getReferencia());
                            String enderecoPesquisa = cliente.getRua() + " " + cliente.getNumero() + " " + cliente.getBairro();
                            adicionar.setEnderecoPesquisa(enderecoPesquisa.toLowerCase());
                        }
                        else
                        {
                            adicionar.setEndereco("-");
                            adicionar.setEnderecoPesquisa("-");
                        }
                        DatabaseReference pedidoDB2 = firebaseBanco.child("andamento");
                        pedidoDB2.child(chaveFinalFB).setValue(adicionar);

                        DatabaseReference notificacaoDB = firebaseBanco.child("notificacao");
                        notificacaoDB.child(chaveFinalFB).setValue(0);

                        //VERIFICAR SE O CUPOM UNICO FOI APLICADO PARA ALTERAR ESTE CUPOM PARA USADO NESTE CLIENTE
                        if(parametroCupomAplicado == 1) {
                            for(CupomDesconto item : listaCuponsDesconto) {
                                if(item.getDescricao().equals(cupomAplicado))
                                {
                                    if(item.getCupomUnico() == 1)
                                    {
                                        DatabaseReference cupomUsadoDB = firebaseBanco.child("cupom-uso").child(identificadorUnicoParaCupom).child(item.getDescricao());
                                        cupomUsadoDB.setValue(1);
                                    }
                                }
                            }
                        }
                        //EXCLUIMOS OS ITENS DA TABELA DO CELULAR
                        bancoDados = TelaConfirmacaoPedido.this.openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
                        bancoDados.execSQL("DELETE FROM item_pedido");

                        SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)
                                .setText("Pedido Confirmado! :)")
                                .setDuration(Style.DURATION_SHORT)
                                .setColor(getResources().getColor(R.color.marrom))
                                .setAnimations(Style.ANIMATIONS_POP)
                                .show();
                        bottomSheetDialog.dismiss();

                        loTelaConfirmacaoPedido.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                Intent intent = new Intent(getApplicationContext(), TelaAcompanhamentoPedido.class);
                                intent.putExtra("dataPedido", novo.getData());
                                intent.putExtra("chavePedido", chaveFinalFB);
                                startActivity(intent);
                            }
                        });
                    }
                });
        bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        bottomSheetDialog.show();
    }

    private Parametros parametros = new Parametros();

    public void recuperaParametros()
    {
        DatabaseReference andamentoDB = firebaseBanco.child("parametro");
        andamentoDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                parametros = snapshot.getValue(Parametros.class);
                verificaFuncionamentoRestaurante();
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public int parametroAbertoOuFechado = 0;

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
            //FECHADO - ABRIMOS A TELA DE ESTABELECIMENTO FECHADO
            loTelaConfirmacaoPedido.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    startActivity(new Intent(getApplicationContext(), TelaEstabelecimentoFechado.class));
                }
            });
        }
        else
        {
            //ABERTO - SEGUIMOS A VIDA NORMALMENTE
        }

    }
}