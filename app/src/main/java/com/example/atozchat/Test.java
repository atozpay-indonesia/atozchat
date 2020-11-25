package com.example.atozchat;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.activeandroid.query.Select;

import java.util.List;

public class Test extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        for (int i = 1; i < 6; i++) {

        }

        List<ChatRoom> rooms = new Select().from(ChatRoom.class).execute();
    }
}
