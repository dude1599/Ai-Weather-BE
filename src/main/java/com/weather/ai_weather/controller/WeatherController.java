package com.weather.ai_weather.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.weather.ai_weather.service.AiWeatherService;
import com.weather.ai_weather.service.KakaoLocalService;
import com.weather.ai_weather.service.KmaWeatherService;
import com.weather.ai_weather.util.KmaCoordinateConverter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WeatherController {

	private final KakaoLocalService kakaoLocalService;
	private final KmaWeatherService kmaWeatherService;
	private final AiWeatherService aiWeatherService;

	@GetMapping("/weather")
	public String showWeather(
		@RequestParam(name = "region", required = false) String region,
		Model model) {

		if (region == null || region.trim().isEmpty()) {
			return "weather";
		}

		KakaoLocalService.KakaoResponse.Document locationInfo = kakaoLocalService.getCoordinates(region);
		if (locationInfo == null) {
			model.addAttribute("error", "'" + region + "' 위치를 찾을 수 없습니다. 도로명 주소 또는 장소명을 입력해 주세요.");
			return "weather";
		}

		double lat = Double.parseDouble(locationInfo.getY());
		double lon = Double.parseDouble(locationInfo.getX());
		int[] grid = KmaCoordinateConverter.convertToGrid(lat, lon);
		Map<String, String> current = kmaWeatherService.getWeatherNow(grid[0], grid[1]);
		List<Map<String, String>> forecast = kmaWeatherService.getWeatherForecast(grid[0], grid[1]);
		AiWeatherService.AiWeatherRequest aiRequest = new AiWeatherService.AiWeatherRequest();
		aiRequest.setLat(lat);
		aiRequest.setLon(lon);
		aiRequest.setCurrent(current);
		aiRequest.setForecast(forecast);
		AiWeatherService.AiWeatherResponse aiResponse = aiWeatherService.getAiWeather(aiRequest);
		String displayName = (locationInfo.getPlace_name() != null && !locationInfo.getPlace_name().isEmpty())
			? locationInfo.getPlace_name() : locationInfo.getAddress_name();

		model.addAttribute("location", displayName);

		if (current != null && current.containsKey("temp")) {
			model.addAttribute("temperature", current.get("temp"));
		}

		if (aiResponse != null) {
			model.addAttribute("aiAdvice", aiResponse.getAiAdvice());
		}

		return "weather";
	}
}
