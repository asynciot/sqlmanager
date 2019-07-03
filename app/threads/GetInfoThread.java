package threads;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.javafx.css.parser.LadderConverter;
import controllers.CommonConfig;
import device.models.Devices;
import device.models.Events;
import device.models.Monitor;
import device.models.Runtime;
import ladder.models.Ladder;
import ladder.models.DeviceInfo;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

import java.util.*;
import java.util.concurrent.CompletionStage;

/**
 * Created by lengxia on 2018/11/28.
 */
public class GetInfoThread extends Thread {

    public static Date old_datex=new Date();
    public static Date new_datex=new Date();
    public static Date datex_one=new Date();
    public static Date datex_two=new Date();
    public static boolean init_device=true;
    private static int TIME_OUT = 5000;
    public GetInfoThread(){
        Logger.info("create GetInfo Thread ok");
    }

    public void update_monitor(){
        List<Monitor> monitorList= Monitor.finder.where().isNotNull("time").gt("time",old_datex).findList();
        List<ladder.models.Monitor> new_monitorList=new ArrayList<ladder.models.Monitor>();

        Logger.error(monitorList.size()+"----");
        for(Monitor monitor:monitorList){
            if(old_datex.getTime()>=monitor.time.getTime()){
                continue;
            }
            if(CommonConfig.device_monitor_set.contains(monitor.device_id)){
                ladder.models.Monitor monitor_new=new ladder.models.Monitor();
                monitor_new.data=monitor.data;
                monitor_new.session=monitor.session;
                monitor_new.interval=monitor.interval;
                monitor_new.length=monitor.length;
                monitor_new.time = monitor.time;
                monitor_new.sequence=monitor.sequence;
                monitor_new.device_id=monitor.device_id;
                new_monitorList.add(monitor_new);
                new_datex=(new_datex.getTime()>monitor.time.getTime())?new_datex:monitor.time;
                if(monitor.length==0){
                    String sql= String.format("UPDATE ladder.device_info set commond='%s' where id=%d","ok",monitor.device_id);
                    Ebean.getServer(CommonConfig.LADDER_SERVER).createSqlUpdate(sql).execute();
                    CommonConfig.device_monitor_set.remove(monitor.device_id);
                }
            }
        }
        Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(new_monitorList);
    }
    @Override
    public void run() {
        while (true){
            try{
                Thread.sleep(1000);
                update_monitor();
                Logger.info("Move monitor from db1 to db2 ok at :"+new_datex);
                old_datex=new Date();
                if(init_device==true){
                    Logger.info("init ok");
                    init_device=false;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
