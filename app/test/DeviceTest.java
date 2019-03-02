package test;

import com.avaje.ebean.Ebean;
import controllers.CommonConfig;
import device.models.Devices;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by lengxia on 2018/11/16.
 */
public class DeviceTest extends Controller{

    @Inject
    private FormFactory formFactory;
    public Result Insert_db1_Test(){
        DynamicForm form = formFactory.form().bindFromRequest();
        int num=Integer.parseInt(form.get("num"));
        List<Devices> save_devices=new ArrayList<Devices>();
        for(int i=0;i<num;i++){
            Devices devices =new Devices();
            devices.t_update=new Date();
            devices.t_create=new Date();
            devices.t_logon=new Date();
            devices.t_logout=new Date();
            devices.cell_cid=62041;
            devices.cell_lac=34860;
            devices.cell_mnc=00;
            devices.cell_mcc=460;
            devices.IMEI=new Date().getTime()+""+i;
            devices.IMSI=new Date().getTime()+i+"s";
            devices.dock_id=i/10;
            devices.device=i%2==0?"door":"ctrl";
            Random random=new Random();
            long ss=random.nextInt(234);
            devices.ipaddr="39."+ss+".91.17";
            save_devices.add(devices);
        }
        Ebean.getServer(CommonConfig.DEVICE_SERVER).saveAll(save_devices);
        return  ok("code 0");

    }


}
