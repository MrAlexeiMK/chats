package com.example.chats;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Register extends AppCompatActivity {
    Button ok;
    EditText nickname;
    SQL sq;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        sq = new SQL(this);
        db = sq.getWritableDatabase();
        nickname = (EditText)findViewById(R.id.nick);
        ok = (Button)findViewById(R.id.ok);
        if(sq.getNickname(db) != null) {
            nextActivity();
        }
    }

    public void onclick(View v) {
        String s = nickname.getText().toString();
        if(s.matches("") || s == null) {
            Toast.makeText(getApplicationContext(), "Введите ник", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            sq.addNickname(db, s);
            nextActivity();
        }
    }

    public void nextActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
