package com.ssoft.sandrafoodsapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ssoft.sandrafoodsapp.R;
import com.ssoft.sandrafoodsapp.model.ItemCardapio;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdapterCardapio extends RecyclerView.Adapter<AdapterCardapio.MyViewHolder> {

    private List<ItemCardapio> listaCardapio;
    public AdapterCardapio(List<ItemCardapio> lista) {
        this.listaCardapio = lista;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_lista_cardapio, parent, false);

        return new AdapterCardapio.MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        ItemCardapio item = listaCardapio.get(position);

        Double valorRecebido = item.getValor();
        Locale ptBr = new Locale("pt", "BR");
        String valorMostrar = NumberFormat.getCurrencyInstance(ptBr).format(valorRecebido);

        holder.nome.setText(item.getNome());
        holder.descricao.setText(item.getDescricao());
        holder.valor.setText(valorMostrar);
        if(item.getGrupo() == 1) {   holder.tipo.setImageResource(R.drawable.ic_icone_filtro_lanches_inativo);}
        if(item.getGrupo() == 2) {   holder.tipo.setImageResource(R.drawable.ic_icone_filtro_crepes_inativo);}
        if(item.getGrupo() == 3) {   holder.tipo.setImageResource(R.drawable.ic_icone_filtro_tapiocas_inativo);}
        if(item.getGrupo() == 4) {   holder.tipo.setImageResource(R.drawable.ic_icone_filtro_pasteis_inativo);}
        if(item.getGrupo() == 5) {   holder.tipo.setImageResource(R.drawable.ic_icone_filtro_porcoes_inativo);}
        if(item.getGrupo() == 6) {   holder.tipo.setImageResource(R.drawable.ic_icone_filtro_bebidas_inativo);}
        if(item.getGrupo() == 7) {   holder.tipo.setImageResource(R.drawable.ic_icone_filtro_outros_inativo);}
    }

    @Override
    public int getItemCount() {
        return listaCardapio.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView nome;
        TextView descricao;
        TextView valor;
        ImageView tipo;
        ConstraintLayout fundo;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            nome = itemView.findViewById(R.id.tvAdpCardapioNome);
            descricao = itemView.findViewById(R.id.tvAdpCardapioDescricao);
            valor = itemView.findViewById(R.id.tvAdpCardapioValor);
            tipo = itemView.findViewById(R.id.ivAdpCardapioTipo);
            fundo = itemView.findViewById(R.id.loAdpCardapio);
        }
    }

    public void listaComFiltro(ArrayList<ItemCardapio> listaFiltrada)
    {
        listaCardapio = listaFiltrada;
        notifyDataSetChanged();
    }

}
