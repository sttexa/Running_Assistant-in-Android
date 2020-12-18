package au.edu.sydney.comp5216.running_assistant;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {RunData.class}, version = 1, exportSchema = false)
public abstract class RunDataDB extends RoomDatabase {
    // The name of database is Rundata_db
    private static final String DATABASE_NAME = "rundata_db";
    private static RunDataDB DBINSTANCE;

    public abstract RunDataDao RunDataDao();

    public static RunDataDB getDatabase(Context context) {
        if (DBINSTANCE == null) {
            synchronized (RunDataDB.class) {
                DBINSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        RunDataDB.class, DATABASE_NAME).build();
            }
        }
        return DBINSTANCE;
    }

    public static void destroyInstance() {
        DBINSTANCE = null;
    }
}
