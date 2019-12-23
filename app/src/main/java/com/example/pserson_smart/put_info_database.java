package com.example.pserson_smart;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class put_info_database extends AppCompatActivity {
    EditText my_thought_text;
    ImageButton voice_btn,carmara_btn;
    Button insert_btn,give_btn,out_put_btn;
    String filename="myrecoder.txt";
    String str;
    String longitude_str;
    String latitude_str;
    String gpsaddr_str;
    String all_person_info="个人信息组:\n";


    private MyDatabaseHelper dbHelper;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.put_info_database);
        my_thought_text=(EditText)findViewById(R.id.edit_location_info);



        voice_btn=(ImageButton)findViewById(R.id.imageButton_voice);
        carmara_btn=(ImageButton)findViewById(R.id.imageButton_camara);
        insert_btn=(Button)findViewById(R.id.button_insert);
        give_btn=(Button)findViewById(R.id.button_give_record);
        out_put_btn=(Button)findViewById(R.id.button_output);
        out_put_btn.setOnClickListener(new mClick());
        voice_btn.setOnClickListener(new mClick());
        carmara_btn.setOnClickListener(new mClick());
        insert_btn.setOnClickListener(new mClick());
        give_btn.setOnClickListener(new mClick());
//        接下来是封装的信息
        Bundle bundle=this.getIntent().getExtras();
        longitude_str=bundle.getString("longitude");
        latitude_str=bundle.getString("latitude");
        gpsaddr_str=bundle.getString("gpsaddr");

        dbHelper =new MyDatabaseHelper(this,"phone_person_info.db",null,2);
        dbHelper.getWritableDatabase();
        Toast.makeText(put_info_database.this,"数据库初始化成功",Toast.LENGTH_SHORT).show();
    }
    public static Date getNetTime(){
        String webUrl = "http://www.ntsc.ac.cn";//中国科学院国家授时中心
        try {
            URL url = new URL(webUrl);
            URLConnection uc = url.openConnection();
            uc.setReadTimeout(5000);
            uc.setConnectTimeout(5000);
            uc.connect();
            long correctTime = uc.getDate();
            Date date = new Date(correctTime);
            return date;
        } catch (Exception e) {
            return new Date();
        }
    }
    private class mClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            if (v==give_btn)
            {
////                写入系统中
//                Log.d("调试信息", "开始读取文本框的内容");
//                try
//                {
//                    FileOutputStream f_out=openFileOutput(filename, Context.MODE_PRIVATE);
//                    f_out.write(thought_info.getBytes());
//                    Log.d("调试信息", "文件写入系统了");
//                    Toast.makeText(put_info_database.this,thought_info, Toast.LENGTH_SHORT).show();
//                }
//                catch (FileNotFoundException e)
//                {
//                    Log.d("调试信息", "文件没有发现");
//                } catch (IOException e) {
////                    e.printStackTrace();
//                    Log.d("调试信息", "文件写入异常");
//                }
////                写入sd卡
//                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
//                {
//                    File path=Environment.getExternalStorageDirectory();
//                    File sdfile=new File(path,filename);
//                    try{
//                        FileOutputStream f_out=new FileOutputStream(sdfile);
//                        f_out.write(thought_info.getBytes());
//                        Log.d("调试信息", "文件写入sd卡了");
//                        Toast.makeText(put_info_database.this,thought_info, Toast.LENGTH_SHORT).show();
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
                all_person_info="个人信息组:\n";
                SQLiteDatabase db=dbHelper.getWritableDatabase();
                //查询Book表中所有数据
                Cursor cursor=db.query("my_info",null,null,null,null,null,null);
                if (cursor.moveToFirst()){
                    do{
                        //遍历Cursor对象，取出数据并打印
                        String name=cursor.getString(cursor.getColumnIndex("this_time_info"));
                        all_person_info=all_person_info+name+"- - -\n";
//                        Log.d("MainActivity", "this time_info is "+name);
//                        Toast.makeText(put_info_database.this,"数据查询成功"+name,Toast.LENGTH_SHORT).show();
                    }while(cursor.moveToNext());
                }
                my_thought_text.setText(all_person_info);
                Toast.makeText(put_info_database.this,"数据总量为:"+all_person_info.length(),Toast.LENGTH_SHORT).show();
//                cursor.close();

            }
            if(v==insert_btn)
            {

                String thought_info;
                String date_now_str = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(getNetTime());
                thought_info=my_thought_text.getText().toString();
                thought_info=longitude_str+"\n"+latitude_str+"\n"+gpsaddr_str+"\n"+"想法:"+thought_info+"\n"+"时间:"+date_now_str+"\n";
                SQLiteDatabase db=dbHelper.getWritableDatabase();
                ContentValues values=new ContentValues();
                //开始组装第一条数据
                String add_info=thought_info;
                values.put("this_time_info",add_info);
                db.insert("my_info",null,values);
                Toast.makeText(put_info_database.this,"数据插入数据库成功",Toast.LENGTH_SHORT).show();
            }
            if (v==voice_btn)
            {
                Intent i = new Intent(Intent.ACTION_MAIN);
                PackageManager manager = getPackageManager();
                i = manager.getLaunchIntentForPackage("com.android.soundrecorder");
                i.addCategory(Intent.CATEGORY_LAUNCHER);
                startActivity(i);

//
//                int give_number;
//                give_number=Integer.parseInt(my_thought_text.getText().toString());
//                text_show_progress_number.setText(my_thought_text.getText().toString());


            }
            if(v==carmara_btn)
            {
                Intent i = new Intent(Intent.ACTION_MAIN);
                PackageManager manager = getPackageManager();
                i = manager.getLaunchIntentForPackage("com.android.camera");
                i.addCategory(Intent.CATEGORY_LAUNCHER);
                startActivity(i);
            }
            if(v==out_put_btn)
            {
                String all_person_info="个人信息组:\n";
                SQLiteDatabase db=dbHelper.getWritableDatabase();
                //查询Book表中所有数据
                Cursor cursor=db.query("my_info",null,null,null,null,null,null);
                if (cursor.moveToFirst()){
                    do{
                        //遍历Cursor对象，取出数据并打印
                        String name=cursor.getString(cursor.getColumnIndex("this_time_info"));
                        all_person_info=all_person_info+name+"- - -\n";
//                        Log.d("MainActivity", "this time_info is "+name);
//                        Toast.makeText(put_info_database.this,"数据查询成功"+name,Toast.LENGTH_SHORT).show();
                    }while(cursor.moveToNext());
                }

                try
                {
                    FileOutputStream f_out=openFileOutput(filename, Context.MODE_PRIVATE);
                    f_out.write(all_person_info.getBytes());
                    Log.d("调试信息", "文件写入系统了");
                    Toast.makeText(put_info_database.this,"文件已经写入成功,大小为:"+all_person_info.length(), Toast.LENGTH_SHORT).show();
                    db.delete("my_info",null,null);
                }
                catch (Exception e) {
//                    e.printStackTrace();
                    Log.d("调试信息", "文件写入异常");
                }
                Intent intent=new Intent(put_info_database.this,control_smart_home.class);
                Bundle bundle=new Bundle();
                bundle.putString("person_info","第二个界面数据传送");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
    }
}
