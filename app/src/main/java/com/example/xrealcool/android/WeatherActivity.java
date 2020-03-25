package com.example.xrealcool.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xrealcool.android.db.Forecast;
import com.example.xrealcool.android.db.Weather;
import com.example.xrealcool.android.util.HttpUtil;
import com.example.xrealcool.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;
    private TextView titlecity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView confortText;
    private TextView carWashText;
    private TextView sportText;
    private Button change_city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        getViews();
        //getDefaultSharedPreferences被弃用-----正在寻找替代品
        SharedPreferences prefs = getSharedPreferences("weather_info",MODE_PRIVATE);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            //有缓存数据，直接解析
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        } else {
            //没有缓存就根据id去查询Weather实体
            String weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
    }

    /**
     * 根据天气id查询城市天气
     *
     * @param weatherId 天气id
     */
    private void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "天气获取数据失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = getSharedPreferences("weather_info",MODE_PRIVATE).edit();
                            editor.putString("weather", responseText);
                            editor.commit();

                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "天气 ok 获取失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }

    /**
     * 处理并展示Weather实体类的天气信息
     *
     * @param weather 传过来的天气实体类
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titlecity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        weatherInfoText.setText(weatherInfo);
        degreeText.setText(degree);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastsList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView info = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            info.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度: " + weather.suggestion.comfort.info;
        String carWash = "洗车指数: " + weather.suggestion.carWssh.info;
        String sport = "运动建议: " + weather.suggestion.sport.info;
        confortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 获取到相关的控件
     */
    private void getViews() {
        //整个天气界面的布局
        weatherLayout = findViewById(R.id.weather_layout);
        //显示城市
        titlecity = findViewById(R.id.title_city);
        //更新时间
        titleUpdateTime = findViewById(R.id.title_update_time);
        //温度
        degreeText = findViewById(R.id.degree_text);
        //天气信息
        weatherInfoText = findViewById(R.id.weather_info_text);
        //天气预报的布局
        forecastLayout = findViewById(R.id.forecast_layout);
        //aqi指数
        aqiText = findViewById(R.id.aqi_text);
        //PM2.5指数
        pm25Text = findViewById(R.id.pm25_text);
        //舒适度
        confortText = findViewById(R.id.confort_text);
        //洗车
        carWashText = findViewById(R.id.car_wash_text);
        //运动指数
        sportText = findViewById(R.id.sport_text);
        //更换城市
        change_city = findViewById(R.id.change_city);

        change_city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("weather_info",MODE_PRIVATE).edit();
                editor.remove("weather");
                //editor.clear();
                editor.commit();
                Intent intent = new Intent(WeatherActivity.this,ChangeCityActivity.class);
                startActivity(intent);

            }
        });

    }
}
