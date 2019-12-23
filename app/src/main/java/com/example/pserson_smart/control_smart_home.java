package com.example.pserson_smart;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;

public class control_smart_home  extends AppCompatActivity {
    ImageButton btn_control_light,btn_control_curtain, btn_control_flower,btn_auto;
    Button start_send_person_data_btn,btn_link_internet;
    private Socket socket = null;
    private DataInputStream dis = null;
    private DataOutputStream dos = null;
    String msg = "";
    send_error_hander seh=new send_error_hander();
    send_progress_hander sph=new send_progress_hander();
    send_text_hander sth=new send_text_hander();
    thread_for_send tfs=new thread_for_send();
    receive_text_hander rth=new receive_text_hander();
//    private thread_for_receive my_recive_thread;
    int flag_light_status=0;
    int flag_curtain_status=0;
    int flag_flower_status=0;
    int flag_auto_status=0;
    String my_person_info_all;
    String filename="myrecoder.txt";
    String str;
    ProgressBar show_transportation_number;
    TextView text_show_progress_number,show_internet;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_smart_home);

        show_internet=(Button)findViewById(R.id.button_recive_data);
        show_internet.setOnClickListener(new mClick());

        btn_control_light = (ImageButton) findViewById(R.id.button_light);
        btn_control_light.setOnClickListener(new mClick());
        btn_control_curtain = (ImageButton) findViewById(R.id.button_curtain);
        btn_control_curtain.setOnClickListener(new mClick());
        btn_control_flower=(ImageButton)findViewById(R.id.button_water_flower) ;
        btn_control_flower.setOnClickListener(new mClick());
        btn_auto=(ImageButton)findViewById(R.id.button_auto) ;
        btn_auto.setOnClickListener(new mClick());
        start_send_person_data_btn=(Button)findViewById(R.id.button_start_send);
        start_send_person_data_btn.setOnClickListener(new mClick());
        text_show_progress_number=(TextView)findViewById(R.id.textView_show_number);
        text_show_progress_number.setText('0'+"%");
        show_transportation_number=(ProgressBar)findViewById(R.id.progressBar_tansportatino_info);
        show_transportation_number.setMax(100);
        show_transportation_number.setProgress(0);
        StrictMode.setThreadPolicy(new StrictMode
                .ThreadPolicy
                .Builder()
                .detectDiskWrites()
                .detectDiskReads()
                .detectNetwork()
                .penaltyLog()
                .build());
        //接下来接收信息进行发送的，个人信息
        Bundle bundle=this.getIntent().getExtras();
        my_person_info_all=bundle.getString("person_info");
        if(my_person_info_all.equals("直接跳转:无数据"))
        {
            start_send_person_data_btn.setClickable(false);
        }
//        Toast.makeText(control_smart_home.this, "收到数据为:"+my_person_info_all, Toast.LENGTH_SHORT).show();
        //                Toast.makeText(control_smart_home.this, "客户端开始创建", Toast.LENGTH_SHORT).show();
//                Log.d("调试信息", " 客户端开始创建");
        try {
            socket = new Socket("192.168.43.115", 9093);
            dis = new DataInputStream(socket.getInputStream());
//            Toast.makeText(control_smart_home.this, "dis为:"+dis, Toast.LENGTH_SHORT).show();
            dos = new DataOutputStream(socket.getOutputStream());
//            Toast.makeText(control_smart_home.this, "dos为:"+dos, Toast.LENGTH_SHORT).show();
            dos.writeUTF("连接上了，给我数据");
            dos.flush();
            Toast.makeText(control_smart_home.this, "客户端创建成功", Toast.LENGTH_SHORT).show();
            Log.d("调试信息", " 客户端创建成功");


        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(control_smart_home.this, "客户端创建失败", Toast.LENGTH_SHORT).show();
            Log.d("调试信息", " 客户端创建失败");
        }



