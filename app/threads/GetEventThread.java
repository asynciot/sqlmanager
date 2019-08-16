package threads;

import com.avaje.ebean.Ebean;
import controllers.CommonConfig;
import device.models.Events;
import ladder.models.Order;
import play.Logger;

import java.util.*;
/**
 * Created by lengxia on 2018/11/28.
 */
public class GetEventThread extends Thread {

    public static Date old_datex = new Date();
    public static Date new_datex = new Date();
    public static Date datex_one = new Date();
    public static Date datex_two = new Date();
    public static boolean init_device = true;
    private static int TIME_OUT = 5000;

    public GetEventThread() {
        Logger.info("create GetEventInfo Thread ok");
    }

    public void update_event() {
        List<Events> eventsList = Events.finder.where().isNotNull("time").gt("time", old_datex).findList();
        List<ladder.models.Events> save_events = new ArrayList<ladder.models.Events>();
        List<ladder.models.Devices> devicesList = new ArrayList<ladder.models.Devices>();
        List<Order> save_order = new ArrayList<Order>();
        for (Events events : eventsList) {
            ladder.models.Devices devices = ladder.models.Devices.finder.byId(events.device_id);
            if (old_datex.getTime() >= events.time.getTime()) {
                continue;
            }
            ladder.models.Events ladder_event = new ladder.models.Events();
            ladder_event.data = events.data;
            ladder_event.device_id = events.device_id;
            ladder_event.interval = events.interval;
            ladder_event.length = events.length;
            ladder_event.time = events.time;
            if (devices.device.equals("15")) {
                byte[] buffer = events.data;
                int count = 0;
                for (int i = 0; i < events.data.length / 8; i++) {
                    if (devices.model.equals("1")) {
                        if ((((buffer[i*8+4]&0xff)<<8)+(buffer[i*8+5]&0xff))>1000) {
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
                                Order orderlast = Order.finder.where()
                                        .eq("device_id", devices.id)
                                        .eq("type", 179)
                                        .eq("islast", 1)
                                        .notIn("state", "treated")
                                        .findUnique();
                                if (orderlast != null) {
                                    orderlast.islast = 0;
                                    save_order.add(orderlast);
                                }
                                save_order.add(order);
                                Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(save_order);
                            }
                            break;
                        } else if (((buffer[i*8+1]&0x03)+(buffer[i*8+2]&0xf0))==0) {
                            Order orderlast = Order.finder.where()
                                    .eq("device_id", devices.id)
                                    .notIn("type", 179)
                                    .eq("islast", 1)
                                    .notIn("state", "treated")
                                    .findUnique();
                            if (orderlast != null) {
                                orderlast.state = "treated";
                                save_order.add(orderlast);
                            }
                            Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(save_order);
                        }
                    } else if (devices.model.equals("2")) {
                        if ((((buffer[i*8+4]&0xff)<<8)+(buffer[i*8+5]&0xff))>2500) {
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
                                Order orderlast = Order.finder.where()
                                        .eq("device_id", devices.id)
                                        .eq("type", 1)
                                        .eq("islast", 1)
                                        .notIn("state", "treated")
                                        .findUnique();
                                if (orderlast != null) {
                                    orderlast.islast = 0;
                                    save_order.add(orderlast);
                                }
                                save_order.add(order);
                                Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(save_order);
                            }
                            break;
                        } else if (((buffer[i*8+1]&0x03)+(buffer[i*8+2]&0xf0))==0) {
                            Order orderlast = Order.finder.where()
                                    .eq("device_id", devices.id)
                                    .eq("type", 1)
                                    .notIn("code", 179)
                                    .eq("islast", 1)
                                    .notIn("state", "treated")
                                    .findUnique();
                            if (orderlast != null) {
                                orderlast.state = "treated";
                                save_order.add(orderlast);
                            }
                            Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(save_order);
                        }
                    }
                }
                if (count == 0) {
                    devices.order_times = 0;
                    devicesList.add(devices);
                    Ebean.getServer(CommonConfig.LADDER_SERVER).saveAll(devicesList);
                }
            }
            save_events.add(ladder_event);
            new_datex = new_datex.getTime() > events.time.getTime() ? new_datex : events.time;
        }
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
                if (init_device == true) {
                    Logger.info("init ok");
                    init_device = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
