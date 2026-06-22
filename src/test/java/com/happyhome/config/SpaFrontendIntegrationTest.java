package com.happyhome.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:spa-frontend-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
@AutoConfigureMockMvc
class SpaFrontendIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @ValueSource(strings = {"/", "/prices", "/transfers/1"})
    void servesVueApplicationForBrowserRoutes(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<div id=\"app\"></div>")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/login/oauth2/code/kakao", "/login/oauth2/code/google"})
    void doesNotServeVueApplicationForOAuthCallbackRoutes(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?oauthError"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/notices", "/api/admin/batch/lh-notices", "/api/bus-stops/sync"})
    void rejectsAnonymousWriteEndpoints(String path) throws Exception {
        mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Notice\",\"content\":\"Body\"}"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/notices", "/api/admin/batch/lh-notices", "/api/bus-stops/sync"})
    void rejectsNonAdminWriteEndpoints(String path) throws Exception {
        mockMvc.perform(post(path)
                        .with(user("ssafy").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Notice\",\"content\":\"Body\"}"))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/houses", "/api/oauth/redirect/kakao"})
    void allowsNetlifyFrontendOrigin(String path) throws Exception {
        mockMvc.perform(options(path)
                        .header(HttpHeaders.ORIGIN, "https://ssafylh.netlify.app")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://ssafylh.netlify.app"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://ssafylh.netlify.app/home", "https://ssafylh.netlify.app/prices"})
    void allowsNetlifyOAuthRedirects(String redirect) throws Exception {
        mockMvc.perform(get("/api/oauth/redirect/kakao").param("redirect", redirect))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://ssafylh.netlify.app/login?oauthSetup=kakao"));
    }
}
