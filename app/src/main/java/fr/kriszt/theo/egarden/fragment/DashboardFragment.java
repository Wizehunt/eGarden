package fr.kriszt.theo.egarden.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.kriszt.theo.egarden.R;
import fr.kriszt.theo.egarden.utils.Connexion;

import static java.lang.Float.parseFloat;

public class DashboardFragment extends Fragment {
    private static final String TAG = "DashboardFragment";

    private LineChart lineChart;
    @BindView(R.id.weather_alert) TextView _weatherAlert;
    @BindView(R.id.temperature_alert) TextView _temperatureAlert;
    @BindView(R.id.hygrometry_alert) TextView _hygrometryAlert;
    @BindView(R.id.temperature_value) TextView _temperatureValue;
    @BindView(R.id.hydrometry_value) TextView _hygrometryValue;
    @BindView(R.id.weather_value) TextView _weatherValue;
    @BindView(R.id.weather_icon) ImageView _weatherIcon;


    public DashboardFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void fetchDHTValues() {
        Connexion.O().sendGetRequest("/DHT", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                ArrayList<Entry> solEntries = new ArrayList<>();
                ArrayList<Entry> humEntries = new ArrayList<>();
                ArrayList<Entry> tempEntries = new ArrayList<>();
                final ArrayList<String> dateLabels = new ArrayList<>();
                try {
                    JSONArray jsonArray = new JSONArray(response);

                    if(jsonArray.length() == 0){
                        getView().findViewById(R.id.dashboard_dht_progressbar).setVisibility(View.GONE);
                        return;
                    }

                    for (int i = 0; i < jsonArray.length(); i++){ // sol; hum, temp, date
                        float sol = 0;
                        float temp = 0;
                        float hum = 0;

                        try{
                            sol = parseFloat(String.valueOf(jsonArray.getJSONObject(i).get("sol")));
                        }
                        catch(NumberFormatException ignored){}
                        try{
                            hum = parseFloat(String.valueOf(jsonArray.getJSONObject(i).get("hum")));
                        }
                        catch(NumberFormatException ignored){}
                        try{
                            temp = parseFloat(String.valueOf(jsonArray.getJSONObject(i).get("temp")));
                        }
                        catch(NumberFormatException ignored){}

                            String date = String.valueOf(jsonArray.getJSONObject(i).get("date"));

                            solEntries.add(new Entry(i, sol));
                            humEntries.add(new Entry(i, hum));
                            tempEntries.add(new Entry(i, temp));
                            dateLabels.add(date);

                    }

                } catch (JSONException e) {
                    Log.e(TAG, "DHT JSONException: ", e);

                }

                LineDataSet humDataSet;

                try {
                    humDataSet = new LineDataSet(humEntries, getString(R.string.humidity) + " %HR");
                    humDataSet.setDrawCircles(false);
                    humDataSet.setColor(Color.BLUE);
                    humDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                }catch (IllegalStateException e){
                    Log.e(TAG, "onResponse: perte de contexte", e);
                    return;
                }
                //Humidity


                //Solar
                LineDataSet solDataSet = new LineDataSet(solEntries, getString(R.string.sunlight));
                solDataSet.setDrawValues(false);
                solDataSet.setDrawCircles(false);
                
                solDataSet.setColor(Color.rgb(255, 102, 0));
                solDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

                //Temperature
                LineDataSet tempDataSet = new LineDataSet(tempEntries, getString(R.string.temperature) + " °C");
                tempDataSet.setDrawCircles(false);
                tempDataSet.setColor(Color.GREEN);
                tempDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

                lineChart.animateX(1000);
                YAxis leftAxis = lineChart.getAxisLeft();
                YAxis rightAxis = lineChart.getAxisRight();
                XAxis xAxis = lineChart.getXAxis();
                xAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        return dateLabels.get((int) value);
                    }
                });

                tempDataSet.setAxisDependency(leftAxis.getAxisDependency());
                solDataSet.setAxisDependency(rightAxis.getAxisDependency());
                humDataSet.setAxisDependency(rightAxis.getAxisDependency());


                LineData lineData = new LineData(humDataSet, tempDataSet, solDataSet);

                //Description
                Description description = new Description();
                description.setText("DHT values");
                lineChart.setDescription(description);
                lineChart.invalidate();
                lineChart.setData(lineData);



                getView().findViewById(R.id.dashboard_dht_progressbar).setVisibility(View.GONE);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                try {
                    Log.e(TAG, "get DHT: \n" + Connexion.O().decodeError(error));
                    Toast.makeText(getContext(), R.string.errorGetDHT, Toast.LENGTH_SHORT).show();
                    getView().findViewById(R.id.dashboard_dht_progressbar).setVisibility(View.GONE);
                }catch (NullPointerException e){
                    Log.e(TAG, "onErrorResponse: ", e);
                }

            }
        });
    }

    private void fetchWeatherAPI(){

        Connexion.O().sendGetRequest("/weather", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    JSONObject weather = (JSONObject) json.get("weather");
                    JSONObject temperature = (JSONObject) json.get("temperature");
                    JSONObject humidity = (JSONObject) json.get("humidity");

                    String humidityValue = humidity.getString("value");
                    String tempMin = temperature.getString("min");
                    String tempMax = temperature.getString("max");
                    String tempAlert = temperature.getString("alert");
                    String weatherMsg = weather.getString("msg");
                    String weatherAlert = weather.getString("alert");
                    String weatherIcon = weather.getString("icon");

                    _weatherValue.setText( weatherMsg );
                    _weatherAlert.setText( weatherAlert );


                    int iconResource = getResources().getIdentifier(
                            "ic_weather_" + weatherIcon, "drawable", getContext().getPackageName() );
                    _weatherIcon.setImageResource(iconResource);


                    _temperatureValue.setText(MessageFormat.format("{0} à {1} (°C)", tempMin, tempMax));
                    _temperatureAlert.setText( tempAlert );

                    _hygrometryValue.setText(MessageFormat.format("{0} (% HR)", humidityValue));



                } catch (JSONException | IllegalStateException e) {
                    Log.e(TAG, "onResponse: ", e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "" + Connexion.O().decodeError(error));
            }
        });
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lineChart = view.findViewById(R.id.dht_chart);
        lineChart.setNoDataText("");
        ButterKnife.bind(this, view);
        fetchDHTValues();
        fetchWeatherAPI();
        fetchPlantsStatuses();


    }

    private void fetchPlantsStatuses() {
        Connexion.O().sendGetRequest("/plants", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                int plantsWarnings = 0;
                try {
                    JSONArray jsonArray = new JSONArray(response);

                    for (int i = 0; i < jsonArray.length(); i++){
                        JSONObject plant = (JSONObject) jsonArray.get(i);
                        int threshold = plant.getInt("threshold");
                        int moisture = plant.getInt("hygrometry");
                        if (moisture < threshold){
                            plantsWarnings++;
                        }

                    }
                } catch (JSONException e) {
                    Log.e(TAG, "fetchPlantsStatuses: ", e);
                }

                if (plantsWarnings > 0){
                    _hygrometryAlert.setText(R.string.plantsNeedWater);
                }
            }
        }, null);
    }


    @Override
    public void onStop() {
        super.onStop();
        Connexion.cancellAll();
    }
}
