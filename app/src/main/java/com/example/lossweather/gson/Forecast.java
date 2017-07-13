package com.example.lossweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/7/12.
 */

public class Forecast {

    public String date;

    @SerializedName("tmp")

    public Temprature temprature;

    @SerializedName("cond")

    public More more;


    public class Temprature {


        public String max;

        public String min;


    }

    public class More {

        @SerializedName("txt_d")
        public String info;


    }


}
