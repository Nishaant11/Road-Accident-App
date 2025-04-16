package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GraphDisplayActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_display);

        // Initialize Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Retrieve extras from intent
        String selectedCity = getIntent().getStringExtra("city");
        String selectedParameter = getIntent().getStringExtra("parameter");

        // Display the selected city and parameter
        TextView cityTextView = findViewById(R.id.cityTextView);
        TextView parameterTextView = findViewById(R.id.parameterTextView);

        cityTextView.setText("Selected City: " + selectedCity);
        parameterTextView.setText("Selected Parameter: " + selectedParameter);

        // Construct the path to the selected city node
        String cityNodePath = selectedCity;

        // Retrieve data from Firebase
        mDatabase.child(cityNodePath).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Entry> entries = new ArrayList<>();
                int year = 2008; // starting year
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Iterate through each sub-node under the selected city node
                    Long parameterDataLong = snapshot.child(selectedParameter).getValue(Long.class);
                    float parameterData = parameterDataLong != null ? parameterDataLong.floatValue() : 0;
                    // Add data entry for the year
                    entries.add(new Entry(year, parameterData));
                    year++;
                }
                // Create a LineDataSet
                LineDataSet dataSet = new LineDataSet(entries, "Parameter Data");
                dataSet.setDrawValues(false); // Disable drawing values on data points

                // Create a LineData object from the LineDataSet
                LineData lineData = new LineData(dataSet);

                // Get a reference to the LineChart
                LineChart lineChart = findViewById(R.id.lineChart);

                // Customize the appearance of the chart
                lineChart.getDescription().setEnabled(false); // Disable chart description
                lineChart.setData(lineData);

                // Customize X-axis
                XAxis xAxis = lineChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getAxisLabel(float value, AxisBase axis) {
                        // Convert float value to year
                        return String.valueOf((int) value);
                    }
                });

                // Customize Y-axis
                YAxis yAxisRight = lineChart.getAxisRight();
                yAxisRight.setEnabled(false); // Disable right Y-axis

                // Enable touch gestures
                lineChart.setTouchEnabled(true);

// Set listener to display data when a point is clicked
                lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                    @Override
                    public void onValueSelected(Entry e, Highlight h) {
                        // Display data of the selected point
                        Toast.makeText(GraphDisplayActivity.this, selectedParameter + " : " + e.getY(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNothingSelected() {
                        // Handle case when nothing is selected
                    }
                });

                dataSet.setDrawFilled(true); // Enable drawing filled area
                dataSet.setFillDrawable(getResources().getDrawable(R.drawable.gradient_background)); // Set the Drawable for filling color

                lineChart.invalidate();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
            }
        });
    }
}
