package devesh.ephrine.backup.sms.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index(value = {"timestamp", "phone", "msg", "type"}, unique = true)})
public class Sms {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    /*       @ColumnInfo(name = "first_name")
        public String firstName;

        @ColumnInfo(name = "last_name")
        public String lastName;
        */
    @ColumnInfo(name = "id")
    public String ID;

    @ColumnInfo(name = "thread_id")
    public String KEY_THREAD_ID;

    @ColumnInfo(name = "name")
    public String KEY_NAME = "name";

    @ColumnInfo(name = "phone")
    public String KEY_PHONE = "phone";

    @ColumnInfo(name = "msg")
    public String KEY_MSG = "msg";

    @ColumnInfo(name = "type")
    public String KEY_TYPE = "type";

    @ColumnInfo(name = "timestamp")
    public String KEY_TIMESTAMP = "timestamp";

    @ColumnInfo(name = "time")
    public String KEY_TIME = "time";

    @ColumnInfo(name = "read")
    public String KEY_READ = "read";

}