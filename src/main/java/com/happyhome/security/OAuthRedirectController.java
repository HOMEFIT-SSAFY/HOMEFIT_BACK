package com.happyhome.security;

import jakarta.servlet.http.HttpSession;
import java.net.URI;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oauth")
public class OAuthRedirectController {

    static final String REDIRECT_SESSION_KEY = "happyhome.oauth.redirect";

    private final ObjectProvider<ClientRegistrationRepository> registrations;

    public OAuthRedirectController(ObjectProvider<ClientRegistrationRepository> registrations) {
        this.registrations = registrations;
    }

    @GetMapping("/redirect/{provider}")
    public ResponseEntity<Void> redirect(
            @PathVariable String provider,
            @RequestParam(required = false) String redirect,
            HttpSession session
    ) {
        ClientRegistrationRepository repository = registrations.getIfAvailable();
        if (!hasRegistration(repository, provider)) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, oauthSetupLocation(provider, redirect))
                    .build();
        }

        session.setAttribute(REDIRECT_SESSION_KEY, safeRedirect(redirect));
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/oauth2/authorization/" + provider)
                .build();
    }

    private boolean hasRegistration(ClientRegistrationRepository repository, String provider) {
        if (repository == null) {
            return false;
        }
        return repository.findByRegistrationId(provider) != null;
    }

    private String safeRedirect(String redirect) {
        if (redirect == null || redirect.isBlank()) {
            return "/home";
        }
        try {
            URI uri = URI.create(redirect);
            if (!uri.isAbsolute()) {
                String path = uri.toString();
                if (path.startsWith("/") && !path.startsWith("//")) {
                    return path;
                }
                return "/home";
            }
            String host = uri.getHost();
            int port = uri.getPort();
            if (("localhost".equals(host) || "127.0.0.1".equals(host))
                    && (port == 8080 || port == 5173)) {
                return uri.toString();
            }
        } catch (IllegalArgumentException ignored) {
            return "/home";
        }
        return "/home";
    }

    private String oauthSetupLocation(String provider, String redirect) {
        String base = "/login";
        try {
            URI uri = URI.create(safeRedirect(redirect));
            if (uri.isAbsolute()) {
                base = uri.getScheme() + "://" + uri.getAuthority() + "/login";
            }
        } catch (IllegalArgumentException ignored) {
            base = "/login";
        }
        return base + "?oauthSetup=" + provider;
    }
}
