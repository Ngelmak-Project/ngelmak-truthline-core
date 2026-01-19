package org.ngelmakproject.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.ngelmakproject.domain.NkAccount;
import org.ngelmakproject.domain.NkFeed;
import org.ngelmakproject.domain.NkPost;
import org.ngelmakproject.domain.NkReaction;
import org.ngelmakproject.domain.enumeration.Status;
import org.ngelmakproject.repository.FeedRepository;
import org.ngelmakproject.repository.MembershipRepository;
import org.ngelmakproject.repository.PostRepository;
import org.ngelmakproject.repository.ReactionRepository;
import org.ngelmakproject.web.rest.dto.FeedDTO;
import org.ngelmakproject.web.rest.dto.PageDTO;
import org.ngelmakproject.web.rest.dto.PostDTO;
import org.ngelmakproject.web.rest.dto.ReactionSummaryDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final FeedRepository feedRepository;
    private final ReactionRepository reactionRepository;
    private final PostRepository postRepository;
    private final AccountService accountService;
    private final MembershipRepository membershipRepository;

    public FeedService(FeedRepository feedRepository, ReactionRepository reactionRepository,
            AccountService accountService,
            MembershipRepository membershipRepository, PostRepository postRepository) {
        this.feedRepository = feedRepository;
        this.reactionRepository = reactionRepository;
        this.accountService = accountService;
        this.membershipRepository = membershipRepository;
        this.postRepository = postRepository;
    }

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

    // public PageDTO<FeedDTO> getFeed(Pageable pageable) {
    //     Optional<NkAccount> optional = accountService.findOneByCurrentUser();
    //     List<FeedDTO> allFeeds = new ArrayList<>();
    //     if (optional.isPresent()) {
    //         log.debug("Request to retrieve Feeds for Account {}.", optional.get());
    //         List<NkFeed> followingAccountsFeed = feedRepository.findByFeedOwner(optional.get(), pageable).getContent();
    //         FeedDTO.from()
    //         allFeeds.FeedDTO(followingAccountsFeed);
    //     }
    //     allFeeds.addAll(this.postService.getPosts(pageable).getContent().stream().map(post -> {
    //         FeedDTO feed = new NkFeed();
    //         feed.setPost(post);
    //         return feed;
    //     }).toList());
    //     // [TODO] Get recommended posts (assuming a method to fetch recommendations)
    //     allFeeds.sort((a, b) -> {
    //         return -1 * a.getPost().getAt().compareTo(b.getPost().getAt());
    //     });
    //     Page<NkFeed> page = new PageImpl<>(allFeeds, pageable, allFeeds.size());
    //     return new PageDTO<>(page);
    // }

    public PageDTO<FeedDTO> getFeed(Long userId, Pageable pageable) {
        // 1. Fetch feed entries with posts, accounts, and files
        Optional<NkAccount> optional = accountService.findOneByCurrentUser();
        List<NkFeed> feeds = new ArrayList<>();
        if (optional.isPresent()) {
            log.debug("Request to retrieve Feeds for Account {}.", optional.get());
            // 1. Fetch feed entries with posts, accounts, and files
            feeds = feedRepository.findByFeedOwner(optional.get(), pageable).getContent();
        }
        feeds.addAll(this.postRepository.findByStatusOrderByAtDesc(
                Status.VALIDATED,
                pageable).getContent().stream().map(post -> {
                    var feed = new NkFeed();
                    feed.setPost(post);
                    return feed;
                }).toList());
        // [TODO] Get recommended posts (assuming a method to fetch recommendations)
        feeds.sort((a, b) -> {
            return -1 * a.getPost().getAt().compareTo(b.getPost().getAt());
        });

        // Extract post IDs
        List<Long> postIds = feeds.stream()
                .map(f -> f.getPost().getId())
                .toList();
        // 2. Bulk fetch reactions for all posts in the feed
        List<NkReaction> reactions = reactionRepository.findByPostIds(postIds);
        // 3. Build reaction summaries
        Map<Long, ReactionSummaryDTO> reactionMap = PostService.buildReactionSummaries(reactions, userId);
        // 4. Map feed entries to DTOs
        List<FeedDTO> feedDTOs = feeds.stream().map(feed -> {
            var post = feed.getPost();
            ReactionSummaryDTO summary = reactionMap.getOrDefault(
                    post.getId(),
                    new ReactionSummaryDTO(Map.of(), null));

            PostDTO postDTO = PostDTO.from(post, summary);

            return FeedDTO.from(feed.getId(), postDTO);
        }).toList();
        Page<FeedDTO> page = new PageImpl<>(feedDTOs, pageable, feedDTOs.size());
        return new PageDTO<>(page);
    }
}
