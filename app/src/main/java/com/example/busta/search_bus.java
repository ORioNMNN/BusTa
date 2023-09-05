package com.example.busta;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class search_bus extends AppCompatActivity {

    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_bus);

        searchButton = findViewById(R.id.searchButton);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deviceName = ((EditText) findViewById(R.id.deviceNameEditText)).getText().toString();

                // 화면 2로 이동하는 인텐트를 생성하고 블루투스 디바이스 이름을 인텐트에 추가합니다.
                Intent intent = new Intent(search_bus.this, search_ing.class);
                intent.putExtra("deviceName", deviceName);
                startActivity(intent);
            }
        });
    }
}
