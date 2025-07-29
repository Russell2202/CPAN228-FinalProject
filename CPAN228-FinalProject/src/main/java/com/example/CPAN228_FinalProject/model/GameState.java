package com.example.CPAN228_FinalProject.model;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;

@Component
@SessionScope
public class GameState {

    private final List<String> storyLog = new ArrayList<>();
    private boolean gameOver = false;

    public void appendToStory(String entry) {
        storyLog.add(entry);
    }

    public String buildPrompt(String userChoice) {
        StringBuilder prompt = new StringBuilder();
        for (String line : storyLog) {
            prompt.append(line).append("\n");
        }
        prompt.append("Player chose: ").append(userChoice);
        return prompt.toString();
    }

    public void reset() {
        storyLog.clear();
        gameOver = false;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public List<String> getStoryLog() {
        return storyLog;
    }
}
