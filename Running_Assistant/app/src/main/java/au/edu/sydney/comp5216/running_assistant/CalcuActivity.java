package au.edu.sydney.comp5216.running_assistant;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.regex.Pattern;

public class CalcuActivity extends AppCompatActivity {

    // Text to set result and edit-bar to input variables
    TextView result;
    EditText distance;
    EditText hour;
    EditText min;
    EditText sec;
    //Spinner to change unit of distance
    Spinner unit;
    // Strings to save pace and speed as output
    String pace = null;
    String speed = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calcu);
        initial();
    }

    //Function to initialize all components
    private void initial() {
        result = findViewById(R.id.Result);
        distance = findViewById(R.id.edit_length);
        hour = findViewById(R.id.edit_hour);
        min = findViewById(R.id.edit_min);
        sec = findViewById(R.id.edit_sec);
        unit = findViewById(R.id.Unit);
    }

    //Function to show the output in TextView
    public void onCalculate(View view) {
        result.setText(Calculate());
    }

    //Calculation process
    private String Calculate() {
        // Get the time
        double time = Toint(hour) * 3600 + Toint(min) * 60 + Toint(sec);
        //dis to save distance
        int dis = 0;
        //Determine the Length unit
        if (unit.getSelectedItem().equals("KM")) {
            dis = Toint(distance) * 1000;
        } else {
            dis = Toint(distance);
        }
        //Retain three decimal places for pace and speed
        if(time!=0 & dis!=0){
            pace = Keep3(time / dis);
            speed = Keep3(dis / time);
            return "Pace: \n" + pace + " s/m" + "\nSpeed:  \n" + speed + " m/s";
        }else{return "Illegal inputs";}


    }
    //Change the Input text to int
    private int Toint(EditText text) {
        String str = text.getText().toString();
        //If there is any character which is not a number in the input bar, it will be 0 for the input.
        if (isInteger(str)) {
            return Integer.parseInt(str);
        } else {
            return 0;
        }

    }
    // Back to main activity
    public void onBack(View view) {
        Intent intent = new Intent(CalcuActivity.this, MainActivity.class);
        startActivity(intent);
        this.finish();
    }
    //Retain three decimal places
    private String Keep3(double d) {
        double d2 = (double) Math.round(d * 1000) / 1000;
        if (d2 < 0.001) {
            return "Less than 0.001";
        } else {
            return String.valueOf(d2);
        }

    }
    // Reset all content in edittext as 0
    public void onReset(View view) {
        distance.setText("0");
        hour.setText("0");
        min.setText("0");
        sec.setText("0");
        unit.setSelection(0);
    }
    // Determine if the input is a number
    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("-?[0-9]+\\.?[0-9]*");
        return pattern.matcher(str).matches();
    }
}
