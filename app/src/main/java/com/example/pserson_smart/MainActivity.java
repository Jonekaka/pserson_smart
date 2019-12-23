package com.example.pserson_smart;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import java.io.File;

class LocationInstance {
    /**
     * 第一步：初始化LocationClient类
     */
    public LocationClient mLocationClient;
    private MyLocationListener myListener;
    //BDAbstractLocationListener为7.2版本新增的Abstract类型的监听接口
//原有BDLocationListener接口暂时同步保留。具体介绍请参考后文第四步的说明

    public LocationInstance(Context context, MyLocationListener myListener) {
        Log.d("调试信息", "定位启动");
        mLocationClient = new LocationClient(context);
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        //注册监听函数

        /**
         * 第二步：配置定位SDK参数
         */
        Log.d("调试信息", "定位配置");
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(1000);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIgnoreKillProcess(false);
        option.SetIgnoreCacheException(false);
        option.setWifiCacheTimeOut(5 * 60 * 1000);
        option.setEnableSimulateGps(false);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    /**
     * 第三步.实现BDAbstractLocationListener接口
     */
    public static class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明
//            Log.d("调试信息", "定位获取信息0000");
            double latitude = location.getLatitude();    //获取纬度信息
            double longitude = location.getLongitude();    //获取经度信息
            float radius = location.getRadius();    //获取定位精度，默认值为0.0f

            String coorType = location.getCoorType();
            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准

            int errorCode = location.getLocType();
            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
//            Log.d("MAP",
//                    location.getAddrStr()+","+"" +
//                            location.getLatitude()+","+
//                            location.getLongitude());
        }
    }

    /**
     * 第四步：设置定位的开始和结束的方法
     * 1s定位一次非常耗电的操作
     */
    public void start() {
        mLocationClient.start();
        Log.d("调试信息", "定位启动成功");
    }

    public void stop() {
        mLocationClient.stop();
        Log.d("调试信息", "定位停止");
    }
}

public class MainActivity extends AppCompatActivity {
    private MapView mMapView = null;
    private LocationInstance mLocationInstance;
    private BDLocation lastLocation;//存放最新一次的位置
    private BaiduMap mBaiduMap;
    public static final int ITEM_ID_NORMAL_MAP = 101;
    public static final int ITEM_ID_SATELLITE = 102;
    public static final int ITEM_ID_TIME = 103;
    public static final int ITEM_ID_HOT = 104;
    public static final int ITEM_ID_CALL=105;
    public static final int ITEM_ID_PLAN=106;
    public static final int ITEM_ID_ALARM=107;
    SensorManager systemService;
    Sensor sensor;
    Message msg=new Message();
    mhander myhandler=new mhander();
    Vibrator vib;
    String hope_location="未知",hope_location_info="未知";
    RelativeLayout location_service_goal;
    Button Btn_loc,Btn_add_info,Btn_control_home;
    EditText location_txt,loication_info_txt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        systemService = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = systemService.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(15f));
        Btn_loc=(Button)findViewById(R.id.button_myloc);
        Btn_loc.setOnClickListener(new mClick());
        Btn_add_info=(Button)findViewById(R.id.button_record_info);
        Btn_add_info.setOnClickListener(new mClick());
        Btn_control_home=(Button)findViewById(R.id.button_control_home);
        Btn_control_home.setOnClickListener(new mClick());
