package com.happyhome.security;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OAuthCallbackFallbackController {

    @GetMapping("/login/oauth2/code/{provider}")
    public String oauthCallbackFallback() {
        return "redirect:/login?oauthError";
    }
}
