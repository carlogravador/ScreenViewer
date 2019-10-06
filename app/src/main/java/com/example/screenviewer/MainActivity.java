package com.example.screenviewer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    CardView mShareCardView;
    CardView mAccessCardView;

    public void initUI()
    {
        mShareCardView = findViewById(R.id.card_view_share);
        mAccessCardView = findViewById(R.id.card_view_access);

        mShareCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, CreateLobbyActivity.class);
                startActivity(i);
            }
        });

        mAccessCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
    }
}
