package com.ssoft.sandrafoodsapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;

import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.ssoft.sandrafoodsapp.R;
import com.ssoft.sandrafoodsapp.adapter.AdapterCardapio;
import com.ssoft.sandrafoodsapp.helper.RecyclerItemClickListener;
import com.ssoft.sandrafoodsapp.model.ItemCardapio;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.thekhaeng.pushdownanim.PushDownAnim.MODE_STATIC_DP;

public class TelaEstabelecimentoFechado extends AppCompatActivity {

    DatabaseReference firebaseBanco = FirebaseDatabase.getInstance().getReference();
    DatabaseReference cardapioDB;

    private Button btInfo, btCardapio;
    private ConstraintLayout loBt, loCardapio, loPrincipal;
    private ImageView ivSF, ivFechado;
    private RecyclerView recyclerView;

    private List<ItemCardapio> listaCardapio = new ArrayList<>();
    private AdapterCardapio adapter = new AdapterCardapio(listaCardapio);

    private String fraseToast;
    private int parametroBotao = 0; //usado para altera as telas entre fechado e card??pio

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_estabelecimento_fechado);
        getSupportActionBar().hide();

        cardapioDB = firebaseBanco.child("cardapio");
        recuperaCardapio();

        btInfo = findViewById(R.id.btTelaFechadoInfo);
        btCardapio = findViewById(R.id.btTelaFechadoCardapio);
        loBt = findViewById(R.id.loTelaFechadoBotoes);
        loCardapio = findViewById(R.id.loTelaFechadoCardapio);
        loPrincipal = findViewById(R.id.loTelaEstabelecimentoFechado);
        ivSF = findViewById(R.id.ivTelaFechadoLogo);
        ivFechado = findViewById(R.id.ivTelaFechadoFechado);

        loCardapio.setVisibility(View.INVISIBLE);

        recyclerView = findViewById(R.id.rvTelaFechadoCardapio);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(TelaEstabelecimentoFechado.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        TelaEstabelecimentoFechado.this,
                        recyclerView,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                gerarFraseToast();
                                SuperActivityToast.create(TelaEstabelecimentoFechado.this, new Style(), Style.TYPE_STANDARD)
                                        .setText(fraseToast)
                                        .setDuration(Style.DURATION_MEDIUM)
                                        .setColor(getResources().getColor(R.color.marrom))
                                        .setAnimations(Style.ANIMATIONS_POP)
                                        .show();
                            }
                            @Override
                            public void onLongItemClick(View view, int position) {

                            }
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        })
        );

        PushDownAnim.setPushDownAnimTo(btInfo)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        gerarDialogoApresentarInformacoes();
                    }
                });
        PushDownAnim.setPushDownAnimTo(btCardapio)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(parametroBotao == 0)
                            alterarParaCardapio();
                        else
                            alterarParaFechado();
                    }
                });
        loPrincipal.setVisibility(View.INVISIBLE);
        loPrincipal.animate().translationY(-250).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                loPrincipal.setVisibility(View.VISIBLE);
                loPrincipal.animate().translationY(0).alpha(1.0f).setDuration(250);
            }
        });
    }

    public void gerarDialogoApresentarInformacoes()
    {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(TelaEstabelecimentoFechado.this);
        bottomSheetDialog.setContentView(R.layout.dialogo_telafechado_mostrar_infos);

        Button btCancelar = bottomSheetDialog.findViewById(R.id.btDialogoTelaFechadoFechar);

        PushDownAnim.setPushDownAnimTo(btCancelar)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss();
                    }
                });

        bottomSheetDialog.show();
    }

    public void alterarParaCardapio()
    {
        loPrincipal.animate().translationY(-250).alpha(0.0f).setDuration(250).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ivSF.setVisibility(View.GONE);
                ivFechado.setVisibility(View.GONE);
                loCardapio.setVisibility(View.VISIBLE);
                btCardapio.setText("Fechar");
                loPrincipal.animate().translationY(0).alpha(1.0f).setDuration(250);
            }
        });
        parametroBotao = 1;
    }

    public void alterarParaFechado()
    {
        loPrincipal.animate().translationY(-250).alpha(0.0f).setDuration(250).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                loCardapio.setVisibility(View.GONE);
                ivSF.setVisibility(View.VISIBLE);
                ivFechado.setVisibility(View.VISIBLE);
                btCardapio.setText("Card??pio");
                loPrincipal.animate().translationY(0).alpha(1.0f).setDuration(250);
            }
        });
        parametroBotao = 0;
    }

    public void gerarFraseToast()
    {
        String[] frases = {
                "Em breve estaremos abertos para voc?? provar essa del??cia :D",
                "Um alimento bem feito, com amor deve ser feito! Logo abriremos! :D",
                "Ainda n??o abrimos, mas certamente voc?? vai gostar desse :D",
                "Se o nome disso j?? d?? fome, imagine o cheiro. Estamos quase abertos! :D",
                "Esse a?? ?? muito bom. Como ele todo dia. Logo abriremos para lhe fazer um! :D",
                "Pense numa coisa boa, ?? esse a??. S?? falta a gente abrir pra voc?? poder provar um. :D",
                "Falando s??rio: se eu pudesse, s?? ia comer isso. Mas s?? quando n??s abrir. :D",
                "Me lembro como se fosse ontem quando comi um desse. E tava bom demais! Logo abrimos! :D",
                "Esse a?? ?? tudo que eu mais queria nesse momento, mas n??o abrimos ainda. Logo logo hein! :D",
                "Esse ?? t??o bom que se fosse poss??vel, eu comeria pelo resto da minha vida s?? isso. Isso quando abrirmos, ?? claro :D"};
        int x = new Random().nextInt(10);
        fraseToast = frases[x];
    }

    public void recuperaCardapio()
    {
        DatabaseReference cardapioDB = firebaseBanco.child("cardapio");
        cardapioDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                listaCardapio.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    if(ds.getValue(ItemCardapio.class).getGrupo() != 0) //se for 0, ?? adicional
                    {
                        listaCardapio.add(ds.getValue(ItemCardapio.class));
                    }
                }
                for (ItemCardapio e : listaCardapio)
                {
                    String nomeAjustado = e.getNome().trim();
                    String descAjustado = e.getDescricao().trim();
                    e.setNome(nomeAjustado);
                    e.setDescricao(descAjustado);
                }
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), TelaEstabelecimentoFechado.class);
        startActivity(intent);
    }
}