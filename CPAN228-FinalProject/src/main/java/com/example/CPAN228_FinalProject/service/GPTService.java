package com.example.CPAN228_FinalProject.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.CPAN228_FinalProject.model.GameState;
import java.util.*;

@Service
public class GPTService {

    private final GameState gameState;

    public GPTService(GameState gameState) {
        this.gameState = gameState;
    }
    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.model}")
    private String model;

    private final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private final RestTemplate restTemplate = new RestTemplate();

    public String generateResponse(List<String> history, String playerInput) {
        List<Map<String, String>> messages = new ArrayList<>();

        // Enhanced system prompt to enforce 3 options only
        messages.add(Map.of("role", "system", "content",
                "You are the narrator of a dark fantasy dungeon 'choose your own adventure' game. "
                        + "The user must choose from 3 options only. "
                        + "After every scene, clearly return exactly 3 options (1, 2, or 3) for what the player can do next. "
                        + "NEVER return a different number of options, and NEVER include option 4 or beyond."));

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
        String introPrompt = "Begin a dark fantasy dungeon adventure. Introduce the setting and the player's role. " +
                "At the end, present exactly 3 clearly numbered choices (1, 2, and 3). " +
                "Do NOT include any other options or choices beyond 1 to 3.";

        return generateResponse(new ArrayList<>(), introPrompt);
    }

    public String continueStory(String playerInput) {
        List<String> history = gameState.getStoryLog();
        String structuredPrompt = playerInput + "\n\n"
                + "Continue the story based on the player's choice above. "
                + "End the response with exactly 3 clearly numbered options (1, 2, and 3). "
                + "Do NOT include options 4 or any extra choices.";

        return generateResponse(history, structuredPrompt);
    }

    public String generateDungeonTitle(String intro) {
        String prompt = """
        Based on the following dark fantasy dungeon summary, return a fitting dungeon name (maximum 5 words).
        Do not include quotation marks or additional descriptions.
        Only return the dungeon name.

        Summary:
        """ + intro;

        return generateResponse(new ArrayList<>(), prompt).trim();
    }



    public String generateDungeonSummaryFromIntro(String intro) {
        String prompt = "Based on the following dark fantasy dungeon introduction, write a 2-4 sentence summary of the dungeon setting. "
                + "This summary should **not** include any numbered choices or options. Only describe the atmosphere and background.\n\n"
                + intro;
        String raw = generateResponse(new ArrayList<>(), prompt);
        return raw.replaceAll("(?m)^\\d+\\.\\s.*", "");  // Just in case GPT adds options again
    }



}

