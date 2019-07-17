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
import ladder.models.Offline;
import ladder.models.DeviceInfo;
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
public class GetDevThread extends Thread {

    public static Date old_datex=new Date();
    public static Date old_logout=new Date();
    public static Date new_datex=new Date();
    public static Date datex_one=new Date();
    public static Date datex_two=new Date();
    public static boolean init_device=true;
    private static int TIME_OUT = 5000;
    public GetDevThread(){
        Logger.info("create GetInfo Thread ok");
    }

    public void update_device(){
        List<Devices> devicesList=null;
        if(init_device){
            devicesList= Devices.finder.where().findList();
        }
        else {
            devicesList = Devices.finder.where().isNotNull("t_update").gt("t_update",old_datex).findList();
        }
        List<ladder.models.Devices> save_devices=new ArrayList<ladder.models.Devices>();
        List<ladder.models.Devices> delete_devices=new ArrayList<ladder.models.Devices>();
        List<DeviceInfo> deviceInfoList=new ArrayList<DeviceInfo>();
        List<Ladder> ladderList=new ArrayList<Ladder>();
        List<Offline> save_offline=new ArrayList<Offline>();
        for(Devices devices : devicesList){
            if(old_datex.getTime()>=devices.t_update.getTime()&&init_device==false){
                continue;
            }
            ladder.models.Devices mamodel= ladder.models.Devices.finder.byId(devices.id);
            if(mamodel!=null){
                delete_devices.add(mamodel);
            }
            ladder.models.Devices machine_device  = new ladder.models.Devices();
            machine_device.id= devices.id;
            machine_device.dock_id= devices.dock_id;
            machine_device.IMSI= devices.IMSI;
            machine_device.IMEI= devices.IMEI;
            machine_device.board= devices.board;
            machine_device.cellular= devices.cellular;
            machine_device.device= devices.device;
            machine_device.firmware= devices.firmware;
            machine_device.ipaddr= devices.ipaddr;
            machine_device.model= devices.model;
            machine_device.contract_id= devices.contract_id;
            machine_device.t_update= devices.t_update;
            machine_device.t_logout= devices.t_logout;
            machine_device.t_logon= devices.t_logon;
            machine_device.t_create= devices.t_create;
            machine_device.cell_mcc= devices.cell_mcc;
            machine_device.cell_cid= devices.cell_cid;
            machine_device.cell_lac= devices.cell_lac;
            machine_device.cell_mnc= devices.cell_mnc;

            if(mamodel!=null){
                if(mamodel.order_times!=null){
                    machine_device.order_times= mamodel.order_times;
                }
            }
            DeviceInfo deviceInfo=new DeviceInfo();
            if(DeviceInfo.finder.byId(devices.id)==null){
                deviceInfo.device_name="未命名设备";
                if(devices.contract_id!=null&& (devices.contract_id[0]&0xFF)==0x11){
                    deviceInfo.register="registered";
                }else{
                    deviceInfo.register="unregistered";
                }
                deviceInfo.id=devices.id;
                deviceInfo.commond="ok";
                deviceInfo.state="online";
                deviceInfo.tagcolor="green";
                deviceInfo.IMEI= devices.IMEI;
                deviceInfo.device_type= devices.device;
                deviceInfoList.add(deviceInfo);
            }else{
                String register="unregistered";
                if(devices.contract_id!=null&& (devices.contract_id[0]&0xFF)==0x11){
                    register="registered";
                }
                String sql= String.format("UPDATE ladder.device_info set register='%s',state='%s' where imei='%s'",register,"online", devices.IMEI);
                Ebean.getServer(CommonConfig.LADDER_SERVER).createSqlUpdate(sql).execute();
            }
            Ladder ladder_one = Ladder.finder.where().eq("ctrl_id",deviceInfo.id).findUnique();
            if(ladder_one!=null){
                ladder_one.state = deviceInfo.state;
                ladderList.add(ladder_one);
            }
            save_devices.add(machine_device);
            new_datex=new_datex.getTime()>devices.t_update.getTime()?new_datex:devices.t_update;
        }
        Ebean.getServer(CommonConfig.LADDER_SERVER).deleteAll(delete_devices);
        Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(save_devices);
        Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(deviceInfoList);
        Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(ladderList);

        datex_one=new Date();
        datex_two=new Date();
        datex_one.setMinutes(datex_one.getMinutes()-5);
        datex_two.setMinutes(datex_two.getMinutes()-30);
        devicesList=Devices.finder.where().le("t_update",datex_one).findList();
        for(Devices devices:devicesList){
            String sql= String.format("UPDATE ladder.device_info set state='%s' where imei='%s'","longoffline", devices.IMEI);
            Ebean.getServer(CommonConfig.LADDER_SERVER).createSqlUpdate(sql).execute();
        }

    }

    @Override
    public void run() {
        while (true){
            try{
                Thread.sleep(1000);
                update_device();
                Logger.info("Move Info from db1 to db2 ok at :"+new_datex);
                old_datex=new_datex;
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
