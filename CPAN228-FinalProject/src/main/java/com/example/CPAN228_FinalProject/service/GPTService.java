package com.example.CPAN228_FinalProject.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GPTService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.model}")
    private String model;

    private final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private final RestTemplate restTemplate = new RestTemplate();

    public String generateResponse(List<String> history, String playerInput) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", "You are the narrator of a dark fantasy dungeon. The user is the player in a 'choose your own adventure' game."));

        for (String message : history) {
            messages.add(Map.of("role", "user", "content", message));
        }

        messages.add(Map.of("role", "user", "content", playerInput));

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.9);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_URL, entity, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return message.get("content").toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String generateIntro() {
        return generateResponse(new ArrayList<>(), "Begin the adventure.");
    }

    public String continueStory(String prompt) {
        return generateResponse(new ArrayList<>(), prompt);
    }
}

