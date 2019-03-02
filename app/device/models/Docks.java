package device.models;

import com.avaje.ebean.Model;
import controllers.CommonConfig;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;


@Entity
public class Docks extends Model {


    @Id
    public Integer id;

    public String name;

    @Column(name = "`desc`")
    public String desc;

    public Date t_create;

    public Date t_update;

    public Date t_logon;

    public Date t_logout;

    public String ipaddr;

    public String uuid;

    public static Finder<Integer, Docks> finder =
            new Finder<Integer, Docks>(CommonConfig.DEVICE_SERVER,Integer.class,Docks.class){};
}