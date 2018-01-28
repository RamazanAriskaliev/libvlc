package com.app.com.vlcstudio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_url;
    private EditText et_title;
    private TextView tv_vip;
    private Spinner sp_vip;
    private TextView enter;
    private ArrayAdapter<CharSequence> adapter;
    private boolean isVip;
    private boolean isAudio;
    private TextView tv_audio;
    private Spinner sp_audio;
    private boolean isLive;
    private Spinner sp_live;
    private TextView tv_live;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        et_url = (EditText) findViewById(R.id.et_url);
        et_title = (EditText) findViewById(R.id.et_title);
        tv_vip = (TextView) findViewById(R.id.tv_vip);
        sp_vip = (Spinner) findViewById(R.id.sp_vip);


        tv_audio = (TextView) findViewById(R.id.tv_audio);

        sp_audio = (Spinner) findViewById(R.id.sp_audio);

        sp_live = (Spinner) findViewById(R.id.sp_live);
        tv_live = (TextView) findViewById(R.id.tv_live);

        enter = (TextView) findViewById(R.id.enter);
        enter.setOnClickListener(this);

        //将可选内容与ArrayAdapter连接起来
        adapter = ArrayAdapter.createFromResource(this, R.array.plantes, android.R.layout.simple_spinner_item);
        //设置下拉列表的风格
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter2 添加到spinner中
        sp_vip.setAdapter(adapter);
        sp_live.setAdapter(adapter);
        sp_audio.setAdapter(adapter);
        //添加事件Spinner事件监听
        sp_vip.setOnItemSelectedListener(new SpinnerXMLSelectedListener());
        sp_audio.setOnItemSelectedListener(new SpinnerXMLSelectedListeneraudio());
        sp_live.setOnItemSelectedListener(new SpinnerXMLSelectedListenerlive());
        //设置默认值
        sp_vip.setVisibility(View.VISIBLE);
        sp_audio.setVisibility(View.VISIBLE);
        sp_live.setVisibility(View.VISIBLE);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.enter:
                if (isAudio){
                    Intent intent = new Intent(this,AudioPlayerActivity.class);
                    intent.putExtra("url",et_url.getText().toString());
                    startActivity(intent);
                }else {
                    Intent intent = new Intent(this,AdvertisementPlayerActivity.class);
                    intent.putExtra("url",et_url.getText().toString());
                    intent.putExtra("title",et_title.getText().toString());
                    intent.putExtra("isVip",isVip);
                    intent.putExtra("isLive",isLive);
                    startActivity(intent);
                }
                break;

        }
    }

    //使用XML形式操作
    class SpinnerXMLSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            if (adapter.getItem(arg2).equals("YES")){
                isVip = true;
            }else{
                isVip = false;
            }
        }
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
    //使用XML形式操作
    class SpinnerXMLSelectedListeneraudio implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            if (adapter.getItem(arg2).equals("YES")){
                isAudio = true;
            }else{
                isAudio = false;
            }
        }
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
    //使用XML形式操作
    class SpinnerXMLSelectedListenerlive implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            if (adapter.getItem(arg2).equals("YES")){
                isLive = true;
            }else{
                isLive = false;
            }
        }
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }



}
