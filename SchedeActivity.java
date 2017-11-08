package com.google.android.gms.samples.vision.barcodereader;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import java.util.Locale;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;


public class SchedeActivity extends Activity implements TextToSpeech.OnInitListener {

    //public String str_finale = "";
    private String server = "http://192.168.1.5";
    private String url = server + "/wordpress/wp-content/plugins/wp-schedeReperto/prova.php";
    private String idSchedaReceived;
    TextView titoloView;
    TextView descrizionesView;
    TextView descrizioneeView;
    private TextView myAwesomeTextView;
    private TextToSpeech tts;
    private FloatingActionButton btnSpeak;

    boolean riproduzione_in_corso = false;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schede);

        tts = new TextToSpeech(this, this);
        btnSpeak = (FloatingActionButton) findViewById(R.id.fab);
        Intent intent = getIntent();
        idSchedaReceived = intent.getExtras().getString("idScheda");
        riproduzione_in_corso = false;
        titoloView = (TextView) findViewById(R.id.titolo);
        descrizionesView = (TextView) findViewById(R.id.descShort);
        descrizioneeView = (TextView) findViewById(R.id.descExt);


        class HttpGetTask extends AsyncTask<String, String, String> {

            @Override
            protected String doInBackground(String... params) {
                String result = "";
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("idScheda", idSchedaReceived));
                InputStream is = null;

                //http post
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(url);
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                } catch (Exception e) {
                    Log.e("TEST", "Errore nella connessione http " + e.toString());
                }
                if (is != null) {
                    //converto la risposta in stringa
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                        StringBuilder sb = new StringBuilder();
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        is.close();

                        result = sb.toString();
                    } catch (Exception e) {
                        Log.e("TEST", "Errore nel convertire il risultato " + e.toString());
                    }

                    System.out.println(result);


                } else {

                }

                return result;

            }

            @Override
            protected void onProgressUpdate(String... values) {

            }

            @Override
            protected void onPostExecute(String result) {

                try {
                    JSONObject jo = new JSONObject(result);
                    Log.i("TEST", "id: " + jo.getString("titolo") +
                            ", descriziones: " + jo.getString("descriziones") +
                            ", descrizionee: " + jo.getString("descrizionee")
                    );

                    String titolo = jo.get("titolo").toString();
                    String descriziones = jo.get("descriziones").toString();
                    String descrizionee = jo.get("descrizionee").toString();

                    titoloView.setText(titolo);
                    descrizionesView.setText(descriziones);
                    descrizioneeView.setText(descrizionee);

                } catch (JSONException e) {
                    Log.e("log_tag", "Error parsing data " + e.toString());
                }
            }
        }

        HttpGetTask task = new HttpGetTask();
        task.execute();

        myAwesomeTextView = (TextView)findViewById(R.id.myAwesomeTextView);
        myAwesomeTextView.setVisibility(TextView.INVISIBLE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                if(riproduzione_in_corso==false) {
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SchedeActivity.this);
                    alertDialogBuilder.setMessage("Attivare riproduzione Audio?");
                    alertDialogBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            myAwesomeTextView.setText(titoloView.getText().toString() + descrizionesView.getText().toString() + descrizioneeView.getText().toString());
                            speakOut();
                            riproduzione_in_corso = true;
                        }
                    })
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    riproduzione_in_corso = false;
                                }
                            });
                    alertDialogBuilder.create();
                    alertDialogBuilder.show();
                }

                else {
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SchedeActivity.this);
                    alertDialogBuilder.setMessage("Disattivare Audio?");
                    alertDialogBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int id) {
                            tts.stop();
                            riproduzione_in_corso = false;
                        }
                    })

                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    riproduzione_in_corso = true;
                                }
                            });

                    alertDialogBuilder.create();
                    alertDialogBuilder.show();
                }
            }
        });

    }
    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            //int result = tts.setLanguage(Locale.US);
            int result = tts.setLanguage(Locale.ITALIAN);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                btnSpeak.setEnabled(true);
                speakOut();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }

    private void speakOut() {
        String text = myAwesomeTextView.getText().toString();
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

}