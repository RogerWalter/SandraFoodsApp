package com.ssoft.sandrafoodsapp.activity;

import android.os.Bundle;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;
import com.ssoft.sandrafoodsapp.R;

public class TelaSlider extends IntroActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setButtonBackVisible(false);
        setButtonNextVisible(false);

        addSlide(new SimpleSlide.Builder()
                .title("Como fazer meu pedido?")
                .description("Tá com fome né?")
                .image(R.drawable.slide1)
                .background(R.color.branco)
                .backgroundDark(R.color.laranja)
                .canGoBackward(false)
                .build()
        );
        addSlide(new SimpleSlide.Builder()
                .title("Informe seus dados")
                .description("A primeira coisa a se fazer é cadastrar os seus dados, preenchendo todos os campos.")
                .image(R.drawable.slide2)
                .background(R.color.branco)
                .backgroundDark(R.color.laranja)
                .build()
        );
        addSlide(new SimpleSlide.Builder()
                .title("Retirada ou Entrega")
                .description("Depois, você deve se perguntar se deseja buscar o seu pedido ou se deseja receber os produtos em sua casa.")
                .image(R.drawable.slide3)
                .background(R.color.branco)
                .backgroundDark(R.color.laranja)
                .build()
        );
        addSlide(new SimpleSlide.Builder()
                .title("Itens do pedido")
                .description("Agora, basta escolher os itens que deseja e adicioná-los ao seu pedido, informando a quantidade do item e observações, caso exista alguma")
                .image(R.drawable.slide4)
                .background(R.color.branco)
                .backgroundDark(R.color.laranja)
                .build()
        );
        addSlide(new SimpleSlide.Builder()
                .title("Finalizar o pedido")
                .description("Com seus dados devidamente informados e os itens já escolhidos, basta informar como deseja pagar e confirmar o pedido")
                .image(R.drawable.slide5)
                .background(R.color.branco)
                .backgroundDark(R.color.laranja)
                .build()
        );
        addSlide(new SimpleSlide.Builder()
                .title("Tudo certo!")
                .description("Agora seu pedido já está sendo preparado! Basta esperar para buscá-lo ou recebê-lo!")
                .image(R.drawable.slide6)
                .background(R.color.branco)
                .backgroundDark(R.color.laranja)
                .build()
        );
    }
}