package com.example.kiit1.audrinoblutooth;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.ParcelUuid;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private OutputStream outputStream;
    private InputStream inStream;
    protected static final int RESULT_SPEECH = 1;
    private TextToSpeech tts;
    public  int up=90,down=90;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        tts =new TextToSpeech(this,this);
        int position=0;
        try {
            BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
            if (blueAdapter != null) {

                if (blueAdapter.isEnabled()) {
                    Set<BluetoothDevice> bondedDevices = blueAdapter.getBondedDevices();

                    if (bondedDevices.size() > 0) {
                        Object[] devices = (Object[]) bondedDevices.toArray();
                        Toast.makeText(getBaseContext(), devices.length+"", Toast.LENGTH_LONG).show();
                        BluetoothDevice device = (BluetoothDevice) devices[position];
                        ParcelUuid[] uuids = device.getUuids();
                        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                        socket.connect();
                        outputStream = socket.getOutputStream();
                        inStream = socket.getInputStream();
                    }

                    Log.e("error", "No appropriate paired devices.");
                } else {
                    Log.e("error", "Bluetooth is disabled.");
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "SETUP Failure", Toast.LENGTH_LONG).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            //Creating dialog box
            AlertDialog alert = builder.create();
            //Setting the title manually
            alert.setTitle("Error");
            String sErr=e.toString();
            alert.setMessage(sErr);
            alert.show();

        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button2=(Button)findViewById(R.id.button2);
        Button button =(Button)findViewById(R.id.button);
        final EditText editText=(EditText)findViewById(R.id.editText);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             String str= editText.getText().toString();
                try {
                    write(str);
                }catch(Exception e){
                    Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                }
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniCall();
                Intent intent =new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
                try {
                    startActivityForResult(intent, RESULT_SPEECH);

                } catch (ActivityNotFoundException a) {
                    Toast t = Toast.makeText(getApplicationContext(),
                            "Opps! Your device doesn't support Speech to Text",
                            Toast.LENGTH_SHORT);
                    t.show();
                }
            }
        });
    }

    public void write(String s) throws IOException {
        outputStream.write(s.getBytes());
    }






    //for speech

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SPEECH: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> text = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String dataVal=text.get(0);
                    String[] strlist=dataVal.split(" ");
                    Toast.makeText(getApplicationContext(),"list"+strlist.length,Toast.LENGTH_SHORT).show();
                    tts.setSpeechRate(1.0f);
                    if(strlist.length<2 && dataVal != "up" && dataVal != "down"){
                        try {
                            tts.speak("Moveing Degres to "+strlist[0], TextToSpeech.QUEUE_FLUSH, null);
                            write(dataVal);
                        }catch(Exception e){
                            Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                        }
                    }
                    else if(strlist.length >= 2){
                        try {
                            tts.speak("Moveing Degres to "+strlist[0], TextToSpeech.QUEUE_FLUSH, null);
                            write(strlist[0]);
                        }catch(Exception e){
                            Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                        }
                    }
                    else if("up".equals(dataVal)){
                        up=up+10;
                        try {
                            tts.speak("Moveing Degres to Up as base 90 degree", TextToSpeech.QUEUE_FLUSH, null);
                            write(""+up);

                        }catch(Exception e){
                            Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                        }
                    }
                    else if("down".equals(dataVal)){
                        down=down-10;
                        try {
                            tts.speak("Moveing Degres to Down as base 90 degree", TextToSpeech.QUEUE_FLUSH, null);
                            write(""+down);
                        }catch(Exception e){
                            Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                        }
                    }


                    //txtText.setText(text.get(0));


                }
                break;
            }

        }
    }


    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.ENGLISH);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                iniCall();
//                buttonSpeak.setEnabled(true);
//                speakOut();
            }

        } else { Log.e("TTS", "Initilization Failed!");}

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
    public void iniCall(){
        tts.setSpeechRate(1.0f);
        tts.speak("Say Degres ", TextToSpeech.QUEUE_FLUSH, null);
    }
}

