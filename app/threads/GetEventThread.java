package threads;

import com.avaje.ebean.Ebean;
import controllers.CommonConfig;
import device.models.Events;
import device.models.Runtime;
import ladder.models.Order;

import play.Logger;
import sun.rmi.runtime.Log;

import java.text.SimpleDateFormat;
import java.util.*;
/**
 * Created by lengxia on 2018/11/28.
 */
public class GetEventThread extends Thread {

    private static Date old_datex = new Date();
    private static Date new_datex = new Date();
    private static boolean init_device = true;

    public GetEventThread() {
        Logger.info("create GetEventInfo Thread ok");
    }

    private int current(byte[] buffer, int i){
        return (((buffer[i * 8 + 4] & 0xff) << 8) + (buffer[i * 8 + 5] & 0xff));
    }
    private int speed(byte[] buffer, int i){
        int speed = (((buffer[i * 8 + 6] & 0xff) << 8) + (buffer[i * 8 + 7] & 0xff));

        if(speed >32767){
            speed = (speed-65536);
        }
        return speed;
    }
    private  int door(byte[] buffer, int i){
        return (((buffer[i * 8 + 2] & 0x0f) << 8) + (buffer[i * 8 + 3] & 0xff));
    }

    private void update_event() {
        List<Events> eventsList = Events.finder.where().isNotNull("time").gt("time", old_datex).findList();
        List<ladder.models.Events> save_events = new ArrayList<>();
        List<ladder.models.SimplifyEvents> simplifyEvents = new ArrayList<>();
        List<ladder.models.Devices> devicesList = new ArrayList<>();
        List<Order> save_order = new ArrayList<>();
        for (Events events : eventsList) {
            ladder.models.Devices devices = ladder.models.Devices.finder.byId(events.device_id);
            if (devices == null ){
                break;
            }
            if (old_datex.getTime() >= events.time.getTime()) {
                continue;
            }
            ladder.models.Events ladder_event = new ladder.models.Events();
            ladder.models.SimplifyEvents simplify_event = new ladder.models.SimplifyEvents();
            ladder_event.data = events.data;
            ladder_event.id = events.id;
            ladder_event.device_id = events.device_id;
            ladder_event.interval = events.interval;
            ladder_event.length = events.length;
            ladder_event.time = events.time;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = sdf.format(events.time.getTime()+events.length*events.interval/2);
            try {
                simplify_event.end_time = sdf.parse(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
            simplify_event.id = events.id;
            simplify_event.device_id = events.device_id;
            simplify_event.start_time = events.time;
            simplify_event.device_type = devices.device;
            simplify_event.current = 0;
            simplify_event.speed = 0;
            if (devices.device.equals("15")) {
                byte[] buffer = events.data;
                int count = 0;
                if((buffer[(events.data.length/8-1)*8]&0x80)>>7==1){
                    simplify_event.event_type = "open";
                }else if((buffer[(events.data.length/8-1)*8]&0x40)>>6==1){
                    simplify_event.event_type = "close";
                }else{
                    simplify_event.event_type = "watch";
                }
                for (int i = 0; i < events.data.length / 8; i++) {
                    if (this.current(buffer,i) > simplify_event.current) {
                        simplify_event.current = this.current(buffer,i);
                    }
                    if (Math.abs(this.speed(buffer,i))  > Math.abs(simplify_event.speed) ) {
                        simplify_event.speed = this.speed(buffer,i);
                    }
                    if (simplify_event.event_type.equals("open")) {
                        simplify_event.door = 0;
                        if (this.door(buffer,i) > simplify_event.door) {
                            simplify_event.door = this.door(buffer,i);
                        }
                    } else {
                        simplify_event.door = 1600;
                        if (this.door(buffer,i) < simplify_event.door) {
                            simplify_event.door = this.door(buffer,i);
                        }
                    }
                }
                for (int i = 0; i < events.data.length / 8; i++) {
                    if (devices.model.equals("1")) {
                        Runtime runtime = Runtime.finder.where().eq("device_id",events.device_id).eq("type",4100).findUnique();
                        if (runtime==null){
                            break;
                        }
                        byte[] buffer2 = runtime.data;
                        simplify_event.max_door = ((buffer2[26]&0xff)<<8)+(buffer2[27]&0xff);
                        if (((buffer[i * 8 + 1] & 0x03) + (buffer[i * 8 + 2] & 0xf0)) == 0) {
                            Order orderList = Order.finder.where()
                                    .eq("device_id", devices.id)
                                    .notIn("type", 179)
                                    .eq("islast", 1)
                                    .notIn("state", "treated")
                                    .findUnique();
                            if (orderList != null) {
                                orderList.state = "treated";
                                save_order.add(orderList);
                            }
                            Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(save_order);
                        }
                        if ((((buffer[i * 8 + 4] & 0xff) << 8) + (buffer[i * 8 + 5] & 0xff)) > 1000) {
                            count = 1;
                            if (devices.order_times == null) {
                                devices.order_times = 1;
                                devicesList.add(devices);
                                Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(devicesList);
                            } else {
                                devices.order_times = devices.order_times + 1;
                                devicesList.add(devices);
                                Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(devicesList);
                            }
                            if (devices.order_times >= 10) {
                                devices.order_times = 0;
                                devicesList.add(devices);
                                Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(devicesList);

                                Order order = new Order();
                                order.device_id = devices.id;
                                order.code = 179;
                                order.type = 1;
                                order.producer = "sys";
                                order.device_type = "door";
                                order.createTime = new Date().getTime() + "";
                                order.state = "untreated";
                                order.producer = "sys";
                                order.islast = 1;
                                int counts = Order.finder.where()
                                        .eq("device_id", devices.id)
                                        .eq("type", 1)
                                        .eq("code", 179)
                                        .notIn("state", "treated")
                                        .findRowCount();
                                if (counts != 0) {
                                    break;
                                }
                                Order orderList = Order.finder.where()
                                        .eq("device_id", devices.id)
                                        .eq("type", 179)
                                        .eq("islast", 1)
                                        .notIn("state", "treated")
                                        .findUnique();
                                if (orderList != null) {
                                    orderList.islast = 0;
                                    save_order.add(orderList);
                                }
                                save_order.add(order);
                                Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(save_order);
                            }
                            break;
                        }
                    } else if (devices.model.equals("2")) {
                        Runtime runtime = Runtime.finder.where().eq("device_id",events.device_id).eq("type",4101).findUnique();
                        if (runtime==null){
                            break;
                        }
                        byte[] buffer2 = runtime.data;
                        simplify_event.max_door = ((buffer2[14]&0xff)<<8)+(buffer2[15]&0xff);
                        if (((buffer[i * 8 + 1] & 0x03) + (buffer[i * 8 + 2] & 0xf0)) == 0) {
                            Order orderList = Order.finder.where()
                                    .eq("device_id", devices.id)
                                    .eq("type", 1)
                                    .notIn("code", 179)
                                    .eq("islast", 1)
                                    .notIn("state", "treated")
                                    .findUnique();
                            if (orderList != null) {
                                orderList.state = "treated";
                                save_order.add(orderList);
                            }
                            Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(save_order);
                        }
                        if ((((buffer[i * 8 + 4] & 0xff) << 8) + (buffer[i * 8 + 5] & 0xff)) > 2500) {
                            count = 1;
                            if (devices.order_times == null) {
                                devices.order_times = 1;
                                devicesList.add(devices);
                                Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(devicesList);
                            } else {
                                devices.order_times = devices.order_times + 1;
                                devicesList.add(devices);
                                Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(devicesList);
                            }
                            if (devices.order_times >= 10) {
                                devices.order_times = 0;
                                devicesList.add(devices);
                                Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(devicesList);

                                Order order = new Order();
                                order.device_id = devices.id;
                                order.code = 179;
                                order.type = 1;
                                order.producer = "sys";
                                order.device_type = "door";
                                order.createTime = new Date().getTime() + "";
                                order.state = "untreated";
                                order.producer = "sys";
                                order.islast = 1;
                                int counts = Order.finder.where()
                                        .eq("device_id", devices.id)
                                        .eq("type", 1)
                                        .eq("code", 179)
                                        .notIn("state", "treated")
                                        .findRowCount();
                                if (counts != 0) {
                                    break;
                                }
                                Order orderList = Order.finder.where()
                                        .eq("device_id", devices.id)
                                        .eq("type", 1)
                                        .eq("islast", 1)
                                        .notIn("state", "treated")
                                        .findUnique();
                                if (orderList != null) {
                                    orderList.islast = 0;
                                    save_order.add(orderList);
                                }
                                save_order.add(order);
                                Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(save_order);
                            }
                            break;
                        }
                    }
                }
                if (count == 0) {
                    devices.order_times = 0;
                    devicesList.add(devices);
                    Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(devicesList);
                }
            }
            simplifyEvents.add(simplify_event);
            save_events.add(ladder_event);
            new_datex = new_datex.getTime() > events.time.getTime() ? new_datex : events.time;
        }
        Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(simplifyEvents);
        Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(save_events);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(500);
                update_event();
                Logger.info("Move EventInfo from db1 to db2 ok at :" + new_datex);
                old_datex = new_datex;
                if (init_device) {
                    Logger.info("init ok");
                    init_device = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
