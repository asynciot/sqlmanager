package threads;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.javafx.css.parser.LadderConverter;
import controllers.CommonConfig;
import device.models.Devices;
import device.models.Events;
import device.models.Monitor;
import device.models.Runtime;
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
    public void update_event(){
        List<Events> eventsList= Events.finder.where().isNotNull("time").gt("time",old_datex).findList();
        List<ladder.models.Events> save_events=new ArrayList<ladder.models.Events>();
        for(Events events :eventsList){
            if(old_datex.getTime()>=events.time.getTime()){
                continue;
            }
            ladder.models.Events ladder_event=new ladder.models.Events();
            ladder_event.data= events.data;
            ladder_event.device_id= events.device_id;
            ladder_event.interval= events.interval;
            ladder_event.length= events.length;
            ladder_event.time= events.time;
            save_events.add(ladder_event);
            new_datex=new_datex.getTime()>events.time.getTime()?new_datex:events.time;
        }

        Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(save_events);
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
                monitor_new.sequence=monitor.sequence;
                monitor_new.device_id=monitor.device_id;
                monitor_new.time=monitor.time;
                new_monitorList.add(monitor_new);
                new_datex=new_datex.getTime()>monitor.time.getTime()?new_datex:monitor.time;
                if(monitor.length==0){
                    String sql= String.format("UPDATE ladder.device_info set commond='%s' where id=%d","ok",monitor.device_id);
                    Ebean.getServer(CommonConfig.LADDER_SERVER).createSqlUpdate(sql).execute();
                    CommonConfig.device_monitor_set.remove(monitor.device_id);
                }

            }
        }
        Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(new_monitorList);
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
        for(Devices devices : devicesList){
            if(old_datex.getTime()>=devices.t_update.getTime()&&init_device==false){
                continue;
            }
            ladder.models.Devices mamodel= ladder.models.Devices.finder.byId(devices.id);
            if(mamodel!=null){
                delete_devices.add(mamodel);
            }
            ladder.models.Devices machine_device=new ladder.models.Devices();
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

            if(DeviceInfo.finder.byId(devices.id)==null){
                DeviceInfo deviceInfo=new DeviceInfo();
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
            }
            else{
                String register="unregistered";
                if(devices.contract_id!=null&& (devices.contract_id[0]&0xFF)==0x11){
                    register="registered";
                }
                String sql= String.format("UPDATE ladder.device_info set register='%s',state='%s' where imei=%s",register,"online", devices.IMEI);
                Ebean.getServer(CommonConfig.LADDER_SERVER).createSqlUpdate(sql).execute();
            }

            save_devices.add(machine_device);
            new_datex=new_datex.getTime()>devices.t_update.getTime()?new_datex:devices.t_update;
        }
        Ebean.getServer(CommonConfig.LADDER_SERVER).deleteAll(delete_devices);
        Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(save_devices);
        Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(deviceInfoList);


        datex_one=new Date();
        datex_two=new Date();
        datex_one.setMinutes(datex_one.getMinutes()-5);
        datex_two.setMinutes(datex_two.getMinutes()-30);
        devicesList=Devices.finder.where().le("t_update",datex_one).gt("t_update",datex_two).findList();
        for(Devices devices:devicesList){
            String sql= String.format("UPDATE ladder.device_info set state='%s' where imei=%s","offline", devices.IMEI);
            Ebean.getServer(CommonConfig.LADDER_SERVER).createSqlUpdate(sql).execute();
        }
        devicesList=Devices.finder.where().le("t_update",datex_two).findList();

        for(Devices devices:devicesList){
            String sql= String.format("UPDATE ladder.device_info set state='%s' where imei=%s","longoffline", devices.IMEI);
            Ebean.getServer(CommonConfig.LADDER_SERVER).createSqlUpdate(sql).execute();
        }



    }
    public void update_dock(){

    }
    public void update_log(){

    }

    public void update_runtime(){
        List<Runtime> runtimeList=null;
        if(init_device){
            runtimeList= Runtime.finder.where().findList();
        }
        else {
            runtimeList = Runtime.finder.where().isNotNull("t_update").gt("t_update",old_datex).findList();
        }

        List<ladder.models.Runtime> save_runtime=new ArrayList<ladder.models.Runtime>();
        List<ladder.models.Runtime> delete_runtime=new ArrayList<ladder.models.Runtime>();
        for(Runtime runtime :runtimeList){
            if(old_datex.getTime()>=runtime.t_update.getTime()&&init_device==false){
                continue;
            }
            ladder.models.Runtime ladder_runtime=new ladder.models.Runtime();
            ladder_runtime.id=runtime.id;
            ladder_runtime.data=runtime.data;
            ladder_runtime.device_id=runtime.device_id;
            ladder_runtime.t_update=runtime.t_update;
            ladder_runtime.type=runtime.type;
            save_runtime.add(ladder_runtime);
            List<ladder.models.Runtime> runtime1=ladder.models.Runtime.finder.where().eq("device_id",runtime.device_id).eq("type",runtime.type).findList();
            if(runtime1!=null&&runtime1.size()>0){
                delete_runtime.addAll(runtime1);
            }
            if(runtime.type==4096||runtime.type==8192){
                try{
                    Integer rssi=null;
                    Integer runtime_state=null;
                    Integer alert=null;
                    String type=null;
                    if(runtime.type==4096){
                        rssi=runtime.data[4]&0xff;
                        runtime_state=runtime.data[7]&0xff;
                        alert=runtime.data[7]&0x03+(runtime.data[8]&0xf0);
                        type="door";
                    }
                    if(runtime.type==8192){
                        rssi=runtime.data[4]&0xff;
                        runtime_state=runtime.data[8]&0xff;
                        alert=runtime.data[18]&0xff;
                        type="ctrl";
                    }
                    String sql= String.format("UPDATE ladder.device_info set rssi=%d,runtime_state=%d where id=%d",rssi,runtime_state,runtime.device_id);
                    Ebean.getServer(CommonConfig.LADDER_SERVER).createSqlUpdate(sql).execute();

                    String url = "http://127.0.0.1:9006/device/alert";
                    Map<String, Object> result = new HashMap<String,Object>();
                    result.put("code",alert);
                    result.put("device_id",runtime.device_id);
                    result.put("device_type",type);
                    result.put("producer","sys");
                    result.put("type","1");
                    CompletionStage<JsonNode> jsonPromise = WS.url(url)
                            .setRequestTimeout(TIME_OUT)
                            .setContentType("application/json")
                            .post(Json.toJson(result))
                            .thenApply(WSResponse::asJson);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
            new_datex=new_datex.getTime()>runtime.t_update.getTime()?new_datex:runtime.t_update;



            try {
                DeviceInfo deviceInfo=DeviceInfo.finder.byId(runtime.device_id);
                if(deviceInfo!=null){
                    long nowl=new Date().getTime();
                    long remind=0;
                    long nexttime=0;
                    if(deviceInfo.maintenance_remind!=null&&deviceInfo.maintenance_nexttime!=null)
                    {
                        remind=Long.parseLong(deviceInfo.maintenance_remind);
                        nexttime=Long.parseLong(deviceInfo.maintenance_nexttime);
                        if(nowl+remind>nexttime&&nowl<nexttime){
                            String url = "http://127.0.0.1:9006/device/alert";
                            Map<String, Object> result = new HashMap<String,Object>();
                            result.put("code","1");
                            result.put("device_id",runtime.device_id);
                            result.put("device_type",deviceInfo.device_type.equals("240")?"door":"ctrl");
                            result.put("producer","sys");
                            result.put("type","2");
                            CompletionStage<JsonNode> jsonPromise = WS.url(url)
                                    .setRequestTimeout(TIME_OUT)
                                    .setContentType("application/json")
                                    .post(Json.toJson(result))
                                    .thenApply(WSResponse::asJson);
                        }
                    }
                    if(deviceInfo.inspection_remind!=null&&deviceInfo.inspection_nexttime!=null)
                    {
                        remind=Long.parseLong(deviceInfo.inspection_remind);
                        nexttime=Long.parseLong(deviceInfo.inspection_nexttime);
                        if(nowl+remind>nexttime&&nowl<nexttime){
                            String url = "http://127.0.0.1:9006/device/alert";
                            Map<String, Object> result = new HashMap<String,Object>();
                            result.put("code","1");
                            result.put("device_id",runtime.device_id);
                            result.put("device_type",deviceInfo.device_type.equals("240")?"door":"ctrl");
                            result.put("producer","sys");
                            result.put("type","3");
                            CompletionStage<JsonNode> jsonPromise = WS.url(url)
                                    .setRequestTimeout(TIME_OUT)
                                    .setContentType("application/json")
                                    .post(Json.toJson(result))
                                    .thenApply(WSResponse::asJson);
                        }

                    }
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }
        Ebean.getServer(CommonConfig.LADDER_SERVER).deleteAll(delete_runtime);
        Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(save_runtime);


    }

    @Override
    public void run() {
        while (true){
            try{
                Thread.sleep(1000);
                update_monitor();
                update_device();
                update_event();
                update_dock();
                update_log();
                update_runtime();
                Logger.info("Move Info from db1 to db2 ok at :"+new_datex);
                old_datex=new_datex;
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
