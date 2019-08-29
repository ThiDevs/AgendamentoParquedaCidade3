package com.thiagoalves;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class Agendamento extends AppCompatActivity {

        ArrayAdapter<String> adapter2;
        ArrayAdapter<String> adapter3;
        Spinner data;
        Spinner hora;
        Spinner quadraSpinner;
        int positionEscolhido2 = -1;
        int positionEscolhido = -1;
        int quadra;

        EditText nome;
        EditText cpf;
        private AdView mAdView;
    String link = "";
    private InterstitialAd mInterstitialAd;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_agendamento);
            FirebaseApp.initializeApp(this);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("url")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    link = document.getData().get("link").toString();
                                    Log.d("firethiago", document.getData().get("link").toString());
                                }
                            } else {
                                Log.w("firethiago", "Error getting documents.", task.getException());
                            }
                        }
                    });

            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                }
            });

            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                }
            });

            mAdView = findViewById(R.id.adView);
            mAdView.loadAd(new AdRequest.Builder().build());

            mAdView = findViewById(R.id.adView2);
            mAdView.loadAd(new AdRequest.Builder().build());


            nome = findViewById(R.id.nome);
            cpf = findViewById(R.id.cpf);
            quadraSpinner = findViewById(R.id.quadra);
            data = findViewById(R.id.spinner2);
            hora = findViewById(R.id.spinner3);

            final String array_spinner[];
            array_spinner=new String[4];
            array_spinner[0]="Quadra 1";
            array_spinner[1]="Quadra 2";
            array_spinner[2]="Quadra de tênis";
            array_spinner[3]="Campo Society";

            String array_spinner2[] = new String[1];
            array_spinner2[0]="Carregando...";


            String array_spinner3[] = new String[1];
            array_spinner3[0]="Carregando...";


            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, array_spinner);
            quadraSpinner.setAdapter(adapter);

            adapter2 = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, array_spinner2);
            data.setAdapter(adapter2);

            adapter3 = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, array_spinner3);
            data.setAdapter(adapter2);

            hora.setAdapter(adapter3);

            quadraSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                    try {
                        if(position != positionEscolhido2) {
                            ArrayList<String> datas = getQuadra(position);
                            String[] b = new String[datas.size()];
                            for (int i = 0; i != datas.size(); i++) {
                                b[i] = datas.get(i);
                            }
                            getNewAdapter(data, b);
                            positionEscolhido2 = position;
                        }
                    }catch (Exception e){e.printStackTrace();}
                }
                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }
            });

            data.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                    try {

                        if(position != positionEscolhido){
                            ArrayList<String> datas = getHoras(data.getItemAtPosition(position).toString().split("/")[2] + "-" + data.getItemAtPosition(position).toString().split("/")[1] + "-" +data.getItemAtPosition(position).toString().split("/")[0]);
                            String[] b = new String[datas.size()];
                            for(int i = 0; i != datas.size(); i ++){
                                b[i] = datas.get(i);
                            }
                            getNewAdapter(hora,b);
                            positionEscolhido = position;
                        }

                    }catch (Exception e){e.printStackTrace();}
                }
                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }
            });

            Button btn = (Button)findViewById(R.id.button) ;

            btn.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           try {


                                               Socket socket = IO.socket(link);
                                               socket.connect();
                                               socket.emit("join",nome.getText().toString(), cpf.getText().toString(), positionEscolhido, hora.getSelectedItemPosition(),quadraSpinner.getSelectedItemPosition() +3 );

                                               if (mInterstitialAd.isLoaded()) {
                                                   mInterstitialAd.show();
                                               }

                                               //socket.disconnect();
                                           } catch (Exception e) {
                                           }
                                       }
                                   }
            );

        }

        public void getNewAdapter(Spinner data, String[] array){

            ArrayAdapter<String> newArray = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, array);

            data.setAdapter(newArray);
        }
        public ArrayList<String> getQuadra(int quadra){
            if(quadra == 0){
                quadra = 19;
                this.quadra = 19;

            } else if(quadra == 1){
                quadra = 20;
                this.quadra = 20;

            } else if(quadra == 2){
                quadra = 21;
                this.quadra = 21;
            }else if(quadra == 3){
                quadra = 22;
                this.quadra = 22;
            }
            RequestTask a = new RequestTask();
            AsyncTask<String, String, String> retorno = a.execute("http://agendamento.serra.es.gov.br/api/servicos/"+ quadra +"/unidades/45");
            try{

                String json = retorno.get();
                JSONObject obj = new JSONObject(json);
                JSONArray dias = obj.getJSONArray("diasDisponiveis");

                ArrayList<String> listdata = new ArrayList<String>();
                JSONArray jArray = dias;
                for (int i=0;i<jArray.length();i++){
                    listdata.add(jArray.getString(i).split("-")[2] + "/" + jArray.getString(i).split("-")[1] + "/" + jArray.getString(i).split("-")[0]  );
                }

                return listdata;
            }catch (Exception e){e.printStackTrace();}

            return new ArrayList<String>();
        }

        public ArrayList<String> getHoras(String data){

            RequestTask a = new RequestTask();
            AsyncTask<String, String, String> retorno = a.execute("http://agendamento.serra.es.gov.br/api/servicos/"+ quadra +"/unidades/45/horarios/" + data);
            try{

                String json = retorno.get();
                JSONArray obj = new JSONArray(json);
                ArrayList<String> listdata = new ArrayList<String>();
                JSONArray jArray = obj;
                for (int i=0;i<jArray.length();i++){
                    listdata.add(jArray.getString(i) );
                }

                return listdata;

            }catch (Exception e){e.printStackTrace();}

            return new ArrayList<String>();
        }

    }
