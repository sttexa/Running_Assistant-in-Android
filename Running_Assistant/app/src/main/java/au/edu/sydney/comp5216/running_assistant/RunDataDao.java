package au.edu.sydney.comp5216.running_assistant;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RunDataDao {
    @Query("SELECT * FROM statistics")
    List<RunData> listAll();

    @Insert
    void insert(RunData statistics);

    @Insert
    void insertAll(RunData... statistics);

    @Query("DELETE FROM statistics")
    void deleteAll();
}