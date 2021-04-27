package com.example.chats;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SocketHandler extends Service {
    ExecutorService es;
    private static PrintWriter sout;

    public void onCreate() {
        es = Executors.newFixedThreadPool(1);
        super.onCreate();
    }

    public static synchronized PrintWriter getPrintWriter() {
        return sout;
    }

    public void onDestroy() {
        super.onDestroy();
        new Thread(new Runnable() {
            @Override
            public void run() {
                sout.print("stop");
                sout.flush();
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        String nickname = intent.getStringExtra("nickname");
        PendingIntent pi = intent.getParcelableExtra("pending");
        Connect con = new Connect(nickname, startId, pi);
        es.execute(con);
        return super.onStartCommand(intent, flags, startId);
    }

    public static void sendMessage(String to, String msg) {
        final String str =  "to:"+to+",msg:"+msg;
        new Thread(new Runnable() {
            @Override
            public void run() {
                sout.print(str);
                sout.flush();
            }
        }).start();
    }

    class Connect implements Runnable {
        String nickname;
        int startId;
        PendingIntent pi;
        //boolean isStop = false;
        public Connect(String nickname, int startId, PendingIntent pi) {
            this.nickname = nickname;
            this.startId = startId;
            this.pi = pi;
        }

        public void run() {
            while (true) {
                try {
                    Socket soc = new Socket("localhost", 1234);
                    sout = new PrintWriter(soc.getOutputStream(), false);
                    sout.print(nickname);
                    sout.flush();
                    pi.send(MainActivity.STATUS_CONNECTED);

                    InputStream stream = soc.getInputStream();
                    String data;
                    byte[] buf = new byte[1024];
                    while (true) {
                        int count = stream.read(buf, 0, buf.length);
                        if (count > 0) {
                            data = new String(buf, 0, count);
                            Intent intent;
                            if (count >= 5 && data.substring(0, 5).equals("from:")) {
                                int i = data.indexOf(",msg");
                                String from = data.substring(5, i);
                                String msg = data.substring(i + 5);
                                SQL sq = MainActivity.getSQL();
                                SQLiteDatabase db = MainActivity.getSQLite();

                                sq.setMessage(db, from, sq.getMessages(db, from)+msg);
                            } else {
                                String[] nicks = data.split(",");
                                intent = new Intent().putExtra("nicknames", nicks);
                                pi.send(SocketHandler.this, MainActivity.STATUS_GET_DATA, intent);
                            }
                        }
                    }
                } catch (Exception e) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

        void stop() {
            stopSelf(startId);
        }
    }
}
