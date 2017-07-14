package com.example.lossweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;

import com.example.lossweather.gson.Weather;
import com.example.lossweather.util.HttpUtil;
import com.example.lossweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        updateWeather();

        updateBingPic();

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        int anHour = 8 * 60 * 60 * 1000;//8小时的毫秒数

        long triggerAttime = SystemClock.elapsedRealtime() + anHour;

        Intent it = new Intent(this, AutoUpdateService.class);

        PendingIntent pi = PendingIntent.getService(this, 0, it, 0);

        manager.cancel(pi);

        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAttime, pi);


        return super.onStartCommand(intent, flags, startId);


    }

    /**
     * 更新必应每日一图
     */

    private void updateBingPic() {

        String requestBingPic = "http://guolin.tech/api/bing_pic";

        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String bingpic = response.body().string();

                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(AutoUpdateService.this).edit();

                editor.putString("bing_pic", bingpic);

                editor.apply();

            }
        });


    }

    /**
     * 更新天气信息
     */

    private void updateWeather() {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        String weatherString = pref.getString("weather", null);

        if (weatherString != null) {

            //有缓存直接解析天气数据
            Weather weather = Utility.handleWeatherResponce(weatherString);

            String weatherId = weather.basic.weatherId;

            String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=5908c2ca520a4682b1c15c28c274e9b4";

            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String responceText = response.body().string();

                    Weather weather = Utility.handleWeatherResponce(responceText);

                    if (weather != null && "ok".equals(weather.status)) {


                        SharedPreferences.Editor edit = PreferenceManager.
                                getDefaultSharedPreferences(AutoUpdateService.this).edit();

                        edit.putString("weather", responceText);
                        edit.apply();

                    }

                }
            });


        }

    }
}
