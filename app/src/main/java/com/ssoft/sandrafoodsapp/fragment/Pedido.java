package com.ssoft.sandrafoodsapp.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.text.TextUtils;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.ssoft.sandrafoodsapp.R;
import com.ssoft.sandrafoodsapp.activity.TelaConfirmacaoPedido;
import com.ssoft.sandrafoodsapp.activity.TelaPrincipal;
import com.ssoft.sandrafoodsapp.adapter.AdapterPedido;
import com.ssoft.sandrafoodsapp.helper.RecyclerItemClickListener;
import com.ssoft.sandrafoodsapp.model.Cliente;
import com.ssoft.sandrafoodsapp.model.ItemCardapio;
import com.ssoft.sandrafoodsapp.model.ItemPedido;
import com.ssoft.sandrafoodsapp.model.Parametros;
import com.ssoft.sandrafoodsapp.model.TaxaEntrega;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;
import static com.thekhaeng.pushdownanim.PushDownAnim.MODE_STATIC_DP;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Pedido#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Pedido extends Fragment {

    DatabaseReference firebaseBanco = FirebaseDatabase.getInstance().getReference();
    DatabaseReference cardapioDB;

    private SQLiteDatabase bancoDados;
    private List<TaxaEntrega> listaTaxasEntrega = new ArrayList<>();
    private List<ItemPedido> listaPedido = new ArrayList<>();
    private List<ItemCardapio> listaAdicionais = new ArrayList<>();
    private AdapterPedido adapter = new AdapterPedido(listaPedido);
    private ItemPedido itemSelecionado = new ItemPedido();
    private Double valorTaxa;

    private RecyclerView rvPedido;
    private ImageView ivSemDados;
    private TextView tvSemDados;
    private TextView tvTotalizador;
    private TextView tvPrazo, tvTaxa;
    private Double totalPedido;
    private ProgressBar pbPedido;
    private FloatingActionButton btCancelarPedido, btConfirmarPedido;
    private TextView tvTesteValor;
    private ConstraintLayout loFragmentPedido;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int parametroBtVoltar = 0;

    public Pedido() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Pedido.
     */
    // TODO: Rename and change types and number of parameters
    public static Pedido newInstance(String param1, String param2) {
        Pedido fragment = new Pedido();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        parametroBtVoltar = 1;
        loFragmentPedido.animate().translationY(0).alpha(1.0f);
    }

    @Override
    public void onResume() {
        super.onResume();
        parametroBtVoltar = 1;
        loFragmentPedido.animate().translationY(0).alpha(1.0f);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pedido, container, false);

        cardapioDB = firebaseBanco.child("cardapio");

        recuperaAdicionais();

        rvPedido = view.findViewById(R.id.rvPedido);
        //tvTotalizador = view.findViewById(R.id.tvPedidoTotalizadorPedido);
        ivSemDados = view.findViewById(R.id.ivPedidoSemDados);
        tvSemDados = view.findViewById(R.id.tvPedidoSemDados);
        pbPedido = view.findViewById(R.id.progressBarPedido);
        btCancelarPedido = view.findViewById(R.id.fabCancelaPedido);
        btConfirmarPedido = view.findViewById(R.id.fabConfirmarPedido);
        tvTesteValor = view.findViewById(R.id.tvPedidoTeste);
        loFragmentPedido = view.findViewById(R.id.fragment_pedido);
        tvPrazo = view.findViewById(R.id.tvPedidoPrazo);
        tvTaxa = view.findViewById(R.id.tvPedidoTaxa);

        recuperaParametrosAppFB();
        recuperaTaxas();

        ivSemDados.setVisibility(View.GONE);
        tvSemDados.setVisibility(View.GONE);
        pbPedido.setVisibility(View.VISIBLE);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvPedido.setLayoutManager(layoutManager);
        rvPedido.setHasFixedSize(true);
        rvPedido.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayout.VERTICAL));
        rvPedido.setAdapter(adapter);
        rvPedido.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getContext(),
                        rvPedido,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                ItemPedido item;
                                item = listaPedido.get(position);
                                itemSelecionado = item;
                                mostrarBSEditItem();
                            }
                            @Override
                            public void onLongItemClick(View view, int position) {
                                ItemPedido item;
                                item = listaPedido.get(position);
                                itemSelecionado = item;
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setCancelable(true);
                                builder.setTitle("Remover Item do Pedido");
                                builder.setMessage("Deseja realmente remover o item do pedido?");
                                builder.setPositiveButton("Sim",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                removerItem();
                                            }
                                        });
                                builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        })
        );
        PushDownAnim.setPushDownAnimTo(btCancelarPedido) //botão que limpa o pedido
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listaPedido.size() == 0)
                {
                    SuperActivityToast.create(getContext(), new Style(), Style.TYPE_STANDARD)
                            .setText("Não há itens em seu pedido. Não é possível zerá-lo.")
                            .setDuration(Style.DURATION_SHORT)
                            .setColor(getResources().getColor(R.color.marrom))
                            .setAnimations(Style.ANIMATIONS_POP)
                            .show();
                    return;
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(true);
                    builder.setTitle("Apagar Pedido");
                    builder.setMessage("Esta ação excluirá todos os itens de seu pedido atual. Deseja realmente fazer isso?");
                    builder.setPositiveButton("Sim",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    limparPedido();
                                }
                            });
                    builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
        PushDownAnim.setPushDownAnimTo(btConfirmarPedido) //botão que confirma o pedido
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        recuperarPedido();
                        parametroBtVoltar = 0;
                        if(listaPedido.size() == 0)
                        {
                            SuperActivityToast.create(getContext(), new Style(), Style.TYPE_STANDARD)
                                    .setText("Não há itens em seu pedido. Insira itens para poder finalizá-lo.")
                                    .setDuration(Style.DURATION_SHORT)
                                    .setColor(getResources().getColor(R.color.marrom))
                                    .setAnimations(Style.ANIMATIONS_POP)
                                    .show();
                            return;
                        }
                        else
                        {
                            //seguir para a tela de decisão se será entrega ou retirada
                            loFragmentPedido.animate().translationY(-200).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    if(parametroBtVoltar == 0)
                                        startActivity(new Intent(getContext(), TelaConfirmacaoPedido.class));
                                }
                            });
                        }
                    }
                });
        configuraTela();
        recuperarPedido();
        return view;
    }


    public int verificaSeTabelaExiste()
    {
        int respostaCursor = 0;
        bancoDados = getActivity().openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
        Cursor cursor = bancoDados.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='item_pedido'", null);
        respostaCursor = cursor.getCount();
        return respostaCursor;
    }


    public void recuperarPedido()
    {
        listaPedido.clear();

        ItemPedido buscado = new ItemPedido();

        bancoDados = getActivity().openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
        //Recuperar pessoas
        try {
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

                listaPedido.add(buscado);

            } while (cursor.moveToNext());
        }

        adapter.notifyDataSetChanged();

        recalculaTotalPedido();
        }catch(Exception e)
        {
            Log.e("ERRO", e.toString());
        }

        if(listaPedido.size() == 0)
        {
            ivSemDados.setVisibility(View.VISIBLE);
            tvSemDados.setVisibility(View.VISIBLE);
        }
    }

    public void recalculaTotalPedido()
    {
        totalPedido = 0.0;
        for(ItemPedido item : listaPedido)
        {
            totalPedido = item.getValor_item() + totalPedido;
        }
        Locale ptBr = new Locale("pt", "BR");
        String valorMostrar = NumberFormat.getCurrencyInstance(ptBr).format(totalPedido);
        //tvTotalizador.setText(valorMostrar);
        /*((Activity) getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)tvTotalizador).setText(valorMostrar);
            }
        });*/
        tvTesteValor.setText(valorMostrar);
    }

    public void configuraTela()
    {
        if(verificaSeTabelaExiste() == 0)
        {
            ivSemDados.setVisibility(View.VISIBLE);
            tvSemDados.setVisibility(View.VISIBLE);
        }
        else
        {
            recuperarPedido();
        }
    }

    public void removerItem()
    {
        bancoDados = getActivity().openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
        bancoDados.execSQL("DELETE FROM item_pedido WHERE id_item = " + itemSelecionado.getId_item());
        SuperActivityToast.create(getContext(), new Style(), Style.TYPE_STANDARD)
                .setText("Item Excluido com Sucesso!")
                .setDuration(Style.DURATION_SHORT)
                .setColor(getResources().getColor(R.color.marrom))
                .setAnimations(Style.ANIMATIONS_POP)
                .show();
        configuraTela();
    }

    public int qtdAtual = 0;
    public Double totalFinalDoItem = 0.00;
    public Double totalFinalDoItemVezesQtd = 0.00;
    public String adicionaisFinal;

    private void mostrarBSEditItem()
    {
        List<ItemCardapio> listaAuxiliarCheckBox = new ArrayList<>();
        ItemPedido itemPedidoAdd = new ItemPedido();
        totalFinalDoItem = 0.00;
        totalFinalDoItemVezesQtd = 0.00;
        adicionaisFinal = "";

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(R.layout.dialogo_editar_item_do_pedido);

        TextView descItemBottomSheet = bottomSheetDialog.findViewById(R.id.tvTituloBSEdtItemDescItem);
        ImageButton btAddQtd = bottomSheetDialog.findViewById(R.id.ibDialogoEdtItemAumentarQtd);
        TextView qtdFinal = bottomSheetDialog.findViewById(R.id.tvDialogoEdtItemQuantidadeMostrar);
        ImageButton btRemQtd = bottomSheetDialog.findViewById(R.id.ibDialogoEdtItemDiminuirQtd);
        LinearLayout layoutCheckBox1 = bottomSheetDialog.findViewById(R.id.linearLayoutAdicionais3);
        LinearLayout layoutCheckBox2 = bottomSheetDialog.findViewById(R.id.linearLayoutAdicionais4);
        ConstraintLayout layoutAdicionais = bottomSheetDialog.findViewById(R.id.constraintLayoutAdicionaisEditItemPedido);
        EditText etObservacoesBottomSheet = bottomSheetDialog.findViewById(R.id.etDialogoEdtItemObservacao);
        TextView totalFinal = bottomSheetDialog.findViewById(R.id.tvTotalizadorEdtItemDialogoTotal);
        Button btAdicionar = bottomSheetDialog.findViewById(R.id.btEdtItemPedidoSalvar);
        Button btCancelar = bottomSheetDialog.findViewById(R.id.btEdtItemPedidoCancelar);
        Button btRemover = bottomSheetDialog.findViewById(R.id.btEdtItemPedidoRemover);

        layoutAdicionais.setVisibility(View.GONE);

        qtdFinal.setText(String.valueOf(itemSelecionado.getQtd_item()));
        etObservacoesBottomSheet.setText(itemSelecionado.getObs_item());

        Double valorTotalRecebido = itemSelecionado.getValor_item();
        Double qtdRecebida = Double.parseDouble(qtdFinal.getText().toString());
        totalFinalDoItem = valorTotalRecebido / qtdRecebida;

        adicionaisFinal = itemSelecionado.getAdicionais_item();

        if(itemSelecionado.getGrupo_item() == 1) //item selecionado é um lanche
        {
            listaAuxiliarCheckBox.clear();
            layoutAdicionais.setVisibility(View.VISIBLE);
            int parametroQualLayout = 0;
            for(int i = 0; i < listaAdicionais.size(); i++) {
                ItemCardapio adicional = listaAdicionais.get(i);
                if(adicional.getTipo() == 9)//adicional de lanche
                {
                    listaAuxiliarCheckBox.add(adicional);
                    CheckBox cb = new CheckBox(getContext());
                    cb.setText(adicional.getNome());
                    cb.setTextColor(getResources().getColor(R.color.laranja));
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
                    if(adicionaisFinal.contains(adicional.getNome()))
                    {
                        cb.setChecked(true);
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
                                valorDoAdicional = adicionalSelecionado.getValor();
                                totalFinalDoItem = totalFinalDoItem + valorDoAdicional;
                                adicionaisFinal = adicionaisFinal + "+" + adicionalSelecionado.getNome() + " ";
                            }
                            else
                            {
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
        if(itemSelecionado.getGrupo_item() == 4) //item selecionado é um pastel
        {
            listaAuxiliarCheckBox.clear();
            layoutAdicionais.setVisibility(View.VISIBLE);
            int parametroQualLayout = 0;
            for(int i = 0; i < listaAdicionais.size(); i++) {
                ItemCardapio adicional = listaAdicionais.get(i);
                if(adicional.getTipo() == 8)//adicional de pastel
                {
                    listaAuxiliarCheckBox.add(adicional);
                    CheckBox cb = new CheckBox(getContext());
                    cb.setText(adicional.getNome());
                    cb.setTextColor(getResources().getColor(R.color.laranja));
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
                    if(adicionaisFinal.contains(adicional.getNome()))
                    {
                        cb.setChecked(true);
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
                                valorDoAdicional = adicionalSelecionado.getValor();
                                totalFinalDoItem = totalFinalDoItem + valorDoAdicional;
                                adicionaisFinal = adicionaisFinal + "+" + adicionalSelecionado.getNome() + " ";
                            }
                            else
                            {
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
        if(itemSelecionado.getGrupo_item() == 5) //item selecionado é uma porção
        {
            listaAuxiliarCheckBox.clear();
            layoutAdicionais.setVisibility(View.VISIBLE);
            int parametroQualLayout = 0;
            for(int i = 0; i < listaAdicionais.size(); i++) {
                ItemCardapio adicional = listaAdicionais.get(i);
                if(adicional.getTipo() == 7)//adicional de porção
                {
                    listaAuxiliarCheckBox.add(adicional);
                    CheckBox cb = new CheckBox(getContext());
                    cb.setText(adicional.getNome());
                    cb.setTextColor(getResources().getColor(R.color.laranja));
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
                    if(adicionaisFinal.contains(adicional.getNome()))
                    {
                        cb.setChecked(true);
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
                                valorDoAdicional = adicionalSelecionado.getValor();
                                totalFinalDoItem = totalFinalDoItem + valorDoAdicional;
                                adicionaisFinal = adicionaisFinal + "+" + adicionalSelecionado.getNome() + " ";
                            }
                            else
                            {
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
        descItemBottomSheet.setText(itemSelecionado.getDesc_item());
        String descricaoItemAtualizar = itemSelecionado.getDesc_item();

        Locale ptBr = new Locale("pt", "BR");
        String valorMostrar = NumberFormat.getCurrencyInstance(ptBr).format(itemSelecionado.getValor_item());
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
        PushDownAnim.setPushDownAnimTo(btRemover) // botão cancelar inserção
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setCancelable(true);
                        builder.setTitle("Remover Item do Pedido");
                        builder.setMessage("Deseja realmente remover o item do pedido?");
                        builder.setPositiveButton("Sim",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        removerItem();
                                        bottomSheetDialog.dismiss();
                                    }
                                });
                        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
        PushDownAnim.setPushDownAnimTo(btAdicionar) // botão confirmar atualização
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
                        itemAdicionar.setId_item(itemSelecionado.getId_item());
                        itemAdicionar.setId_pedido("");
                        itemAdicionar.setDesc_item(descricaoItemAtualizar);
                        itemAdicionar.setAdicionais_item(adicionaisFinal);
                        itemAdicionar.setObs_item(observacoesItem);
                        itemAdicionar.setValor_item(totalFinalDoItemVezesQtd);
                        itemAdicionar.setQtd_item(qtdSalvarInt);
                        itemAdicionar.setGrupo_item(itemSelecionado.getGrupo_item());

                        bancoDados = getActivity().openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
                        bancoDados.execSQL("CREATE TABLE IF NOT EXISTS item_pedido (id_item INTEGER PRIMARY KEY AUTOINCREMENT, id_pedido INTEGER, desc_item VARCHAR, adicionais_item VARCHAR, obs_item VARCHAR, valor_item REAL, qtd_item INTEGER, grupo_item INTEGER)");
                        bancoDados.execSQL("UPDATE item_pedido SET adicionais_item = '" + itemAdicionar.getAdicionais_item() + "', obs_item = '" + itemAdicionar.getObs_item() + "', valor_item = " + itemAdicionar.getValor_item() + ", qtd_item = " + itemAdicionar.getQtd_item() +  " WHERE id_item = " + itemAdicionar.getId_item());

                        Pedido fragmentPedido = new Pedido();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_pedido, fragmentPedido, "FRAGMENT_PEDIDO")
                                .addToBackStack(null)
                                .commit();

                        configuraTela();
                        SuperActivityToast.create(getContext(), new Style(), Style.TYPE_STANDARD)
                                .setText("Item atualizado com sucesso!")
                                .setDuration(Style.DURATION_SHORT)
                                .setColor(getResources().getColor(R.color.marrom))
                                .setAnimations(Style.ANIMATIONS_POP)
                                .show();
                        bottomSheetDialog.dismiss();
                    }                });
        bottomSheetDialog.show();
    }

    public void recuperaAdicionais()
    {
        DatabaseReference cardapioDB = firebaseBanco.child("cardapio");
        cardapioDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                listaAdicionais.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    if(ds.getValue(ItemCardapio.class).getGrupo() == 0) //se for 0, é adicional
                    {
                        listaAdicionais.add(ds.getValue(ItemCardapio.class));
                    }
                    for (ItemCardapio e : listaAdicionais)
                    {
                        String nomeAjustado = e.getNome().trim();
                        String descAjustado = e.getDescricao().trim();
                        e.setNome(nomeAjustado);
                        e.setDescricao(descAjustado);
                    }
                }
                pbPedido.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    public void limparPedido()
    {
        bancoDados = getActivity().openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
        bancoDados.execSQL("DELETE FROM item_pedido");
        SuperActivityToast.create(getContext(), new Style(), Style.TYPE_STANDARD)
                .setText("Os itens de seu pedido foram removidos!")
                .setDuration(Style.DURATION_SHORT)
                .setColor(getResources().getColor(R.color.marrom))
                .setAnimations(Style.ANIMATIONS_POP)
                .show();
        configuraTela();
    }

    public void gerarTutorialPedidoItens()
    {
        new ShowcaseView.Builder(getActivity())
                .withMaterialShowcase()
                .setStyle(R.style.CustomShowCaseTheme)
                .setTarget(new ViewTarget(rvPedido))
                .hideOnTouchOutside()
                .setContentTitle("Seu Pedido")
                .setContentText("Os itens adicionados ao seu pedido aparecerão aqui. Se desejar alterar ou remover, basta clicar sobre o item.")
                .setShowcaseEventListener(new SimpleShowcaseEventListener()
                {
                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        gerarTutorialPedidoPrazos();
                    }

                })
                .withHoloShowcase()
                .build();
    }

    public void gerarTutorialPedidoPrazos()
    {
        new ShowcaseView.Builder(getActivity())
                .withMaterialShowcase()
                .setStyle(R.style.CustomShowCaseTheme)
                .setTarget(new ViewTarget(tvPrazo))
                .hideOnTouchOutside()
                .setContentTitle("Prazos do seu pedido")
                .setContentText("Estes são os prazos de conclusão do seu pedido e o valor da entrega para o seu bairro. Entregas geralmente levam um pouco mais de tempo do que retiradas.")
                .setShowcaseEventListener(new SimpleShowcaseEventListener()
                {
                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        gerarTutorialPedidoConcluir();
                    }

                })
                .withHoloShowcase()
                .build();
    }

    public void gerarTutorialPedidoConcluir()
    {
        new ShowcaseView.Builder(getActivity())
                .withMaterialShowcase()
                .setStyle(R.style.CustomShowCaseTheme)
                .setTarget(new ViewTarget(btConfirmarPedido))
                .hideOnTouchOutside()
                .setContentTitle("Confirmar Pedido")
                .setContentText("Após selecionar tudo que deseja, basta clicar neste botão para confimar seu pedido.")
                .setShowcaseEventListener(new SimpleShowcaseEventListener()
                {
                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        ((TelaPrincipal) getActivity()).onHiddenSecondShowcase();
                    }

                })
                .withHoloShowcase()
                .build();
    }

    public String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

    public void gerarPrazos()
    {
        //int unicode = 0x231B;
        int unicode = 0x23F1;
        String emoji = getEmojiByUnicode(unicode);

        String prazoEnt = String.valueOf(parametrosApp.getEntrega());
        String prazoRet = String.valueOf(parametrosApp.getRetirada());

        String prazo = emoji + " Entrega: <b>"
                + prazoEnt + " min </b>"
                + emoji + "  Retirada: <b>"
                + prazoRet + " min </b>"
                + emoji;

        tvPrazo.setText(Html.fromHtml(prazo));
        //U+23F1 cronometro
        //U+1F6F5 moto scooter
    }
    private Parametros parametrosApp = new Parametros();
    public void recuperaParametrosAppFB()
    {
        DatabaseReference notificacaoFirebase = firebaseBanco.child("parametro");
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null)
                {
                    parametrosApp = dataSnapshot.getValue(Parametros.class);
                    gerarPrazos();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        notificacaoFirebase.addListenerForSingleValueEvent(eventListener);
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
                try {
                    recuperaDados();
                }catch(Exception e)
                {
                    return;
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private Cliente cliente = new Cliente();
    public void calcularValorTaxa()
    {
        int unicode = 0x1F6F5;
        String emoji = getEmojiByUnicode(unicode);

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

                String taxaFinal = emoji + " Entrega: <b>" + valorMostrar + "</b> " + emoji;
                tvTaxa.setText(Html.fromHtml(taxaFinal));
                break;
            }
        }
    }
    public void recuperaDados()
    {
        int unicode = 0x1F6F5;
        String emoji = getEmojiByUnicode(unicode);

        String id = "Erro";
        String nome = "Erro";
        String celular = "Erro";
        String rua = "Erro";
        String numero = "Erro";
        String bairro = "Erro";
        String referencia = "Erro";

        bancoDados = getActivity().openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
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
        }
        String taxaSemBairro = emoji + " Preencha seus dados para valor de entrega " + emoji;
        if(!TextUtils.isEmpty(cliente.getBairro()))
            calcularValorTaxa();
        else
            tvTaxa.setText(taxaSemBairro);
    }
}