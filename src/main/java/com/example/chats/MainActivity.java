package com.example.chats;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public final static int STATUS_CONNECTED = 100;
    public final static int STATUS_DISCONNECTED = 101;
    public final static int STATUS_GET_DATA = 102;
    String[] nicks;
    ListView list;
    TextView text,nick;
    ArrayAdapter<String> adapter;
    String nickname;
    private static SQL sq;
    private static SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = (ListView)findViewById(R.id.list);
        text = (TextView)findViewById(R.id.text);
        nick = (TextView)findViewById(R.id.nick);

        sq = new SQL(this);
        db = sq.getWritableDatabase();

        nickname = sq.getNickname(db);
        nick.setText(nickname);

        text.setText("Подключение...");

        //To Dialog.java
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                String to = list.getItemAtPosition(position).toString();
                Intent intent = new Intent(MainActivity.this, Dialog.class);
                intent.putExtra("to", to);
                startActivity(intent);
            }
        });

        startConnect();

    }
    @Override
    public void onDestroy() {
        stopConnect();
        super.onDestroy();
    }

    public static SQL getSQL() {
        return sq;
    }

    public static SQLiteDatabase getSQLite() {
        return db;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case STATUS_CONNECTED:
                text.setText("Подключено");
                break;
            case STATUS_DISCONNECTED:
                text.setText("Подключение...");
                break;
            case STATUS_GET_DATA:
                nicks = data.getStringArrayExtra("nicknames");
                updateList();
                break;
        }
    }

    public void startConnect() {
        PendingIntent pi;
        Intent intent;
        Intent www = new Intent();
        pi = createPendingResult(1, www, 0);
        intent = new Intent(MainActivity.this, SocketHandler.class).putExtra("nickname", nickname)
                .putExtra("pending", pi);
        startService(intent);
    }
    public void stopConnect() {
        stopService(new Intent(MainActivity.this, SocketHandler.class));
    }

    public void updateList() {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nicks);
        list.setAdapter(adapter);
    }

    public void changeNickname(View v) {
        stopConnect();
        sq.delUser(db);
        Intent intent = new Intent(MainActivity.this, Register.class);
        startActivity(intent);
        finish();
    }
}
