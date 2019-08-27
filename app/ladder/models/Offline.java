package ladder.models;

import com.avaje.ebean.Model;
import controllers.CommonConfig;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by lengxia on 2018/11/28.
 */

@Entity
public class Offline extends Model {
    @Id
    public Integer id;

    public Integer device_id;

    public Date t_logout;

    public Integer duration;

    public static Finder<Integer, Offline> finder =
            new Finder<Integer, Offline>(CommonConfig.LADDER_SERVER,Integer.class, Offline.class){};
}
