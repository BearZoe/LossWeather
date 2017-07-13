package com.example.lossweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/7/12.
 */

public class Now {

    @SerializedName("tmp")

    public String tempratrue;

    @SerializedName("cond")

    public More more;

    public class  More{

        @SerializedName("txt")
        public String info;
    }

}
