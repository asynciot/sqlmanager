package test;

import com.avaje.ebean.Ebean;
import controllers.CommonConfig;
import device.models.Devices;
import device.models.Events;
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
 * Created by lengxia on 2018/11/22.
 */
public class EventTest extends Controller {
    @Inject
    FormFactory formFactory;

    Random random=new Random();

    public byte[] getdata(int num){
        byte[] bytes=new byte[8*num];
        for(int i=0;i<num;i++){

            bytes[i*8+0]=(byte)random.nextInt(254);
            bytes[i*8+1]=(byte)random.nextInt(254);
            bytes[i*8+2]=(byte)random.nextInt(254);
            bytes[i*8+3]=(byte)random.nextInt(254);
            bytes[i*8+4]=(byte)random.nextInt(254);
            bytes[i*8+5]=(byte)random.nextInt(254);
            bytes[i*8+6]=(byte)random.nextInt(254);
            bytes[i*8+7]=(byte)random.nextInt(254);

        }

        return bytes;
    }
    public Result Insert_db2_Test(){

        DynamicForm form = formFactory.form().bindFromRequest();
        int num=Integer.parseInt(form.get("num"));
        List<Events> eventsList =new ArrayList<Events>();
        List<Devices> devicesList = Devices.finder.setMaxRows(num).findList();
        int interval=100;
        int blobnums=10;
        for(int i=0;i<num;i++){
            Events events =new Events();
            events.time=new Date();
            events.length=blobnums;
            events.device_id= devicesList.get(i).id;
            events.interval=interval;
            events.data=getdata(events.length);
            eventsList.add(events);

        }
        Ebean.getServer(CommonConfig.DEVICE_SERVER).saveAll(eventsList);

        return  ok("code 0");

    }

}
