package com.dmz.airdnd.accommodation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dmz.airdnd.accommodation.dto.request.AccommodationCreateRequest;
import com.dmz.airdnd.accommodation.dto.response.AccommodationCreateResponse;
import com.dmz.airdnd.accommodation.service.AccommodationService;
import com.dmz.airdnd.common.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/api/accommodations")
@RestController
public class AccommodationController {
	private final AccommodationService accommodationService;

	@PostMapping()
	public ResponseEntity<ApiResponse<AccommodationCreateResponse>> createAccommodation(
		@Valid @RequestBody AccommodationCreateRequest request) {
		AccommodationCreateResponse response = accommodationService.createAccommodation(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}
}
