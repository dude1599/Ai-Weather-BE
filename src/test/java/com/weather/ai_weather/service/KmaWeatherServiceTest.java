package com.weather.ai_weather.service;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class KmaWeatherServiceTest {

	@Autowired
	private KmaWeatherService kmaWeatherService;

	@Test
	void initTest(){
		int nx = 67;
		int ny = 101;

		Map<String, String> current = kmaWeatherService.getWeatherNow(nx, ny);
		System.out.println("현재 날씨: " + current);
		assertThat(current).isNotEmpty();
		assertThat(current.get("temp")).isNotNull();

		List<Map<String, String>> forecast = kmaWeatherService.getWeatherForecast(nx, ny);
		System.out.println("6시간 예보: " + forecast);
		assertThat(forecast).isNotEmpty();
		assertThat(forecast.size()).isGreaterThanOrEqualTo(6);
	}
}
