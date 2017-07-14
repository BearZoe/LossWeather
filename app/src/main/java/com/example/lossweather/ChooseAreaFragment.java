package com.example.lossweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lossweather.db.City;
import com.example.lossweather.db.County;
import com.example.lossweather.db.Province;
import com.example.lossweather.util.HttpUtil;
import com.example.lossweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

/**
 * Created by Administrator on 2017/7/10.
 */

public class ChooseAreaFragment extends Fragment {

    private static final String TAG = "ChooseAreaFragment";

    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;

    private TextView titleText;

    private Button backButton;

    private ListView listview;

    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();

    //省列表
    private List<Province> provinceList;

    //市列表

    private List<City> cityList;

    //县列表

    private List<County> countyList;

    //选中的省份

    private Province selectedProvince;

    //选中的城市

    private City selectedCity;

    //选中的级别

    private int currentLevel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.choose_area, container, false);

        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listview = (ListView) view.findViewById(R.id.list_view);

        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, dataList);

        listview.setAdapter(adapter);

        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (currentLevel == LEVEL_PROVINCE) {

                    selectedProvince = provinceList.get(position);

                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {

                    selectedCity = cityList.get(position);

                    queryCounties();


                } else if (currentLevel == LEVEL_COUNTY) {

                    String weatherId = countyList.get(position).getWeatherId();


                    if (getActivity() instanceof MainActivity) {

                        Intent it = new Intent(getActivity(), WeatherActivity.class);
                        it.putExtra("weather_id", weatherId);
                        startActivity(it);
                        getActivity().finish();



                    } else if (getActivity() instanceof WeatherActivity) {

                        Log.d(TAG, "onItemClick: "+ getActivity());
                        WeatherActivity activity = (WeatherActivity) getActivity();

                        activity.drawerLayout.closeDrawers();

                        activity.swipeRefresh.setRefreshing(true);

                        activity.requestWeather(weatherId);

                    }


                }


            }


        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY) {

                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });

        queryProvinces();


    }

    /**
     * 查询全国所有省，优先从数据库查询，再从服务器查询
     */

    private void queryProvinces() {

        titleText.setText("中国");

        backButton.setVisibility(View.GONE);

        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {

            dataList.clear();
            for (Province province : provinceList
                    ) {

                dataList.add(province.getProvinceName());

            }
            adapter.notifyDataSetChanged();
            listview.setSelection(0);
            currentLevel = LEVEL_PROVINCE;

        } else {


            String address = "http://guolin.tech/api/china";
            queryFromSever(address, "province");

        }


    }


    /**
     * 查询选中省内所有的市，优先从数据库查询，再到服务器查询
     */

    private void queryCounties() {

        titleText.setText(selectedCity.getCityName());

        backButton.setVisibility(View.VISIBLE);

        countyList = DataSupport.where("cityid = ?", String.valueOf
                (selectedCity.getId())).find(County.class);

        if (countyList.size() > 0) {

            dataList.clear();

            for (County county : countyList) {

                dataList.add(county.getCountyName());

            }

            adapter.notifyDataSetChanged();

            listview.setSelection(0);

            currentLevel = LEVEL_COUNTY;


        } else {

            int provinceCode = selectedProvince.getProvinceCode();

            int cityCode = selectedCity.getCityCode();

            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;

            queryFromSever(address, "county");
        }


    }

    /**
     * 查询选中市内所有的市。优先从数据库查询，再从服务器查询
     */

    private void queryCities() {

        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);

        cityList = DataSupport.where("provinceid = ?", String.valueOf
                (selectedProvince.getId())).find(City.class);

        if (cityList.size() > 0) {

            dataList.clear();

            for (City city : cityList) {

                dataList.add(city.getCityName());

            }

            adapter.notifyDataSetChanged();

            listview.setSelection(0);

            currentLevel = LEVEL_CITY;

        } else {

            int provinceCode = selectedProvince.getProvinceCode();

            String address = "http://guolin.tech/api/china/" + provinceCode;

            queryFromSever(address, "city");

        }


    }


    /**
     * 根据插入的地址和类型从服务器查询省市县数据
     *
     * @param address 服务器地址
     * @param type    省、市、县类型
     */

    private void queryFromSever(String address, final String type) {

        showProgressDialog();

        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                //通过runOnUiThread()方法回到主线程处理逻辑

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        closeProgressDialog();

                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {


                String responseText = response.body().string();

                boolean result = false;

                if ("province".equals(type)) {


                    result = Utility.handleProvinceResponse(responseText);

                } else if ("city".equals(type)) {


                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());


                } else if ("county".equals(type)) {


                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());

                }

                //服务器获取的数据处理成功

                if (result) {

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //关闭进度对话框
                            closeProgressDialog();

                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {

                                queryCities();
                            } else if ("county".equals(type)) {

                                queryCounties();
                            }
                        }
                    });


                }


            }
        });


    }

    /**
     * 关闭进度对话框
     */

    private void closeProgressDialog() {

        if (progressDialog != null) {

            progressDialog.dismiss();
        }


    }


    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {

        if (progressDialog == null) {

            progressDialog = new ProgressDialog(getActivity());

            progressDialog.setMessage("正在加载");

            progressDialog.setCancelable(false);

        }


        progressDialog.show();
    }
}
