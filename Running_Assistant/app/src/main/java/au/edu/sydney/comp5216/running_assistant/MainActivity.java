package au.edu.sydney.comp5216.running_assistant;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //Variables to initialise the database
    RunDataDB db;
    RunDataDao DataDao;
    //Variables to apply content in ListView
    ArrayList<String> AdapterData = new ArrayList<String>();
    ArrayAdapter ListAdd;
    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Initialise and set variables
        db = RunDataDB.getDatabase(this.getApplication().getApplicationContext());
        DataDao = db.RunDataDao();
        ListAdd = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, AdapterData);
        list = (ListView) findViewById(R.id.List);
        list.setAdapter(ListAdd);
        Load();
    }
    //Read from Database
    public void Load() {

        try {
            //Use AsyncTask because Synctask is not allowed
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    AdapterData.clear();
                    List<RunData> running = DataDao.listAll();
                    if (running != null & running.size() > 0) {
                        //Get the week of first data item as current week
                        int current_week = running.get(0).getWeek();
                        // Sum of distance, running time and running days of current week
                        double Sum_distance = 0;
                        int Sum_time = 0;
                        int Sum_day = 0;
                        for (RunData item : running) {
                            //Get different data columns and calculate
                            String item_distance = item.getDistance();
                            String item_runtime = item.getRun_time();
                            double item_pace = Keep2(item.getPace());
                            double item_speed = Keep2(item.getSpeed());
                            String item_date = item.getDate();
                            int item_week = item.getWeek();
                            //If the data item comes to a new week, reset the weekly sum distance, running time and running days. Then insert the Weekly Average
                            if (item_week != current_week) {
                                AdapterData.add(" WEEKLY AVERAGE" + "\n Average speed " + Keep2(Sum_distance / Sum_time) + "m/s \n Average pace " + Keep2((Sum_distance == 0) ? 0 : Sum_time / Sum_distance) + "s/m \n Distance per running day " + Keep2(Sum_distance / Sum_day) + "m \n Time per running day " + secToTime((int) Keep2(Sum_time / Sum_day)));
                                current_week = item_week;
                                Sum_distance = 0;
                                Sum_time = 0;
                                Sum_day = 0;
                            }
                            Sum_distance += Integer.valueOf(item_distance);
                            Sum_time += Integer.valueOf(item_runtime);
                            Sum_day++;
                            AdapterData.add(" Distance " + item_distance + "m \n Time Lasted " + secToTime(Integer.valueOf(item_runtime)) + "\n Speed " + item_speed + "m/s \n Pace " + item_pace + "s/m \n Date " + item_date);
                            //Show the Weekly Average for the last week
                            if (item.getRunID() == running.get(running.size() - 1).getRunID()) {
                                AdapterData.add(" WEEKLY AVERAGE" + "\n Average speed " + Keep2(Sum_distance / Sum_time) + "m/s \n Average pace " + Keep2((Sum_distance == 0) ? 0 : Sum_time / Sum_distance) + "s/m \n Distance per running day " + Keep2(Sum_distance / Sum_day) + "m \n Time per running day " + secToTime((int) Keep2(Sum_time / Sum_day)));
                            }
                        }
                    }
                    //Update the ListView
                    if (AdapterData.size() > 0 & AdapterData != null) {
                        ListUpdate();
                    }
                    return null;
                }
            }.execute().get();
        } catch (Exception ex) {
            Log.i("readItemsFromDatabase", ex.getStackTrace().toString());
        }

    }
    //Function to update the ListView
    private void ListUpdate() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ListAdd.notifyDataSetChanged();
            }
        });
    }

    // Press button to mapsActivity
    public void onStart(View view) {
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        startActivityForResult(intent, 1);
    }
    //Press button to Calculator
    public void onCalculator(View view) {
        Intent intent = new Intent(MainActivity.this, CalcuActivity.class);
        startActivity(intent);
    }

    //Load data again when get back from mapsActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Load();
    }
    //Retain two decimal places
    private double Keep2(double d) {
        return (double) Math.round(d * 100) / 100;
    }
    //Set seconds to hh:MM:ss
    public static String secToTime(int time) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (time <= 0)
            return "00:00";
        else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    public static String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }
}
