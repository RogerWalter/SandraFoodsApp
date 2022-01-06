package com.ssoft.sandrafoodsapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.ssoft.sandrafoodsapp.R;
import com.thekhaeng.pushdownanim.PushDownAnim;

import static com.thekhaeng.pushdownanim.PushDownAnim.MODE_STATIC_DP;

public class TelaAplicativoOffline extends AppCompatActivity {

    private ImageView btWhats;
    private ConstraintLayout loTelaOffline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_aplicativo_offline);
        getSupportActionBar().hide();

        btWhats = findViewById(R.id.btWhatsTelaOffline);
        loTelaOffline = findViewById(R.id.loTelaAppOffline);

        loTelaOffline.setVisibility(View.INVISIBLE);

        PushDownAnim.setPushDownAnimTo(btWhats)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clickWhats();
                    }
                });

        loTelaOffline.animate().translationY(-250).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                loTelaOffline.setVisibility(View.VISIBLE);
                loTelaOffline.animate().translationY(0).alpha(1.0f).setDuration(250);
            }
        });
        //TELA PRA QUANDO DER MERDA. DEIXAMOS TUDO FECHADO QUALQUER COISA
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
                                SuperActivityToast.create(TelaAplicativoOffline.this, new Style(), Style.TYPE_STANDARD)
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
}