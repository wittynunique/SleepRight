package com.example.iol.sleepright;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TimePicker;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;


public class MainScreen extends AppCompatActivity {
    //Globals
    HttpsURLConnection connection;

    URL url_start;
    URL url_stop;
    URL url_volume;
    URL url_mode;

    Button button_start;
    Button button_setTime;
    Button button_cons;
    Button button_inter;
    Button button_volume;

    static final int DIALOG_ID = 0;

    int hour;
    int minute;
    int curVolume = 0;

    boolean device_started = false;
    boolean device_mode_default = true;

    protected TimePickerDialog.OnTimeSetListener timePickerListener =
            new TimePickerDialog.OnTimeSetListener(){
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int min){
                    hour = hourOfDay;
                    minute = min;
                    String hourString = String.format("%02d", MainScreen.this.hour);
                    String minString = String.format("%02d", MainScreen.this.minute);
                    button_setTime.setText(hourString + " : " + minString , null);
                }
            };
    /*****************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        button_start = (Button) findViewById(R.id.startButton);
        button_cons = (Button) findViewById(R.id.button_cons);
        button_inter = (Button) findViewById(R.id.button_inter);
        button_volume = (Button) findViewById(R.id.button_volume);

        button_cons.setTextColor(Color.GREEN);
        button_cons.setEnabled(false);

        try {
            url_start = new URL("https://api.particle.io/v1/devices/350040000847343232363230/start?access_token=ac2d68a320c4cb13fae1b5980b33b9c44ae68440");
            url_stop = new URL("https://api.particle.io/v1/devices/350040000847343232363230/stop?access_token=ac2d68a320c4cb13fae1b5980b33b9c44ae68440");
            url_volume = new URL("https://api.particle.io/v1/devices/350040000847343232363230/volume?access_token=ac2d68a320c4cb13fae1b5980b33b9c44ae68440");
            url_mode = new URL("https://api.particle.io/v1/devices/350040000847343232363230/mode?access_token=ac2d68a320c4cb13fae1b5980b33b9c44ae68440");
        }
        catch( MalformedURLException e ){
            Log.d("Error: ", e.getMessage() );
        }
        setWakeTime();
        setVolume();
    }

    private class MyTask extends AsyncTask<Void, Void, Void>{

        String command_value;
        URL command_url;

        private MyTask(String arg, URL u) {
            super();
            command_value = arg;
            command_url = u;
        }

        @Override
        protected Void doInBackground(Void... params){

                try {
                    connection = (HttpsURLConnection) command_url.openConnection();
                    connection.setRequestMethod("POST");

                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    Uri.Builder builder = new Uri.Builder()
                            .appendQueryParameter("firstParam", command_value);
                    String query = builder.build().getEncodedQuery();

                    OutputStream os = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(query);
                    writer.flush();
                    writer.close();
                    os.close();

                    int c = connection.getResponseCode();
                    System.out.println("Code: " + c);
                    connection.disconnect();
                }
                catch( IllegalStateException ie ){
                    ie.printStackTrace();
                }
                catch( Exception e ){
                    e.printStackTrace();
                }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            System.out.println("Executed");
            super.onPostExecute(aVoid);
        }
    }

    public void startClick(View v) {
        if( !device_started ) {
            button_start.setText("Stop", null);
            device_started = true;

            if(device_mode_default){
                button_inter.setTextColor(Color.parseColor("#B6B6B4"));
                button_cons.setTextColor(Color.GREEN);
            }
            else{
                button_cons.setTextColor(Color.parseColor("#B6B6B4"));
                button_inter.setTextColor(Color.GREEN);
            }
            button_cons.setEnabled(false);
            button_inter.setEnabled(false);


            MyTask m = new MyTask(MainScreen.this.hour + " " + MainScreen.this.minute, url_start);
            m.execute();
        }
        else{
            button_start.setText("Start", null);
            device_started = false;

            if(device_mode_default){
                button_cons.setTextColor(Color.GREEN);
                button_inter.setTextColor(Color.BLACK);
            }
            else{
                button_inter.setTextColor(Color.GREEN);
                button_cons.setTextColor(Color.BLACK);
            }
            button_cons.setEnabled(true);
            button_inter.setEnabled(true);

            MyTask m = new MyTask("1", url_stop);
            m.execute();
        }
    }

    public void setWakeTime(){
        button_setTime = (Button)findViewById(R.id.set_time);
        button_setTime.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        showDialog(DIALOG_ID);
                    }
                }
        );
    }

    public void setVolume(){
        button_volume = (Button)findViewById(R.id.button_volume);
        button_volume.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        showDialog();
                    }
                }
        );
    }

    public void setMode(View v){
        if( device_mode_default ) {
            device_mode_default = false;
            button_cons.setTextColor(Color.BLACK);
            button_inter.setTextColor(Color.GREEN);
            button_cons.setEnabled(true);
            button_inter.setEnabled(false);
            MyTask m = new MyTask("1", url_mode);
            m.execute();
        }
        else{
            device_mode_default = true;
            button_inter.setTextColor(Color.BLACK);
            button_cons.setTextColor(Color.GREEN);
            button_inter.setEnabled(true);
            button_cons.setEnabled(false);
            MyTask m = new MyTask("0", url_mode);
            m.execute();
        }
        Log.d("Mode:", ""+device_mode_default );
    }

    @Override
    protected Dialog onCreateDialog(int id){
        if( id == DIALOG_ID )
            return new TimePickerDialog(MainScreen.this, timePickerListener, hour, minute, true);
        return null;
    }

    public void showDialog(){
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        final SeekBar seek = new SeekBar(this);
        seek.setMax(255);
        seek.setKeyProgressIncrement(1);
        seek.setProgress(curVolume);

        popDialog.setIcon(android.R.drawable.presence_audio_online);
        popDialog.setTitle("Set Volume");
        popDialog.setView(seek);

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                curVolume = progress;
                //Log.d("CurVolume: ", ""+curVolume);
                MyTask m = new MyTask( ""+progress, url_volume);
                m.execute();
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        popDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        popDialog.create();
        popDialog.show();
    }
}

    //curl https://api.particle.io/v1/devices/35040000847343232363230/volume -d arg="50" -d access_token=ac2d68a320c4cb13fae1b5980b33b9c44ae68440




////////////////////////////////////////////////
//Volume (-1 up, -2 down, 0-255 valid )
//Wake up time ( 0-23, 0-59 ) Military time maybe convert.
//Pause, Stop, Reset (1, 1, 1)
//Save device ID and replace the URL string ( All me ) Rob wrote to a text file.
//Select between implementations  Intermittent/Constant 0/1/X