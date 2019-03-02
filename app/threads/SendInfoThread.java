package threads;

import com.avaje.ebean.Ebean;
import controllers.CommonConfig;
import device.models.Commands;
import device.models.Binaries;
import ladder.models.DeviceInfo;
import play.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lengxia on 2018/12/3.
 */
public class SendInfoThread extends Thread{

    public static Date old_datex=new Date();
    public static Date new_datex=new Date();

    public SendInfoThread(){
        Logger.info("create SendInfo Thread ok");
    }

    public  void stop_monitor(int device_id){
        if(CommonConfig.device_monitor_set.contains(device_id)){
            CommonConfig.device_monitor_set.remove(device_id);
            ladder.models.Monitor ladder_monitor=new ladder.models.Monitor();
            ladder_monitor.time=new Date();
            ladder_monitor.session=0;
            ladder_monitor.length=0;
            ladder_monitor.device_id=device_id;
            Ebean.getServer(CommonConfig.LADDER_SERVER).save(ladder_monitor);
        }
    }

    public  void snedCommond(){

        List<ladder.models.Commands> commandList=ladder.models.Commands.finder.where().isNotNull("submit").gt("submit",old_datex).findList();
        List<Commands> save_commands=new ArrayList<Commands>();
        for(ladder.models.Commands command: commandList){
            if(old_datex.getTime()>=command.submit.getTime()){
                continue;
            }
            ladder.models.Devices devices = ladder.models.Devices.finder.where().eq("IMEI",command.IMEI).findUnique();
            if(devices ==null){
                continue;
            }
            Commands device_command=new Commands();
            device_command.binary_id=command.binary_id;
            device_command.contract=command.contract;
            device_command.command=command.command;
            device_command.int1=command.int1;
            device_command.int2=command.int2;
            device_command.int3=command.int3;
            device_command.int4 =command.int4;
            device_command.execute=command.execute;
            device_command.finish=command.finish;
            device_command.result=command.result;
            device_command.submit = command.submit;
            device_command.str1=command.str1;
            device_command.str2=command.str2;
            device_command.str3=command.str3;
            device_command.str4=command.str4;
            device_command.IMEI=command.IMEI;
            device_command.binary=command.binary;
            save_commands.add(device_command);
            new_datex= command.submit.getTime()>new_datex.getTime()? command.submit:new_datex;
                //下发监视命令，监视墙device_monitor_set开启
            if(command.command.equals("MONITOR")&&command.int1!=0){
                CommonConfig.device_monitor_set.add(devices.id);
                Date datex_tmp=new Date();
                long date_l=datex_tmp.getTime()+(command.int2>600?600:command.int2+30)*1000;
                String sql= String.format("UPDATE ladder.device_info set commond='%s',delay='%s' where id=%d","monitor",Long.toString(date_l),devices.id);
                Ebean.getServer(CommonConfig.LADDER_SERVER).createSqlUpdate(sql).execute();
            }
            if(command.command.equals("MONITOR")&&command.int1==0) {
                String sql= String.format("UPDATE ladder.device_info set commond='%s' where id=%d","ok",devices.id);
                Ebean.getServer(CommonConfig.LADDER_SERVER).createSqlUpdate(sql).execute();
                stop_monitor(devices.id);
            }
            if(command.command.equals("CONTRACT")){
                Date datex_tmp=new Date();
                long date_l=datex_tmp.getTime()+5*60*1000;
                String sql= String.format("UPDATE ladder.device_info set commond='%s',delay='%s' where id=%d","contract",Long.toString(date_l),devices.id);
                Ebean.getServer(CommonConfig.LADDER_SERVER).createSqlUpdate(sql).execute();
            }
            if(command.command.equals("UPDATE")){
                Date datex_tmp=new Date();
                long date_l=datex_tmp.getTime()+5*60*1000;
                String sql= String.format("UPDATE ladder.device_info set commond='%s',delay='%s' where id=%d","update",Long.toString(date_l),devices.id);
                Ebean.getServer(CommonConfig.LADDER_SERVER).createSqlUpdate(sql).execute();
            }

        }
        Ebean.getServer(CommonConfig.DEVICE_SERVER).saveAll(save_commands);

        Date datex_tmp=new Date();
        long date_l=datex_tmp.getTime();
        List<DeviceInfo> deviceInfos=DeviceInfo.finder.where().lt("delay",Long.toString(date_l)).findList();
        for (DeviceInfo deviceInfo:deviceInfos){
            String sql= String.format("UPDATE ladder.device_info set commond='%s' where id=%d","ok",deviceInfo.id);
            Ebean.getServer(CommonConfig.LADDER_SERVER).createSqlUpdate(sql).execute();
            if(deviceInfo.commond.equals("monitor")){
                stop_monitor(deviceInfo.id);
            }
        }
/*
        for(Commands command:save_commands){
            if(command.command.equals("MONITOR")){

                new Thread(){
                    public void run(){
                        monitorTest.Insert_db3_Test(command.id);
                    }
                }.start();
            }
            if(command.command.equals("CONTRACT")){
                new Thread(){
                    public void run(){
                        monitorTest.Insert_db4_Test(command.id);
                    }
                }.start();

            }
        }
*/



    }
    public  void sendbinary(){
        List<ladder.models.Binaries> binariesList = ladder.models.Binaries.finder.where().isNotNull("t_create").gt("t_create",old_datex).findList();
        List<Binaries> save_binaries =new ArrayList<Binaries>();
        for(ladder.models.Binaries binaries : binariesList){
            if(old_datex.getTime()>= binaries.t_create.getTime()){
                continue;
            }
            Binaries device_binaries =new Binaries();
            device_binaries.data= binaries.data;
            device_binaries.id= binaries.id;
            device_binaries.t_create= binaries.t_create;
            Binaries binaries1 = Binaries.finder.byId(binaries.id);
            if(binaries1 !=null){
                Ebean.getServer(CommonConfig.DEVICE_SERVER).delete(binaries1);
            }
            new_datex= binaries.t_create.getTime()>new_datex.getTime()? binaries.t_create:new_datex;
            save_binaries.add(device_binaries);
        }
        Ebean.getServer(CommonConfig.DEVICE_SERVER).saveAll(save_binaries);
    }

    @Override
    public void run() {
        while (true){
            try{
                Thread.sleep(500);
                snedCommond();
                sendbinary();
                Logger.info("Move Info from db2 to db1 ok at :"+new_datex);
                old_datex=new_datex;
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}
