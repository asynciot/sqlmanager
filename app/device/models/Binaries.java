package device.models;

import com.avaje.ebean.Model;
import controllers.CommonConfig;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by lengxia on 2018/11/28.
 */

@Entity
public class Binaries extends Model {
    @Id
    public Integer id;

    public Date t_create;

    public byte[] data;

    public static Model.Finder<Integer, Binaries> finder =
            new Model.Finder<Integer, Binaries>(CommonConfig.DEVICE_SERVER,Integer.class,Binaries.class){};
}
