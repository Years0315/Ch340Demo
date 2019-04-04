package com.years.ch340demo.actity;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.years.ch340demo.R;

import java.io.UnsupportedEncodingException;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;

/**
 * 作者：Created by Years on 2019/4/3.
 * 邮箱：791276337@qq.com
 */

public class Activity_Home extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_Receive,tv_rx,tv_tx,tv_LinkState;
    private Button btn_Open,btn_Clear,btn_Send;
    private CheckBox chek_HEX;


    private CH34xUARTDriver Driver;
    private static final String ACTION_USB_PERMISSION =
            "com.years.Ch340Demo.USB_PERMISSION";//定义常量
    private int retval;
    private boolean Flag_IsHex=true;
    private boolean Flag_Open=false;
    private static int baudRate=115200;       //波特率
    private static byte dataBit=8;           //数据位
    private static byte stopBit=1;           //停止位
    private static byte parity=0;            //校验
    private static byte flowControl=0;       //流控
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initActionBar();

        initView();

        initDriver();

        initListener();

        chek_HEX.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    Flag_IsHex=true;
                else
                    Flag_IsHex=false;
            }
        });
    }

    private void initListener() {
        btn_Open.setOnClickListener(this);
        btn_Clear.setOnClickListener(this);
        btn_Send.setOnClickListener(this);
    }

    private void initDriver() {
        Driver=new CH34xUARTDriver((UsbManager) getSystemService(Context.USB_SERVICE),this,ACTION_USB_PERMISSION);
        if (!Driver.UsbFeatureSupported()){
            Toast.makeText(this, "您手机不支持OTG", Toast.LENGTH_SHORT).show();
        }
    }

    private void initView() {
        btn_Open=findViewById(R.id.btn_OpenSerial);
        btn_Clear=findViewById(R.id.btn_clear);
        btn_Send=findViewById(R.id.btn_send);
        tv_Receive=findViewById(R.id.tv_receive);
        chek_HEX=findViewById(R.id.cek_hex);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_home,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.action_about)
        {
            Intent intent =new Intent(Activity_Home.this,Activity_About.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void initActionBar() {
        ActionBar localActionBar=getSupportActionBar();
        if (localActionBar!=null)
        {
            localActionBar.setElevation(0);
            ActionBar.LayoutParams localLayoutParams=new ActionBar.LayoutParams(-2,-2);
            localLayoutParams.gravity=(0x1|0xfffffff8&localLayoutParams.gravity);

            localActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME|ActionBar.DISPLAY_SHOW_CUSTOM|ActionBar.DISPLAY_HOME_AS_UP);
            localActionBar.setDisplayShowCustomEnabled(true);
            localActionBar.setDisplayShowHomeEnabled(false);
            localActionBar.setDisplayHomeAsUpEnabled(false);
            View localView=null;

            localView=getLayoutInflater().inflate(R.layout.actionbar_home,null);

            localActionBar.setCustomView(localView, localLayoutParams);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_OpenSerial:
                openDevice();
                break;
            case R.id.btn_clear:
                tv_Receive.setText("");
                break;
            case R.id.btn_send:
                break;
        }
    }
    private void openDevice(){
        if (!Flag_Open){

            retval=Driver.ResumeUsbList();
            if (retval==-1){
                Toast.makeText(this, "打开设备失败", Toast.LENGTH_SHORT).show();
                Driver.CloseDevice();
            }
            else if (retval==0){
                if (!Driver.UartInit()){
                    Toast.makeText(this, "设备初始化失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(this, "打开设备成功", Toast.LENGTH_SHORT).show();
                Flag_Open=true;
                btn_Open.setText("关闭");

                configSerialPort();
                readData();

            }
        }
        else{
            Driver.CloseDevice();
            btn_Open.setText("打开");
            Flag_Open=false;
        }
    }
    byte[] buffer=new byte[64];         //接收缓冲
    private void readData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(Flag_Open){
                    int length=Driver.ReadData(buffer,64);
                    if (length>0) {
                        String str;
                        if(Flag_IsHex)
                            str=byte2HexString(buffer,length);
                        else
                            str=byte2String(buffer);

                        textScroll(str);
                    }
                }
            }
        }).start();
    }
    private String byte2String(byte[] data) {

        String res = new String();
        try {
            res=new String(data,"GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return res;
    }
    private void textScroll(final String str)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_Receive.append(str);
                int offset=tv_Receive.getLineCount()*tv_Receive.getLineHeight();
                if (offset>tv_Receive.getHeight())
                    tv_Receive.scrollTo(0,offset-tv_Receive.getHeight());
            }
        });
    }
    private String byte2HexString(byte[] arg, int length) {
        String result = new String();
        if (arg != null) {
            for (int i = 0; i < length; i++) {
                result = result
                        + (Integer.toHexString(
                        arg[i] < 0 ? arg[i] + 256 : arg[i]).length() == 1 ? "0"
                        + Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])
                        : Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])) + " ";
            }
            return result;
        }
        return "";
    }
    private void configSerialPort(){
        if (Driver.SetConfig(baudRate,dataBit,stopBit,parity,flowControl)){
            Toast.makeText(this, "串口配置成功", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "串口配置失败", Toast.LENGTH_SHORT).show();
        }
    }
}
