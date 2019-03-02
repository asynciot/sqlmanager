package test;

import com.avaje.ebean.Ebean;
import controllers.CommonConfig;
import device.models.Commands;
import device.models.Devices;
import device.models.Monitor;
import play.Logger;

import java.util.Date;
import java.util.Random;

/**
 * Created by lengxia on 2018/11/28.
 */
public class MonitorTest {

    public byte[] getdata(int num){
        Random random=new Random();
        byte[] bytes=new byte[8*num];
        for(int i=0;i<num;i++){

            bytes[i*8+0]=(byte)random.nextInt(254);
            bytes[i*8+1]=(byte)random.nextInt(254);
            bytes[i*8+2]=(byte)random.nextInt(254);
            bytes[i*8+3]=(byte)random.nextInt(254);
            bytes[i*8+4]=(byte)random.nextInt(254);
            bytes[i*8+5]=(byte)random.nextInt(254);
            bytes[i*8+6]=(byte)random.nextInt(254);
            bytes[i*8+7]=(byte)random.nextInt(254);

        }

        return bytes;
    }
    public void Insert_db3_Test(Integer command_id){

        try{
            Thread.sleep(2000);
            Commands command= Commands.finder.byId(command_id);
            Commands command1=new Commands();
            command1.execute=new Date();
            command1.result="EXECUTING";
            command1.command=command.command;
            command1.IMEI=command.IMEI;
            command1.id=command.id;
            command1.int1=command.int1;
            command1.submit=command.submit;
            Ebean.getServer(CommonConfig.DEVICE_SERVER).delete(command);
            Ebean.getServer(CommonConfig.DEVICE_SERVER).save(command1);

            for(int i=0;i<command.int2*1000/command.int3;i++){
                Monitor monitor=new Monitor();
                monitor.session=command.int1;
                if(i==command.int2*1000/command.int3-1)
                {
                    Thread.sleep(2000);
                    monitor.length = 0;
                }
                else{
                    monitor.length=1;
                }

                monitor.device_id= Devices.finder.where().eq("IMEI",command.IMEI).findUnique().id;
                monitor.time=new Date();
                monitor.sequence=i;
                monitor.interval=command.int3;
                monitor.data=getdata(1);
                Thread.sleep(command.int3);
                Ebean.getServer(CommonConfig.DEVICE_SERVER).save(monitor);
            }

            command= Commands.finder.byId(command_id);
            Thread.sleep(2000);
            Commands command2=new Commands();
            command2.result="SUCCESS";
            command2.command=command.command;
            command2.finish=new Date();
            command2.IMEI=command.IMEI;
            command2.id=command.id;
            command2.int1=command.int1;
            command2.submit=command.submit;
            Ebean.getServer(CommonConfig.DEVICE_SERVER).delete(command);
            Ebean.getServer(CommonConfig.DEVICE_SERVER).save(command2);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void Insert_db4_Test(int command_id){
        try{
            Logger.info("register  command_id" );
            Thread.sleep(2000);
            Commands command= Commands.finder.byId(command_id);
            Devices devices = Devices.finder.where().eq("IMEI",command.IMEI).findUnique();
            Devices devices1 =new Devices();
            devices1.board= devices.board;
            devices1.cell_cid= devices.cell_cid;
            devices1.cell_lac= devices.cell_lac;
            devices1.cell_mcc= devices.cell_mcc;
            devices1.cell_mnc= devices.cell_mnc;
            devices1.cellular= devices.cellular;
            devices1.contract_id=command.contract;
            devices1.device= devices.device;
            devices1.dock_id= devices.dock_id;
            devices1.firmware= devices.firmware;
            devices1.IMEI= devices.IMEI;
            devices1.ipaddr= devices.ipaddr;
            devices1.IMSI= devices.IMSI;
            devices1.model= devices.model;
            devices1.t_create= devices.t_create;
            devices1.t_update=new Date();
            devices1.t_logon= devices.t_logon;
            devices1.t_logout= devices.t_logout;
            devices1.id= devices.id;
            Ebean.getServer(CommonConfig.DEVICE_SERVER).delete(devices);
            Ebean.getServer(CommonConfig.DEVICE_SERVER).save(devices1);

            Commands command2=new Commands();
            command2.result="SUCCESS";
            command2.command=command.command;
            command2.finish=new Date();
            command2.IMEI=command.IMEI;
            command2.id=command.id;
            command2.int1=command.int1;
            command2.submit=command.submit;
            Ebean.getServer(CommonConfig.DEVICE_SERVER).delete(command);
            Ebean.getServer(CommonConfig.DEVICE_SERVER).save(command2);

        }catch (Exception e){
            e.printStackTrace();
        }


    }
}
