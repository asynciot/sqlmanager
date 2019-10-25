package ladder.models;

import com.avaje.ebean.Model;
import controllers.CommonConfig;
import ladder.models.Cellocation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;


@Entity
public class Ladder extends Model {
    @Id
    public Integer id;

    public Integer ctrl_id;

    public String name;

    public String ctrl;

    public String door1;

    public String door2;

    public String install_addr;

    public String state;

    public String item;

    public static Finder<Integer, Ladder> finder =
            new Finder<Integer, Ladder>(CommonConfig.LADDER_SERVER,Integer.class,Ladder.class){};
}