package com.github.kassadin.shoppingcart;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void jdClick(View view) {
        JdCartActivity.start(this);
    }
    public void xcfClick(View view) {
        XcfActivity.start(this);
    }
}
