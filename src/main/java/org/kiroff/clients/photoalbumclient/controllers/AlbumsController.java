package org.kiroff.clients.photoalbumclient.controllers;

import org.kiroff.clients.photoalbumclient.domain.Album;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Controller
public class AlbumsController
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AlbumsController.class);
    public static final String URL = "http://localhost:8082/albums";

    @Autowired
    OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    RestClient restClient;

    @Autowired
    WebClient webClient;

    @GetMapping("/albums")
    public String getAlbums(Model model)
    {
        var responseEntity = webClient.get()
                .uri(URL)
                .retrieve()
                .onStatus(HttpStatusCode::isError, r ->
                r.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .flatMap(body -> Mono.error(new IllegalStateException("HTTP " + r.statusCode() + " body=" + body))))
                .bodyToMono(new ParameterizedTypeReference<List<Album>>()
                {
                }).block();

        model.addAttribute("albums", responseEntity);
        return "albums";
    }

    @GetMapping("/albums/{id}")
    public String getAlbums(@PathVariable("id") String id, Model model, @AuthenticationPrincipal OidcUser principal, Authentication authentication)
    {
        Optional.ofNullable(authentication)
                .map(token -> ((OAuth2AuthenticationToken) token).getAuthorizedClientRegistrationId())
                .map(regId -> (OAuth2AuthorizedClient) authorizedClientService.loadAuthorizedClient(regId, principal.getName()))
                .map(c -> c.getAccessToken().getTokenValue())
                .ifPresent(c -> {
//                    LOGGER.info("tokenValue: {}", c);
                    final var responseEntity = restClient.get()
                            .uri(URL + "/" + id)
                            .headers(h -> h.setBearerAuth(c))
                            .retrieve()
                            .body(new ParameterizedTypeReference<>()
                            {
                            });
                    model.addAttribute("album", responseEntity);
                });
        return "album";
    }
}
