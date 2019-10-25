package threads;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.CommonConfig;
import device.models.Devices;
import device.models.Runtime;
import ladder.models.Ladder;
import ladder.models.DeviceInfo;
import ladder.models.Order;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

import java.util.*;
import java.util.concurrent.CompletionStage;

/**
 * Created by lengxia on 2018/11/28.
 */
public class GetDevThread extends Thread {

    private static Date old_date =new Date();
    private static Date new_date =new Date();
    private static boolean init_device=true;

    public GetDevThread(){
        Logger.info("create GetInfo Thread ok");
    }

    private void update_device(){
        List<Devices> devicesList;
        if(init_device){
            devicesList= Devices.finder.where().findList();
        }
        else {
            devicesList = Devices.finder.where().isNotNull("t_update").gt("t_update", old_date).findList();
        }

        List<ladder.models.Devices> save_devices=new ArrayList<>();
        List<ladder.models.Devices> delete_devices= new ArrayList<>();
        List<DeviceInfo> deviceInfoList= new ArrayList<>();
        List<Ladder> ladderList= new ArrayList<>();
        for(Devices devices : devicesList){
            if(old_date.getTime()>=devices.t_update.getTime()&&!init_device){
                continue;
            }
            ladder.models.Devices mamodel= ladder.models.Devices.finder.byId(devices.id);
            ladder.models.Devices machine_device  = new ladder.models.Devices();
            if(mamodel!=null){
                delete_devices.add(mamodel);
                if(mamodel.order_times!=null){
                    machine_device.order_times= mamodel.order_times;
                }
            }
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
            DeviceInfo device_Info= DeviceInfo.finder.where().eq("id",devices.id).findUnique();
            Ladder ladder_one = Ladder.finder.where().eq("ctrl_id",devices.id).findUnique();
            if(ladder_one!=null){
                ladder_one.state = device_Info.state;
                ladderList.add(ladder_one);
            }
            save_devices.add(machine_device);
            new_date = new_date.getTime()>devices.t_update.getTime()? new_date :devices.t_update;
        }
        Ebean.getServer(CommonConfig.LADDER_SERVER).deleteAll(delete_devices);
        Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(save_devices);
        Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(deviceInfoList);
        Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(ladderList);

        Date date_one = new Date();
        Date date_two = new Date();
        date_one.setMinutes(date_one.getMinutes()-5);
        date_two.setMinutes(date_two.getMinutes()-30);
        devicesList=Devices.finder.where().le("t_update", date_one).findList();
        for(Devices devices:devicesList){
            String sql= String.format("UPDATE ladder.device_info set state='%s' where imei='%s'","longoffline", devices.IMEI);
            String sql2= String.format("UPDATE ladder.ladder set state='%s' where ctrl_id='%s'","longoffline", devices.id);
            Ebean.getServer(CommonConfig.LADDER_SERVER).createSqlUpdate(sql).execute();
            Ebean.getServer(CommonConfig.LADDER_SERVER).createSqlUpdate(sql2).execute();
        }
    }

