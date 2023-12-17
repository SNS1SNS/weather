package com.example.weather.Activitis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weather.Adapters.HourlyAdapters;
import com.example.weather.Domains.Hourly;
import com.example.weather.R;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.lang.String;
import com.google.firebase.database.DatabaseReference;
import com.example.weather.WeatherSearch;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
private RecyclerView.Adapter adapterHourly;
private ArrayList<Hourly> items = new ArrayList<>();
private RecyclerView recyclerView;
private TextView description, gradus, time, humidity, wind, rain, latlon;
private ImageView imageView;
public static EditText cityET;
private Button searchBtn;
private FirebaseDatabase mDatabase;
private String defaultCity = "Астана";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityET = findViewById(R.id.cityInput);
        initRecyclerview();
        setVarible();
        gradus = findViewById(R.id.textView2);
        description = findViewById(R.id.textView);
        time = findViewById(R.id.textView1);
        humidity =findViewById(R.id.textView9);
        rain = findViewById(R.id.textView5);
        wind = findViewById(R.id.textView7);
        imageView = findViewById(R.id.imageView);
        mDatabase = FirebaseDatabase.getInstance();
        searchBtn = findViewById(R.id.searchBtn);
        latlon = findViewById(R.id.textView4);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("E dd MMM HH:mm", Locale.getDefault());
        String date = dateFormat.format(calendar.getTime());

        String[] words = date.split(" ");

        for (int i = 0; i < words.length; i++) {
            if (!words[i].isEmpty()) {
                words[i] = words[i].substring(0, 1).toUpperCase(Locale.getDefault()) + words[i].substring(1);
            }
        }

        date = TextUtils.join(" ", words);
        if (defaultCity != null && !defaultCity.trim().equals("")) {
            cityET.setText(defaultCity);
            updateWeatherData(defaultCity);
            updateForecastrData(defaultCity);
        }
        time.setText(date);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cityET.getText().toString().trim().equals(""))
                    Toast.makeText(MainActivity.this, "Напишите город", Toast.LENGTH_SHORT).show();
                else {
                    String city = cityET.getText().toString().trim();
                    updateWeatherData(city);
                    updateForecastrData(city);


                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd MM yyyy", Locale.getDefault());
                    String currentTime = String.format("%d %02d %d %02d:%02d:%02d",
                            calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            calendar.get(Calendar.SECOND));

                    String temperatureString = gradus.getText().toString();
                    double temperature = Double.parseDouble(temperatureString.replace("°", ""));
                    WeatherSearch weatherSearch = new WeatherSearch(city, currentTime, 1, temperature);
                    DatabaseReference searchRef = mDatabase.getReference("SearchHistory").push();
                    searchRef.setValue(weatherSearch);


                }
            }
        });

    }

    private void updateWeatherData(String city) {
        String key = "Your key";
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + key + "&units=metric&lang=ru";
        new GetURLData().execute(url);
    }
    private void updateForecastrData(String city) {
        String key = "Your key";
        String url = "https://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + key + "&units=metric&lang=ru";
        new FetchWeatherTask().execute(url);
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
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.has("cod")) {
                        int responseCode = jsonObject.getInt("cod");
                        if (responseCode == 404) {
                            Toast.makeText(MainActivity.this, "Город не найден", Toast.LENGTH_SHORT).show();
                            return; // Прекратить выполнение, если город не найден
                        }
                    }

                    gradus.setText(String.valueOf(jsonObject.getJSONObject("main").getDouble("temp")) + "°");

                    String descriptionText = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
                    String capitalizedDescription = descriptionText.substring(0, 1).toUpperCase() + descriptionText.substring(1);
                    description.setText(capitalizedDescription);

                    humidity.setText(jsonObject.getJSONObject("main").getDouble("humidity") + "%");

                    double rainValue = 0.0;
                    if (jsonObject.has("rain") && !jsonObject.isNull("rain")) {
                        rainValue = jsonObject.getJSONObject("rain").getDouble("1h");
                    }
                    rain.setText( rainValue + " мм");

                    double windSpeed = jsonObject.getJSONObject("wind").getDouble("speed");
                    wind.setText( windSpeed + " м/с");

                    setWeatherImage(descriptionText);

                    latlon.setText("Lat: " + jsonObject.getJSONObject("coord").getDouble("lat")+ " Lon: " + jsonObject.getJSONObject("coord").getDouble("lon"));
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
                    case "clear":
                        imageResource = R.drawable.sunny;
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
                        break;
                    case "windy":
                        imageResource = R.drawable.windy;
                        break;
                    case "wind":
                        imageResource = R.drawable.wind;
                        break;
                    case "cloudy":
                        imageResource = R.drawable.cloudy;
                        break;
                    default:
                        imageResource = R.drawable.cloudy_sunny;
                        break;
                }

                imageView.setImageResource(imageResource);
            }
    }

    private void setVarible() {
        TextView next7dayBtn = findViewById(R.id.nextBtn);
        next7dayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = cityET.getText().toString().trim();
                if (!city.isEmpty()) {
                    Intent intent = new Intent(MainActivity.this, FutureActivity.class);
                    intent.putExtra("CITY_KEY", city);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Введите город", Toast.LENGTH_SHORT).show();
                }
            }
        });


}



    private class FetchWeatherTask extends AsyncTask<String, Void, ArrayList<Hourly>> {
        @Override
        protected ArrayList<Hourly> doInBackground(String... strings) {
            ArrayList<Hourly> items = new ArrayList<>();

            try {
                String apiKey = "Your key";
                String city = cityET.getText().toString().trim();

                String apiUrl = "https://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + apiKey + "&units=metric&lang=ru";

                URL url = new URL(apiUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                String response = convertStreamToString(in);

                items = (ArrayList<Hourly>) parseWeatherData(response);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return items;
        }

        @Override
        protected void onPostExecute(ArrayList<Hourly> items) {
            // Выполняется в главном потоке после завершения doInBackground
            recyclerView = findViewById(R.id.view2);
            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
            adapterHourly = new HourlyAdapters((ArrayList<Hourly>) items);
            recyclerView.setAdapter(adapterHourly);
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

    private ArrayList<Hourly> parseWeatherData(String jsonData) {
        ArrayList<Hourly> items = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray forecastArray = jsonObject.getJSONArray("list");

            for (int i = 0; i < forecastArray.length(); i += 1) {
                JSONObject forecastObject = forecastArray.getJSONObject(i);

                String dtTxt = forecastObject.getString("dt_txt");
                int temperature = forecastObject.getJSONObject("main").getInt("temp");
                JSONArray weatherArray = forecastObject.getJSONArray("weather");
                String weatherDescription = weatherArray.getJSONObject(0).getString("main");
                String wD = weatherDescription.substring(0, 1).toUpperCase() + weatherDescription.substring(1);

                String time = dtTxt.split(" ")[1]; // Получаем время из строки даты-времени
                String weatherImageResource = getWeatherImageResource(weatherDescription);


                Hourly hourly = new Hourly(time, temperature, weatherImageResource);
                items.add(hourly);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
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