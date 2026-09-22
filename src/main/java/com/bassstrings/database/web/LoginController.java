package com.bassstrings.database.web;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    private final ClientRegistrationRepository clientRegistrationRepository;

    public LoginController(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @GetMapping("/login")
    public String login(Model model) {
        // Only show a provider's button once it's actually registered (see
        // OAuth2ClientRegistrationConfig), so an unconfigured provider doesn't 404 on click.
        model.addAttribute("googleEnabled", isRegistered("google"));
        model.addAttribute("facebookEnabled", isRegistered("facebook"));
        model.addAttribute("appleEnabled", isRegistered("apple"));
        return "login";
    }

    private boolean isRegistered(String registrationId) {
        return clientRegistrationRepository.findByRegistrationId(registrationId) != null;
    }
}
