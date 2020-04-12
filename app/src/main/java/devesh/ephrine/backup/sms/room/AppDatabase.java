package devesh.ephrine.backup.sms.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Sms.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
}