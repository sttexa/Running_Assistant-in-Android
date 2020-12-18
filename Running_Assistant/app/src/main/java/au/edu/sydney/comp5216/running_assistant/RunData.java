package au.edu.sydney.comp5216.running_assistant;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "statistics")
public class RunData {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "runID")
    //Primary Key for a running
    private int runID;

    @ColumnInfo(name = "distance")
    //The distance of one running
    private String distance;

    @ColumnInfo(name = "run_time")
    // The duration of one running
    private String run_time;

    @ColumnInfo(name = "pace")
    // The pace of one running
    private double pace;

    @ColumnInfo(name = "speed")
    // The speed of one running
    private double speed;

    @ColumnInfo(name = "date")
    // When the running happened
    private String date;

    @ColumnInfo(name = "week")
    // Which week of the year the running happened
    private int week;

    // Constructor
    public RunData(String distance, String run_time, double pace, double speed,
                   String date, int week) {
        this.distance = distance;
        this.run_time = run_time;
        this.pace = pace;
        this.speed = speed;
        this.date = date;
        this.week = week;
    }

    //Get functions
    public int getRunID() {
        return runID;
    }

    public String getDistance() {
        return distance;
    }

    public String getRun_time() {
        return run_time;
    }

    public double getPace() {
        return pace;
    }

    public double getSpeed() {
        return speed;
    }

    public String getDate() {
        return date;
    }

    public int getWeek() {
        return week;
    }


    //Set functions
    public void setRunID(int runID) {
        this.runID = runID;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setRun_time(String run_time) {
        this.run_time = run_time;
    }

    public void setPace(double pace) {
        this.pace = pace;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setWeek(int week) {
        this.week = week;
    }

}


