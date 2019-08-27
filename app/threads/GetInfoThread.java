package threads;

import com.avaje.ebean.Ebean;
import controllers.CommonConfig;
import device.models.Monitor;
import play.Logger;

import java.util.*;

/**
 * Created by lengxia on 2018/11/28.
 */
public class GetInfoThread extends Thread {

    private static Date old_datex=new Date();
    private static Date new_datex=new Date();
    private static boolean init_device=true;
    public GetInfoThread(){
        Logger.info("create GetInfo Thread ok");
    }

    private void update_monitor(){
        List<Monitor> monitorList= Monitor.finder.where().isNotNull("time").gt("time",old_datex).findList();
        List<ladder.models.Monitor> new_monitorList= new ArrayList<>();

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
                    new_datex = new Date();
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
                old_datex=new_datex;
                if(init_device){
                    Logger.info("init ok");
                    init_device=false;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
