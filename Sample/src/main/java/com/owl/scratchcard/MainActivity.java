package com.owl.scratchcard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.owl.widget.scratchcard.ScratchCard;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ScratchCard scratchCard = (ScratchCard) findViewById(R.id.id_main_scratch);
        scratchCard.setScratchCompleteListener(new ScratchCard.OnScratchCompleteListener() {
            @Override
            public void onComplete() {
                Toast.makeText(MainActivity.this, "用户消除了", Toast.LENGTH_SHORT).show();
            }
        });
        scratchCard.setText("中大奖了");
    }
}