//        my_recive_thread=new thread_for_receive();
//        my_recive_thread.start();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            WriteString("goodbye");
            socket.close();
        }
        catch (Exception e){

        }
    }

    private void ReadStr() {
            try {
                //            Toast.makeText(control_smart_home.this, "收到客户端的测试数据为:"+dis.readUTF(), Toast.LENGTH_SHORT).show();
                if((msg = dis.readUTF())!=null)
                    Toast.makeText(control_smart_home.this, "收到客户端的数据为:" + msg, Toast.LENGTH_SHORT).show();
//                Toast.makeText(control_smart_home.this, "收到客户端的数据为:" + msg, Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(control_smart_home.this, "客户端获取数据失败", Toast.LENGTH_SHORT).show();
            }
    }

    private void WriteString(String msg) {
        try {
            dos.writeUTF(msg);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class mClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v == btn_control_light) {
                if (flag_light_status==1)
                {
                    btn_control_light.setImageResource(R.drawable.close_light_1);
                    flag_light_status=0;
                    WriteString("close_light");

                }
                else if (flag_light_status==0)
                {
                    btn_control_light.setImageResource(R.drawable.open_light_0);
                    flag_light_status=1;
                    WriteString("open_light");

                }
//                WriteString("open_light");
//                Toast.makeText(control_smart_home.this, "socket 为:"+socket, Toast.LENGTH_SHORT).show();
//                ReadStr();
            }
            if (v == btn_control_curtain) {
                if(flag_curtain_status==1)
                {
                    btn_control_curtain.setImageResource(R.drawable.close_curtain_1);
                    flag_curtain_status=0;
                    WriteString("close_curtain");
                }
                else if(flag_curtain_status==0)
                {
                    btn_control_curtain.setImageResource(R.drawable.open_curtain_0);
                    flag_curtain_status=1;
                    WriteString("open_curtain");
                }
            }
            if (v == btn_control_flower) {
                if(flag_flower_status==1)
                {
                    btn_control_flower.setImageResource(R.drawable.close_flower_1);
                    flag_flower_status=0;
                    WriteString("close_flower");
                }
                else if(flag_flower_status==0)
                {
                    btn_control_flower.setImageResource(R.drawable.open_flower_0);
                    flag_flower_status=1;
                    WriteString("open_flower");
                }
            }
            if(v==show_internet)
            {
//                thread_for_receive tfr=new thread_for_receive();
                ReadStr();
//                tfr.start();
//                Toast.makeText(control_smart_home.this, "接收线程已经启动", Toast.LENGTH_SHORT).show();
            }
            if (v == btn_auto) {
                if(flag_auto_status==1)
                {
                    btn_auto.setImageResource(R.drawable.close_auto_1);
                    flag_auto_status=0;
                    WriteString("close_auto");
                }
                else if(flag_auto_status==0)
                {
                    btn_auto.setImageResource(R.drawable.open_auto_0);
                    flag_auto_status=1;
                    WriteString("open_auto");
                }
            }
            if (v==start_send_person_data_btn)
            {
                show_transportation_number.incrementProgressBy(100);
                show_transportation_number.incrementProgressBy(-100);
                tfs.start();
            }
        }
    }
    private class receive_text_hander extends Handler{
        @Override
        public void handleMessage(@NonNull Message receive_msg) {
            super.handleMessage(receive_msg);
//            if(receive_msg.what>0)
//            {
//                Toast.makeText(control_smart_home.this, "收到客户端的测试数据为:"+msg, Toast.LENGTH_SHORT).show();
//            }
//            else{
//                Toast.makeText(control_smart_home.this, "收到客户端的测试数据失败", Toast.LENGTH_SHORT).show();
//            }
            if (receive_msg.what==1){
                Toast.makeText(control_smart_home.this, "收到客户端的数据为:"+msg.length(), Toast.LENGTH_SHORT).show();
                msg=null;
            }

                if(receive_msg.what==2)
                {
                    Toast.makeText(control_smart_home.this, "收到客户端的错误为:"+msg, Toast.LENGTH_SHORT).show();
                    msg=null;
                }

            Toast.makeText(control_smart_home.this, "收到客户端的状态为:"+receive_msg.what, Toast.LENGTH_SHORT).show();
        }
    }
    private class internet_thread_for_receive extends Thread
    {
        @Override
        public void run() {
            while(true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    if((msg = dis.readUTF())!=null)
                    {
                        Message  receive_my=new Message();
                        receive_my.what= 1;
                        rth.sendMessage(receive_my);
                    }
                    else
                    {
                        Message  receive_my5=new Message();
                        receive_my5.what= 5;
                        rth.sendMessage(receive_my5);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message  receive_my2=new Message();
                    receive_my2.what= 2;
                    msg="1"+e;
                    rth.sendMessage(receive_my2);
                }
            }
        }
    }
    private class send_text_hander extends Handler{
        @Override
        public void handleMessage(@NonNull Message change_msg) {
            super.handleMessage(change_msg);
                text_show_progress_number.setText(String.valueOf(change_msg.arg1) + '%');
        }
    }
    private class send_progress_hander extends Handler{
        @Override
        public void handleMessage(@NonNull Message change_msg) {
            super.handleMessage(change_msg);
                show_transportation_number.incrementProgressBy(change_msg.what);
        }
    }
    private class send_error_hander extends Handler{
        @Override
        public void handleMessage(@NonNull Message change_msg) {
            super.handleMessage(change_msg);
            if(change_msg.arg2<0)
            {
                Toast.makeText(control_smart_home.this, "数据传送失败", Toast.LENGTH_SHORT).show();
            }
            if (change_msg.arg2==100)
            {
                Toast.makeText(control_smart_home.this, "数据传送完成", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private class thread_for_send extends Thread
    {

        @Override
        public void run() {

        try {
            String send_msg;
            start_send_person_data_btn.setClickable(false);
            Message send_change_msg_first = new Message();
            send_change_msg_first.what = 0;
            sph.sendMessage(send_change_msg_first);
            Message send_text_msg_first = new Message();
            send_text_msg_first.arg1 = 0;
            sth.sendMessage(send_text_msg_first);
            byte[] buffer = new byte[9999999];
            FileInputStream in_file = null;
            try {
                in_file = openFileInput(filename);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            int bytes = 0;
            try {
                bytes = in_file.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            str = new String(buffer, 0, bytes);
            int str_long = str.length();
//                Toast.makeText(control_smart_home.this,"信息量为:"+str_long, Toast.LENGTH_SHORT).show();
            int number_count = str_long / 300;
            int increat_number = 100 / (number_count + 1);
            int i = 1;

            for (i = 1; i <= number_count; i++) {
                send_msg = str.substring((i - 1) * 300, i * 300);
                WriteString(send_msg);
                Message send_text_msg1 = new Message();
                send_text_msg1.arg1 = i * increat_number;
                sth.sendMessage(send_text_msg1);
                Message send_change_msg1 = new Message();
                send_change_msg1.what = increat_number;
                sph.sendMessage(send_change_msg1);

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            send_msg = str.substring((number_count - 1) * 300, str_long);
            WriteString(send_msg);
            Message send_text_msg2 = new Message();
            send_text_msg2.arg1 = 100;
            sth.sendMessage(send_text_msg2);
            Message send_change_msg2 = new Message();
            send_change_msg2.what = 100;
            sph.sendMessage(send_change_msg2);
            Message send_error_msg = new Message();
            send_error_msg.arg2 =100;
            seh.sendMessage(send_error_msg);
            start_send_person_data_btn.setClickable(true);
        }
        catch (Exception e)
        {
            Message send_error_msg1 = new Message();
            send_error_msg1.arg2 = -1;
            seh.sendMessage(send_error_msg1);
        }

        }
    }
}
