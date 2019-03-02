package threads;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.CommonConfig;
import ladder.models.Cellocation;
import ladder.models.DeviceInfo;
import ladder.models.Devices;
import ladder.models.IPlocation;
import play.Logger;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Created by lengxia on 2018/11/22.
 */

public class GetMessThread extends Thread {


    private static int TIME_OUT = 5000;
    public GetMessThread(){
        Logger.info("creat Get Mess Thread ok");
    }


    public  Integer cell2addr(int mcc, int mnc, int lac, int ci){
        Cellocation cellocation=Cellocation.finder.where().eq("cell_mcc",mcc).eq("cell_mnc",mnc).eq("cell_lac",lac).eq("cell_cid",ci).findUnique();
        if(cellocation!=null)return cellocation.id;
        try {
            String url = "http://api.cellocation.com:81/cell/";
            CompletionStage<JsonNode> jsonPromise = WS.url(url)
                    .setContentType("application/json")
                    .setRequestTimeout(TIME_OUT)
                    .setQueryParameter("mcc", String.valueOf(mcc))
                    .setQueryParameter("mnc", String.valueOf(mnc))
                    .setQueryParameter("lac", String.valueOf(lac))
                    .setQueryParameter("ci", String.valueOf(ci))
                    .get()
                    .thenApply(WSResponse::asJson);
            JsonNode retVal = jsonPromise.toCompletableFuture().get();
            if(retVal.get("errcode").asInt()==0){
                cellocation=new Cellocation();
                cellocation.cell_cid=ci;
                cellocation.address=retVal.get("address").asText();
                cellocation.cell_lac=lac;
                cellocation.cell_mcc=mcc;
                cellocation.cell_mnc=mnc;
                cellocation.lat=retVal.get("lat").asDouble();
                cellocation.lon=retVal.get("lon").asDouble();
                cellocation.radius=retVal.get("radius").asDouble();
                Ebean.getServer(CommonConfig.LADDER_SERVER).save(cellocation);
                return cellocation.id;
            }
        } catch (Throwable e) {
            Logger.error("Get celllocation Error");
            //e.printStackTrace();
        }
        return null;
    }
    public Integer iptoaddr(String ip){
        IPlocation iploc=IPlocation.finder.where().eq("ip",ip).findUnique();
        if(iploc!=null){return iploc.id;}

        String url="http://ip.taobao.com/service/getIpInfo.php?ip="+ip;

        try{
            CompletionStage<JsonNode> jsonPromise =WS.url(url)
                    .setContentType("application/json")
                    .setRequestTimeout(TIME_OUT)
                    .get()
                    .thenApply(WSResponse::asJson);
            JsonNode retVal = jsonPromise.toCompletableFuture().get();
            if(retVal.get("code").asInt()==0){

                retVal=retVal.get("data");
                IPlocation iPlocation=new IPlocation();
                iPlocation.city=retVal.get("city").asText();
                iPlocation.area=retVal.get("area").asText();
                iPlocation.area_id=retVal.get("area_id").asText();
                iPlocation.city_id=retVal.get("city_id").asText();
                iPlocation.country=retVal.get("country").asText();
                iPlocation.country_id=retVal.get("country_id").asText();
                iPlocation.county_id=retVal.get("county_id").asText();
                iPlocation.county=retVal.get("county").asText();
                iPlocation.ip=retVal.get("ip").asText();
                iPlocation.isp=retVal.get("isp").asText();
                iPlocation.region=retVal.get("region").asText();
                iPlocation.region_id=retVal.get("region_id").asText();
                Ebean.getServer(CommonConfig.LADDER_SERVER).save(iPlocation);
                return iPlocation.id;
            }

        }
        catch (Throwable e) {
            Logger.error("Get iptoaddr Error");
            //e.printStackTrace();
        }
        return  null;
    }
    @Override
    public void run() {

        while (true){
            try {
                List<DeviceInfo> deviceInfoList;
                deviceInfoList= DeviceInfo.finder.where().isNull("cellocation_id").findList();
                for(DeviceInfo deviceInfo:deviceInfoList){
                    Devices device=Devices.finder.where().eq("IMEI",deviceInfo.IMEI).findUnique();
                    if(device==null)
                        continue;
                    Integer cellid=cell2addr(device.cell_mcc,device.cell_mnc,device.cell_lac,device.cell_cid);
                    if(cellid==null)
                        continue;
                    String sql= String.format("UPDATE ladder.device_info set cellocation_id=%d where id=%s",cellid,deviceInfo.id);
                    Ebean.getServer(CommonConfig.LADDER_SERVER).createSqlUpdate(sql).execute();
                    try{
                        Thread.sleep(2000);
                    }catch (Exception e){};
                }
                deviceInfoList=DeviceInfo.finder.where().isNull("iplocation_id").findList();
                for(DeviceInfo deviceInfo:deviceInfoList){
                    Devices device=Devices.finder.where().eq("IMEI",deviceInfo.IMEI).findUnique();
                    if(device==null)
                        continue;
                    Integer ipaddr_id=iptoaddr(device.ipaddr);
                    if(ipaddr_id==null)
                        continue;

                    String sql= String.format("UPDATE ladder.device_info set iplocation_id=%d where id=%s",ipaddr_id,deviceInfo.id);
                    Ebean.getServer(CommonConfig.LADDER_SERVER).createSqlUpdate(sql).execute();
                    try{
                        Thread.sleep(2000);
                    }catch (Exception e){};
                }

                Date date=new Date();
                Logger.info("Get Mess Thread ok at "+date+" Thread"+Thread.currentThread().getId());

            }catch (Exception e) {
                e.printStackTrace();
                Logger.info("Get Mess Thread fault Thread"+ Thread.currentThread().getId());
            }
            finally {
                try{
                    Thread.sleep(30000);
                }catch (Exception e){};
            }



        }

    }

}