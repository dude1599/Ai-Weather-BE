package com.weather.ai_weather.service;

import com.weather.ai_weather.dto.KmaResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class KmaWeatherService {

	private final RestClient restClient;
	private final String kmaKey;

	private static final Map<String, String> SKY_MAP = Map.of(
		"1", "맑음",
		"3", "구름많음",
		"4", "흐림"
	);

	private static final Map<String, String> PTY_MAP = Map.of(
		"0", "없음",
		"1", "비",
		"2", "비/눈",
		"3", "눈",
		"5", "빗방울",
		"6", "빗방울눈날림",
		"7", "눈날림"
	);

	public KmaWeatherService(
		@Value("${api.kma.url}") String kmaUrl,
		@Value("${api.kma.key}") String kmaKey) {
		this.kmaKey = kmaKey;
		DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(kmaUrl);
		factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
		this.restClient = RestClient.builder().uriBuilderFactory(factory).build();
	}

	private String parseWeatherValue(String value) {
		if (value == null || value.equals("강수없음")) {
			return "0";
		}
		try {
			double num = Double.parseDouble(value);
			if (num <= -900 || num >= 900) {
				return "0";
			}
		} catch (NumberFormatException ignored) {
		}
		return value;
	}

	public Map<String, String> getWeatherNow(int nx, int ny) {
		LocalDateTime now = LocalDateTime.now();
		if (now.getMinute() < 40) {
			now = now.minusHours(1);
		}

		String baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String baseTime = now.format(DateTimeFormatter.ofPattern("HH00"));

		try {
			KmaResponse response = restClient.get()
				.uri(uriBuilder -> uriBuilder.path("/getUltraSrtNcst")
					.queryParam("serviceKey", kmaKey)
					.queryParam("pageNo", 1)
					.queryParam("numOfRows", 20)
					.queryParam("dataType", "JSON")
					.queryParam("base_date", baseDate)
					.queryParam("base_time", baseTime)
					.queryParam("nx", nx)
					.queryParam("ny", ny)
					.build())
				.retrieve().body(KmaResponse.class);

			Map<String, String> current = new HashMap<>();
			if (response != null
				&& response.getResponse() != null
				&& response.getResponse().getBody() != null
				&& response.getResponse().getBody().getItems() != null
				&& response.getResponse().getBody().getItems().getItem() != null) {
				response.getResponse().getBody().getItems().getItem().forEach(item -> {
					String cat = item.getCategory();
					String val = parseWeatherValue(item.getObsrValue());

					if ("T1H".equals(cat)) {
						current.put("temp", val);
					}
					if ("RN1".equals(cat)) {
						current.put("rain", val);
					}
					if ("REH".equals(cat)) {
						current.put("humidity", val);
					}
					if ("WSD".equals(cat)) {
						current.put("wind", val);
					}
					if ("PTY".equals(cat)) {
						current.put("pty", PTY_MAP.getOrDefault(val, "없음"));
					}
				});
			}
			return current;
		} catch (Exception e) {
			log.error("초단기 실황 API 호출 실패", e);
			return Collections.emptyMap();
		}
	}

	public List<Map<String, String>> getWeatherForecast(int nx, int ny) {
		LocalDateTime now = LocalDateTime.now();
		if (now.getMinute() < 45) {
			now = now.minusHours(1);
		}

		String baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String baseTime = now.format(DateTimeFormatter.ofPattern("HH30"));

		try {
			KmaResponse response = restClient.get()
				.uri(uriBuilder -> uriBuilder.path("/getUltraSrtFcst")
					.queryParam("serviceKey", kmaKey)
					.queryParam("pageNo", 1)
					.queryParam("numOfRows", 60)
					.queryParam("dataType", "JSON")
					.queryParam("base_date", baseDate)
					.queryParam("base_time", baseTime)
					.queryParam("nx", nx)
					.queryParam("ny", ny)
					.build())
				.retrieve().body(KmaResponse.class);

			Map<String, Map<String, String>> forecastMap = new TreeMap<>();

			if (response != null
				&& response.getResponse() != null
				&& response.getResponse().getBody() != null
				&& response.getResponse().getBody().getItems() != null
				&& response.getResponse().getBody().getItems().getItem() != null) {
				response.getResponse().getBody().getItems().getItem().forEach(item -> {
					String time = item.getFcstTime();
					String cat = item.getCategory();
					String val = parseWeatherValue(item.getFcstValue());

					forecastMap.putIfAbsent(time, new HashMap<>());
					Map<String, String> timeData = forecastMap.get(time);

					if ("T1H".equals(cat)) {
						timeData.put("temp", val);
					}
					if ("RN1".equals(cat)) {
						timeData.put("rain", val);
					}
					if ("SKY".equals(cat)) {
						timeData.put("sky", SKY_MAP.getOrDefault(val, "알 수 없음"));
					}
					if ("PTY".equals(cat)) {
						timeData.put("pty", PTY_MAP.getOrDefault(val, "없음"));
					}
				});
			}
			List<Map<String, String>> resultList = new ArrayList<>();
			for (Map.Entry<String, Map<String, String>> entry : forecastMap.entrySet()) {
				Map<String, String> data = entry.getValue();
				data.put("time", entry.getKey());
				resultList.add(data);
			}
			return resultList;
		} catch (Exception e) {
			log.error("초단기 예보 API 호출 실패", e);
			return Collections.emptyList();
		}
	}
}
