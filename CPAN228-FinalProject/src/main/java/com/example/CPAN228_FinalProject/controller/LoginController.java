package com.example.CPAN228_FinalProject.controller;

import com.example.CPAN228_FinalProject.model.User;
import com.example.CPAN228_FinalProject.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // login.html
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {

        // Hardcoded fallback for testing
        if ("admin".equals(username) && "admin".equals(password)) {
            session.setAttribute("user", new User(username, password)); // or dummy User
            return "redirect:/"; // success
        }

        Optional<User> optionalUser = userRepository.findByUsernameAndPassword(username, password);

        if (optionalUser.isPresent()) {
            session.setAttribute("user", optionalUser.get());
            return "redirect:/";
        } else {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/signup")
    public String showSignupForm() {
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(@RequestParam String username,
                                @RequestParam String password,
                                Model model) {

        Optional<User> existingUser = userRepository.findByUsernameAndPassword(username, password);

        if (existingUser.isPresent()) {
            model.addAttribute("error", "Username already exists!");
            return "signup";
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password); // In production, hash this!
        userRepository.save(newUser);

        model.addAttribute("success", "Account created! You can now log in.");
        return "signup";
    }

}
