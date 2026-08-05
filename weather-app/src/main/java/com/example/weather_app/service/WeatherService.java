package com.example.weatherapp.service;

import com.example.weatherapp.dto.ForecastDayDto;
import com.example.weatherapp.dto.WeatherResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class WeatherService {

    private final RestTemplate restTemplate;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.base-url}")
    private String baseUrl;

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Cacheable(value = "forecast", key = "#city.toLowerCase()")
    public WeatherResponseDto getWeatherForecast(String city) {
        // Step 1: External OpenWeatherMap API call (5-day / 3-hour forecast API)
        String url = String.format("%s/forecast?q=%s&appid=%s&units=metric", baseUrl, city, apiKey);

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null || !response.containsKey("city")) {
            throw new RuntimeException("City not found or OpenWeatherMap API error!");
        }

        // Step 2: Extract City details
        Map<String, Object> cityData = (Map<String, Object>) response.get("city");
        String cityName = (String) cityData.get("name");
        String country = (String) cityData.get("country");

        // Step 3: Extract Forecast List
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");

        // Current weather payload (list me se pehla index current time data hota hai)
        Map<String, Object> currentData = list.get(0);
        Map<String, Object> currentMain = (Map<String, Object>) currentData.get("main");
        Map<String, Object> currentWind = (Map<String, Object>) currentData.get("wind");
        List<Map<String, Object>> currentWeatherList = (List<Map<String, Object>>) currentData.get("weather");
        Map<String, Object> currentCondition = currentWeatherList.get(0);

        // Step 4: Aggregate 3-hour slots into daily 5-day forecast
        Map<String, List<Map<String, Object>>> dailyMap = new LinkedHashMap<>();
        for (Map<String, Object> item : list) {
            String dtTxt = (String) item.get("dt_txt"); // Format: "YYYY-MM-DD HH:mm:ss"
            String date = dtTxt.split(" ")[0];

            dailyMap.computeIfAbsent(date, k -> new ArrayList<>()).add(item);
        }

        List<ForecastDayDto> forecastList = new ArrayList<>();
        int count = 0;

        for (Map.Entry<String, List<Map<String, Object>>> entry : dailyMap.entrySet()) {
            if (count >= 5) break; // Sirf 5 din ka forecast chahiye

            List<Map<String, Object>> dayItems = entry.getValue();
            double minTemp = Double.MAX_VALUE;
            double maxTemp = Double.MIN_VALUE;
            int totalHumidity = 0;
            String icon = "";
            String condition = "";

            for (Map<String, Object> item : dayItems) {
                Map<String, Object> main = (Map<String, Object>) item.get("main");
                double tempMin = Double.parseDouble(main.get("temp_min").toString());
                double tempMax = Double.parseDouble(main.get("temp_max").toString());

                if (tempMin < minTemp) minTemp = tempMin;
                if (tempMax > maxTemp) maxTemp = tempMax;

                totalHumidity += Integer.parseInt(main.get("humidity").toString());

                // Mid-day condition preferred for forecast icon representation
                if (condition.isEmpty() || item.get("dt_txt").toString().contains("12:00:00")) {
                    List<Map<String, Object>> wList = (List<Map<String, Object>>) item.get("weather");
                    condition = wList.get(0).get("main").toString();
                    icon = wList.get(0).get("icon").toString();
                }
            }

            forecastList.add(ForecastDayDto.builder()
                    .date(entry.getKey())
                    .minTemp(Math.round(minTemp * 10.0) / 10.0)
                    .maxTemp(Math.round(maxTemp * 10.0) / 10.0)
                    .humidity(totalHumidity / dayItems.size())
                    .condition(condition)
                    .icon(icon)
                    .build());

            count++;
        }

        // Step 5: Construct complete Response DTO
        return WeatherResponseDto.builder()
                .city(cityName)
                .country(country)
                .currentTemperature(Double.parseDouble(currentMain.get("temp").toString()))
                .feelsLike(Double.parseDouble(currentMain.get("feels_like").toString()))
                .humidity(Integer.parseInt(currentMain.get("humidity").toString()))
                .windSpeed(Double.parseDouble(currentWind.get("speed").toString()))
                .condition(currentCondition.get("main").toString())
                .icon(currentCondition.get("icon").toString())
                .forecast(forecastList)
                .build();
    }
}