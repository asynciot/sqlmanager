package device.models;

import com.avaje.ebean.Model;
import controllers.CommonConfig;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;


@Entity
public class Runtime extends Model {
    @Id
    public Integer id;

    public  Integer device_id;

    public Integer type;

    public byte[] data;

    public Date t_update;

    public static Finder<Integer, Runtime> finder =
            new Finder<Integer, Runtime>(CommonConfig.DEVICE_SERVER,Integer.class,Runtime.class){};
}