package ladder.models;

import com.avaje.ebean.Model;
import controllers.CommonConfig;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by lengxia on 2018/12/4.
 */
@Entity
@Table(name =  "`order`")
public class Order extends Model {
    @Id
    public Integer id;

    public Integer device_id;

    public String device_type;

    public Integer code;

    public String state;

    public String createTime;

    public Integer type;//1:故障 2:维修

    public String producer;

    public Integer islast;

    public String item;

    public static Finder<Integer, Order> finder =
            new Finder<Integer, Order>(CommonConfig.LADDER_SERVER,Integer.class,Order.class){};
}