//        mBaiduMap = mMapView.getMap();
//        //普通地图 ,mBaiduMap是地图控制器对象
//        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
//        mBaiduMap.setMyLocationEnabled(true);
        Log.d("调试信息", "设置为普通地图");
        getregist();
        Log.d("调试信息", "开始注册");
        mMapView = findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        initLocation();
        Log.d("调试信息", "展示定位");
    }
    /**
     * 创建添加菜单
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, ITEM_ID_NORMAL_MAP, 1, "普通地图");
        menu.add(Menu.NONE, ITEM_ID_SATELLITE, 2, "卫星地图");
        menu.add(Menu.NONE, ITEM_ID_TIME, 3, "实时路况");
        menu.add(Menu.NONE, ITEM_ID_HOT, 4, "城市热力");
        menu.add(Menu.NONE,ITEM_ID_CALL,5,"地点提醒");
        menu.add(Menu.NONE,ITEM_ID_PLAN,6,"今日计划");
        menu.add(Menu.NONE,ITEM_ID_ALARM,7,"时间提醒");
        return super.onCreateOptionsMenu(menu);
    }
    /**
     * 菜单点击事件
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ITEM_ID_NORMAL_MAP:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                mBaiduMap.setTrafficEnabled(false);
                mBaiduMap.setBaiduHeatMapEnabled(false);
                break;
            case ITEM_ID_SATELLITE:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                mBaiduMap.setTrafficEnabled(false);
                mBaiduMap.setBaiduHeatMapEnabled(false);

                break;
            case ITEM_ID_TIME:
                mBaiduMap.setTrafficEnabled(true);
                mBaiduMap.setBaiduHeatMapEnabled(false);
                break;
            case ITEM_ID_HOT:
                mBaiduMap.setBaiduHeatMapEnabled(true);
                mBaiduMap.setTrafficEnabled(false);
                break;
            case ITEM_ID_CALL:
                thread_for_receive tfr=new thread_for_receive();
                tfr.start();
                phone_dialog();
                break;
            case ITEM_ID_PLAN:
                File file = new File("/sdcard/11plan_pic/today_plan.jpg");
////                Intent intent_img = new Intent(Intent.ACTION_VIEW);
////                Uri uri = Uri.fromFile(file);
////                intent_img .addCategory(Intent.CATEGORY_DEFAULT);
////                intent_img .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                intent_img .setDataAndType(uri, "image/*");
////                startActivity(intent_img );
                Uri uri = Uri.fromFile(file);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.setDataAndType(uri, "image/*");
                startActivity(intent);
//                openAssignFolder();
                break;
            case ITEM_ID_ALARM:
                Intent alarm_go = new Intent(Intent.ACTION_MAIN);
                PackageManager manager = getPackageManager();
                alarm_go = manager.getLaunchIntentForPackage("com.android.deskclock");
                alarm_go.addCategory(Intent.CATEGORY_LAUNCHER);
                startActivity(alarm_go);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
    private  void getregist()
    {
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT=101;
            final int REQUEST_LOCATION=1;
            String[] PERMISSIIONS_LOCATION={Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
            for (String str: PERMISSIIONS_LOCATION)
            {

                    if(this.checkSelfPermission(str)!= PackageManager.PERMISSION_GRANTED)
                    {
                        this.requestPermissions(PERMISSIIONS_LOCATION,REQUEST_CODE_CONTACT);
                    }
            }
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

    }


    /**
     * 开启Location
     */
    @Override
    protected void onStart() {
        mLocationInstance.start();
        super.onStart();

    }

    /**
     * 关闭Location
     */
    @Override
    protected void onStop() {
        mLocationInstance.stop();
        super.onStop();
    }
    private void initLocation() {
//        Toast.makeText(MainActivity.this,"开始展示地址", Toast.LENGTH_SHORT).show();
        mLocationInstance = new LocationInstance(
                this,
                new LocationInstance.MyLocationListener() {

                    @Override

                    public void onReceiveLocation(BDLocation location) {
                        super.onReceiveLocation(location);
                        //在这里拿到返回的Location的信息
                        lastLocation=location;
                        Log.d("调试信息", "展示定位");
                        Log.d("MAP",
                                location.getAddrStr()+","+"" +
                                        location.getLatitude()+","+
                                        location.getLongitude());
//                        mBaiduMap.clear();//每次点击不清除就会叠加
//                        LatLng point = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
//                        BitmapDescriptor bitmap = BitmapDescriptorFactory
//                                .fromResource(R.drawable.a);
//                        //构建MarkerOption，用于在地图上添加Marker
//
//                        OverlayOptions option = new MarkerOptions()
//                                .position(point)
//                                .icon(bitmap);
//
//                        //在地图上添加Marker，并显示
//
//                        mBaiduMap.addOverlay(option);
//
//                        //地图可以移动到中心点过去
//
//                        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(point));
//                        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(20f));
//                        Toast.makeText(MainActivity.this,"开始展示地址"+location.getAddrStr(), Toast.LENGTH_SHORT).show();
//                        Toast.makeText(MainActivity.this,"开始展示经度"+location.getLatitude(), Toast.LENGTH_SHORT).show();
//                        Toast.makeText(MainActivity.this,"开始展示纬度"+location.getLongitude(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private class mClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v == Btn_loc) {
                Log.d("调试信息", " 手动定位开启");
                mBaiduMap.clear();//每次点击不清除就会叠加
                //定义Maker坐标点(获取经纬度)
                LatLng point = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                Toast.makeText(MainActivity.this, "查询出经度为:" + lastLocation.getLatitude()+'\n'+"查询出纬度为:" + lastLocation.getLongitude()+'\n'+"查询出地址为:" + lastLocation.getAddrStr(), Toast.LENGTH_SHORT).show();
                //构建Marker图标

                BitmapDescriptor bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.a);
                //构建MarkerOption，用于在地图上添加Marker

                OverlayOptions option = new MarkerOptions()
                        .position(point)
                        .icon(bitmap);

                //在地图上添加Marker，并显示

                mBaiduMap.addOverlay(option);

                //地图可以移动到中心点过去

                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(point));
                mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(20f));
            }
            if (v==Btn_add_info)
            {
                Intent intent=new Intent(MainActivity.this,put_info_database.class);
                Bundle bundle=new Bundle();
                bundle.putString("longitude","经度:"+lastLocation.getLongitude());
                bundle.putString("latitude","纬度:"+lastLocation.getLatitude());
                bundle.putString("gpsaddr","地址:"+lastLocation.getAddrStr());
                intent.putExtras(bundle);
                startActivity(intent);

            }
            if(v==Btn_control_home)
            {
                Intent home_intent=new Intent(MainActivity.this,control_smart_home.class);
                Bundle bundle=new Bundle();
                bundle.putString("person_info","直接跳转:无数据");
                home_intent.putExtras(bundle);
                startActivity(home_intent);
            }
        }
    }
    private class mhander extends Handler{

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.arg1)
            {
                case 1:
                {
                    show_dialog_location();break;
                }
            }
        }
    }
    private class thread_for_receive extends Thread
    {
        @Override
        public void run() {
            while(true) {
                if (hope_location != "未知" && lastLocation!=null) {

                        if (lastLocation.getAddrStr().contains(hope_location)) {
//                            Log.d("调试信息", " 找到此地点");
                            vib = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
                            vib.vibrate(5000);
                            msg.arg1=1;
                            myhandler.sendMessage(msg);
                            break;
                        }
                        else{
                            msg.arg1=0;
                            myhandler.sendMessage(msg);
                        }
//                        else {
//                            Log.d("调试信息", " 没有找到此地点");
//                        }
                }
//                else{
//                    Log.d("调试信息", "地点组:"+lastLocation);
//                }
            }
        }
    }
    public void show_dialog_location()
    {
        AlertDialog.Builder emmm1 = new AlertDialog.Builder(MainActivity.this);
        emmm1.setTitle("位置已经确定:");
        emmm1.setMessage(hope_location+'\n'+ hope_location_info+'\n');
        emmm1.setPositiveButton("确定", new cancel_show_click());
        emmm1.create();
        emmm1.show();
    }
    public void phone_dialog()
    {
        AlertDialog.Builder emmm = new AlertDialog.Builder(MainActivity.this);
        location_service_goal=(RelativeLayout)getLayoutInflater().inflate(R.layout.location_service,null);
        emmm.setTitle("位置服务:").setMessage("在未来的某个地点想起来做某事\n").setView(location_service_goal);
        emmm.setPositiveButton("确定", new dialog_okClick());
        emmm.setNegativeButton("取消",new cancell_click());
        emmm.create();
        emmm.show();
    }

    private class dialog_okClick implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            location_txt=(EditText)location_service_goal.findViewById(R.id.editText_goal_location) ;
            loication_info_txt=(EditText)location_service_goal.findViewById(R.id.edit_location_info) ;
            hope_location=location_txt.getText().toString();
            hope_location_info=loication_info_txt.getText().toString();
        }
    }

    private class cancell_click implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            hope_location="未知";
            hope_location_info="未知";
            dialog.cancel();
        }
    }

    private class cancel_show_click implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    }
}

