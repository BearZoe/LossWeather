package com.example.lossweather.util;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.example.lossweather.db.City;
import com.example.lossweather.db.County;
import com.example.lossweather.db.Province;
import com.example.lossweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/7/10.
 */

public class Utility {
    /**
     * 解析和处理服务器返回的省级数据
     */

    public static boolean handleProvinceResponse(String responce) {

        if (!TextUtils.isEmpty(responce)) {

            try {

                JSONArray allProvinces = new JSONArray(responce);
                for (int i = 0; i < allProvinces.length(); i++) {

                    JSONObject provinceObject = allProvinces.getJSONObject(i);

                    Province province = new Province();

                    province.setProvinceName(provinceObject.getString("name"));

                    province.setProvinceCode(provinceObject.getInt("id"));

                    province.save();


                }
                return true;


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return false;
    }


    /**
     * 解析和处理服务器返回的市级数据
     */

    public static boolean handleCityResponse(String responce, int provinceId) {

        if (!TextUtils.isEmpty(responce)) {

            try {
                JSONArray allCities = new JSONArray(responce);

                for (int i = 0; i < allCities.length(); i++) {


                    JSONObject cityObject = allCities.getJSONObject(i);

                    City city = new City();

                    city.setCityCode(cityObject.getInt("id"));

                    city.setCityName(cityObject.getString("name"));

                    city.setProvinceId(provinceId);
                    city.save();


                }
                return true;


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return false;


    }


    /**
     *
     * 解析和处理服务器返回的县级数据
     *
     *
     */

    public static boolean handleCountyResponse(String response, int cityId) {

        if (!TextUtils.isEmpty(response)) {

            try {

                JSONArray allCounties = new JSONArray(response);

                for (int i = 0; i < allCounties.length(); i++) {

                    JSONObject countyObject = allCounties.getJSONObject(i);

                    County county = new County();

                    county.setCountyName(countyObject.getString("name"));

                    county.setCityId(cityId);

                    county.setWeatherId(countyObject.getString("weather_id"));

                    county.save();


                }

                return true;


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        return false;

    }


    /**
     * 将返回的json数据解析成weather实体类
     */

        public static Weather handleWeatherResponce(String responce) {

        try {

            JSONObject jsonObject = new JSONObject(responce);


            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");

            String weatherContent = jsonArray.getJSONObject(0).toString();

            return new Gson().fromJson(weatherContent, Weather.class);


        } catch (JSONException e) {
            e.printStackTrace();
        }


        return null;
    }


}
