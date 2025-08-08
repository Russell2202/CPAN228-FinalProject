package com.example.CPAN228_FinalProject.controller;

import com.example.CPAN228_FinalProject.model.GameState;
import com.example.CPAN228_FinalProject.model.User;
import com.example.CPAN228_FinalProject.service.GPTService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
public class GameController {

    private final GPTService gptService;
    private final GameState gameState;

    public GameController(GPTService gptService, GameState gameState) {
        this.gptService = gptService;
        this.gameState = gameState;
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

        //Check if the response contains Ending marker
        if (response.trim().toLowerCase().startsWith("##game_over##")) {
            return "redirect:/";
        }

        // Continue if not ending
        gameState.appendToStory("> " + userChoice + "\n" + response);
        model.addAttribute("response", response);
        model.addAttribute("user", session.getAttribute("user"));
        model.addAttribute("session", session);

        return "adventure";
    }

    @GetMapping("/leaderboard")
    public String showLeaderboard() {
        return "leaderboard";
    }

    @GetMapping("/end")
    public String showEndPage() {
        return "end";
    }
}
