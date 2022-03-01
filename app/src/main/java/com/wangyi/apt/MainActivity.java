package com.wangyi.apt;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.wangyi.annotation.BindView;
import com.wangyi.annotation.OnClick;
import com.wangyi.library.ButterKnife;
import com.wangyi.library.DebouncingOnClickListener;
import com.wangyi.library.ViewBinder;

public class MainActivity extends AppCompatActivity{

    @BindView(R.id.tv_name)
    TextView mTvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mTvName.setText("APT");
    }

    @OnClick(R.id.tv_name)
    void onClick(View view) {
        Toast.makeText(this, "click", Toast.LENGTH_SHORT).show();
    }

}