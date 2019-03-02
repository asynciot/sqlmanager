package ladder.models;

import com.avaje.ebean.Model;
import controllers.CommonConfig;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;


@Entity
public class Logs extends Model {
    @Id
    public Integer id;

    public Integer dock_id;

    public Integer device_id;

    public Date time;

    public static Finder<Integer, Logs> finder =
            new Finder<Integer, Logs>(CommonConfig.LADDER_SERVER,Integer.class, Logs.class){};
}