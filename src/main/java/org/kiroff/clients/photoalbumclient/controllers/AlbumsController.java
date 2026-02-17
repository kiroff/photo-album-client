package org.kiroff.clients.photoalbumclient.controllers;

import org.kiroff.clients.photoalbumclient.domain.Album;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Controller
public class AlbumsController
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AlbumsController.class);

    @Autowired
    OAuth2AuthorizedClientService authorizedClientService;

    @GetMapping("/albums")
    public String getAlbums(Model model, @AuthenticationPrincipal OidcUser principal/*, Authentication authentication*/)
    {
        LOGGER.info("Principal: {}", principal);
//        LOGGER.info("idTokenValue: {}", principal.getIdToken().getTokenValue());
        final OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        return Optional.ofNullable(oAuth2AuthenticationToken)
                .map(OAuth2AuthenticationToken::getAuthorizedClientRegistrationId)
                .map(regId -> (OAuth2AuthorizedClient) authorizedClientService.loadAuthorizedClient(regId, principal.getName()))
                .map(c -> {
            final String tokenValue = c.getAccessToken().getTokenValue();
            LOGGER.info("tokenValue: {}", tokenValue);

            final List<Album> albumList = IntStream.range(1, 10)
                    .mapToObj(i -> new Album(i, "Album " + i, "http://localhost:8020/albums/" + i))
                    .toList();
            model.addAttribute("albums", albumList);
            return "albums";
        }).orElse("albums");
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
                    model.addAttribute("album", new Album(Integer.parseInt(id), "Album " + id, "http://localhost:8020/albums/" + id));
                });
        return "album";
    }
}
