package com.ssoft.sandrafoodsapp.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ssoft.sandrafoodsapp.R;
import com.ssoft.sandrafoodsapp.activity.TelaPrincipal;
import com.ssoft.sandrafoodsapp.model.Cliente;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;
import static com.thekhaeng.pushdownanim.PushDownAnim.MODE_STATIC_DP;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Dados#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Dados extends Fragment {

    private SQLiteDatabase bancoDados;
    private int idUsuario = 0;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng meuLocal;

    private Cliente cliente = new Cliente();

    private ConstraintLayout layoutDados, layoutBuscaCep;
    private EditText etNome, etCelular, etRua, etNumero, etReferencia;
    private AutoCompleteTextView etBairro;
    private Button btBuscarLocal, btSalvar;
    private ProgressBar progressBar;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Dados() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Dados.
     */
    // TODO: Rename and change types and number of parameters
    public static Dados newInstance(String param1, String param2) {
        Dados fragment = new Dados();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dados, container, false);
        // Inflate the layout for this fragment
        layoutDados = view.findViewById(R.id.layoutDados);
        etNome = view.findViewById(R.id.etDadosNome);
        etCelular = view.findViewById(R.id.etDadosCelular);
        etRua = view.findViewById(R.id.etDadosRua);
        etNumero = view.findViewById(R.id.etDadosNumero);
        etBairro = view.findViewById(R.id.etDadosBairro);
        etReferencia = view.findViewById(R.id.etDadosReferencia);
        btSalvar = view.findViewById(R.id.btDadosSalvar);
        progressBar = view.findViewById(R.id.pbDados);
        btBuscarLocal = view.findViewById(R.id.btDadosBuscaLocal);
        //Insere máscara no campo celular
        //etCelular.addTextChangedListener(Mask.insert("(##)#####-####", etCelular));
        //etCep.addTextChangedListener(Mask.insert("#####-###", etCep));

        String[] ITENS = {
                "25 De Julho", "Alpestre", "Alpino", "Bela Aliança", "Boehmerwald", "Brasilia", "Centro", "Colonial", "Cruzeiro", "Dona Francisca", "Fundão",
                "Industrial Sudoeste", "Lençol", "Mato Preto", "Oxford", "Progresso", "Rio Negro", "Rio Vermelho", "Rio Vermelho Estação", "Rio Vermelho Povoado",
                "Schramm", "Serra Alta", "Urca", "Vila Centenário", "Vila São Paulo",
        };

        PushDownAnim.setPushDownAnimTo(btSalvar)
                .setScale( MODE_STATIC_DP, 16  )
                .setInterpolatorPush( PushDownAnim.DEFAULT_INTERPOLATOR )
                .setInterpolatorRelease( PushDownAnim.DEFAULT_INTERPOLATOR )
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nomeSalvar = etNome.getText().toString();
                String celularSalvar = etCelular.getText().toString();

                if(!nomeSalvar.isEmpty()) {
                    if(!celularSalvar.isEmpty()) {
                        if(celularSalvar.length() == 14) {
                            String ruaSalvar = etRua.getText().toString();
                            String numeroSalvar = etNumero.getText().toString();
                            String bairroSalvar = etBairro.getText().toString();
                            String referenciaSalvar = etReferencia.getText().toString();
                            if(ruaSalvar.isEmpty() || (numeroSalvar.isEmpty() && referenciaSalvar.isEmpty()) || bairroSalvar.isEmpty())
                            {
                                gerarDialogoAlertaDadosAusentes();
                            }
                            else {
                                if(Arrays.asList(ITENS).contains(bairroSalvar))
                                {
                                    salvarDados();
                                }
                                else
                                {
                                    SuperActivityToast.create(getContext(), new Style(), Style.TYPE_STANDARD)
                                            .setText("O bairro informado é inválido. Verifique e tente novamente.")
                                            .setDuration(Style.DURATION_SHORT)
                                            .setColor(getResources().getColor(R.color.marrom))
                                            .setAnimations(Style.ANIMATIONS_POP)
                                            .show();
                                    etBairro.requestFocus();
                                }
                            }
                        }
                        else{
                            SuperActivityToast.create(getContext(), new Style(), Style.TYPE_STANDARD)
                                    .setText("O numero de celular não está completo. Verifique e tente novamente.")
                                    .setDuration(Style.DURATION_SHORT)
                                    .setColor(getResources().getColor(R.color.marrom))
                                    .setAnimations(Style.ANIMATIONS_POP)
                                    .show();
                            etCelular.requestFocus();
                        }
                    }
                    else{
                        SuperActivityToast.create(getContext(), new Style(), Style.TYPE_STANDARD)
                                .setText("Por gentileza, informe seu celular.")
                                .setDuration(Style.DURATION_SHORT)
                                .setColor(getResources().getColor(R.color.marrom))
                                .setAnimations(Style.ANIMATIONS_POP)
                                .show();
                        etCelular.requestFocus();
                    }
                }
                else{
                    SuperActivityToast.create(getContext(), new Style(), Style.TYPE_STANDARD)
                            .setText("Por gentileza, informe seu nome.")
                            .setDuration(Style.DURATION_SHORT)
                            .setColor(getResources().getColor(R.color.marrom))
                            .setAnimations(Style.ANIMATIONS_POP)
                            .show();
                    etNome.requestFocus();
                }
            }
        });
        PushDownAnim.setPushDownAnimTo(btBuscarLocal)
                .setScale( MODE_STATIC_DP, 16  )
                .setInterpolatorPush( PushDownAnim.DEFAULT_INTERPOLATOR )
                .setInterpolatorRelease( PushDownAnim.DEFAULT_INTERPOLATOR )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(checkLocationPermission())
                        {
                            LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
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
                                new AlertDialog.Builder(getActivity())
                                        .setMessage("Para utilizar esta função, é necessário que a sua localização esteja ativa.")
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
                            else
                            {
                                progressBar.setVisibility(View.VISIBLE);
                                recuperarLocalizacaoDoUsuario();
                            }
                        }
                    }
                });

        ArrayAdapter<String> adapterAutoComplete = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, ITENS);
        etBairro.setAdapter(adapterAutoComplete);

        recuperaDados();

        progressBar.setVisibility(View.GONE);

        return  view;
    }

    public void buscarLocalCliente()
    {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getActivity(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(meuLocal.latitude, meuLocal.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String rua = addresses.get(0).getThoroughfare();
            String bairro = addresses.get(0).getSubLocality();
            String numero = addresses.get(0).getSubThoroughfare();

            etRua.setText(rua.trim());
            etBairro.setText(bairro.trim());
            etNumero.setText(numero.trim());
            etReferencia.setText("");

            etNumero.selectAll();
            etNumero.requestFocus();

            progressBar.setVisibility(View.GONE);

        } catch (IOException e) {
            e.printStackTrace();
            progressBar.setVisibility(View.GONE);
            SuperActivityToast.create(getContext(), new Style(), Style.TYPE_STANDARD)
                    .setText("Ocorreu um erro ao recuperar a sua localização. Verifique se sua localização está ativa e tente novamente.")
                    .setDuration(Style.DURATION_SHORT)
                    .setColor(getResources().getColor(R.color.marrom))
                    .setAnimations(Style.ANIMATIONS_POP)
                    .show();
        }
    }

    public void salvarDados()
    {
        layoutDados.requestFocus();
        String nomeSalvar = etNome.getText().toString();
        String celularSalvar = etCelular.getText().toString();
        String ruaSalvar = etRua.getText().toString();
        String numeroSalvar = etNumero.getText().toString();
        String bairroSalvar = etBairro.getText().toString();
        String referenciaSalvar = etReferencia.getText().toString();


        if(!nomeSalvar.isEmpty()) {
            if(!celularSalvar.isEmpty()) {
                if(celularSalvar.length() == 14) {

                    try{
                        //SQLiteDatabase bancoDados = openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
                        bancoDados = getActivity().openOrCreateDatabase("sandra_foods", MODE_PRIVATE, null);
                        //Executa o update do cliente
                        bancoDados.execSQL("UPDATE cliente SET nome = '"+ nomeSalvar +"', celular = '"+ celularSalvar +"', rua = '"+ ruaSalvar +"', numero = '"+ numeroSalvar +"', bairro = '"+ bairroSalvar +"', referencia = '"+ referenciaSalvar +"' WHERE id = " + idUsuario);
                        SuperActivityToast.create(getContext(), new Style(), Style.TYPE_STANDARD)
                                .setText("Dados salvos com sucesso!")
                                .setDuration(Style.DURATION_SHORT)
                                .setColor(getResources().getColor(R.color.marrom))
                                .setAnimations(Style.ANIMATIONS_POP)
                                .show();
                        ViewPager tabhost = getActivity().findViewById(R.id.viewPagerTelaPrincipal);
                        tabhost.setCurrentItem(1);
                    }
                    catch(Exception e){
                        String erro = e.toString();
                        SuperActivityToast.create(getContext(), new Style(), Style.TYPE_STANDARD)
                                .setText("Ocorreu um erro ao salvar os dados: " + erro)
                                .setDuration(Style.DURATION_SHORT)
                                .setColor(getResources().getColor(R.color.marrom))
                                .setAnimations(Style.ANIMATIONS_POP)
                                .show();
                        Log.i("ERROBANCO", erro);
                    }

                }
                else{
                    SuperActivityToast.create(getContext(), new Style(), Style.TYPE_STANDARD)
                            .setText("O numero de celular não está completo. Verifique ou digite novamente")
                            .setDuration(Style.DURATION_SHORT)
                            .setColor(getResources().getColor(R.color.marrom))
                            .setAnimations(Style.ANIMATIONS_POP)
                            .show();
                    etCelular.requestFocus();
                }
            }
            else{
                SuperActivityToast.create(getContext(), new Style(), Style.TYPE_STANDARD)
                        .setText("Por gentileza, informe seu celular.")
                        .setDuration(Style.DURATION_SHORT)
                        .setColor(getResources().getColor(R.color.marrom))
                        .setAnimations(Style.ANIMATIONS_POP)
                        .show();
                etCelular.requestFocus();
            }
        }
        else{
            SuperActivityToast.create(getContext(), new Style(), Style.TYPE_STANDARD)
                    .setText("Por gentileza, informe seu nome.")
                    .setDuration(Style.DURATION_SHORT)
                    .setColor(getResources().getColor(R.color.marrom))
                    .setAnimations(Style.ANIMATIONS_POP)
                    .show();
            etNome.requestFocus();
        }
        recuperaDados();
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

            idUsuario = Integer.parseInt(id.trim());;
        }
        preencheDados();
    }

    public void preencheDados()
    {
        if(cliente != null)
        {
            etNome.setText(cliente.getNome());
            etCelular.setText(cliente.getCelular());
            etRua.setText(cliente.getRua());
            etNumero.setText(cliente.getNumero());
            etBairro.setText(cliente.getBairro());
            etReferencia.setText(cliente.getReferencia());
        }
    }

    public void gerarDialogoAlertaDadosAusentes()
    {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(R.layout.alertdialog_dados_alerta_dados_ausentes);

        Button botaoContinuar = bottomSheetDialog.findViewById(R.id.btDadosAlertaDadosAusenteContinuar);
        Button botaoVoltar = bottomSheetDialog.findViewById(R.id.btDadosAlertaDadosAusenteVoltar);

        PushDownAnim.setPushDownAnimTo(botaoContinuar)
                .setScale( MODE_STATIC_DP, 16  )
                .setInterpolatorPush( PushDownAnim.DEFAULT_INTERPOLATOR )
                .setInterpolatorRelease( PushDownAnim.DEFAULT_INTERPOLATOR )
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarDados();
                bottomSheetDialog.dismiss();
                ViewPager tabhost = getActivity().findViewById(R.id.viewPagerTelaPrincipal);
                tabhost.setCurrentItem(1);
            }
        });
        PushDownAnim.setPushDownAnimTo(botaoVoltar)
                .setScale( MODE_STATIC_DP, 16  )
                .setInterpolatorPush( PushDownAnim.DEFAULT_INTERPOLATOR )
                .setInterpolatorRelease( PushDownAnim.DEFAULT_INTERPOLATOR )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });
        bottomSheetDialog.show();
    }

    public void gerarTutorialDados()
    {
        new ShowcaseView.Builder(getActivity())
                .withMaterialShowcase()
                .setStyle(R.style.CustomShowCaseTheme)
                .setTarget(new ViewTarget(layoutDados))
                .hideOnTouchOutside()
                .setContentTitle("Seus Dados")
                .setContentText("Aqui ficam seus dados. É importante sempre mantê-los atualizados para que tudo dê certo com seu pedido.")
                .setShowcaseEventListener(new SimpleShowcaseEventListener()
                {
                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        gerarTutorialBotaoCep();
                    }

                })
                .withHoloShowcase()
                .build();
    }
    public void gerarTutorialBotaoCep()
    {
        new ShowcaseView.Builder(getActivity())
                .withMaterialShowcase()
                .setStyle(R.style.CustomShowCaseTheme)
                .setTarget(new ViewTarget(btBuscarLocal))
                .hideOnTouchOutside()
                .setContentTitle("Buscar seu Local")
                .setContentText("Se desejar, ative sua localização e busque pelo seu local para preencher sua rua e bairro automaticamente.")
                .setShowcaseEventListener(new SimpleShowcaseEventListener()
                {
                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        ((TelaPrincipal) getActivity()).onHiddenThirdShowcase();
                    }

                })
                .withHoloShowcase()
                .build();
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 189;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                recuperarLocalizacaoDoUsuario();
            else {

            }
            return;
        }

    }
    public void recuperarLocalizacaoDoUsuario() {
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                meuLocal = new LatLng(latitude, longitude);
                buscarLocalCliente();
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

        // Now create a location manager
        //final LocationManager locationManager = getActivity().getSystemService(Context.LOCATION_SERVICE);

        // This is the Best And IMPORTANT part
        final Looper looper = null;

        // Now whenever the button is clicked fetch the location one time
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestSingleUpdate(criteria, locationListener, looper);
        }
    }


}