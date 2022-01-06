package com.ssoft.sandrafoodsapp.activity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.ssoft.sandrafoodsapp.R;
import com.ssoft.sandrafoodsapp.helper.LatLngInterpolator;
import com.ssoft.sandrafoodsapp.helper.MarkerAnimation;
import com.ssoft.sandrafoodsapp.model.Andamento;
import com.ssoft.sandrafoodsapp.model.PosicaoMotoboy;
import com.thekhaeng.pushdownanim.PushDownAnim;

import static com.thekhaeng.pushdownanim.PushDownAnim.MODE_STATIC_DP;

public class TelaAcompanharMotoboy extends FragmentActivity implements OnMapReadyCallback {

    DatabaseReference firebaseBanco = FirebaseDatabase.getInstance().getReference();
    DatabaseReference andamentoDB;
    private Andamento recuperadoFB = new Andamento();

    private String chavePedido;
    private String dataPedido;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng meuLocal;
    private LatLng sandraFoodsAntigo = new LatLng(-26.277517977364223, -49.38532104724969);
    private LatLng sandraFoods = new LatLng(-26.277517977364223, -49.38532104724969);
    private GoogleMap mMap;
    private int parametro = 0; //usado para posicionar a camera no motoboy só ao acessar a tela
    private Button btVoltar;
    private FloatingActionButton btFocarMotoboy, btFocarMeuLocal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        andamentoDB = firebaseBanco.child("andamento");
        try {
            Bundle dados = getIntent().getExtras();
            chavePedido = dados.getString("chavePedido");
            dataPedido = dados.getString("dataPedido");
        } catch (Exception e) {
            SuperActivityToast.create(this, new Style(), Style.TYPE_STANDARD)
                    .setText("Erro ao abrir tela de acompanhamento.")
                    .setDuration(Style.DURATION_SHORT)
                    .setColor(getResources().getColor(R.color.marrom))
                    .setAnimations(Style.ANIMATIONS_POP)
                    .show();
            Intent intent = new Intent(getApplicationContext(), TelaBemVindo.class);
            startActivity(intent);
        }
        setContentView(R.layout.activity_tela_acompanhar_motoboy);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btVoltar = findViewById(R.id.btVoltarTelaAcompanharMotoboy);
        btFocarMotoboy = findViewById(R.id.btFocarMotoboy);
        btFocarMeuLocal = findViewById(R.id.btFocarMeuLocal);

        PushDownAnim.setPushDownAnimTo(btVoltar)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        voltarPressionado();
                    }
                });

        PushDownAnim.setPushDownAnimTo(btFocarMotoboy)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(sandraFoods, 18)
                        );
                    }
                });

        PushDownAnim.setPushDownAnimTo(btFocarMeuLocal)
                .setScale( MODE_STATIC_DP, 8  )
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(meuLocal != null)
                        {
                            mMap.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(meuLocal, 18)
                            );
                        }
                        else
                        {
                            SuperActivityToast.create(TelaAcompanharMotoboy.this ,new Style(), Style.TYPE_STANDARD)
                                    .setText("Erro ao recuperar o seu local. Tente acessar novamente esta tela.")
                                    .setDuration(Style.DURATION_SHORT)
                                    .setColor(getResources().getColor(R.color.marrom))
                                    .setAnimations(Style.ANIMATIONS_POP)
                                    .show();
                        }
                    }
                });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (checkLocationPermission()) {
            sandraFoodsAntigo = new LatLng(-26.277513758824416, -49.385326971675134);
            mMap.addMarker(new MarkerOptions()
                    .position(sandraFoods)
                    .title("Motoboy")
                    .icon(
                            BitmapDescriptorFactory.fromResource(R.drawable.icone_motoboy)
                    )
            );
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sandraFoods, 18));
            recuperarLocalizacaoDoUsuario();
        }
        /*
        // Add a marker in Sydney and move the camera -26.277513758824416, -49.385326971675134
        sandraFoods = new LatLng(-26.277513758824416, -49.385326971675134);
        mMap.addMarker(new MarkerOptions()
                .position(sandraFoods)
                .title("Sandra Foods")
                .icon(
                        BitmapDescriptorFactory.fromResource(R.drawable.icone_motoboy)
                )
        );
        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(sandraFoods, 18)
        );*/
    }

    public void criaListenerParaAlteracaoDePosicao() {
        DatabaseReference andamentoDB = firebaseBanco.child("posicao-motoboy");
        andamentoDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                PosicaoMotoboy recuperado = snapshot.getValue(PosicaoMotoboy.class);
                if(sandraFoods != null)
                    sandraFoodsAntigo = sandraFoods;
                Double lat = recuperado.getLatitude();
                Double lon = recuperado.getLongitude();
                sandraFoods = new LatLng(lat, lon);
                mMap.clear();
                Marker markerMotoboy = mMap.addMarker(new MarkerOptions()
                                .position(sandraFoodsAntigo)
                                .title("Entregador")
                                .icon(
                                        BitmapDescriptorFactory.fromResource(R.drawable.icone_motoboy)
                                )
                );
                MarkerAnimation.animateMarkerToICS(markerMotoboy, sandraFoods, new LatLngInterpolator.Spherical());

                mMap.addMarker(new MarkerOptions()
                        .position(meuLocal)
                        .title("Meu Local")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                );
                if(parametro == 0)
                {
                    mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(sandraFoods, 18)
                    );
                    parametro = 1;
                }
                /*
                CÓDIGO ANTIGO:
                PosicaoMotoboy recuperado = snapshot.getValue(PosicaoMotoboy.class);
                Double lat = recuperado.getLatitude();
                Double lon = recuperado.getLongitude();

                sandraFoods = new LatLng(lat, lon);

                mMap.clear();

                mMap.addMarker(new MarkerOptions()
                        .position(meuLocal)
                        .title("Meu Local")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                );

                mMap.addMarker(new MarkerOptions()
                        .position(sandraFoods)
                        .title("Entregador")
                        .icon(
                                BitmapDescriptorFactory.fromResource(R.drawable.icone_motoboy)
                        )
                );
                if(parametro == 0)
                {
                    mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(sandraFoods, 18)
                    );
                    parametro = 1;
                }
                */
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public void recuperarLocalizacaoDoUsuario() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                meuLocal = new LatLng(latitude, longitude);
                criaListenerParaAlteracaoDePosicao();
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 10, locationListener);
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Acessar sua Localização")
                        .setMessage("Para acompanhar a sua entrega, é necessário que forneça sua localização. Permitir acesso à sua localização?")
                        .setPositiveButton("Permitir", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(TelaAcompanharMotoboy.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
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
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        recuperarLocalizacaoDoUsuario();
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        voltarPressionado();
    }

    public void voltarPressionado()
    {
        Intent intent = new Intent(getApplicationContext(), TelaAcompanhamentoPedido.class);
        intent.putExtra("dataPedido", dataPedido);
        intent.putExtra("chavePedido", chavePedido);
        startActivity(intent);
    }


}