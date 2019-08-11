package device.models;

import com.avaje.ebean.Model;
import controllers.CommonConfig;
import org.springframework.context.annotation.Primary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
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

    public String ladder_id;

    public String item;

    public static Find<Integer, DeviceInfo> finder =
            new Find<Integer, DeviceInfo>(){};
}
