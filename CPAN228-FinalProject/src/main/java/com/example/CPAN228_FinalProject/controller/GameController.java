package com.example.CPAN228_FinalProject.controller;

import com.example.CPAN228_FinalProject.model.GameState;
import com.example.CPAN228_FinalProject.service.GPTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * GameController is responsible for handling API routes that
 * interact with the frontend. It manages the flow of the dungeon
 * game by talking to GPTService and updating the GameState.
 */
@RestController
@RequestMapping("/api")
public class GameController {

    private final GPTService gptService;
    private final GameState gameState;

    /**
     * Constructor injection of GPTService and GameState.
     * GameState is session-scoped to maintain continuity for a user.
     */
    @Autowired
    public GameController(GPTService gptService, GameState gameState) {
        this.gptService = gptService;
        this.gameState = gameState;
    }

    /**
     * Starts a new adventure. Calls GPT to generate the intro scenario.
     */
    @PostMapping("/start")
    public String startGame() {
        gameState.reset(); // Clear old game state
        String intro = gptService.generateIntro(); // Ask GPT to create a dungeon intro
        gameState.appendToStory("Game Start: " + intro); // Track it
        return intro;
    }

    /**
     * Continues the game based on player's choice.
     * This sends the current story and choice to GPT for the next scene.
     *
     * @param userChoice - user's selected action (e.g., "Open the chest")
     */
    @PostMapping("/next")
    public String continueGame(@RequestParam String userChoice) {
        if (gameState.isGameOver()) {
            return "Game over! Please restart.";
        }

        String prompt = gameState.buildPrompt(userChoice); // Constructs the prompt from history
        String response = gptService.continueStory(prompt); // GPT continues the story
        gameState.appendToStory("> " + userChoice + "\n" + response);

        if (response.toLowerCase().contains("you died") || response.toLowerCase().contains("game over")) {
            gameState.setGameOver(true); // Set game over if GPT decides
        }

        return response;
    }

    /**
     * Returns the current game log and state.
     * Helpful for debugging or displaying a summary.
     */
    @GetMapping("/state")
    public GameState getState() {
        return gameState;
    }
}
