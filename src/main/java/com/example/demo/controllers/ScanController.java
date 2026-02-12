package com.example.demo.controllers;

import com.example.demo.dtos.ScanRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Objects;


@RestController
@RequestMapping("/scan")
@CrossOrigin(origins = "*")
public class ScanController {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/run-scan")
    public ResponseEntity<String> scan(@RequestBody ScanRequest scanRequest) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newBuilder().build();

        System.out.println("Received scan request for URL: " + scanRequest.getUrl());
        var payload = String.join("\n"
                , "{"
                , String.format(" \"url\": \"%s\",", scanRequest.getUrl())
                , " \"visibility\": \"public\","
                , " \"country\": \"de\","
                , " \"tags\": ["
                , "  \"iloveurlscan\","
                , "  \"testing\""
                , " ]"
                , "}"
        );

        var host = "https://urlscan.io";
        var pathname = "/api/v1/scan";
        var request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .uri(URI.create(host + pathname ))
                .header("Content-Type", "application/json")
                .header("api-key", "019c22dd-dc80-751b-86c4-0d39bd63ff72")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Scan initiation response: " + response.body());
        var scanId = objectMapper.readTree(response.body()).path("uuid").asText();

        pathname = "/api/v1/result/" + scanId + "/";
        request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(host + pathname ))
                .header("api-key", "019c22dd-dc80-751b-86c4-0d39bd63ff72")
                .build();
        String statusCode;
        do {
            Thread.sleep(5000);
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            statusCode = objectMapper.readTree(response.body()).path("status").asText();
        }while(Objects.equals(statusCode, "404"));
        JsonNode verdictOverall = objectMapper.readTree(response.body())
                .path("verdicts")
                .path("overall");
        return ResponseEntity.ok(objectMapper.writeValueAsString(verdictOverall));
    }

}
