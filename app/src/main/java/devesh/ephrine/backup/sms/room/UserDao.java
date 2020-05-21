package devesh.ephrine.backup.sms.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {
    // @Query("SELECT * FROM Sms")
    //   List<Sms> getAll();
    @Query("SELECT uid,type,msg,phone,timestamp FROM Sms")
    List<Sms> getAll();

    @Query("SELECT * FROM Sms WHERE uid IN (:userIds)")
    List<Sms> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM Sms WHERE phone IN (:phn)")
    List<Sms> loadAllByPhoneNo(String phn);

    @Query("DELETE FROM Sms WHERE uid = :uid")
    void deleteByUid(int uid);

    /*  @Query("SELECT * FROM Sms WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    User findByName(String first, String last);
*/
    @Insert
    void insertAll(Sms... sms);

    @Delete
    void delete(Sms sms);

    // void delete(int uid);

    @Query("DELETE FROM Sms")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllr(Sms sms);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllThread(List<Sms> sms);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllr2(List<Sms> sms);

    @Query("DELETE FROM Sms")
    void nukeTable();

}