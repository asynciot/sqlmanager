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
public class Binaries extends Model {
    @Id
    public Integer id;
    public  String name;

    public String type;

    public Date t_create;

    public byte[] data;


    public static Finder<Integer, Binaries> finder =
            new Finder<Integer, Binaries>(CommonConfig.LADDER_SERVER,Integer.class, Binaries.class){};
}
