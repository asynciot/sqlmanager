package ladder.models;

import com.avaje.ebean.Model;
import controllers.CommonConfig;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by lengxia on 2018/11/23.
 */
@Entity
public class DeviceInfo extends Model{
    @Id
    public Integer id;

    public String IMEI;

    public Integer IPlocation_id;

    public Integer cellocation_id;

    public String device_name;

    public String maintenance_type;

    public String maintenance_nexttime;

    public String maintenance_remind;

    public String maintenance_lasttime;

    public String inspection_type;

    public String inspection_lasttime;

    public String inspection_nexttime;

    public String inspection_remind;

    public String install_date;

    public String install_addr;

    public String register;

    public String tagcolor;

    public String state;

    public String device_type;

    public String commond;

    public String delay;

    public Integer rssi;

    public Integer runtime_state;

    public Integer group_id;

    public Integer ladder_id;

    public static Finder<Integer, DeviceInfo> finder =
            new Finder<Integer, DeviceInfo>(CommonConfig.LADDER_SERVER,Integer.class,DeviceInfo.class){};
}
