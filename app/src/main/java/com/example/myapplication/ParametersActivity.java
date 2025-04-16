package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ParametersActivity extends AppCompatActivity {
    private Spinner parameterSpinner;
    private Button submitButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameters);

        parameterSpinner = findViewById(R.id.parameterSpinner);
        submitButton = findViewById(R.id.submitButton);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.parameter_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        parameterSpinner.setAdapter(adapter);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedParameter = parameterSpinner.getSelectedItem().toString();
                if (!selectedParameter.contains("Select a Parameter")) {
                    // Start GraphDisplay activity and pass the selected city and parameter
                    Intent intent = new Intent(ParametersActivity.this, GraphDisplayActivity.class);
                    intent.putExtra("city", getIntent().getStringExtra("city"));
                    intent.putExtra("parameter", selectedParameter);
                    startActivity(intent);
                } else {
                    Toast.makeText(ParametersActivity.this, "Select a Parameter", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
