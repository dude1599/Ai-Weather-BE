package com.weather.ai_weather.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AiWeatherService {

	private final RestClient restClient;

	public AiWeatherService() {
		this.restClient = RestClient.builder()
			.baseUrl("http://localhost:8000")
			.requestFactory(new SimpleClientHttpRequestFactory())
			.build();
	}

	@Data
	public static class AiWeatherRequest {
		private double lat;
		private double lon;
		private Map<String, String> current;
		private List<Map<String, String>> forecast;
	}

	@Data
	public static class AiWeatherResponse {
		private String model;
		private String aiAdvice;
	}

	public AiWeatherResponse getAiWeather(AiWeatherRequest request) {
		log.info("날씨 AI 분석을 위해 AI 엔진 서버에 데이터를 요청합니다.");
		try {
			return restClient.post()
				.uri("/api/ai-weather")
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				.body(request)
				.retrieve()
				.body(AiWeatherResponse.class);
		} catch (Exception e) {
			log.error("AI 분석 요청 중 오류 발생: {}", e.getMessage());
			return null;
		}
	}
}
