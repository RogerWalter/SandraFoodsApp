package com.ssoft.sandrafoodsapp.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentStatePagerItemAdapter;
import com.squareup.picasso.Picasso;
import com.ssoft.sandrafoodsapp.R;
import com.ssoft.sandrafoodsapp.fragment.Cardapio;
import com.ssoft.sandrafoodsapp.fragment.Dados;
import com.ssoft.sandrafoodsapp.fragment.Pedido;
import com.ssoft.sandrafoodsapp.model.Aviso;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.thekhaeng.pushdownanim.PushDownAnim.MODE_STATIC_DP;

public class TelaPrincipal extends AppCompatActivity {

    DatabaseReference firebaseBanco = FirebaseDatabase.getInstance().getReference();
    DatabaseReference avisoDB;

    private SmartTabLayout smartTabLayout;
    private ViewPager viewPager;
    private ConstraintLayout loTelaPrincipal;
    private ImageButton btAviso;

    private ImageView ivDados, ivCardapio, ivPedido;

    private Aviso aviso = new Aviso();

    private String imagemAviso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_principal);
        getSupportActionBar().hide();

        loTelaPrincipal = findViewById(R.id.loTelaPrincipal);
        smartTabLayout = findViewById(R.id.viewPagerTabTelaPrincipal);
        viewPager = findViewById(R.id.viewPagerTelaPrincipal);

        ivDados = findViewById(R.id.ivPrincipalDados);
        ivCardapio = findViewById(R.id.ivPrincipalCardapio);
        ivPedido = findViewById(R.id.ivPrincipalPedido);

        btAviso = findViewById(R.id.btAviso);

        btAviso.setVisibility(View.GONE);

        loTelaPrincipal.setVisibility(View.INVISIBLE);

        verificaSeExisteAviso();

        //Configurar adapter para abas



        FragmentStatePagerItemAdapter adapter = new FragmentStatePagerItemAdapter(
                getSupportFragmentManager(),
                FragmentPagerItems.with(this)
                        .add("", Dados.class)
                        .add("", Cardapio.class)
                        .add("", Pedido.class)
                        .create()
        );

        viewPager.setAdapter(adapter);
        smartTabLayout.setViewPager(viewPager);
        //viewPager.set(getResources().getColor(R.color.laranja));
        viewPager.setCurrentItem(1);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //recriar fragments para recarregar dados
            }

            @Override
            public void onPageSelected(int position) {
                if(position == 0)//DADOS
                {
                    ivDados.setImageResource(R.drawable.dados_ativo);
                    ivCardapio.setImageResource(R.drawable.cardapio_inativo);
                    ivPedido.setImageResource(R.drawable.pedido_inativo);

                    /*Dados fragmentDados = new Dados();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.add(R.id.layoutDados, fragmentDados).commit();
                    fragmentDados.recuperaDados();*/
                }
                if(position == 1)//CARDAPIO
                {
                    ivDados.setImageResource(R.drawable.dados_inativo);
                    ivCardapio.setImageResource(R.drawable.cardapio_ativo);
                    ivPedido.setImageResource(R.drawable.pedido_inativo);
                }
                if(position == 2)//PEDIDO
                {
                    ivDados.setImageResource(R.drawable.dados_inativo);
                    ivCardapio.setImageResource(R.drawable.cardapio_inativo);
                    ivPedido.setImageResource(R.drawable.pedido_ativo);

                    Pedido fragmentPedido = new Pedido();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_pedido, fragmentPedido).commitNow();
                    fragmentPedido.recuperaParametrosAppFB();
                    fragmentPedido.recuperarPedido();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        PushDownAnim.setPushDownAnimTo(btAviso)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        gerarDialogoAviso();
                    }
                });


        loTelaPrincipal.animate().translationY(-250).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                loTelaPrincipal.setVisibility(View.VISIBLE);
                loTelaPrincipal.animate().translationY(0).alpha(1.0f).setDuration(250);
            }
        });
    }

    public String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

    public void gerarDialogoAviso()
    {
        int unicode = 0x1F604;

        final Dialog dialog = new Dialog(TelaPrincipal.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.dialogo_aviso);

        TextView titulo = (TextView) dialog.findViewById(R.id.tvDialogoAvisoTitulo);
        ImageView aviso = (ImageView) dialog.findViewById(R.id.ivAviso);
        ImageView btFechar = (ImageButton) dialog.findViewById(R.id.btDialogoAvisoFechar);

        String emoji = getEmojiByUnicode(unicode);

        titulo.setText("Recado do Sandra Foods " + emoji);

        PushDownAnim.setPushDownAnimTo(btFechar)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
        //AQUI DEVEMOS SETAR A IMAGEM RECUPERADA DO FIREBASE
        Picasso.get().load(imagemAviso)
                .fit()
                .centerInside()
                .error(R.drawable.ic_erro_sem_dados)
                .placeholder(R.drawable.ic_baseline_hourglass_top_24)
                .into(aviso);

        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
    }



    public  void verificaSeExisteAviso()
    {
        int parametro = verificaPrimeiroAcesso();
        if(parametro != 0)
        {
            DatabaseReference avisoDB = firebaseBanco.child("aviso");
            avisoDB.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    aviso = snapshot.getValue(Aviso.class);

                    Calendar calendar = Calendar.getInstance();
                    final SimpleDateFormat formatoHora = new SimpleDateFormat("dd/MM/yyyy");
                    String hora = formatoHora.format(new Date());
                    Date dataAtual = null, dataAviso = null;
                    try {
                        dataAtual = formatoHora.parse(hora);
                        dataAviso = formatoHora.parse(aviso.getData());
                    }catch(ParseException e){
                        String novo = "erro";
                    }
                    if((dataAtual.before(dataAviso) || dataAtual.equals(dataAviso)) && aviso.getAviso() == 1)
                    {
                        //AVISO ATIVO. CONFIGURAMOS A IMAGEM DO AVISO
                        imagemAviso = aviso.getImagem();
                        btAviso.setVisibility(View.VISIBLE);
                        gerarDialogoAviso();
                    }
                }
                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }
    }

    public void onHiddenFirstShowcase() {
        viewPager.setCurrentItem(2);
        Pedido fragmentPedido = new Pedido();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_pedido, fragmentPedido).commitNow();
        fragmentPedido.gerarTutorialPedidoItens();
    }

    public void onHiddenSecondShowcase() {
        viewPager.setCurrentItem(0);
        Dados fragmentDados = new Dados();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.layoutDados, fragmentDados).commitNow();
        fragmentDados.gerarTutorialDados();
    }

    public void onHiddenThirdShowcase() {
        int unicode = 0x1F604;
        String emoji = getEmojiByUnicode(unicode);
        viewPager.setCurrentItem(1);
        SuperActivityToast.create(this, new Style(), Style.TYPE_STANDARD)
                .setText("Tudo pronto para começar!" + emoji)
                .setDuration(Style.DURATION_SHORT)
                .setColor(getResources().getColor(R.color.laranja))
                .setAnimations(Style.ANIMATIONS_SCALE)
                .setTextSize(Style.TEXTSIZE_LARGE)
                .show();
        atualizarPrimeiroAcesso();
    }

    public void atualizarPrimeiroAcesso()
    {
        SQLiteDatabase bancoDados;
        try{
            bancoDados = this.openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
            bancoDados.execSQL("UPDATE primeiroacesso SET parametro = 1");
        }
        catch(Exception e){
            String erro = e.toString();
            Log.i("ERROBANCO", erro);
        }
    }

    public int verificaPrimeiroAcesso()
    {
        int parametro = -1;
        SQLiteDatabase bancoDados;
        bancoDados = this.openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
        //Recuperar pessoas
        try {
            Cursor cursor = bancoDados.rawQuery("SELECT parametro FROM primeiroacesso", null);

            //Recupera indices
            int indicePar = cursor.getColumnIndex("parametro");

            if(cursor.moveToFirst()){
                do {
                    parametro = cursor.getInt(indicePar);
                } while (cursor.moveToNext());
            };
        }catch(Exception e)
        {
            Log.e("ERRO", e.toString());
        }
        return parametro;
    }

    @Override
    public void onBackPressed() {
        viewPager.setCurrentItem(1);
    }


}