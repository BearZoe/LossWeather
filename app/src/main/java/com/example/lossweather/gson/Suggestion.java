package com.example.lossweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/7/12.
 */

public class Suggestion {

    @SerializedName("comf")
    public Confort comfort;

    @SerializedName("cw")

    public Carwash carwash;


    public Sport sport;


    public class Confort {
        @SerializedName("txt")

        public String info;


    }


    public class Carwash {
        @SerializedName("txt")

        public String info;


    }

    public class Sport {
        @SerializedName("txt")

        public String info;


    }


}
