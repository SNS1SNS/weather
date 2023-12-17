package com.example.weather.Activitis;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weather.Adapters.FutureAdapters;
import com.example.weather.Domains.FutureDomain;
import com.example.weather.Domains.Hourly;
import com.example.weather.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

public class FutureActivity extends AppCompatActivity {
    private RecyclerView.Adapter adapterTommorow;
    public RecyclerView recyclerView;
    private ImageView imageView;
    private String defaultCity = "Астана";

    private TextView TM, DS, rain, humidity, wind, Gradus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_future);
        imageView = findViewById(R.id.imageView6);
        rain = findViewById(R.id.textView5);
        humidity = findViewById(R.id.textView9);
        wind = findViewById(R.id.textView7);
        TM = findViewById(R.id.textView3);
        Gradus = findViewById(R.id.textView13);
        DS = findViewById(R.id.textView14);
        setVaribale();
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("CITY_KEY")) {
            String cityKey = intent.getStringExtra("CITY_KEY");

            if (MainActivity.cityET.getText().toString().trim().equals("")) {
                Toast.makeText(FutureActivity.this, "Напишите город", Toast.LENGTH_SHORT).show();
            } else {
                String city = MainActivity.cityET.getText().toString();
                updateWeatherData(city);
            }
        }
    }

    private void updateWeatherData(String city) {
        String key = "Your key";
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + key + "&units=metric&lang=ru";
        new GetURLData().execute(url);
    }

    private class GetURLData extends AsyncTask<String, String, String> {



        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null)
                    buffer.append(line).append("\n");

                return buffer.toString();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (connection != null)
                    connection.disconnect();
                if (reader != null)
                    try {
                        reader.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
            }

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                // UI updates here
                JSONObject jsonObject = new JSONObject(result);
                Gradus.setText(String.valueOf(jsonObject.getJSONObject("main").getDouble("temp")) + "°");
                humidity.setText(String.valueOf(jsonObject.getJSONObject("main").getInt("humidity") + "%"));
                double rainValue = 0.0;
                if (jsonObject.has("rain") && !jsonObject.isNull("rain")) {
                    rainValue = Double.parseDouble(String.valueOf(jsonObject.getJSONObject("main").getDouble("humidity") + "%"));
                }
                rain.setText(rainValue + " мм");
                String descriptionText = jsonObject.getJSONArray("weather").getJSONObject(0).getString("main");
                String capitalizedDescription = descriptionText.substring(0, 1).toUpperCase() + descriptionText.substring(1);
                DS.setText(capitalizedDescription);

                double windSpeed = jsonObject.getJSONObject("wind").getDouble("speed");
                wind.setText(windSpeed + " м/с");
                setWeatherImage(descriptionText);

                initRecyclerview();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void setWeatherImage(String description) {
            int imageResource;

            switch (description.toLowerCase()) {
                case "rain":
                    imageResource = R.drawable.rain;
                    break;
                case "rainy":
                    imageResource = R.drawable.rainy;
                    break;
                case "snowy":
                    imageResource = R.drawable.snowy;
                    break;
                case "storm":
                    imageResource = R.drawable.storm;
                    break;
                case "sunny":
                    imageResource = R.drawable.sunny;

                case "windy":
                    imageResource = R.drawable.windy;
                    break;
                case "wind":
                    imageResource = R.drawable.wind;
                    break;
                case "cloudy":
                    imageResource = R.drawable.cloudy;
                    break;
                case "clouds":
                    imageResource = R.drawable.cloudy;
                    break;
                default:
                    imageResource = R.drawable.cloudy_sunny;
                    break;
            }

            imageView.setImageResource(imageResource);
        }
    }


    private void setVaribale() {
        ConstraintLayout backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FutureActivity.this, MainActivity.class));
            }
        });
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, ArrayList<FutureDomain>> {
        @Override
        protected ArrayList<FutureDomain> doInBackground(String... strings) {
            ArrayList<FutureDomain> items = new ArrayList<>();

            try {
                String apiKey = "Your key";
                String city = MainActivity.cityET.getText().toString();
                String apiUrl = "https://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + apiKey + "&units=metric&lang=ru";

                URL url = new URL(apiUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                String response = convertStreamToString(in);

                items = (ArrayList<FutureDomain>) parseWeatherData(response);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return items;
        }

        @Override
        protected void onPostExecute(ArrayList<FutureDomain> items) {
            // Выполняется в главном потоке после завершения doInBackground
            recyclerView = findViewById(R.id.view2);
            recyclerView.setLayoutManager(new LinearLayoutManager(FutureActivity.this, LinearLayoutManager.VERTICAL, false));
            adapterTommorow = new FutureAdapters(items);
            recyclerView.setAdapter(adapterTommorow);
        }
    }

    private void initRecyclerview() {
        new FetchWeatherTask().execute();
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private ArrayList<FutureDomain> parseWeatherData(String jsonData) {
        ArrayList<FutureDomain> items = new ArrayList<>();
        HashSet<String> uniqueDays = new HashSet<>();

        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray forecastArray = jsonObject.getJSONArray("list");

            for (int i = 0; i < forecastArray.length(); i++) {
                JSONObject forecastObject = forecastArray.getJSONObject(i);

                String dtTxt = forecastObject.getString("dt_txt");
                String dayOfWeek = getDayOfWeek(dtTxt);

                // Проверяем, был ли этот день уже добавлен
                if (!uniqueDays.contains(dayOfWeek)) {
                    JSONArray weatherArray = forecastObject.getJSONArray("weather");
                    String weatherDescription = weatherArray.getJSONObject(0).getString("main");
                    String wD = weatherDescription.substring(0, 1).toUpperCase() + weatherDescription.substring(1);

                    // Getting the maximum and minimum temperature
                    int maxTemp = forecastObject.getJSONObject("main").getInt("temp_max");
                    int minTemp = forecastObject.getJSONObject("main").getInt("temp_min");

                    String weatherImageResource = getWeatherImageResource(weatherDescription);

                    FutureDomain futureDomain = new FutureDomain(dayOfWeek, weatherImageResource, wD, maxTemp, minTemp);
                    items.add(futureDomain);

                    // Добавляем день в HashSet, чтобы избежать повторений
                    uniqueDays.add(dayOfWeek);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }

    private String abbreviateDayOfWeek(String fullDayOfWeek) {
        switch (fullDayOfWeek) {
            case "понедельник":
                return "Пн";
            case "вторник":
                return "Вт";
            case "среда":
                return "Ср";
            case "четверг":
                return "Чт";
            case "пятница":
                return "Пт";
            case "суббота":
                return "Сб";
            case "воскресенье":
                return "Вс";
            default:
                return "";
        }
    }

    private String getDayOfWeek(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = dateFormat.parse(dateString);
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
            String fullDayOfWeek = dayFormat.format(date);
            return abbreviateDayOfWeek(fullDayOfWeek);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }



    private String getWeatherImageResource(String description) {
        String imageRes;
        switch (description.toLowerCase()) {
            case "rain":
                imageRes = "rain";
                break;
            case "clear":
                imageRes = "sunny";
                break;
            case "rainy":
                imageRes = "rainy";
                break;
            case "snowy":
                imageRes = "snowy";
                break;
            case "storm":
                imageRes = "storm";
                break;
            case "sunny":
                imageRes = "sunny";
                break;
            case "windy":
                imageRes = "windy";
                break;
            case "wind":
                imageRes = "wind";
                break;
            case "cloudy":
                imageRes = "cloudy";
                break;
            default:
                imageRes = "cloudy_sunny";
                break;
        }
        return imageRes;
    }



}