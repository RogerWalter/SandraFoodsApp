[33mcommit a5468b88f3a21ac86cac3bd711efa4dde0ac8623[m[33m ([m[1;36mHEAD -> [m[1;32mmain[m[33m)[m
Author: Roger Walter <rogerwaltercorreia@gmail.com>
Date:   Mon Jun 6 10:07:21 2022 -0300

    Versao mais recente do sistema

[1mdiff --git a/.idea/vcs.xml b/.idea/vcs.xml[m
[1mnew file mode 100644[m
[1mindex 0000000..94a25f7[m
[1m--- /dev/null[m
[1m+++ b/.idea/vcs.xml[m
[36m@@ -0,0 +1,6 @@[m
[32m+[m[32m<?xml version="1.0" encoding="UTF-8"?>[m
[32m+[m[32m<project version="4">[m
[32m+[m[32m  <component name="VcsDirectoryMappings">[m
[32m+[m[32m    <mapping directory="$PROJECT_DIR$" vcs="Git" />[m
[32m+[m[32m  </component>[m
[32m+[m[32m</project>[m
\ No newline at end of file[m
[1mdiff --git a/app/build.gradle b/app/build.gradle[m
[1mindex b0978a6..61b8fb2 100644[m
[1m--- a/app/build.gradle[m
[1m+++ b/app/build.gradle[m
[36m@@ -9,8 +9,8 @@[m [mandroid {[m
         applicationId "com.stizsoftware.sandrafoodsapp"[m
         minSdkVersion 28[m
         targetSdkVersion 30[m
[31m-        versionCode 6 //versão 4 pq o Google encheu a paciencia[m
[31m-        versionName "1.0.2"[m
[32m+[m[32m        versionCode 9 //versão 5 pq o Google encheu a paciencia[m
[32m+[m[32m        versionName "1.0.5"[m
         android.defaultConfig.vectorDrawables.useSupportLibrary true[m
         testInstrumentationRunner 'androidx.test.runner.AndroidJUnitRunner'[m
         multiDexEnabled true[m
[1mdiff --git a/app/release/app-release.aab b/app/release/sandra-foods-1.0.5.aab[m
[1msimilarity index 90%[m
[1mrename from app/release/app-release.aab[m
[1mrename to app/release/sandra-foods-1.0.5.aab[m
[1mindex 7fdabb9..f17e489 100644[m
Binary files a/app/release/app-release.aab and b/app/release/sandra-foods-1.0.5.aab differ
[1mdiff --git a/app/src/main/java/com/ssoft/sandrafoodsapp/activity/TelaConfirmacaoPedido.java b/app/src/main/java/com/ssoft/sandrafoodsapp/activity/TelaConfirmacaoPedido.java[m
[1mindex dbdedf3..d551218 100644[m
[1m--- a/app/src/main/java/com/ssoft/sandrafoodsapp/activity/TelaConfirmacaoPedido.java[m
[1m+++ b/app/src/main/java/com/ssoft/sandrafoodsapp/activity/TelaConfirmacaoPedido.java[m
[36m@@ -223,6 +223,29 @@[m [mpublic class TelaConfirmacaoPedido extends AppCompatActivity {[m
 [m
         etDesconto.setText("");[m
 [m
[32m+[m[32m        //OBRIGAR O USUÁRIO A SELECIONAR UM TIPO DE PEDIDO ANTES DE APLICAR O CUPOM[m
[32m+[m[32m        etDesconto.setOnFocusChangeListener(new View.OnFocusChangeListener() {[m
[32m+[m[32m            @Override[m
[32m+[m[32m            public void onFocusChange(View view, boolean b) {[m
[32m+[m[32m                if(b)[m
[32m+[m[32m                {[m
[32m+[m[32m                    if(parametroTipoPedido == -1)[m
[32m+[m[32m                    {[m
[32m+[m[32m                        SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)[m
[32m+[m[32m                                .setText("Antes de aplicar um cupom, é necessário que informe o tipo do seu pedido. Escolha uma das opções: Entrega, Retirada ou Consumo no Local.")[m
[32m+[m[32m                                .setTextSize(Style.TEXTSIZE_VERY_LARGE)[m
[32m+[m[32m                                .setDuration(Style.DURATION_LONG)[m
[32m+[m[32m                                .setColor(getResources().getColor(R.color.vermelho))[m
[32m+[m[32m                                .setAnimations(Style.ANIMATIONS_POP)[m
[32m+[m[32m                                .show();[m
[32m+[m[32m                        etDesconto.setEnabled(false);[m
[32m+[m[32m                        etDesconto.setEnabled(true);[m
[32m+[m[32m                        return;[m
[32m+[m[32m                    }[m
[32m+[m[32m                }[m
[32m+[m[32m            }[m
[32m+[m[32m        });[m
[32m+[m
         tvDescontoDesc.setVisibility(View.GONE);[m
         tvDescontoValor.setVisibility(View.GONE);[m
 [m
[36m@@ -277,6 +300,7 @@[m [mpublic class TelaConfirmacaoPedido extends AppCompatActivity {[m
                     @Override[m
                     public void onClick(View view) {[m
                         parametroTipoPedido = 0;[m
[32m+[m[32m                        etDesconto.setEnabled(true);[m
                         mostrarPagamento();[m
                         limparBotoesPagamento();[m
                         recuperaDados();[m
[36m@@ -288,6 +312,7 @@[m [mpublic class TelaConfirmacaoPedido extends AppCompatActivity {[m
                     @Override[m
                     public void onClick(View view) {[m
                         parametroTipoPedido = 1;[m
[32m+[m[32m                        etDesconto.setEnabled(true);[m
                         esconderPagamento();[m
                         limparBotoesPagamento();[m
                         recuperaDados();[m
[36m@@ -299,6 +324,7 @@[m [mpublic class TelaConfirmacaoPedido extends AppCompatActivity {[m
                     @Override[m
                     public void onClick(View view) {[m
                         parametroTipoPedido = 2;[m
[32m+[m[32m                        etDesconto.setEnabled(true);[m
                         esconderPagamento();[m
                         limparBotoesPagamento();[m
                         recuperaDados();[m
[36m@@ -309,6 +335,8 @@[m [mpublic class TelaConfirmacaoPedido extends AppCompatActivity {[m
                 .setOnClickListener(new View.OnClickListener() {[m
                     @Override[m
                     public void onClick(View view) {[m
[32m+[m[32m                        if(etDesconto.getText().length() == 0)[m
[32m+[m[32m                            return;[m
                         verificaCupomDesconto();[m
                     }[m
                 });[m
[36m@@ -375,6 +403,8 @@[m [mpublic class TelaConfirmacaoPedido extends AppCompatActivity {[m
             return;[m
         }[m
 [m
[32m+[m[32m        calulcarTotalDoPedido();[m
[32m+[m
         loTipoPedido.setVisibility(View.INVISIBLE);[m
         loTotalizador.setVisibility(View.INVISIBLE);[m
         loObservacao.setVisibility(View.INVISIBLE);[m
[36m@@ -801,6 +831,7 @@[m [mpublic class TelaConfirmacaoPedido extends AppCompatActivity {[m
         bottomSheetDialog.setContentView(R.layout.dialogo_alterar_dados_cliente);[m
 [m
         bottomSheetDialog.setCanceledOnTouchOutside(false);[m
[32m+[m[32m        bottomSheetDialog.setCancelable(false);[m
 [m
         String[] ITENS = {[m
                 "25 De Julho", "Alpestre", "Alpino", "Bela Aliança", "Bohemerwald", "Brasilia", "Centro", "Colonial", "Cruzeiro", "Dona Francisca", "Fundão",[m
[36m@@ -843,6 +874,7 @@[m [mpublic class TelaConfirmacaoPedido extends AppCompatActivity {[m
                         if(TextUtils.isEmpty(cliente.getRua()) || TextUtils.isEmpty(cliente.getBairro()))[m
                         {[m
                             parametroTipoPedido = 1;[m
[32m+[m[32m                            loPagamento.setVisibility(View.INVISIBLE);[m
                             configuraBotoesTipoDePedido();[m
                             SuperActivityToast.create(TelaConfirmacaoPedido.this, new Style(), Style.TYPE_STANDARD)[m
                                     .setText("Os dados para entrega não foram preenchidos.")[m
[36m@@ -851,6 +883,9 @@[m [mpublic class TelaConfirmacaoPedido extends AppCompatActivity {[m
                                     .setAnimations(Style.ANIMATIONS_POP)[m
                                     .show();[m
                         }[m
[32m+[m[32m                        loPagamento.setVisibility(View.INVISIBLE);[m
[32m+[m[32m                        parametroTipoPedido = 1;[m
[32m+[m[32m                        configuraBotoesTipoDePedido();[m
                         bottomSheetDialog.dismiss();[m
                     }[m
                 });[m
[36m@@ -1203,9 +1238,12 @@[m [mpublic class TelaConfirmacaoPedido extends AppCompatActivity {[m
             String bairro = addresses.get(0).getSubLocality();[m
             String numero = addresses.get(0).getSubThoroughfare();[m
 [m
[31m-            retorno.setRua(rua.trim());[m
[31m-            retorno.setBairro(bairro.trim());[m
[31m-            retorno.setNumero(numero.trim());[m
[32m+[m[32m            if(!TextUtils.isEmpty(rua))[m
[32m+[m[32m            {[m
[32m+[m[32m                retorno.setRua(rua.trim());[m
[32m+[m[32m                retorno.setBairro(bairro.trim());[m
[32m+[m[32m                retorno.setNumero(numero.trim());[m
[32m+[m[32m            }[m
 [m
         } catch (IOException e) {[m
             e.printStackTrace();[m
[36m@@ -1427,7 +1465,14 @@[m [mpublic class TelaConfirmacaoPedido extends AppCompatActivity {[m
             valorTotalPedido = totalItensPedido;[m
         }[m
         Locale ptBr = new Locale("pt", "BR");[m
[31m-        String valorMostrar = NumberFormat.getCurrencyInstance(ptBr).format(valorTotalPedido);[m
[32m+[m[32m        String valorMostrar = "-";[m
[32m+[m[32m        try{[m
[32m+[m[32m            valorMostrar = NumberFormat.getCurrencyInstance(ptBr).format(valorTotalPedido);[m
[32m+[m[32m        }[m
[32m+[m[32m        catch(Exception e)[m
[32m+[m[32m        {[m
[32m+[m[32m            valorMostrar = "R$0,00";[m
[32m+[m[32m        }[m
         tvTotalPedido.setText(valorMostrar);[m
     }[m
 [m
[36m@@ -2478,7 +2523,7 @@[m [mpublic class TelaConfirmacaoPedido extends AppCompatActivity {[m
         });[m
     }[m
 [m
[31m-    public int parametroAbertoOuFechado = 0;[m
[32m+[m[32m    public int parametroAbertoOuFechado = 1;[m
 [m
     public void verificaFuncionamentoRestaurante()[m
     {[m
[36m@@ -2492,6 +2537,11 @@[m [mpublic class TelaConfirmacaoPedido extends AppCompatActivity {[m
 [m
         final SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");[m
         //String hora = formatoHora.format(new Date());[m
[32m+[m[32m        if(horaLib == null)[m
[32m+[m[32m        {[m
[32m+[m[32m            Date currentTime = Calendar.getInstance().getTime();[m
[32m+[m[32m            horaLib = currentTime;[m
[32m+[m[32m        }[m
         String hora = formatoHora.format(horaLib);[m
 [m
         Date horaAbre = null, horaFecha = null, horaAtual = null;[m
[1mdiff --git a/app/src/main/java/com/ssoft/sandrafoodsapp/activity/TelaIdentificacao.java b/app/src/main/java/com/ssoft/sandrafoodsapp/activity/TelaIdentificacao.java[m
[1mindex a75f291..62cb253 100644[m
[1m--- a/app/src/main/java/com/ssoft/sandrafoodsapp/activity/TelaIdentificacao.java[m
[1m+++ b/app/src/main/java/com/ssoft/sandrafoodsapp/activity/TelaIdentificacao.java[m
[36m@@ -68,6 +68,7 @@[m [mpublic class TelaIdentificacao extends AppCompatActivity {[m
         getSupportActionBar().hide();[m
         //Inicializa os componentes do layout[m
         inicializaComponentes();[m
[32m+[m
         AsyncTask.execute(new Runnable() {[m
             @Override[m
             public void run() {[m
[36m@@ -676,7 +677,7 @@[m [mpublic class TelaIdentificacao extends AppCompatActivity {[m
         });[m
     }[m
 [m
[31m-    private int parametroAbertoOuFechado = 0;[m
[32m+[m[32m    private int parametroAbertoOuFechado = 1;[m
     Date horaLib = null;[m
 [m
     public void verificaFuncionamentoRestaurante()[m
[36m@@ -691,6 +692,11 @@[m [mpublic class TelaIdentificacao extends AppCompatActivity {[m
 [m
         final SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");[m
         //String hora = formatoHora.format(new Date());[m
[32m+[m[32m        if(horaLib == null)[m
[32m+[m[32m        {[m
[32m+[m[32m            Date currentTime = Calendar.getInstance().getTime();[m
[32m+[m[32m            horaLib = currentTime;[m
[32m+[m[32m        }[m
         String hora = formatoHora.format(horaLib);[m
 [m
         Date horaAbre = null, horaFecha = null, horaAtual = null;[m
[1mdiff --git a/app/src/main/res/drawable/background_botao_arredondado_verde.xml b/app/src/main/res/drawable/background_botao_arredondado_verde.xml[m
[1mnew file mode 100644[m
[1mindex 0000000..b92d5da[m
[1m--- /dev/null[m
[1m+++ b/app/src/main/res/drawable/background_botao_arredondado_verde.xml[m
[36m@@ -0,0 +1,10 @@[m
[32m+[m[32m<?xml version="1.0" encoding="utf-8"?>[m
[32m+[m[32m<layer-list xmlns:android="http://schemas.android.com/apk/res/android">[m
[32m+[m[32m    <item android:bottom="4px">[m
[32m+[m[32m        <shape  android:shape="rectangle">[m
[32m+[m[32m            <solid android:color="#00CA14" />[m
[32m+[m[32m            <corners android:radius="25dp" />[m
[32m+[m[32m        </shape>[m
[32m+[m[32m    </item>[m
[32m+[m[32m</layer-list>[m
[32m+[m
[1mdiff --git a/app/src/main/res/drawable/background_elevated_marrom.xml b/app/src/main/res/drawable/background_elevated_marrom.xml[m
[1mnew file mode 100644[m
[1mindex 0000000..2268b58[m
[1m--- /dev/null[m
[1m+++ b/app/src/main/res/drawable/background_elevated_marrom.xml[m
[36m@@ -0,0 +1,26 @@[m
[32m+[m[32m<?xml version="1.0" encoding="utf-8"?>[m
[32m+[m[32m<layer-list xmlns:android="http://schemas.android.com/apk/res/android">[m
[32m+[m
[32m+[m[32m    <!-- Bottom 2dp Shadow -->[m
[32m+[m[32m    <item>[m
[32m+[m[32m        <shape  android:shape="rectangle">[m
[32m+[m
[32m+[m[32m            <solid android:color="#66D8D8D8" />[m
[32m+[m[32m            <corners android:radius="6dp" />[m
[32m+[m
[32m+[m[32m        </shape>[m
[32m+[m[32m    </item>[m
[32m+[m
[32m+[m[32m    <!-- White Top color -->[m
[32m+[m[32m    <item android:bottom="5px">[m
[32m+[m
[32m+[m[32m        <shape  android:shape="rectangle">[m
[32m+[m
[32m+[m[32m            <solid android:color="#80FF6900" />[m
[32m+[m[32m            <corners android:radius="6dp" />[m
[32m+[m[32m        </shape>[m
[32m+[m
[32m+[m[32m    </item>[m
[32m+[m
[32m+[m
[32m+[m[32m</layer-list>[m
\ No newline at end of file[m
[1mdiff --git a/app/src/main/res/drawable/drawable_textbox_destacado.xml b/app/src/main/res/drawable/drawable_textbox_destacado.xml[m
[1mnew file mode 100644[m
[1mindex 0000000..418709b[m
[1m--- /dev/null[m
[1m+++ b/app/src/main/res/drawable/drawable_textbox_destacado.xml[m
[36m@@ -0,0 +1,8 @@[m
[32m+[m[32m<?xml version="1.0" encoding="utf-8"?>[m
[32m+[m[32m<shape xmlns:android="http://schemas.android.com/apk/res/android"[m
[32m+[m[32m    android:shape="rectangle">[m
[32m+[m[32m    <solid android:color="#26FF6900"></solid>[m
[32m+[m[32m    <corners android:radius="20dp"></corners>[m
[32m+[m[32m    <stroke android:width="2dp"[m
[32m+[m[32m        android:color="@color/marrom"></stroke>[m
[32m+[m[32m</shape>[m
\ No newline at end of file[m
[1mdiff --git a/app/src/main/res/layout/activity_tela_confirmacao_pedido.xml b/app/src/main/res/layout/activity_tela_confirmacao_pedido.xml[m
[1mindex 57436d6..5bf9a10 100644[m
[1m--- a/app/src/main/res/layout/activity_tela_confirmacao_pedido.xml[m
[1m+++ b/app/src/main/res/layout/activity_tela_confirmacao_pedido.xml[m
[36m@@ -273,7 +273,7 @@[m
             android:layout_marginTop="8dp"[m
             android:layout_marginEnd="4dp"[m
             android:layout_marginBottom="8dp"[m
[31m-            android:background="@drawable/drawable_textbox"[m
[32m+[m[32m            android:background="@drawable/drawable_textbox_destacado"[m
             android:gravity="center|left"[m
             android:hint="Informe seu cupom aqui"[m
             android:inputType="textPersonName"[m
[1mdiff --git a/app/src/main/res/layout/dialogo_alterar_dados_cliente.xml b/app/src/main/res/layout/dialogo_alterar_dados_cliente.xml[m
[1mindex 13f3133..8761a2c 100644[m
[1m--- a/app/src/main/res/layout/dialogo_alterar_dados_cliente.xml[m
[1m+++ b/app/src/main/res/layout/dialogo_alterar_dados_cliente.xml[m
[36m@@ -22,9 +22,9 @@[m
     <Button[m
         android:id="@+id/btDialogoDadosBuscaLocal"[m
         android:layout_width="wrap_content"[m
[31m-        android:layout_height="40dp"[m
[32m+[m[32m        android:layout_height="50dp"[m
         android:layout_marginTop="4dp"[m
[31m-        android:background="@drawable/background_botao_arredondado"[m
[32m+[m[32m        android:background="@drawable/background_botao_arredondado_verde"[m
         android:drawableLeft="@drawable/ic_baseline_my_location_24"[m
         android:drawableRight="@drawable/ic_baseline_my_location_24"[m
         android:padding="8dp"[m
[1mdiff --git a/app/src/main/res/values/colors.xml b/app/src/main/res/values/colors.xml[m
[1mindex 7a2844b..d48b8ac 100644[m
[1m--- a/app/src/main/res/values/colors.xml[m
[1m+++ b/app/src/main/res/values/colors.xml[m
[36m@@ -14,5 +14,6 @@[m
     <color name="branco">#FFFFFF</color>[m
     <color name="cinza">#9B9C78</color>[m
     <color name="verde">#07FF00</color>[m
[32m+[m[32m    <color name="vermelho">#D13A2F</color>[m
 [m
 </resources>[m
\ No newline at end of file[m
