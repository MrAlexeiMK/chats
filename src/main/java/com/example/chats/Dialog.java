package com.example.chats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

public class Dialog extends AppCompatActivity {
    ScrollView list;
    TextView ms;
    EditText text;
    Button send,back,clear;
    SQL sq;
    SQLiteDatabase db;
    String to;
    String messages;
    String nickname;
    Handler h;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        Intent intent = getIntent();
        to = intent.getStringExtra("to");
        sq = new SQL(this);
        db = sq.getWritableDatabase();
        nickname = sq.getNickname(db);

        list = (ScrollView)findViewById(R.id.list);
        text = (EditText)findViewById(R.id.text);
        ms = (TextView)findViewById(R.id.ms);
        send = (Button)findViewById(R.id.send);
        back = (Button)findViewById(R.id.back);
        clear = (Button)findViewById(R.id.clear);

        messages = sq.getMessages(db, to);
        ms.setText(messages);
        h = new Handler() {
            public void handleMessage(Message msg) {
                if(msg.what == 1) {
                    ms.setText(messages);
                }
            }
        };

        th.start();
        list.post(new Runnable() {
            @Override
            public void run() {
                list.scrollTo(0, list.getBottom());
            }
        });
    }

    public static void hideKeyboard(Activity ac) {
        InputMethodManager imm = (InputMethodManager) ac.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(ac.getCurrentFocus().getWindowToken(), 0);
    }

    Thread th = new Thread(new Runnable() {
        @Override
        public void run() {
            while(true) {
                String msg = sq.getMessages(db, to);
                if (!msg.equals(messages)) {
                    messages = msg;
                    h.sendEmptyMessage(1);
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    Log.e("error", e.getMessage());
                }
            }
        }
    });

    public void onclick(View v) {
        if(v.getId() == R.id.send) {
            String s = text.getText().toString();
            if (s.matches("") || s == null) {
                Toast.makeText(getApplicationContext(), "Вы ничего не набрали", Toast.LENGTH_SHORT).show();
                return;
            }
            messages += s + "\n";
            ms.setText(messages);
            sq.setMessage(db, to, messages);
            if(!to.equals(nickname)) SocketHandler.sendMessage(to, "--> "+s+"\n");

            text.setText("");
            hideKeyboard(this);
        }
        else if(v.getId() == R.id.back) {
            finish();
        }
        else if(v.getId() == R.id.clear) {
            sq.clearMessages(db, to);
        }
    }
}
