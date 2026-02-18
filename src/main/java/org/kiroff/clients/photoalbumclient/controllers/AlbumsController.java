package org.kiroff.clients.photoalbumclient.controllers;

import org.kiroff.clients.photoalbumclient.domain.Album;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestClient;

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

    @GetMapping("/albums")
    public String getAlbums(Model model, @AuthenticationPrincipal OidcUser principal, @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient/*, Authentication authentication*/)
    {
        LOGGER.info("Principal: {}", principal);
//        LOGGER.info("idTokenValue: {}", principal.getIdToken().getTokenValue());
        final String tokenValue = Optional.ofNullable((OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication())
                .map(OAuth2AuthenticationToken::getAuthorizedClientRegistrationId)
                .map(regId -> (OAuth2AuthorizedClient) authorizedClientService.loadAuthorizedClient(regId, principal.getName()))
                .map(c -> c.getAccessToken().getTokenValue())
                .orElseGet(() -> authorizedClient.getAccessToken().getTokenValue());
        LOGGER.info("tokenValue: {}", tokenValue);
        final List<Album> responseEntity = restClient.get()
                .uri(URL)
                .headers(h -> h.setBearerAuth(tokenValue))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        model.addAttribute("albums", responseEntity);
        return "albums";
    }

    @GetMapping("/albums/{id}")
    public String getAlbums(@PathVariable("id") String id, Model model, @AuthenticationPrincipal OidcUser principal, Authentication authentication)
    {
        Optional.ofNullable(authentication)
                .map(token -> ((OAuth2AuthenticationToken) token).getAuthorizedClientRegistrationId())
                .map(regId -> (OAuth2AuthorizedClient) authorizedClientService.loadAuthorizedClient(regId, principal.getName()))
                .ifPresent(c -> {
                    final String tokenValue = c.getAccessToken().getTokenValue();
                    LOGGER.info("tokenValue: {}", tokenValue);
                    model.addAttribute("album", new Album(Integer.parseInt(id), "Album " + id, URL + "/" + id));
                });
        return "album";
    }
}
