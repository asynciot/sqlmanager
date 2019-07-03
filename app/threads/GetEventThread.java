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
public class GetEventThread extends Thread {

    public static Date old_datex=new Date();
    public static Date new_datex=new Date();
    public static Date datex_one=new Date();
    public static Date datex_two=new Date();
    public static boolean init_device=true;
    private static int TIME_OUT = 5000;
    public GetEventThread(){
        Logger.info("create GetEventInfo Thread ok");
    }
    public void update_event() {
        List<Events> eventsList = Events.finder.where().isNotNull("time").gt("time", old_datex).findList();
        List<ladder.models.Events> save_events = new ArrayList<ladder.models.Events>();
        for (Events events : eventsList) {
            if (old_datex.getTime() >= events.time.getTime()) {
                continue;
            }
            ladder.models.Events ladder_event = new ladder.models.Events();
            ladder_event.data = events.data;
            ladder_event.device_id = events.device_id;
            ladder_event.interval = events.interval;
            ladder_event.length = events.length;
            ladder_event.time = events.time;
            save_events.add(ladder_event);
            new_datex = new_datex.getTime() > events.time.getTime() ? new_datex : events.time;
        }

        Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(save_events);
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
                    if((type=="door"&&alert==2)||(type=="ctrl"&&(alert!=16||alert!=18))){
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
                    }
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
                update_event();
                update_runtime();
                Logger.info("Move EventInfo from db1 to db2 ok at :"+new_datex);
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
