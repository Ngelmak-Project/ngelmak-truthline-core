package org.ngelmakproject.web.rest;

import java.util.concurrent.TimeUnit;

import org.ngelmakproject.domain.NkFeed;
import org.ngelmakproject.repository.FeedRepository;
import org.ngelmakproject.security.UserPrincipal;
import org.ngelmakproject.service.FeedService;
import org.ngelmakproject.web.rest.dto.PageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing {@link org.ngelmakproject.domain.NkFeed}.
 */
@RestController
@RequestMapping("/api/feeds")
public class FeedResource {

    private static final Logger log = LoggerFactory.getLogger(FeedResource.class);

    private static final String ENTITY_NAME = "feed";

    @Value("${spring.application.name}")
    private String applicationName;

    private final FeedService feedService;

    private final FeedRepository feedRepository;

    public FeedResource(FeedService feedService, FeedRepository feedRepository) {
        this.feedService = feedService;
        this.feedRepository = feedRepository;
    }

    /**
     * {@code GET  /feeds?q=} : get all the feeds.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of feeds in body.
     */
    @GetMapping("")
    public ResponseEntity<PageDTO<NkFeed>> getFeeds(@RequestParam(value = "q", defaultValue = "") String query,
            Authentication authentication,
            Pageable pageable) {
        log.debug("REST request to get a page of Feeds : {}", query);

        PageDTO<NkFeed> pageDTO;
        if (authentication != null) {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            pageDTO = feedService.getFeed(principal.getUserId(), pageable);
        } else {
            pageDTO = feedService.getFeed(pageable);
        }
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
                .body(pageDTO);
    }
}