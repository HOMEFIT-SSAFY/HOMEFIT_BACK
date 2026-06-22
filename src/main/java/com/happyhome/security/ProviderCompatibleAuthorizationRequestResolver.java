package com.happyhome.security;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;

public class ProviderCompatibleAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private static final String AUTHORIZATION_BASE_URI = "/oauth2/authorization";

    private final DefaultOAuth2AuthorizationRequestResolver delegate;

    public ProviderCompatibleAuthorizationRequestResolver(ClientRegistrationRepository registrations) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(registrations, AUTHORIZATION_BASE_URI);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = delegate.resolve(request);
        return customize(request, authorizationRequest, registrationIdFromRequest(request));
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = delegate.resolve(request, clientRegistrationId);
        return customize(request, authorizationRequest, clientRegistrationId);
    }

    private OAuth2AuthorizationRequest customize(
            HttpServletRequest request,
            OAuth2AuthorizationRequest authorizationRequest,
            String registrationId
    ) {
        if (authorizationRequest == null || !requiresMinimalAuthorizationRequest(registrationId)) {
            return authorizationRequest;
        }

        Map<String, Object> additionalParameters = new LinkedHashMap<>(authorizationRequest.getAdditionalParameters());
        additionalParameters.remove(PkceParameterNames.CODE_CHALLENGE);
        additionalParameters.remove(PkceParameterNames.CODE_CHALLENGE_METHOD);
        additionalParameters.remove("nonce");

        Map<String, Object> attributes = new LinkedHashMap<>(authorizationRequest.getAttributes());
        attributes.remove(PkceParameterNames.CODE_VERIFIER);
        attributes.remove("nonce");

        String authorizationRequestUri = authorizationRequest.getAuthorizationUri()
                + "?response_type=" + encode(authorizationRequest.getResponseType().getValue())
                + "&client_id=" + encode(authorizationRequest.getClientId())
                + "&redirect_uri=" + encode(authorizationRequest.getRedirectUri())
                + "&state=" + encode(authorizationRequest.getState());

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .scopes(java.util.Set.of())
                .additionalParameters(additionalParameters)
                .attributes(attributes)
                .authorizationRequestUri(authorizationRequestUri)
                .build();
    }

    private boolean requiresMinimalAuthorizationRequest(String registrationId) {
        return "kakao".equals(registrationId) || "naver".equals(registrationId);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String registrationIdFromRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        int index = uri.indexOf(AUTHORIZATION_BASE_URI + "/");
        if (index < 0) {
            return "";
        }
        return uri.substring(index + AUTHORIZATION_BASE_URI.length() + 1);
    }
}
