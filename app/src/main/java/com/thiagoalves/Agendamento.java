package com.thiagoalves;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class Agendamento extends AppCompatActivity {

        ArrayAdapter<String> adapter2;
        ArrayAdapter<String> adapter3;
        Spinner data;
        Spinner hora;
        int positionEscolhido2 = -1;
        int positionEscolhido = -1;
        int quadra;
        EditText nome;
        EditText cpf;
        private AdView mAdView;



        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_agendamento);

            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                }
            });

            mAdView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);


            mAdView = findViewById(R.id.adView2);
            AdRequest adRequest2 = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest2);
            //ca-app-pub-4653575622321119/3652199197



            nome = findViewById(R.id.nome);
            cpf = findViewById(R.id.cpf);
            Spinner quadra = findViewById(R.id.quadra);
            data = findViewById(R.id.spinner2);
            hora = findViewById(R.id.spinner3);

            final String array_spinner[];
            array_spinner=new String[2];
            array_spinner[0]="Quadra 1";
            array_spinner[1]="Quadra 2";

            String array_spinner2[] = new String[1];
            array_spinner2[0]="Carregando...";


            String array_spinner3[] = new String[1];
            array_spinner3[0]="Carregando...";


            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, array_spinner);
            quadra.setAdapter(adapter);

            adapter2 = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, array_spinner2);
            data.setAdapter(adapter2);

            adapter3 = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, array_spinner3);
            data.setAdapter(adapter2);

            hora.setAdapter(adapter3);

            quadra.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

                                               Socket socket = IO.socket("http://adf57058.ngrok.io");
                                               socket.connect();
                                               socket.emit("join",nome.getText().toString(), cpf.getText().toString(), positionEscolhido, hora.getSelectedItemPosition());
                                           } catch (
                                                   URISyntaxException e) {
                                               e.printStackTrace();
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

            } else {
                quadra = 20;
                this.quadra = 20;

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
