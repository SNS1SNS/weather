package com.example.weather;

public class WeatherSearch {
    private String cityName;
    private String searchTime;
    private int searchCount;
    private double temperature;

    public WeatherSearch() {
    }

    public WeatherSearch(String cityName, String searchTime, int searchCount, double temperature) {
        this.cityName = cityName;
        this.searchTime = searchTime;
        this.searchCount = searchCount;
        this.temperature = temperature;
    }

    public String getCityName() {
        return cityName;
    }

    public String getSearchTime() {
        return searchTime;
    }

    public int getSearchCount() {
        return searchCount;
    }

    public double getTemperature() {
        return temperature;
    }
}

