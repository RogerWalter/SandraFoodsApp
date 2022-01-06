package com.ssoft.sandrafoodsapp.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
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
import com.ssoft.sandrafoodsapp.activity.TelaPrincipal;
import com.ssoft.sandrafoodsapp.adapter.AdapterCardapio;
import com.ssoft.sandrafoodsapp.helper.RecyclerItemClickListener;
import com.ssoft.sandrafoodsapp.model.ItemCardapio;
import com.ssoft.sandrafoodsapp.model.ItemPedido;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;
import static com.thekhaeng.pushdownanim.PushDownAnim.MODE_STATIC_DP;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Cardapio#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Cardapio extends Fragment {

    private SQLiteDatabase bancoDados;

    DatabaseReference firebaseBanco = FirebaseDatabase.getInstance().getReference();
    DatabaseReference cardapioDB;

    public int idItem = 0;

    private ProgressBar progressBar;
    private ImageView semDados;

    private RecyclerView recyclerView;
    private List<ItemCardapio> listaAdicionais = new ArrayList<>();
    private List<ItemCardapio> listaCardapio = new ArrayList<>();
    private ArrayList<ItemCardapio> listaFiltrada = new ArrayList<>();
    private ArrayList<ItemCardapio> listaFiltradaTipo = new ArrayList<>();
    private AdapterCardapio adapter = new AdapterCardapio(listaCardapio);
    private ItemCardapio itemSelecionado = new ItemCardapio();

    //btf = botão filtro
    private ImageButton btfLanche, btfCrepe, btfTapioca, btfPastel, btfPorcao, btfBebida, btfOutro, btfSalgado, btfDoce, btMostrarFiltros;
    private ConstraintLayout layoutFiltros;
    private TextView tituloCardapio;
    //usado pra saber se o botao já foi pressionado - 0 = não || 1 = sim
    public int parametroLanche = 0, parametroCrepe = 0, parametroTapioca = 0, parametroPastel = 0, parametroPorcao = 0, parametroBebida = 0, parametroOutro = 0, parametroSalgado = 0, parametroDoce = 0;
    public int grupoSelecionado = 0; // 0 - não || 1 - sim
    public int parametroQualListaRV = 0; // 0-listaCardapio | 1-listaFiltrada | 2-listaGrupo
    public int parametroFiltro = 0;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Cardapio() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Cardapio.
     */
    // TODO: Rename and change types and number of parameters
    public static Cardapio newInstance(String param1, String param2) {
        Cardapio fragment = new Cardapio();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recuperaCardapio();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cardapio, container, false);

        cardapioDB = firebaseBanco.child("cardapio");

        bancoDados = getActivity().openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);

        String secao1 = "HAMBÚRGUERES";
        String secao2 = "CREPES";
        String secao3 = "TAPIOCAS";
        String secao4 = "PASTÉIS";
        String secao5 = "PORÇÕES";
        String secao6 = "BEBIDAS";
        String secao7 = "OUTROS";

        btfLanche = view.findViewById(R.id.ibFiltroLanche);
        btfCrepe = view.findViewById(R.id.ibFiltroCrepe);
        btfTapioca = view.findViewById(R.id.ibFiltroTapioca);
        btfPastel = view.findViewById(R.id.ibFiltroPastel);
        btfPorcao = view.findViewById(R.id.ibFiltroPorcao);
        btfBebida = view.findViewById(R.id.ibFiltroBebida);
        btfOutro = view.findViewById(R.id.ibFiltroOutros);
        btfSalgado = view.findViewById(R.id.ibFiltroSalgado);
        btfDoce = view.findViewById(R.id.ibFiltroDoce);
        btMostrarFiltros = view.findViewById(R.id.btCardapioMostrarFiltros);
        layoutFiltros = view.findViewById(R.id.loCardapioFiltros);
        tituloCardapio = view.findViewById(R.id.tvCardapioTitulo);

        progressBar = view.findViewById(R.id.progressBarCardápio);
        semDados = view.findViewById(R.id.ivErroCardapio);
        recyclerView = view.findViewById(R.id.rvFragmentCardapio);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        //recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayout.VERTICAL));
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getContext(),
                        recyclerView,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                ItemCardapio item;
                                if(parametroQualListaRV == 0) //nenhum filtro aplicado
                                {
                                    item = listaCardapio.get(position);
                                    itemSelecionado = item;
                                }
                                if(parametroQualListaRV == 1) //filtro de grupo
                                {
                                    item = listaFiltrada.get(position);
                                    itemSelecionado = item;
                                }
                                if(parametroQualListaRV == 2) //filtro de tipo
                                {
                                    item = listaFiltradaTipo.get(position);
                                    itemSelecionado = item;
                                }
                                mostrarBSAddItem();
                            }
                            @Override
                            public void onLongItemClick(View view, int position) {

                            }
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        })
        );

        //configuracao dos filtros
        //btSalgado
        PushDownAnim.setPushDownAnimTo(btfSalgado)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(parametroBebida == 1 || parametroLanche == 1 || parametroPorcao == 1)
                        {
                            return;
                        }

                        if(parametroSalgado == 0)
                        {
                            parametroSalgado = 1;
                            parametroDoce = 0;
                            if(grupoSelecionado == 0)
                            {
                                parametroQualListaRV = 2;
                                inativaBotoesTipo();
                                btfSalgado.setImageResource(R.drawable.ic_icone_filtro_salgado_ativo);
                                filtroTipo(0, 1); //parametro - 0 = nenhum grupo selecionado | 1 - possui grupo selecionado
                            }
                            else
                            {
                                parametroQualListaRV = 2;
                                inativaBotoesTipo();
                                btfSalgado.setImageResource(R.drawable.ic_icone_filtro_salgado_ativo);
                                filtroTipo(1, 1); //parametro - 0 = nenhum grupo selecionado | 1 - possui grupo selecionado
                            }

                        }
                        else
                        {
                            parametroSalgado = 0;
                            parametroDoce = 0;
                            if(grupoSelecionado == 0)
                            {
                                parametroQualListaRV = 0;
                                inativaBotoesTipo();
                                btfSalgado.setImageResource(R.drawable.ic_icone_filtro_salgado_inativo);
                                adapter = new AdapterCardapio(listaCardapio);
                                adapter.notifyDataSetChanged();
                                recyclerView.setAdapter(adapter);
                            }
                            else
                            {
                                parametroQualListaRV = 1;
                                inativaBotoesTipo();
                                btfSalgado.setImageResource(R.drawable.ic_icone_filtro_salgado_inativo);
                                adapter = new AdapterCardapio(listaFiltrada);
                                adapter.notifyDataSetChanged();
                                recyclerView.setAdapter(adapter);
                            }
                        }
                    }
                });
        //btSalgado
        PushDownAnim.setPushDownAnimTo(btfDoce)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(parametroBebida == 1 || parametroLanche == 1 || parametroPorcao == 1)
                        {
                            return;
                        }
                        if(parametroDoce == 0)
                        {
                            parametroQualListaRV = 2;
                            parametroDoce = 1;
                            parametroSalgado = 0;
                            if(grupoSelecionado == 0)
                            {
                                parametroQualListaRV = 2;
                                inativaBotoesTipo();
                                btfDoce.setImageResource(R.drawable.ic_icone_filtro_doce_ativo);
                                filtroTipo(0, 2); //parametro - 0 = nenhum grupo selecionado | 1 - possui grupo selecionado
                            }
                            else
                            {
                                parametroQualListaRV = 2;
                                inativaBotoesTipo();
                                btfDoce.setImageResource(R.drawable.ic_icone_filtro_doce_ativo);
                                filtroTipo(1, 2); //parametro - 0 = nenhum grupo selecionado | 1 - possui grupo selecionado
                            }

                        }
                        else
                        {
                            parametroQualListaRV = 0;
                            parametroDoce = 0;
                            parametroSalgado = 0;
                            if(grupoSelecionado == 0)
                            {
                                parametroQualListaRV = 0;
                                inativaBotoesTipo();
                                btfDoce.setImageResource(R.drawable.ic_icone_filtro_doce_inativo);
                                adapter = new AdapterCardapio(listaCardapio);
                                adapter.notifyDataSetChanged();
                                recyclerView.setAdapter(adapter);
                            }
                            else
                            {
                                parametroQualListaRV = 1;
                                inativaBotoesTipo();
                                btfDoce.setImageResource(R.drawable.ic_icone_filtro_doce_inativo);
                                adapter = new AdapterCardapio(listaFiltrada);
                                adapter.notifyDataSetChanged();
                                recyclerView.setAdapter(adapter);
                            }
                        }
                    }
                });

        //btLanche
        PushDownAnim.setPushDownAnimTo(btfLanche)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        inativaBotoesTipo();
                        parametroSalgado = 0;
                        parametroDoce = 0;
                        if(parametroLanche == 0)
                        {
                            parametroQualListaRV = 1;
                            grupoSelecionado = 1;
                            zeraParametros();
                            parametroLanche = 1;
                            inativaBotoes();
                            btfLanche.setImageResource(R.drawable.ic_icone_filtro_lanches_ativo);
                            filtro(1);
                        }
                        else
                        {
                            parametroQualListaRV = 0;
                            grupoSelecionado = 0;
                            zeraParametros();
                            parametroLanche = 0;
                            btfLanche.setImageResource(R.drawable.ic_icone_filtro_lanches_inativo);
                            limpaFiltros();
                        }
                    }
                });
        //btCrepes
        PushDownAnim.setPushDownAnimTo(btfCrepe)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        inativaBotoesTipo();
                        parametroSalgado = 0;
                        parametroDoce = 0;
                        if(parametroCrepe == 0)
                        {
                            parametroQualListaRV = 1;
                            grupoSelecionado = 1;
                            zeraParametros();
                            parametroCrepe = 1;
                            inativaBotoes();
                            btfCrepe.setImageResource(R.drawable.ic_icone_filtro_crepes_ativo);
                            filtro(2);
                        }
                        else
                        {
                            parametroQualListaRV = 0;
                            grupoSelecionado = 0;
                            zeraParametros();
                            parametroCrepe = 0;
                            btfCrepe.setImageResource(R.drawable.ic_icone_filtro_crepes_inativo);
                            limpaFiltros();
                        }
                    }
                });
        //btTapioca
        PushDownAnim.setPushDownAnimTo(btfTapioca)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        inativaBotoesTipo();
                        parametroSalgado = 0;
                        parametroDoce = 0;
                        if(parametroTapioca == 0)
                        {
                            parametroQualListaRV = 1;
                            grupoSelecionado = 1;
                            zeraParametros();
                            parametroTapioca = 1;
                            inativaBotoes();
                            btfTapioca.setImageResource(R.drawable.ic_icone_filtro_tapiocas_ativo);
                            filtro(3);
                        }
                        else
                        {
                            parametroQualListaRV = 0;
                            grupoSelecionado = 0;
                            zeraParametros();
                            parametroTapioca = 0;
                            btfTapioca.setImageResource(R.drawable.ic_icone_filtro_tapiocas_inativo);
                            limpaFiltros();
                        }
                    }
                });
        //btPastel
        PushDownAnim.setPushDownAnimTo(btfPastel)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        inativaBotoesTipo();
                        parametroSalgado = 0;
                        parametroDoce = 0;
                        if(parametroPastel == 0)
                        {
                            parametroQualListaRV = 1;
                            grupoSelecionado = 1;
                            zeraParametros();
                            parametroPastel = 1;
                            inativaBotoes();
                            btfPastel.setImageResource(R.drawable.ic_icone_filtro_pasteis_ativo);
                            filtro(4);
                        }
                        else
                        {
                            parametroQualListaRV = 0;
                            grupoSelecionado = 0;
                            zeraParametros();
                            parametroPastel = 0;
                            btfPastel.setImageResource(R.drawable.ic_icone_filtro_pasteis_inativo);
                            limpaFiltros();
                        }
                    }
                });
        //btPorcao
        PushDownAnim.setPushDownAnimTo(btfPorcao)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        inativaBotoesTipo();
                        parametroSalgado = 0;
                        parametroDoce = 0;
                        if(parametroPorcao == 0)
                        {
                            parametroQualListaRV = 1;
                            grupoSelecionado = 1;
                            zeraParametros();
                            parametroPorcao = 1;
                            inativaBotoes();
                            btfPorcao.setImageResource(R.drawable.ic_icone_filtro_porcoes_ativo);
                            filtro(5);
                        }
                        else
                        {
                            parametroQualListaRV = 0;
                            grupoSelecionado = 0;
                            zeraParametros();
                            parametroPorcao = 0;
                            btfPorcao.setImageResource(R.drawable.ic_icone_filtro_porcoes_inativo);
                            limpaFiltros();
                        }
                    }
                });
        //btBebida
        PushDownAnim.setPushDownAnimTo(btfBebida)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        inativaBotoesTipo();
                        parametroSalgado = 0;
                        parametroDoce = 0;
                        if(parametroBebida == 0)
                        {
                            parametroQualListaRV = 1;
                            grupoSelecionado = 1;
                            zeraParametros();
                            parametroBebida = 1;
                            inativaBotoes();
                            btfBebida.setImageResource(R.drawable.ic_icone_filtro_bebidas_ativo);
                            filtro(6);
                        }
                        else
                        {
                            parametroQualListaRV = 0;
                            grupoSelecionado = 0;
                            zeraParametros();
                            parametroBebida = 0;
                            btfBebida.setImageResource(R.drawable.ic_icone_filtro_bebidas_inativo);
                            limpaFiltros();
                        }
                    }
                });
        //btOutro
        PushDownAnim.setPushDownAnimTo(btfOutro)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        inativaBotoesTipo();
                        parametroSalgado = 0;
                        parametroDoce = 0;
                        if(parametroOutro == 0)
                        {
                            parametroQualListaRV = 1;
                            grupoSelecionado = 1;
                            zeraParametros();
                            parametroOutro = 1;
                            inativaBotoes();
                            btfOutro.setImageResource(R.drawable.ic_icone_filtro_outros_ativo);
                            filtro(7);
                        }
                        else
                        {
                            parametroQualListaRV = 0;
                            grupoSelecionado = 0;
                            zeraParametros();
                            parametroOutro = 0;
                            btfOutro.setImageResource(R.drawable.ic_icone_filtro_outros_inativo);
                            limpaFiltros();
                        }
                    }
                });

        PushDownAnim.setPushDownAnimTo(btMostrarFiltros)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(parametroFiltro == 0)
                        {
                            parametroQualListaRV = 0;
                            grupoSelecionado = 0;
                            zeraParametros();
                            parametroOutro = 0;
                            inativaBotoes();
                            inativaBotoesTipo();
                            limpaFiltros();
                            parametroFiltro = 1;
                            layoutFiltros.animate().alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    layoutFiltros.setVisibility(View.VISIBLE);
                                    layoutFiltros.animate().alpha(1.0f).setDuration(200);

                                }
                            });
                        }
                        else
                        {
                            parametroQualListaRV = 0;
                            grupoSelecionado = 0;
                            zeraParametros();
                            parametroOutro = 0;
                            inativaBotoes();
                            inativaBotoesTipo();
                            limpaFiltros();
                            parametroFiltro = 0;
                            layoutFiltros.animate().alpha(1.0f);
                            layoutFiltros.animate().alpha(0.0f).setDuration(200).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    layoutFiltros.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                });

        layoutFiltros.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        semDados.setVisibility(View.GONE);
        verificaPrimeiroAcesso();
        return view;
    }

    public void recuperaCardapio()
    {
        DatabaseReference cardapioDB = firebaseBanco.child("cardapio");
        cardapioDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                listaCardapio.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    if(ds.getValue(ItemCardapio.class).getGrupo() != 0) //se for 0, é adicional
                    {
                        listaCardapio.add(ds.getValue(ItemCardapio.class));
                    }
                    else
                    {
                        listaAdicionais.add(ds.getValue(ItemCardapio.class));
                    }
                }
                for (ItemCardapio e : listaCardapio)
                {
                    String nomeAjustado = e.getNome().trim();
                    String descAjustado = e.getDescricao().trim();
                    e.setNome(nomeAjustado);
                    e.setDescricao(descAjustado);
                }
                for (ItemCardapio e : listaAdicionais)
                {
                    String nomeAjustado = e.getNome().trim();
                    String descAjustado = e.getDescricao().trim();
                    e.setNome(nomeAjustado);
                    e.setDescricao(descAjustado);
                }
                if (listaCardapio.size() > 0) {
                    Collections.sort(listaCardapio, new Comparator<ItemCardapio>() {
                        @Override
                        public int compare(ItemCardapio t1, ItemCardapio t2) {
                            return Integer.valueOf(t1.getGrupo()).compareTo(Integer.valueOf(t2.getGrupo()));
                        }
                    });
                }
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                recyclerView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                semDados.setVisibility(View.GONE);
                if(listaCardapio.size() == 0)
                    semDados.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public void inativaBotoes()
    {
        btfLanche.setImageResource(R.drawable.ic_icone_filtro_lanches_inativo);
        btfCrepe.setImageResource(R.drawable.ic_icone_filtro_crepes_inativo);
        btfTapioca.setImageResource(R.drawable.ic_icone_filtro_tapiocas_inativo);
        btfPastel.setImageResource(R.drawable.ic_icone_filtro_pasteis_inativo);
        btfPorcao.setImageResource(R.drawable.ic_icone_filtro_porcoes_inativo);
        btfBebida.setImageResource(R.drawable.ic_icone_filtro_bebidas_inativo);
        btfOutro.setImageResource(R.drawable.ic_icone_filtro_outros_inativo);
        //btfSalgado.setImageResource(R.drawable.ic_icone_filtro_salgado_inativo);
        //btfDoce.setImageResource(R.drawable.ic_icone_filtro_doce_inativo);
    }
    public void inativaBotoesTipo()
    {
        btfSalgado.setImageResource(R.drawable.ic_icone_filtro_salgado_inativo);
        btfDoce.setImageResource(R.drawable.ic_icone_filtro_doce_inativo);
    }

    public void zeraParametros()
    {
        parametroLanche = 0;
        parametroCrepe = 0;
        parametroTapioca = 0;
        parametroPastel = 0;
        parametroPorcao = 0;
        parametroBebida = 0;
        parametroOutro = 0;
    }
    public void zeraParametrosTipo()
    {
        parametroSalgado = 0;
        parametroDoce = 0;
    }

    public void limpaFiltros()
    {
        listaFiltrada.clear();
        adapter = new AdapterCardapio(listaCardapio);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }
    public void limpaFiltrosTipo()
    {
        listaFiltrada.clear();
        adapter = new AdapterCardapio(listaCardapio);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    public void filtro(int grupo)
    {
        listaFiltrada.clear();
        for(ItemCardapio item : listaCardapio)
        {
            if(item.getGrupo() == grupo)
            {
                listaFiltrada.add(item);
            }
        }
        adapter.listaComFiltro(listaFiltrada);
        if(listaFiltrada.size() == 0)
        {
            semDados.setVisibility(View.VISIBLE);
        }
        else
        {
            semDados.setVisibility(View.GONE);
        }
    }

    public void filtroTipo(int parametro, int tipo)
    {
        listaFiltradaTipo.clear();
        if(parametro == 0) //nenhum grupo selecionado -> criamos filtro de tipo a partir da listaCardapio
        {
            for(ItemCardapio item : listaCardapio)
            {
                if(item.getTipo() == tipo)
                {
                    listaFiltradaTipo.add(item);
                }
            }
            adapter.listaComFiltro(listaFiltradaTipo);
            if(listaFiltradaTipo.size() == 0)
            {
                semDados.setVisibility(View.VISIBLE);
            }
            else
            {
                semDados.setVisibility(View.GONE);
            }
        }
        if(parametro == 1) //existe um grupo selecionado -> criamos um filtro de tipo a partir da lista filtrada
        {
            for(ItemCardapio item : listaFiltrada)
            {
                if(item.getTipo() == tipo)
                {
                    listaFiltradaTipo.add(item);
                }
            }
            adapter.listaComFiltro(listaFiltradaTipo);
            if(listaFiltradaTipo.size() == 0)
            {
                semDados.setVisibility(View.VISIBLE);
            }
            else
            {
                semDados.setVisibility(View.GONE);
            }
        }
    }

    public int qtdAtual = 0;
    public Double totalFinalDoItem = 0.00;
    public Double totalFinalDoItemVezesQtd = 0.00;
    public String adicionaisFinal;

    private void mostrarBSAddItem() {

        List<ItemCardapio> listaAuxiliarCheckBox = new ArrayList<>();
        ItemPedido itemPedidoAdd = new ItemPedido();
        totalFinalDoItem = 0.00;
        totalFinalDoItemVezesQtd = 0.00;
        adicionaisFinal = "";

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(R.layout.dialogo_adicionar_item_ao_pedido);

        TextView tituloBottomSheet = bottomSheetDialog.findViewById(R.id.tvTituloBSAddItemPedido);
        TextView descItemBottomSheet = bottomSheetDialog.findViewById(R.id.tvTituloBSAddItemDescItem);
        TextView infoItemBottomSheet = bottomSheetDialog.findViewById(R.id.tvTituloBSAddItemInfoItem);
        TextView quantidadeBottomSheet = bottomSheetDialog.findViewById(R.id.tvDialogoAddItemQuantidade);
        ImageButton btAddQtd = bottomSheetDialog.findViewById(R.id.ibDialogoAddItemAumentarQtd);
        TextView qtdFinal = bottomSheetDialog.findViewById(R.id.tvDialogoAddItemQuantidadeMostrar);
        ImageButton btRemQtd = bottomSheetDialog.findViewById(R.id.ibDialogoAddItemDiminuirQtd);
        TextView adicionaisBottomSheet = bottomSheetDialog.findViewById(R.id.tvDialogoAddItemAdicionais);
        LinearLayout layoutCheckBox1 = bottomSheetDialog.findViewById(R.id.linearLayoutAdicionais1);
        LinearLayout layoutCheckBox2 = bottomSheetDialog.findViewById(R.id.linearLayoutAdicionais2);
        ConstraintLayout layoutAdicionais = bottomSheetDialog.findViewById(R.id.constraintLayout3);
        TextView obsevacoesBottomSheet = bottomSheetDialog.findViewById(R.id.tvDescricaoObservacoes);
        EditText etObservacoesBottomSheet = bottomSheetDialog.findViewById(R.id.etDialogoAddItemObservacao);
        TextView totalizadorBottomSheet = bottomSheetDialog.findViewById(R.id.tvTotalizadorItemDialogoBS);
        TextView totalFinal = bottomSheetDialog.findViewById(R.id.tvTotalizadorItemDialogoTotal);
        Button btAdicionar = bottomSheetDialog.findViewById(R.id.btAddItemPedidoSalvar);
        Button btCancelar = bottomSheetDialog.findViewById(R.id.btAddItemPedidoCancelar);

        layoutAdicionais.setVisibility(View.GONE);
        if(itemSelecionado.getGrupo() == 1 && itemSelecionado.getTipo() == 1) //adicionais de lanches
        {
            etObservacoesBottomSheet.setHint("Ex.: quero sem cebola meus lindão.");

            listaAuxiliarCheckBox.clear();
            layoutAdicionais.setVisibility(View.VISIBLE);
            int parametroQualLayout = 0;
            for(int i = 0; i < listaAdicionais.size(); i++) {
                ItemCardapio adicional = listaAdicionais.get(i);
                if(adicional.getTipo() == 9)
                {
                    listaAuxiliarCheckBox.add(adicional);
                    //CheckBox cb = new CheckBox(getContext());
                    CheckBox cb = new CheckBox(new ContextThemeWrapper(getContext(), R.style.MyCheckboxStyle));;
                    cb.setText(adicional.getNome());
                    if(parametroQualLayout == 0)
                    {
                        parametroQualLayout = 1;
                        layoutCheckBox1.addView(cb);
                    }
                    else
                    {
                        parametroQualLayout = 0;
                        layoutCheckBox2.addView(cb);
                    }
                    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            int indiceDoAdicional = -1;
                            String nomeAdicional = cb.getText().toString();
                            ItemCardapio adicionalSelecionado = new ItemCardapio();
                            for(ItemCardapio item : listaAuxiliarCheckBox)
                            {
                                if(item.getDescricao().toLowerCase().contains(nomeAdicional.toLowerCase()))
                                {
                                    adicionalSelecionado = item;
                                }
                            }
                            Double valorDoAdicional;
                            if(cb.isChecked())
                            {
                                cb.setTextColor(getResources().getColor(R.color.laranja));
                                valorDoAdicional = adicionalSelecionado.getValor();
                                totalFinalDoItem = totalFinalDoItem + valorDoAdicional;
                                adicionaisFinal = adicionaisFinal.trim() + " +" + adicionalSelecionado.getNome().trim();
                            }
                            else
                            {
                                cb.setTextColor(getResources().getColor(R.color.marrom));
                                valorDoAdicional = adicionalSelecionado.getValor();
                                totalFinalDoItem = totalFinalDoItem - valorDoAdicional;
                                String remover = "+" + adicionalSelecionado.getNome();
                                adicionaisFinal = adicionaisFinal.replace(remover, "");
                            }

                            String qtdFinalTexto = qtdFinal.getText().toString();
                            Double valorQtd = Double.parseDouble(qtdFinalTexto);
                            totalFinalDoItemVezesQtd = totalFinalDoItem * valorQtd;

                            Locale ptBr = new Locale("pt", "BR");
                            String valorMostrar = NumberFormat.getCurrencyInstance(ptBr).format(totalFinalDoItemVezesQtd);
                            totalFinal.setText(valorMostrar);
                        }
                    });
                }
            }
        }
        if(itemSelecionado.getGrupo() == 4 && itemSelecionado.getTipo() == 1)  //adicional de pastéis
        {
            etObservacoesBottomSheet.setHint("Ex.: quero sem azeitona meus queridos.");

            listaAuxiliarCheckBox.clear();
            layoutAdicionais.setVisibility(View.VISIBLE);
            int parametroQualLayout = 0;
            for(int i = 0; i < listaAdicionais.size(); i++) {
                ItemCardapio adicional = listaAdicionais.get(i);
                if(adicional.getTipo() == 8)
                {
                    listaAuxiliarCheckBox.add(adicional);
                    CheckBox cb = new CheckBox(new ContextThemeWrapper(getContext(), R.style.MyCheckboxStyle));
                    cb.setText(adicional.getNome());
                    if(parametroQualLayout == 0)
                    {
                        parametroQualLayout = 1;
                        layoutCheckBox1.addView(cb);
                    }
                    else
                    {
                        parametroQualLayout = 0;
                        layoutCheckBox2.addView(cb);
                    }
                    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            int indiceDoAdicional = -1;
                            String nomeAdicional = cb.getText().toString();
                            ItemCardapio adicionalSelecionado = new ItemCardapio();
                            for(ItemCardapio item : listaAuxiliarCheckBox)
                            {
                                if(item.getDescricao().toLowerCase().contains(nomeAdicional.toLowerCase()))
                                {
                                    adicionalSelecionado = item;
                                }
                            }
                            Double valorDoAdicional;
                            if(cb.isChecked())
                            {
                                cb.setTextColor(getResources().getColor(R.color.laranja));
                                valorDoAdicional = adicionalSelecionado.getValor();
                                totalFinalDoItem = totalFinalDoItem + valorDoAdicional;
                                adicionaisFinal = adicionaisFinal.trim() + " +" + adicionalSelecionado.getNome().trim();
                            }
                            else
                            {
                                cb.setTextColor(getResources().getColor(R.color.marrom));
                                valorDoAdicional = adicionalSelecionado.getValor();
                                totalFinalDoItem = totalFinalDoItem - valorDoAdicional;
                                String remover = "+" + adicionalSelecionado.getNome();
                                adicionaisFinal = adicionaisFinal.replace(remover, "");
                            }
                            String qtdFinalTexto = qtdFinal.getText().toString();
                            Double valorQtd = Double.parseDouble(qtdFinalTexto);
                            totalFinalDoItemVezesQtd = totalFinalDoItem * valorQtd;

                            Locale ptBr = new Locale("pt", "BR");
                            String valorMostrar = NumberFormat.getCurrencyInstance(ptBr).format(totalFinalDoItemVezesQtd);
                            totalFinal.setText(valorMostrar);
                        }
                    });
                }
            }
        }
        if(itemSelecionado.getGrupo() == 5 && itemSelecionado.getTipo() == 1) //adicionais de porções
        {
            etObservacoesBottomSheet.setHint("Ex.: quero duas maioneses nessa belezinha");

            listaAuxiliarCheckBox.clear();
            layoutAdicionais.setVisibility(View.VISIBLE);
            int parametroQualLayout = 0;
            for(int i = 0; i < listaAdicionais.size(); i++) {
                ItemCardapio adicional = listaAdicionais.get(i);
                if(adicional.getTipo() == 7)
                {
                    listaAuxiliarCheckBox.add(adicional);
                    CheckBox cb = new CheckBox(new ContextThemeWrapper(getContext(), R.style.MyCheckboxStyle));
                    cb.setText(adicional.getNome());
                    if(parametroQualLayout == 0)
                    {
                        parametroQualLayout = 1;
                        layoutCheckBox1.addView(cb);
                    }
                    else
                    {
                        parametroQualLayout = 0;
                        layoutCheckBox2.addView(cb);
                    }
                    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            int indiceDoAdicional = -1;
                            String nomeAdicional = cb.getText().toString();
                            ItemCardapio adicionalSelecionado = new ItemCardapio();
                            for(ItemCardapio item : listaAuxiliarCheckBox)
                            {
                                if(item.getDescricao().toLowerCase().contains(nomeAdicional.toLowerCase()))
                                {
                                    adicionalSelecionado = item;
                                }
                            }
                            Double valorDoAdicional;
                            if(cb.isChecked())
                            {
                                cb.setTextColor(getResources().getColor(R.color.laranja));
                                valorDoAdicional = adicionalSelecionado.getValor();
                                totalFinalDoItem = totalFinalDoItem + valorDoAdicional;
                                adicionaisFinal = adicionaisFinal.trim() + " +" + adicionalSelecionado.getNome().trim();
                            }
                            else
                            {
                                cb.setTextColor(getResources().getColor(R.color.marrom));
                                valorDoAdicional = adicionalSelecionado.getValor();
                                totalFinalDoItem = totalFinalDoItem - valorDoAdicional;
                                String remover = "+" + adicionalSelecionado.getNome();
                                adicionaisFinal = adicionaisFinal.replace(remover, "");
                            }
                            String qtdFinalTexto = qtdFinal.getText().toString();
                            Double valorQtd = Double.parseDouble(qtdFinalTexto);
                            totalFinalDoItemVezesQtd = totalFinalDoItem * valorQtd;

                            Locale ptBr = new Locale("pt", "BR");
                            String valorMostrar = NumberFormat.getCurrencyInstance(ptBr).format(totalFinalDoItemVezesQtd);
                            totalFinal.setText(valorMostrar);
                        }
                    });
                }
            }
        }

        if(itemSelecionado.getGrupo() == 4 && itemSelecionado.getTipo() == 2)//pastel doce
            etObservacoesBottomSheet.setHint("Ex.: sem muito morango, pfvr meus amores");
        if(itemSelecionado.getGrupo() == 2 && itemSelecionado.getTipo() == 1)//crepe salgado
            etObservacoesBottomSheet.setHint("Ex.: o meio pode ser de chocolate queridos...");
        if(itemSelecionado.getGrupo() == 2 && itemSelecionado.getTipo() == 2)//crepe doce
            etObservacoesBottomSheet.setHint("Ex.: o meio pode ser de frango queridos...");
        if(itemSelecionado.getGrupo() == 3 && itemSelecionado.getTipo() == 1)//tapioca salgada
            etObservacoesBottomSheet.setHint("Ex.: sem muito recheio pfvr meus amores");
        if(itemSelecionado.getGrupo() == 3 && itemSelecionado.getTipo() == 2)//tapioca doce
            etObservacoesBottomSheet.setHint("Ex.: pouco chocolate pfvr meus lindão");
        if(itemSelecionado.getGrupo() == 6)//bebidas
            etObservacoesBottomSheet.setHint("Ex.: sem gelo, pfvr meus queridos");
        if(itemSelecionado.getGrupo() == 7)//outros
            etObservacoesBottomSheet.setHint("informações importantes");

        descItemBottomSheet.setText(itemSelecionado.getNome());
        infoItemBottomSheet.setText(itemSelecionado.getDescricao());
        String descricaoItemAdicionar = itemSelecionado.getNome();

        Double valorDoItem = itemSelecionado.getValor();
        totalFinalDoItem = valorDoItem;
        Locale ptBr = new Locale("pt", "BR");
        String valorMostrar = NumberFormat.getCurrencyInstance(ptBr).format(valorDoItem);

        totalFinal.setText(valorMostrar);

        PushDownAnim.setPushDownAnimTo(btAddQtd) //botão que aumenta a quantidade.
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qtdAtual = Integer.parseInt(qtdFinal.getText().toString());
                if(qtdAtual == 99)
                {
                    return;
                }
                else
                {
                    int quantidadeFinal = qtdAtual + 1;
                    qtdFinal.setText(String.valueOf(quantidadeFinal));

                    String qtdFinalTexto = qtdFinal.getText().toString();
                    Double valorQtd = Double.parseDouble(qtdFinalTexto);
                    totalFinalDoItemVezesQtd = totalFinalDoItem * valorQtd;

                    Locale ptBr = new Locale("pt", "BR");
                    String valorMostrar = NumberFormat.getCurrencyInstance(ptBr).format(totalFinalDoItemVezesQtd);
                    totalFinal.setText(valorMostrar);
                }
            }
        });
        PushDownAnim.setPushDownAnimTo(btRemQtd) // botão que diminui a quantidade
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        qtdAtual = Integer.parseInt(qtdFinal.getText().toString());
                        if(qtdAtual == 1)
                        {
                            return;
                        }
                        else
                        {
                            int quantidadeFinal = qtdAtual - 1;
                            qtdFinal.setText(String.valueOf(quantidadeFinal));

                            String qtdFinalTexto = qtdFinal.getText().toString();
                            Double valorQtd = Double.parseDouble(qtdFinalTexto);
                            totalFinalDoItemVezesQtd = totalFinalDoItem * valorQtd;

                            Locale ptBr = new Locale("pt", "BR");
                            String valorMostrar = NumberFormat.getCurrencyInstance(ptBr).format(totalFinalDoItemVezesQtd);
                            totalFinal.setText(valorMostrar);
                        }
                    }
                });
        PushDownAnim.setPushDownAnimTo(btCancelar) // botão cancelar inserção
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss();
                    }
                });
        PushDownAnim.setPushDownAnimTo(btAdicionar) // botão confirmar inserção
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String qtdFinalTexto = qtdFinal.getText().toString();
                        Double valorQtd = Double.parseDouble(qtdFinalTexto);
                        totalFinalDoItemVezesQtd = totalFinalDoItem * valorQtd;

                        String observacoesItem = etObservacoesBottomSheet.getText().toString();
                        String qtdSalvarString = qtdFinal.getText().toString();
                        int qtdSalvarInt = Integer.parseInt(qtdSalvarString);

                        ItemPedido itemAdicionar = new ItemPedido();
                        //itemAdicionar.setId_item(recuperaUltimoIdItemPedidoInserido() + 1);
                        itemAdicionar.setId_pedido("");
                        itemAdicionar.setDesc_item(descricaoItemAdicionar);
                        itemAdicionar.setAdicionais_item(adicionaisFinal.trim());
                        itemAdicionar.setObs_item(observacoesItem);
                        itemAdicionar.setValor_item(totalFinalDoItemVezesQtd);
                        itemAdicionar.setQtd_item(qtdSalvarInt);
                        itemAdicionar.setGrupo_item(itemSelecionado.getGrupo());

                        bancoDados = getActivity().openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
                        bancoDados.execSQL("CREATE TABLE IF NOT EXISTS item_pedido (id_item INTEGER PRIMARY KEY AUTOINCREMENT, id_pedido VARCHAR, desc_item VARCHAR, adicionais_item VARCHAR, obs_item VARCHAR, valor_item REAL, qtd_item INTEGER, grupo_item INTEGER)");
                        bancoDados.execSQL("INSERT INTO item_pedido (id_pedido, desc_item, adicionais_item, obs_item, valor_item, qtd_item, grupo_item) VALUES ('" + itemAdicionar.getId_pedido() + "', '" + itemAdicionar.getDesc_item() + "', '" + itemAdicionar.getAdicionais_item() + "', '" + itemAdicionar.getObs_item() + "', " + itemAdicionar.getValor_item() +  ", " + itemAdicionar.getQtd_item() +  ", " + itemAdicionar.getGrupo_item() + ")");

                        Pedido fragmentPedido = new Pedido();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_pedido, fragmentPedido, "FRAGMENT_PEDIDO")
                                .addToBackStack(null)
                                .commit();

                        SuperActivityToast.create(getContext(), new Style(), Style.TYPE_STANDARD)
                                .setText("Item adicionado ao pedido! :)")
                                .setDuration(Style.DURATION_SHORT)
                                .setColor(getResources().getColor(R.color.marrom))
                                .setAnimations(Style.ANIMATIONS_POP)
                                .show();
                        bottomSheetDialog.dismiss();
                    }
                });
        bottomSheetDialog.show();
    }

    public void gerarTutorialCardapioFiltro()
    {
        new ShowcaseView.Builder(getActivity())
                .withMaterialShowcase()
                .setStyle(R.style.CustomShowCaseTheme)
                .setTarget(new ViewTarget(btMostrarFiltros))
                .hideOnTouchOutside()
                .setContentTitle("Botão de Filtros")
                .setContentText("Utilize esse botão para aplicar filtros e encontrar o que procura de forma rápida")
                .setShowcaseEventListener(new SimpleShowcaseEventListener()
                {
                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        gerarTutorialCardapio();
                    }

                })
                .withHoloShowcase()
                .build();
    }
    public void gerarTutorialCardapio()
    {
        new ShowcaseView.Builder(getActivity())
                .withMaterialShowcase()
                .setStyle(R.style.CustomShowCaseTheme)
                .setTarget(new ViewTarget(recyclerView))
                .hideOnTouchOutside()
                .setContentTitle("Item do Cardápio")
                .setContentText("Selecione o item que deseja e coloque seus adicionais e observações, se desejar")
                .setShowcaseEventListener(new SimpleShowcaseEventListener()
                {
                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        ((TelaPrincipal) getActivity()).onHiddenFirstShowcase();
                    }

                })
                .withHoloShowcase()
                .build();
    }

    public void verificaPrimeiroAcesso()
    {
        int parametro = -1;
        SQLiteDatabase bancoDados;
        bancoDados = getActivity().openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
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

        if(parametro == 0)
        {
            //primeiro acesso
            gerarTutorialCardapioFiltro();
            //atualizarPrimeiroAcesso();
        }
        else
        {
            //ja acessou antes
            return;
        }
    }
}