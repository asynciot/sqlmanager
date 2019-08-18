package threads;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.javafx.css.parser.LadderConverter;
import controllers.CommonConfig;
import device.models.Devices;
import device.models.DeviceInfo;
import device.models.Runtime;
import ladder.models.Ladder;
import ladder.models.Offline;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

import java.util.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.CompletionStage;

/**
 * Created by lengxia on 2018/11/28.
 */
public class GetOffThread extends Thread {

    public static Date old_logout=new Date();
    public static Date new_datex=new Date();
    public static boolean init_device=true;
    private static int TIME_OUT = 5000;
    public GetOffThread(){
        Logger.info("create GetOff Thread ok");
    }


    public void update_offline(){
        List<Devices> devicesList=null;
        devicesList = Devices.finder.where().isNotNull("t_logout").gt("t_logout",old_logout).findList();
        List<Offline> save_offline=new ArrayList<Offline>();
        for(Devices devices : devicesList){
            if(old_logout.getTime()>devices.t_logout.getTime()&&init_device==false){
                continue;
            }
            ladder.models.Devices lad_devices = ladder.models.Devices.finder.byId(devices.id);
            if(devices.t_logout.getTime()>lad_devices.t_logout.getTime()){
                Offline offline  = new ladder.models.Offline();
                offline.device_id = devices.id;
                offline.t_logout = devices.t_logout;
                save_offline.add(offline);
            }
            new_datex=new_datex.getTime()>devices.t_logout.getTime()?new_datex:devices.t_logout;
        }
        Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(save_offline);
		
		String time=(new Date().getTime()-28800000)+"";
		List<Devices> operating=null;
		operating = Devices.finder.where().isNotNull("t_logout").le("t_logout",time).findList();
		List<DeviceInfo> nodate=null;
		nodate = DeviceInfo.finder.where().isNotNull("install_date").in("id",operating.id).findList();
		List<DeviceInfo> save=new ArrayList<DeviceInfo>();
		for(DeviceInfo adddate : nodate){
			DeviceInfo add  = new device.models.DeviceInfo();
			add = adddate;
			add.install_date=time;
			save.add(add);
		}
		Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(save);
    }

    @Override
    public void run() {
        while (true){
            try{
                Thread.sleep(1000);
                update_offline();
                Logger.info("Move Info from db1 to db2 ok at :"+old_logout);
                old_logout=new_datex;
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
