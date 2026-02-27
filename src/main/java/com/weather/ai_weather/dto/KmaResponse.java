package com.weather.ai_weather.dto;

import java.util.List;

import lombok.Data;

@Data
public class KmaResponse {

	private Response response;

	@Data
	public static class Response {
		private Header header;
		private Body body;
	}

	@Data
	public static class Header {
		private String resultCode;
		private String resultMsg;
	}

	@Data
	public static class Body {
		private String dataType;
		private Items items;
	}

	@Data
	public static class Items {
		private List<WeatherItem> item;
	}

	@Data
	public static class WeatherItem {
		private String baseDate;
		private String baseTime;
		private String category;
		private String obsrValue;
		private int nx;
		private int ny;
		private String fcstDate;
		private String fcstTime;
		private String fcstValue;
	}

}
