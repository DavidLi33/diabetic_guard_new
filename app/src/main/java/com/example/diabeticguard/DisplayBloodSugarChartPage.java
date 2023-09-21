package com.example.diabeticguard;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class DisplayBloodSugarChartPage extends AppCompatActivity implements View.OnClickListener {

    private LineChart lineChart;
    private LineDataSet lineDataSet;
    private ArrayList<Entry> data;
    private List<String> datetimes;
    private List<Integer> levels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_blood_sugar_chart);


        lineChart = findViewById(R.id.lineChart);

        // Initializing the data list for chart entries
        data = new ArrayList<>();

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

        readDataFromCSV();

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
                    Toast.makeText(DisplayBloodSugarChartPage.this, levelNum, Toast.LENGTH_SHORT).show();
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
                startActivity(new Intent(DisplayBloodSugarChartPage.this, MainActivity.class));
                break;
            case R.id.displayChartButton:
                //TODO
                startActivity(new Intent(DisplayBloodSugarChartPage.this, MainActivity.class));

                break;
        }
    }

    private void readDataFromCSV() {
        Resources res = getResources();
        // Make Sure You Specify Your CSV File Name in My Case the CSV File Name is gfg
        InputStream inputStream = res.openRawResource(R.raw.blood_sugar_track_1);
        //InputStream inputStream = res.openRawResource(R.raw.salesperyear);

        try {
            CSVReader reader = new CSVReader(new InputStreamReader(inputStream));
            String[] nextLine;
            // Flag to skip the first line of the CSV file that is header
            String mostRecent;
            boolean isFirstLine = true;
            while ((nextLine = reader.readNext()) != null) {
                if (isFirstLine) {
                    // Skiping the first line header of the CSV file
                    isFirstLine = false;
                    continue;
                }
                if (nextLine.length >= 3) {
                    // Extracting the year from the CSV data
                    String date = nextLine[0].trim();
                    String time = nextLine[1].trim();
                    int level = Integer.parseInt( nextLine[2].trim() );
                    // Extract and parsing the sales value parsing because the sales value
                    // is written with comma like that (12,500)
                   // float sales = Float.parseFloat(nextLine[1].replace(",", "").trim());

                    addEntryToChart(date, time, level);

                    TextView banner2Text2 = findViewById(R.id.banner2Text2);

                    banner2Text2.setText(String.valueOf(level));
                }
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
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
