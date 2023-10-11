package com.example.diabeticguard;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.TextView;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.diabeticguard.db.DBController;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DisplayBloodSugarChartPage extends AppCompatActivity implements View.OnClickListener {

    DBController controller;
    FirebaseUser currentUser;
    private LineChart lineChart;
    private LineDataSet lineDataSet;
    private ArrayList<Entry> data;
    private List<String> datetimes;
    private List<Integer> levels;
    private ImageView bannerLogo;

    private String[] status =  {"Normal <100", "Prediabetic 100-125", "Diabetic >126"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_blood_sugar_chart);

        controller = new DBController(this);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        lineChart = findViewById(R.id.lineChart);

        // Initializing the data list for chart entries
        data = new ArrayList<>();

        bannerLogo = findViewById(R.id.bannerLogo);
        bannerLogo.setOnClickListener(this);

        // Creating a LineDataSet with the data and a label for the legend
        lineDataSet = new LineDataSet(data, "Blood Sugar Level Tracking");
        lineDataSet.setColors(ColorTemplate.MATERIAL_COLORS);


        lineDataSet.setValueTextColor(Color.BLACK);
        lineDataSet.setValueTextSize(16f);

        // Creating a LineData object with the LineDataSet
        LineData lineData = new LineData(lineDataSet);

        // Seting up the LineData object to the LineChart
        lineChart.setData(lineData);

        // Seting a description for the LineChart
        Description description = new Description();
        description.setText("Line Chart");
        lineChart.setDescription(description);

        // Configuring the X-axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);


        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setGranularity(1f);

        // Disabling the right Y-axis
        lineChart.getAxisRight().setEnabled(false);

        // Initializing lists to store years and product names
        datetimes = new ArrayList<>();
        levels = new ArrayList<>();

        readDataFromDB();

        setupChart();

        // Seting up OnChartValueSelectedListener to handle selection events
        // When Someone Clicks On The Line Chart It Will Show The Product Name
        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                // Get the index of the selected entry
                int index = (int) e.getX();
                if (index >= 0 && index < levels.size()) {

                    // Geting corresponding product name from the products list
                    Integer levelNum = levels.get(index);

                    // Displaying toast message with the selected product name
                   // Toast.makeText(DisplayBloodSugarChartPage.this, levelNum, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected() {
                // Let it be empty only
            }
        });

        //Making Banner2 taller
        RelativeLayout bannerContainer2 = findViewById(R.id.bannerContainer2);
        int currentHeight = bannerContainer2.getLayoutParams().height;
        int newHeight = currentHeight + (int) getResources().getDimension(R.dimen.dp_100);

        bannerContainer2.getLayoutParams().height = newHeight;

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bannerLogo:
                startActivity(new Intent(DisplayBloodSugarChartPage.this, HomePage.class));
                break;
        }
    }

    private void readDataFromDB() {
        ArrayList<HashMap<String, String>> resultMaps = new ArrayList<HashMap<String, String>>();
        int latest_level = 100;
        try {
            resultMaps = controller.getAllTracks(currentUser.getUid());

            for( HashMap<String, String> myMap : resultMaps ) {
                String date = myMap.get("date");
                String time = myMap.get("time");
                int level = Integer.parseInt(  myMap.get("level") );

                addEntryToChart(date, time, level);
                latest_level = level;
            }

            //set dynamic text and color value based on latest_level value
            setNoticeBannerText( latest_level);

        } catch (Exception e) {
            Log.e("Error", e.getMessage() );
            e.printStackTrace();
        }
    }

    private void setNoticeBannerText( int latest_level ) {
        //set dynamic text and color value based on latest_level value
        TextView banner2Text2 = findViewById(R.id.banner2Text2);
        banner2Text2.setText(String.valueOf(latest_level) + " mmol/L");
        TextView banner2Text3 = findViewById(R.id.banner2Text3);
        if( latest_level <= 100) {
            banner2Text3.setText(status[0]);
            banner2Text3.setTextColor(getResources().getColor(R.color.teal_200, null));
        }else if( latest_level > 100 && latest_level < 126) {
            banner2Text3.setText(status[1]);
            banner2Text3.setTextColor(getResources().getColor(R.color.yellow, null));
        }else{
            banner2Text3.setText(status[2]);
            banner2Text3.setTextColor(getResources().getColor(R.color.red_dark, null));
        }
    }


    private void addEntryToChart(String date, String time, int level ) {
        data.add(new Entry(data.size(), level));
        // Storing year and product names for each data entry
        //TODO datetimes.add(date + time);
        datetimes.add(date );
        levels.add(level);
    }


    private void setupChart() {
        lineDataSet.notifyDataSetChanged();
        lineChart.getData().notifyDataChanged();
        lineChart.notifyDataSetChanged();

        // Displaying maximum 6 visible entries on the x-axis
        lineChart.setVisibleXRangeMaximum(30);
        // Moving the chart view to the last 7 entries
        lineChart.moveViewToX(data.size() - 30);

        // Seting the description text for the graph that is visible at the right side bottom
        lineChart.getDescription().setText("Sales Info");

        lineChart.getXAxis().setValueFormatter(new TimeValueFormatter(datetimes));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // Custom ValueFormatter class for X-axis labels
    public static class TimeValueFormatter extends ValueFormatter {

        private final List<String> datetimes;

        public TimeValueFormatter(List<String> datetimes) {
            this.datetimes = datetimes;
        }

        @Override
        public String getFormattedValue(float value) {
            // Type Casting float value to integer index
            int index = (int) value;
            if (index >= 0 && index < datetimes.size()) {
                // Retrieving the year string from
                // the years list based on the index
                return datetimes.get(index);
            }
            // Returning empty string if
            // the index is out of bounds
            return "";
        }
    }
}
