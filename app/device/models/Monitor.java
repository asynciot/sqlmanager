package device.models;

import com.avaje.ebean.Model;
import controllers.CommonConfig;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by lengxia on 2018/11/27.
 */
@Entity
public class Monitor extends Model {
    @Id
    public Integer id;

    public Integer device_id;

    public Integer session;

    public Integer sequence;
    @Column(name = "`length`")
    public Integer length;
    @Column(name = "`interval`")
    public Integer interval;

    public Date time;

    public byte[] data;

    public static Model.Finder<Integer, Monitor> finder =
            new Model.Finder<Integer, Monitor>(CommonConfig.DEVICE_SERVER,Integer.class,Monitor.class){};
}
