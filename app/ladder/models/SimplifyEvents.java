package ladder.models;

import com.avaje.ebean.Model;
import controllers.CommonConfig;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;


@Entity
public class SimplifyEvents extends Model {
    @Id
    public Integer id;

    public Integer device_id;

    public String event_type;

    public String device_type;

    public Date start_time;

    public Date end_time;

    public Integer current;

    public Integer speed;

    public static Finder<Integer, SimplifyEvents> finder =
            new Finder<Integer, SimplifyEvents>(CommonConfig.LADDER_SERVER,Integer.class, SimplifyEvents.class){};
}