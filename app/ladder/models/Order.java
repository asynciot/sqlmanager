package ladder.models;

import com.avaje.ebean.Model;

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

    public Integer type;//1:故障 2:维修

    public String createTime;

    public String state;

    public Integer code;

    public String device_type;

    public String producer;

    public  Integer islast;
    public static Find<Integer, Order> finder =
            new Find<Integer, Order>(){};
}
