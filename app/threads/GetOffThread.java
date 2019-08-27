package threads;

import com.avaje.ebean.Ebean;
import controllers.CommonConfig;
import device.models.Devices;
import ladder.models.Offline;
import play.Logger;

import java.util.*;

public class GetOffThread extends Thread {

    private static Date old_logon =new Date();
    private static Date new_date =new Date();
    private static boolean init_device=true;
    public GetOffThread(){
        Logger.info("create GetOff Thread ok");
    }

    private void update_offline(){
        List<Devices> devicesList = Devices.finder.where().isNotNull("t_logon").gt("t_logon", old_logon).findList();
        List<Offline> save_offline= new ArrayList<>();
        for(Devices devices : devicesList){
            if(old_logon.getTime()>devices.t_logon.getTime()&&!init_device){
                continue;
            }
            ladder.models.Devices ladder_device = ladder.models.Devices.finder.byId(devices.id);
            if(ladder_device != null && devices.t_logon.getTime()>ladder_device.t_logon.getTime()){
                Offline offline  = new ladder.models.Offline();
                offline.device_id = devices.id;
                offline.t_logout = devices.t_logout;
                offline.duration = Math.toIntExact(devices.t_logon.getTime() - devices.t_logout.getTime());
                save_offline.add(offline);
            }
            new_date = new_date.getTime()>devices.t_logon.getTime()? new_date :devices.t_logon;
        }
        Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(save_offline);
    }

    @Override
    public void run() {
        while (true){
            try{
                Thread.sleep(1000);
                update_offline();
                Logger.info("Devices offline :"+ old_logon);
                old_logon = new_date;
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
