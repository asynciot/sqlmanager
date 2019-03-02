package ladder.models;

import com.avaje.ebean.Model;
import controllers.CommonConfig;

import javax.persistence.Entity;
import javax.persistence.Id;


/**
 * Created by lengxia on 2018/11/13.
 */
@Entity
public class Cellocation extends Model {
    @Id
    public Integer id;

    public Integer cell_mcc;

    public Integer cell_mnc;

    public Integer cell_lac;

    public Integer cell_cid;

    public Double lat;

    public Double lon;

    public Double radius;

    public String address;

    public static Finder<Integer, Cellocation> finder =
            new Finder<Integer, Cellocation>(CommonConfig.LADDER_SERVER,Integer.class,Cellocation.class){};
}
