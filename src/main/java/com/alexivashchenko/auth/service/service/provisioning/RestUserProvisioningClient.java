package com.alexivashchenko.auth.service.service.provisioning;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RestUserProvisioningClient implements UserProvisioningClient {

    private final WebClient webClient;

    @Value("${user.service.base-url}")
    private String baseUrl;

    @Override
    public void createProfile(UUID userId, String email) {

        CreateUserProfileRequest request =
                new CreateUserProfileRequest(userId, email);

        webClient.post()
                .uri(baseUrl + "/internal/users")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        response -> Mono.error(
                                new RuntimeException("User service error")
                        ))
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(3))
                .block();
    }

    private record CreateUserProfileRequest(
            UUID userId,
            String email
    ) {}
}
