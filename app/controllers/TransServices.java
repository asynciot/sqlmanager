package controllers;

import threads.GetInfoThread;
import threads.GetMessThread;
import threads.GetDevThread;
import threads.SendInfoThread;
import threads.GetEventThread;
import threads.GetOffThread;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by lengxia on 2018/10/31.
 * 轮询方法来把一号数据库的数据转移到二号数据库。
 */
@Singleton
public class TransServices {

    @Inject
    public TransServices(){
        new Thread(new GetInfoThread()).start();
        new Thread(new GetDevThread()).start();
        new Thread(new GetMessThread()).start();
        new Thread(new SendInfoThread()).start();
        new Thread(new GetOffThread()).start();
        new Thread(new GetEventThread()).start();
    }
}
