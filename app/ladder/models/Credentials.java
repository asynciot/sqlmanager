package ladder.models;

import com.avaje.ebean.Model;
import controllers.CommonConfig;

import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
public class Credentials extends Model {
    @Id
    public Integer id;

    public Integer device_id;

    public byte[] credential;


    public static Finder<Integer, Credentials> finder =
            new Finder<Integer, Credentials>(CommonConfig.LADDER_SERVER,Integer.class, Credentials.class){};
}