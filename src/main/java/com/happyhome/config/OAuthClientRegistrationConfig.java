package com.happyhome.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnExpression("'${happyhome.oauth.kakao.client-id:}' != '' "
        + "|| '${happyhome.oauth.naver.client-id:}' != '' "
        + "|| '${happyhome.oauth.google.client-id:}' != ''")
public class OAuthClientRegistrationConfig {

    @Bean
    ClientRegistrationRepository clientRegistrationRepository(
            @Value("${happyhome.oauth.kakao.client-id:}") String kakaoClientId,
            @Value("${happyhome.oauth.kakao.client-secret:}") String kakaoClientSecret,
            @Value("${happyhome.oauth.naver.client-id:}") String naverClientId,
            @Value("${happyhome.oauth.naver.client-secret:}") String naverClientSecret,
            @Value("${happyhome.oauth.google.client-id:}") String googleClientId,
            @Value("${happyhome.oauth.google.client-secret:}") String googleClientSecret
    ) {
        List<ClientRegistration> registrations = new ArrayList<>();
        addIfPresent(registrations, kakao(kakaoClientId, kakaoClientSecret));
        addIfPresent(registrations, naver(naverClientId, naverClientSecret));
        addIfPresent(registrations, google(googleClientId, googleClientSecret));
        return new InMemoryClientRegistrationRepository(registrations);
    }

    private void addIfPresent(List<ClientRegistration> registrations, ClientRegistration registration) {
        if (registration != null) {
            registrations.add(registration);
        }
    }

    private ClientRegistration kakao(String clientId, String clientSecret) {
        if (!StringUtils.hasText(clientId)) {
            return null;
        }
        ClientRegistration.Builder builder = ClientRegistration.withRegistrationId("kakao")
                .clientId(clientId)
                .clientName("Kakao")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("profile_nickname", "account_email")
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .tokenUri("https://kauth.kakao.com/oauth/token")
                .userInfoUri("https://kapi.kakao.com/v2/user/me")
                .userNameAttributeName("id");
        applyClientSecret(builder, clientSecret);
        return builder.build();
    }

    private ClientRegistration naver(String clientId, String clientSecret) {
        if (!StringUtils.hasText(clientId)) {
            return null;
        }
        ClientRegistration.Builder builder = ClientRegistration.withRegistrationId("naver")
                .clientId(clientId)
                .clientName("Naver")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("name", "email")
                .authorizationUri("https://nid.naver.com/oauth2.0/authorize")
                .tokenUri("https://nid.naver.com/oauth2.0/token")
                .userInfoUri("https://openapi.naver.com/v1/nid/me")
                .userNameAttributeName("response");
        applyClientSecret(builder, clientSecret);
        return builder.build();
    }

    private ClientRegistration google(String clientId, String clientSecret) {
        if (!StringUtils.hasText(clientId) || !StringUtils.hasText(clientSecret)) {
            return null;
        }
        return ClientRegistration.withRegistrationId("google")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientName("Google")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .issuerUri("https://accounts.google.com")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .build();
    }

    private void applyClientSecret(ClientRegistration.Builder builder, String clientSecret) {
        if (StringUtils.hasText(clientSecret)) {
            builder.clientSecret(clientSecret)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST);
            return;
        }
        builder.clientAuthenticationMethod(ClientAuthenticationMethod.NONE);
    }
}
