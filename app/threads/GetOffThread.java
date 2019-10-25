package threads;

import com.avaje.ebean.Ebean;
import controllers.CommonConfig;
import device.models.Devices;
import ladder.models.Offline;
import play.Logger;

import java.util.*;

public class GetOffThread extends Thread {

    private static Date old_logon =new Date();
    private static Date old_logout =new Date();
    private static Date new_date =new Date();
    private static Date new_on =new Date();
    private static boolean init_device=true;
    public GetOffThread(){
        Logger.info("create GetOff Thread ok");
    }

    private void update_offline(){
        List<Devices> offlineList;
        if(init_device){
            offlineList= Devices.finder.where().findList();
        }
        else {
            offlineList = Devices.finder.where().isNotNull("t_logout").gt("t_logout", old_logout).findList();

        }
        List<Offline> save_offline= new ArrayList<>();
        for(Devices devices : offlineList){
            if(!init_device&&old_logout.getTime()>=devices.t_logout.getTime()){
                continue;
            }
            ladder.models.Devices ladder_device = ladder.models.Devices.finder.byId(devices.id);
            if(ladder_device != null && devices.t_logout!=null && devices.t_logout.getTime()>ladder_device.t_logout.getTime()){
                Offline offline  = new Offline();
                offline.device_id = devices.id;
                offline.t_logout = devices.t_logout;
                offline.duration = 0;
                List<Offline> offlineCount = Offline.finder.where().eq("device_id",devices.id).eq("t_logout",devices.t_logout).findList();
                if(offlineCount.size()<=0){
                    save_offline.add(offline);
                }
            }
            if(devices.t_logout!=null){
                new_date = new_date.getTime()>devices.t_logout.getTime()? new_date :devices.t_logout;
            }
        }
        Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(save_offline);
    }

    private void update_online(){
        List<Devices> onlineList = Devices.finder.where().isNotNull("t_logon").gt("t_logon", old_logon).findList();
        List<Offline> save_online= new ArrayList<>();

        for(Devices devices : onlineList){
            ladder.models.Devices ladder_device = ladder.models.Devices.finder.byId(devices.id);
            if(ladder_device != null){
                Offline offline = Offline.finder.where().eq("device_id",ladder_device.id).orderBy("id desc").setMaxRows(1).findUnique();
                if (offline!=null){
                    long time = devices.t_logon.getTime() - devices.t_logout.getTime();
                    if(time<0){
                        offline.duration = 0;
                    }else{
                        offline.duration = Math.toIntExact(time/1000);
                    }
                }
                save_online.add(offline);
            }
            new_on = new_on.getTime()>devices.t_logon.getTime()? new_on :devices.t_logon;
        }
        if(save_online.size()!=0){
            Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(save_online);
        }
    }

    @Override
    public void run() {
        while (true){
            try{
                Thread.sleep(1000);
                update_offline();
                update_online();
                Logger.info("Devices offline :"+ old_logout);
                old_logout = new_date;
                old_logon = new_on;
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
