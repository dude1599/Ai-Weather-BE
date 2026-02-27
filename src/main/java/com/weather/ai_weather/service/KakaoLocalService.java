package com.weather.ai_weather.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KakaoLocalService {

	private final RestClient restClient;
	private final String kakaoKey;

	public KakaoLocalService(@Value("${api.kakao.key}") String kakaoKey) {
		this.kakaoKey = kakaoKey;
		this.restClient = RestClient.builder()
			.baseUrl("https://dapi.kakao.com/v2/local/search")
			.build();
	}

	@Data
	public static class KakaoResponse {
		private List<Document> documents;

		@Data
		public static class Document {
			private String place_name;
			private String address_name;
			private String y;
			private String x;
		}
	}

	public KakaoResponse.Document getCoordinates(String query) {
		KakaoResponse.Document doc = searchApi("/address.json", query);
		if (doc != null) {
			return doc;
		}
		log.info("'{}' 주소 검색 결과가 없어 키워드 검색으로 재시도합니다.", query);
		return searchApi("/keyword.json", query);
	}

	private KakaoResponse.Document searchApi(String endpoint, String query) {
		try {
			KakaoResponse response = restClient.get()
				.uri(uriBuilder -> uriBuilder
					.path(endpoint)
					.queryParam("query", query)
					.build())
				.header("Authorization", "KakaoAK " + kakaoKey)
				.retrieve()
				.body(KakaoResponse.class);

			if (response != null && response.getDocuments() != null && !response.getDocuments().isEmpty()) {
				return response.getDocuments().getFirst();
			}
		} catch (Exception e) {
			log.error("카카오 로컬 API 호출 실패 ({}): {}", endpoint, e.getMessage());
		}
		return null;
	}
}
