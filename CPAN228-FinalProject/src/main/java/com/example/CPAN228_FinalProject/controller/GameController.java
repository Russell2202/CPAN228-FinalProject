package com.example.CPAN228_FinalProject.controller;

import com.example.CPAN228_FinalProject.model.GameState;
import com.example.CPAN228_FinalProject.model.User;
import com.example.CPAN228_FinalProject.service.GPTService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.CPAN228_FinalProject.model.LeaderboardEntry;
import com.example.CPAN228_FinalProject.repository.LeaderboardRepository;
import com.example.CPAN228_FinalProject.model.User;


import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


@Controller
public class GameController {

    private final GPTService gptService;
    private final GameState gameState;
    private final LeaderboardRepository leaderboardRepository;

    public GameController(GPTService gptService, GameState gameState, LeaderboardRepository leaderboardRepository) {
        this.gptService = gptService;
        this.gameState = gameState;
        this.leaderboardRepository = leaderboardRepository;
    }

    // Show the homepage
    @GetMapping("/")
    public String home(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        return "index";
    }

    //Starts the game
    @RequestMapping(value = "/adventure/start", method = {RequestMethod.GET, RequestMethod.POST})
    public String startGame(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        model.addAttribute("session", session);

        // 1. Generate Intro
        gameState.reset();
        String intro = gptService.generateIntro();
        gameState.appendToStory("Game Start: " + intro);
        model.addAttribute("response", "Game Start: " + intro);

        // 2. Generate Related Title & Summary
        String dungeonTitle = gptService.generateDungeonTitle(intro);
        String dungeonSummary = gptService.generateDungeonSummaryFromIntro(intro);

        session.setAttribute("dungeonTitle", dungeonTitle);
        session.setAttribute("dungeonSummary", dungeonSummary);

        return "adventure";
    }

    // Show the adventure page manually (if needed)
    @GetMapping("/adventure")
    public String showAdventurePage(Model model) {
        model.addAttribute("story", gameState.getStoryLog());
        return "adventure";
    }

    @PostMapping("/adventure/next")
    public String continueGame(@RequestParam String userChoice, Model model, HttpSession session) {
        List<String> validChoices = Arrays.asList("1", "2", "3");

        if (!validChoices.contains(userChoice)) {
            model.addAttribute("response", "Invalid choice. Please select 1, 2, or 3.");
            return "adventure";
        }

        String response = gptService.continueStory(userChoice);

        // NEW: extract hidden per-turn score <!--SCORE:N--> and add to total (N must be 0..100)
        try {
            int earned = 0;
            if (response != null) {
                Matcher m = Pattern.compile("<!--\\s*SCORE\\s*:\\s*(\\d{1,3})\\s*-->").matcher(response);
                if (m.find()) {
                    int n = Integer.parseInt(m.group(1));
                    if (n >= 0 && n <= 100) {
                        earned = n; // per-turn cap 0..100
                    }
                }
            }
            // Try to add to your cumulative score if GameState supports it
            try {
                gameState.getClass().getMethod("addScore", int.class).invoke(gameState, earned);
            } catch (NoSuchMethodException e) {
                // Fallback: if you only have setScore/getScore, use that instead
                try {
                    int current = gameState.getScore();
                    int updated = current + earned; // totals can exceed 100 (e.g., 1000+)
                    gameState.getClass().getMethod("setScore", int.class).invoke(gameState, updated);
                } catch (Exception ignore) { /* no-op if not available */ }
            }
        } catch (Exception ignore) {
            // stay silent—don’t break flow if score parse fails
        }
        // END NEW

        // keep your existing flow
        // Continue if not ending
        gameState.appendToStory("> " + userChoice + "\n" + response);
        model.addAttribute("response", response);
        model.addAttribute("user", session.getAttribute("user"));
        model.addAttribute("session", session);

        // Detect ending (supports both markers you're using)
        String respLower = response == null ? "" : response.toLowerCase();
        boolean isEnding = respLower.startsWith("[ending]") || respLower.contains("##game_over##");

        if (isEnding) {
            // Use current logged-in user or fallback
            User user = (User) session.getAttribute("user");
            String username = (user != null && user.getUsername() != null) ? user.getUsername() : "anonymous";

            // Use score if your GameState has it, else default to 0
            int score = 0;
            try {
                score = gameState.getScore();
            } catch (NoSuchMethodError | Exception ignored) {
                // leave score as 0
            }

            leaderboardRepository.save(new LeaderboardEntry(username, score));
            return "redirect:/end";
        }

        return "adventure";
    }



    @GetMapping("/leaderboard")
    public String showLeaderboard(Model model) {
        model.addAttribute("leaderboard", leaderboardRepository.findTop10ByOrderByScoreDesc());
        return "leaderboard";
    }


    @GetMapping("/end")
    public String showEndPage(Model model) {
        model.addAttribute("score", gameState.getScore());
        return "end";
    }

    @GetMapping("/story")
    public String getMethodName(Model model) {
        model.addAttribute("storylog", gameState.getStoryLog());
        return "storylog";
    }
}
