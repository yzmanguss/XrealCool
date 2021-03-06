package com.example.xrealcool.android;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.example.xrealcool.android.db.Weather;

/**
 * 获取全区天气情况
 *  创建数据库和表
 *      1.创建省，市，县的类  ---->   2.创建assets包，创建litepal.xml   -----> 3.在AndroidManifest里面配置android:name="org.litepal.LitePalApplication"
 *
 */

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences pref = getSharedPreferences("weather_info",MODE_PRIVATE);
        if (pref.getString("weather",null) !=  null){
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
