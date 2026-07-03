package com.app.CloudShareApi.controller;

import com.app.CloudShareApi.dto.ProfileDTO;
import com.app.CloudShareApi.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService service;

    @PostMapping ("/register")
    public ResponseEntity<?> registerProfile(@RequestBody ProfileDTO dto){
        HttpStatus status = service.existsByClerkId(dto.getClerkId()) ? HttpStatus.OK : HttpStatus.CREATED;
        ProfileDTO savedProfile = service.createProfile(dto);


        return ResponseEntity.status(status).body(savedProfile);
    }

}
