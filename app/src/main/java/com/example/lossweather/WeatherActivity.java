package com.example.lossweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.lossweather.gson.Forecast;
import com.example.lossweather.gson.Weather;
import com.example.lossweather.util.HttpUtil;
import com.example.lossweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/7/12.
 */

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = "WeatherActivity";

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeInfoText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private ImageView bingPicImg;

    public SwipeRefreshLayout swipeRefresh;

    public DrawerLayout drawerLayout;

    private Button navButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {

            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.weather_main);

        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);

        titleCity = (TextView) findViewById(R.id.title_city);

        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);

        degreeInfoText = (TextView) findViewById(R.id.degree_text);

        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);

        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);

        aqiText = (TextView) findViewById(R.id.aqi_text);

        pm25Text = (TextView) findViewById(R.id.pm25_text);

        comfortText = (TextView) findViewById(R.id.comfor_text);

        carWashText = (TextView) findViewById(R.id.car_wash_text);

        sportText = (TextView) findViewById(R.id.sport_text);

        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);


        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);

        //设置下拉刷新按钮背景颜色
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        navButton = (Button) findViewById(R.id.nav_button);

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开滑动菜单
                drawerLayout.openDrawer(GravityCompat.START);

            }
        });


        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        final String weatherId;

        String weatherString = pref.getString("weather", null);

        String bing_pic = pref.getString("bing_pic", null);

        if (bing_pic != null) {

            Glide.with(this).load(bing_pic).into(bingPicImg);

        } else {
            loadBingPic();
        }


        if (weatherString != null) {
            //有缓存时直接解析数据

            Weather weather = Utility.handleWeatherResponce(weatherString);

            weatherId = weather.basic.weatherId;

            //解析并展示获取到的weather对象

            showWeatherInfo(weather);


        } else {
            //无缓存时从服务器去查询天气
            weatherId = getIntent().getStringExtra("weather_id");

            weatherLayout.setVisibility(View.INVISIBLE);

            requestWeather(weatherId);


        }

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {

                requestWeather(weatherId);

            }
        });


    }

    /**
     * 加载必应每日一图
     */

    public void loadBingPic() {

        String requestBingPic = "http://guolin.tech/api/bing_pic";

        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();


            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String bingPic = response.body().string();

                SharedPreferences.Editor edit = PreferenceManager.
                        getDefaultSharedPreferences(WeatherActivity.this).edit();

                edit.putString("bing_pic", bingPic);

                edit.apply();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);

                    }
                });


            }
        });


    }

    /**
     * 根据天气id请求城市天气信息
     *
     * @param weather_id
     */


    public void requestWeather(final String weather_id) {

        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weather_id
                + "&key=5908c2ca520a4682b1c15c28c274e9b4";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                e.printStackTrace();


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: onfailure");

                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();

                        swipeRefresh.setRefreshing(false);
                    }
                });





            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String responceText = response.body().string();

                Log.d(TAG, "onResponse: responseText " + responceText);

                final Weather weather = Utility.handleWeatherResponce(responceText);

                Log.d(TAG, "onResponse: handleweatherResponce");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();

                            editor.putString("weather", responceText);
                            editor.apply();

                            showWeatherInfo(weather);

                            Log.d(TAG, "run: showweatherInfo" + weather);


                        } else {

                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();


                        }

                        //false代表表示刷新时间结束，并隐藏刷新进度条
                        swipeRefresh.setRefreshing(false);


                    }
                });

            }
        });

        loadBingPic();


    }

    /**
     * 根据传入的天气Id解析并展示数据
     *
     * @param weather
     */

    public void showWeatherInfo(Weather weather) {

        //Log.d(TAG, "showWeatherInfo: " + weather);

        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];

        String degree = weather.now.tempratrue + "℃";

        String weatherInfo = weather.now.more.info;

        titleCity.setText(cityName);

        titleUpdateTime.setText(updateTime);


        degreeInfoText.setText(degree);
        weatherInfoText.setText(weatherInfo);

        forecastLayout.removeAllViews();

        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);

            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);

            dateText.setText(forecast.date);

            infoText.setText(forecast.more.info);

            Log.d(TAG, "show-WeatherInfo: " + forecast.more.info);

            maxText.setText(forecast.temprature.max);

            Log.d(TAG, "showWeatherInfo: " + forecast.temprature.max);

            minText.setText(forecast.temprature.min);


            forecastLayout.addView(view);

        }

        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);

            pm25Text.setText(weather.aqi.city.pm25);
        }

        String comfort = "舒适度: " + weather.suggestion.comfort.info;

        String carWash = "洗车指数：" + weather.suggestion.carwash.info;

        String sport = "运动建议：" + weather.suggestion.sport.info;

        comfortText.setText(comfort);

        carWashText.setText(carWash);

        sportText.setText(sport);

        weatherLayout.setVisibility(View.VISIBLE);


    }
}
