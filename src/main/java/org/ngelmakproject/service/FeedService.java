package org.ngelmakproject.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.ngelmakproject.domain.NkAccount;
import org.ngelmakproject.domain.NkFeed;
import org.ngelmakproject.domain.NkPost;
import org.ngelmakproject.repository.FeedRepository;
import org.ngelmakproject.repository.MembershipRepository;
import org.ngelmakproject.web.rest.dto.PageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing
 * {@link org.ngelmakproject.domain.NkFeed}.
 */
@Service
@Transactional
public class FeedService {

    private static final Logger log = LoggerFactory.getLogger(FeedService.class);

    @Autowired
    private FeedRepository feedRepository;
    @Autowired
    private MembershipRepository membershipRepository;

    /**
     * Create a personalized feed for each user based on their connections and
     * recommendations.
     * 
     * "Fan-Out on Write” approach, where each user has their own feed, and new
     * posts are propagated to all followers’ feeds upon creation. This allows fo
     * efficient feed retrieval.
     * 
     * @param post
     */
    public void propagatePostToFollowers(NkPost post) {
        log.debug("Propagate NkPost to get all followers.");
        List<NkFeed> feeds = membershipRepository.findByFollowing(post.getAccount()).stream().map(membership -> {
            NkFeed feed = new NkFeed();
            feed.setFeedOwner(membership.getFollower());
            feed.setPost(post);
            return feed;
        }).collect(Collectors.toList());
        feedRepository.saveAll(feeds);
    }

    public PageDTO<NkFeed> getFeed(Long feedOwnerId, Pageable pageable) {
        log.debug("Request to retrieve Feeds for owner {}.", feedOwnerId);
        List<NkFeed> followingAccountsFeed = feedRepository.findByFeedOwner(new NkAccount().id(feedOwnerId), pageable).getContent();
        // Get recommended posts (assuming a method to fetch recommendations)
        List<NkFeed> recommendedPosts = getRecommendedPosts(feedOwnerId, pageable).getContent();
        List<NkFeed> allFeeds = new ArrayList<>();
        allFeeds.addAll(followingAccountsFeed);
        allFeeds.addAll(recommendedPosts);
        allFeeds.sort((a, b) -> {
            return -1 * a.getPost().getAt().compareTo(b.getPost().getAt());
        });
        Page<NkFeed> page = new PageImpl<>(allFeeds, pageable, allFeeds.size());
        return new PageDTO<>(page);
    }

    public PageDTO<NkFeed> getFeed(Pageable pageable) {
        log.debug("Request to retrieve Feeds any user.");
        // Get recommended posts (assuming a method to fetch recommendations)
        List<NkFeed> recommendedPosts = getRecommendedPosts(null, pageable).getContent();
        List<NkFeed> allFeeds = new ArrayList<>();
        allFeeds.addAll(recommendedPosts);
        allFeeds.sort((a, b) -> {
            return -1 * a.getPost().getAt().compareTo(b.getPost().getAt());
        });
        Page<NkFeed> page = new PageImpl<>(allFeeds, pageable, allFeeds.size());
        return new PageDTO<>(page);
    }

    /**
     * [TODO]
     * To fetch recommended posts, you can integrate a recommendation engine or
     * machine learning model that analyzes user preferences and suggests relevant
     * content.
     * 
     * @param id
     * @param pageRequest
     * @return
     */
    private Page<NkFeed> getRecommendedPosts(Long id, Pageable pageable) {
        log.warn("Need to be implemented.");
        return feedRepository.findByOrderByDateDesc(pageable);
    }
}