    private void update_runtime() {
        List<Runtime> runtimeList;
        List<Order> save_order = new ArrayList<>();
        if (init_device) {
            runtimeList = Runtime.finder.where().findList();
        } else {
            runtimeList = Runtime.finder.where().isNotNull("t_update").gt("t_update", old_date).findList();
        }

        List<ladder.models.Runtime> save_runtime = new ArrayList<>();
        List<ladder.models.Runtime> delete_runtime = new ArrayList<>();
        for (Runtime runtime : runtimeList) {
            if (old_date.getTime() >= runtime.t_update.getTime() && !init_device) {
                continue;
            }
            ladder.models.Runtime ladder_runtime = new ladder.models.Runtime();
            ladder_runtime.id = runtime.id;
            ladder_runtime.data = runtime.data;
            ladder_runtime.device_id = runtime.device_id;
            ladder_runtime.t_update = runtime.t_update;
            ladder_runtime.type = runtime.type;
            save_runtime.add(ladder_runtime);
            List<ladder.models.Runtime> runtime1 = ladder.models.Runtime.finder.where().eq("device_id", runtime.device_id).eq("type", runtime.type).findList();
            if (runtime1 != null && runtime1.size() > 0) {
                delete_runtime.addAll(runtime1);
            }
            if(runtime.type == 8192){
                byte[] buffer = runtime.data;
                int bufferData = (((buffer[8]&0xff))&0x20)>>5;
                if( bufferData==0){
                    Order orderLast = Order.finder.where()
                            .eq("device_id", runtime.device_id)
                            .eq("type", 1)
                            .eq("device_type", "ctrl")
                            .eq("islast", 1)
                            .notIn("state", "treated")
                            .findUnique();
                    if (orderLast != null) {
                        orderLast.state = "treated";
                        save_order.add(orderLast);
                        Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(save_order);
                    }
                }
            }
            int TIME_OUT = 5000;
            if (runtime.type == 4096 || runtime.type == 8192) {
                try {
                    Integer rssi = null;
                    Integer runtime_state = null;
                    Integer alert = null;
                    String type = null;
                    if (runtime.type == 4096) {
                        rssi = runtime.data[4] & 0xff;
                        runtime_state = runtime.data[7] & 0xff;
                        alert = runtime.data[7] & 0x03 + (runtime.data[8] & 0xf0);
                        type = "door";
                    }
                    if (runtime.type == 8192) {
                        rssi = runtime.data[4] & 0xff;
                        runtime_state = runtime.data[8] & 0xff;
                        alert = runtime.data[18] & 0xff;
                        type = "ctrl";
                    }
                    if ((type.equals("door") && alert == 2) || (type.equals("ctrl") && (alert != 16 && alert != 18))) {
                        String sql = String.format("UPDATE ladder.device_info set rssi=%d,runtime_state=%d where id=%d", rssi, runtime_state, runtime.device_id);
                        Ebean.getServer(CommonConfig.LADDER_SERVER).createSqlUpdate(sql).execute();

                        byte[] buffer = runtime.data;
                        int bufferData = (((buffer[8]&0xff))&0x20)>>5;
                        if(bufferData==1){
                            String url = "http://127.0.0.1:9006/device/alert";
                            Map<String, Object> result = new HashMap<>();
                            result.put("code", alert);
                            result.put("device_id", runtime.device_id);
                            result.put("device_type", type);
                            result.put("producer", "sys");
                            result.put("type", "1");
                            CompletionStage<JsonNode> jsonPromise = WS.url(url)
                                    .setRequestTimeout(TIME_OUT)
                                    .setContentType("application/json")
                                    .post(Json.toJson(result))
                                    .thenApply(WSResponse::asJson);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            new_date = new_date.getTime() > runtime.t_update.getTime() ? new_date : runtime.t_update;

            try {
                DeviceInfo deviceInfo = DeviceInfo.finder.byId(runtime.device_id);
                if (deviceInfo != null) {
                    long now = new Date().getTime();
                    long remind;
                    long nexttime;
                    if (deviceInfo.maintenance_remind != null && deviceInfo.maintenance_nexttime != null) {
                        remind = Long.parseLong(deviceInfo.maintenance_remind);
                        nexttime = Long.parseLong(deviceInfo.maintenance_nexttime);
                        if (now + remind > nexttime && now < nexttime) {
                            int counts = Order.finder.where()
                                    .eq("device_id", runtime.device_id)
                                    .eq("type", "2")
                                    .eq("code", "1")
                                    .eq("device_type", deviceInfo.device_type.equals("240") ? "ctrl" : "door")
                                    .notIn("state", "treated")
                                    .findRowCount();
                            if (counts != 0) {
                                break;
                            }
                            String url = "http://127.0.0.1:9006/device/alert";
                            Map<String, Object> result = new HashMap<>();
                            result.put("code", "1");
                            result.put("device_id", runtime.device_id);
                            result.put("device_type", deviceInfo.device_type.equals("240") ? "ctrl" : "door");
                            result.put("producer", "sys");
                            result.put("type", "2");
                            CompletionStage<JsonNode> jsonPromise = WS.url(url)
                                    .setRequestTimeout(TIME_OUT)
                                    .setContentType("application/json")
                                    .post(Json.toJson(result))
                                    .thenApply(WSResponse::asJson);
                        }
                    }
                    if (deviceInfo.inspection_remind != null && deviceInfo.inspection_nexttime != null) {
                        remind = Long.parseLong(deviceInfo.inspection_remind);
                        nexttime = Long.parseLong(deviceInfo.inspection_nexttime);
                        if (now + remind > nexttime && now < nexttime) {
                            int counts = Order.finder.where()
                                    .eq("device_id", runtime.device_id)
                                    .eq("type", "3")
                                    .eq("code", "1")
                                    .eq("device_type", deviceInfo.device_type.equals("240") ? "ctrl" : "door")
                                    .notIn("state", "treated")
                                    .findRowCount();
                            if (counts != 0) {
                                break;
                            }
                            String url = "http://127.0.0.1:9006/device/alert";
                            Map<String, Object> result = new HashMap<>();
                            result.put("code", "1");
                            result.put("device_id", runtime.device_id);
                            result.put("device_type", deviceInfo.device_type.equals("240") ? "ctrl" : "door");
                            result.put("producer", "sys");
                            result.put("type", "3");
                            CompletionStage<JsonNode> jsonPromise = WS.url(url)
                                    .setRequestTimeout(TIME_OUT)
                                    .setContentType("application/json")
                                    .post(Json.toJson(result))
                                    .thenApply(WSResponse::asJson);
                        }
                    }
                }
            } catch (Exception e) {
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
                update_device();
                update_runtime();
                Logger.info("Move Info from db1 to db2 ok at :"+ new_date);
                old_date = new_date;
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
