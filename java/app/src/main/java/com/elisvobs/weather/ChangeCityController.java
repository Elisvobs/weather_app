package com.elisvobs.weather;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class ChangeCityController extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.city_changer);

        final TextInputEditText queryCity = findViewById(R.id.cityQuery);

        queryCity.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String newCity = queryCity.getEditableText().toString();
                Intent intent = new Intent(ChangeCityController.this, WeatherController.class);
                intent.putExtra("City", newCity);
                ChangeCityController.this.setResult(-1, intent);
                ChangeCityController.this.finish();
                return true;
            }
        });
    }

    public void backClick(View view) {
        // Go back and destroy the ChangeCityController
        finish();
    }
}