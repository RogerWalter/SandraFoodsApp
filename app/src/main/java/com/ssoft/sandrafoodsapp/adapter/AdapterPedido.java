package com.ssoft.sandrafoodsapp.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ssoft.sandrafoodsapp.R;
import com.ssoft.sandrafoodsapp.model.ItemPedido;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AdapterPedido extends RecyclerView.Adapter<AdapterPedido.MyViewHolder> {

    private List<ItemPedido> listaPedido;
    public AdapterPedido(List<ItemPedido> lista) {
        this.listaPedido = lista;
    }

    @NonNull
    @Override
    public AdapterPedido.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_lista_pedido, parent, false);

        return new AdapterPedido.MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPedido.MyViewHolder holder, int position) {

        ItemPedido item = listaPedido.get(position);

        String qtdConvertida = String.valueOf(item.getQtd_item());

        Double valorTotalRecebido = item.getValor_item();
        Double qtdRecebida = Double.parseDouble(qtdConvertida);
        Double valorUnitarioRecebido = valorTotalRecebido / qtdRecebida;
        Locale ptBr = new Locale("pt", "BR");
        String valorUnitarioMostrar = NumberFormat.getCurrencyInstance(ptBr).format(valorUnitarioRecebido);
        String valorTotalMostrar = NumberFormat.getCurrencyInstance(ptBr).format(valorTotalRecebido);

        holder.qtd.setText(qtdConvertida);
        holder.nome.setText(item.getDesc_item());

        if(item.getAdicionais_item() == "" || TextUtils.isEmpty(item.getAdicionais_item()))
        {
            if(item.getGrupo_item() == 1 || item.getGrupo_item() == 4 || item.getGrupo_item() == 5)
                holder.adicionais.setText("SEM ADICIONAIS");
            else
                holder.adicionais.setText("-");
        }
        else
        {
            holder.adicionais.setText(item.getAdicionais_item().trim());
        }
        holder.valorUnitario.setText(valorUnitarioMostrar);
        holder.valorTotal.setText(valorTotalMostrar);
    }

    @Override
    public int getItemCount() {
        return listaPedido.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView qtd;
        TextView nome;
        TextView adicionais;
        TextView valorUnitario;
        TextView valorTotal;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            qtd = itemView.findViewById(R.id.tvAdpPedidoQtd);
            nome = itemView.findViewById(R.id.tvAdpPedidoNome);
            adicionais = itemView.findViewById(R.id.tvAdpPedidoAdicionais);
            valorUnitario = itemView.findViewById(R.id.tvAdpPedidoValorUnitario);
            valorTotal = itemView.findViewById(R.id.tvAdpPedidoValorTotal);
        }
    }
}
