package org.ngelmakproject.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.ngelmakproject.domain.NkAccount;
import org.ngelmakproject.domain.NkComment;
import org.ngelmakproject.domain.NkFile;
import org.ngelmakproject.domain.NkPost;
import org.ngelmakproject.domain.NkReaction;
import org.ngelmakproject.domain.enumeration.Status;
import org.ngelmakproject.domain.enumeration.Visibility;
import org.ngelmakproject.repository.PostRepository;
import org.ngelmakproject.repository.ReactionRepository;
import org.ngelmakproject.web.rest.dto.PageDTO;
import org.ngelmakproject.web.rest.dto.PostDTO;
import org.ngelmakproject.web.rest.dto.ReactionSummaryDTO;
import org.ngelmakproject.web.rest.errors.AccountNotFoundException;
import org.ngelmakproject.web.rest.errors.BadRequestAlertException;
import org.ngelmakproject.web.rest.errors.ResourceNotFoundException;
import org.ngelmakproject.web.rest.errors.UnauthorizedResourceAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;

/**
 * Service Implementation for managing
 * {@link org.ngelmakproject.domain.NkPost}.
 */
@Service
@Transactional
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    private static final String ENTITY_NAME = "post";

    private final PostRepository postRepository;
    private final CommentService commentService;
    private final FileService fileService;
    private final AccountService accountService;
    private final EntityManager entityManager;
    private final ReactionRepository reactionRepository;

    PostService(PostRepository postRepository,
            CommentService commentService,
            FileService fileService,
            AccountService accountService,
            ReactionRepository reactionRepository,
            EntityManager entityManager) {
        this.postRepository = postRepository;
        this.commentService = commentService;
        this.fileService = fileService;
        this.accountService = accountService;
        this.reactionRepository = reactionRepository;
        this.entityManager = entityManager;
    }

    /**
     * Save a post.
     *
     * @param post the entity to save.
     * @return the persisted entity.
     */
    @Transactional
    public NkPost save(NkPost post, List<MultipartFile> medias, List<MultipartFile> covers) {
        log.debug("Request to save Post : {} | {}x file(s) and {}x cover(s)", post, medias.size(), covers.size());
        if (post.getContent().length() > 3000) {
            throw new BadRequestAlertException("Contenu trop long > 3000 caractères.", ENTITY_NAME, "contentTooLong");
        }
        return accountService.findOneByCurrentUser().map(account -> {
            /* 1. we start by saving the files if exists */
            List<NkFile> files = fileService.save(medias, covers);
            /* 2. then save the post with the attachments */
            // [TODO] we will need to change the default status to match with the fact that
            // some users can create posts that bypass some step validations.
            post.status(Status.VALIDATED) // default status is PENDING
                    .at(Instant.now()) // set the current time
                    .files(new HashSet<NkFile>(files)) // attach files to the post
                    .account(account); // set the current connected user as owner of the post.
            return postRepository.save(post);
        }).orElseThrow(AccountNotFoundException::new);
    }

    /**
     * Update a post.
     * This function can eventually delete some files through the given
     * deletedNkFiles variable.
     *
     * @param post the entity to save.
     * @return the persisted entity.
     */
    public NkPost update(NkPost post, List<NkFile> deletedMedias,
            List<MultipartFile> medias, List<MultipartFile> covers) {
        log.debug("Request to update Post : {} | {}x file(s), {}x cover(s), and {}x to be deleted", post, medias.size(),
                covers.size(), deletedMedias.size());
        if (post.getContent().length() > 3000) {
            throw new BadRequestAlertException("Contenu trop long > 3000 caractères.", ENTITY_NAME, "contentTooLong");
        }
        return accountService.findOneByCurrentUser().map(account -> {
            return postRepository.findById(post.getId())
                    .map(existingPost -> {
                        if (account.getId() != existingPost.getAccount().getId()) {
                            throw new UnauthorizedResourceAccessException(account.getUser(), existingPost.getId(),
                                    ENTITY_NAME);
                        }
                        /* 1. we start by saving the files if exists */
                        List<NkFile> files = fileService.save(medias, covers);
                        /* 2. update the existing post */
                        existingPost.getFiles().addAll(files);
                        if (post.getKeywords() != null) {
                            existingPost.setKeywords(post.getKeywords());
                        }
                        if (post.getAt() != null) {
                            existingPost.setAt(post.getAt());
                        }
                        if (post.getLastUpdate() != null) {
                            existingPost.setLastUpdate(post.getLastUpdate());
                        }
                        if (post.getVisibility() != null) {
                            existingPost.setVisibility(post.getVisibility());
                        }
                        if (post.getContent() != null) {
                            existingPost.setContent(post.getContent());
                        }
                        if (post.getStatus() != null) {
                            existingPost.setStatus(post.getStatus());
                        }
                        postRepository.save(existingPost);
                        /* 3. delete removed files */
                        // [WARN] make sure to delete files only when all other actions are successfully
                        // completed. Since the deleted actions of file may have actions that cannot be
                        // cancelled, like removing files.
                        fileService.delete(deletedMedias);

                        return existingPost;
                    })
                    .orElseThrow(() -> new ResourceNotFoundException("Entity not found", ENTITY_NAME, "idnotfound"));
        }).orElseThrow(AccountNotFoundException::new);
    }

    /**
     * Retrieves a pageable list of validated posts enriched with:
     * - minimal account information (via EntityGraph on the repository)
     * - attached files (also via EntityGraph)
     * - aggregated reaction summaries (emoji → count + current user reaction)
     * - commentCount already stored on NkPost (no comment fetching required)
     *
     * This method avoids N+1 queries by:
     * 1. Fetching posts with account + files in a single query
     * 2. Fetching all reactions for all posts in one bulk query
     * 3. Building reaction summaries in memory
     * 4. Mapping everything into PostDTO objects
     *
     * @param pageable pagination information (page number, size, sort)
     * @return a page of PostDTO objects ready for API consumption
     */
    public Slice<PostDTO> getPosts(Pageable pageable) {
        // 1. Fetch posts with account + files using an EntityGraph (no N+1 here)
        Slice<NkPost> posts = postRepository.findByStatusOrderByAtDesc(
                Status.VALIDATED,
                pageable);

        // Extract post IDs for bulk operations
        List<Long> postIds = posts.stream()
                .map(NkPost::getId)
                .toList();

        // 2. Bulk fetch all reactions for all posts in the page
        // This avoids N+1 queries for reactions
        List<NkReaction> reactions = reactionRepository.findByPostIds(postIds);

        // 3. Build reaction summaries (emoji counts + user reaction)
        // Passing null for currentUserId for now — replace with actual user ID later
        Map<Long, ReactionSummaryDTO> reactionMap = PostService.buildReactionSummaries(reactions, null);

        // 4. Map each NkPost to PostDTO, injecting reaction summary and commentCount
        return posts.map(post -> PostDTO.from(
                post,
                reactionMap.getOrDefault(post.getId(), new ReactionSummaryDTO(Map.of(), null))));
    }

    /**
     * Builds a map of reaction summaries for a list of reactions.
     *
     * Input:
     * - A flat list of NkReaction objects (for many posts)
     * - The current user ID (nullable)
     *
     * Output:
     * - A Map keyed by postId
     * - Each value is a ReactionSummaryDTO containing:
     * • counts per emoji
     * • the emoji used by the current user (if any)
     *
     * This method is N+1 safe because it operates entirely in memory
     * after a single bulk fetch of reactions.
     */
    public static Map<Long, ReactionSummaryDTO> buildReactionSummaries(
            List<NkReaction> reactions,
            Long currentUserId) {
        // Temporary structure: postId → (emoji → count)
        Map<Long, Map<String, Integer>> countsByPost = new HashMap<>();

        // Temporary structure: postId → emoji reacted by current user
        Map<Long, String> userReactionByPost = new HashMap<>();

        for (NkReaction reaction : reactions) {
            Long postId = reaction.getPost().getId();
            String emoji = reaction.getEmoji();

            // Increment emoji count for this post
            countsByPost
                    .computeIfAbsent(postId, id -> new HashMap<>())
                    .merge(emoji, 1, Integer::sum);

            // Track the current user's reaction (if applicable)
            if (currentUserId != null && currentUserId.equals(reaction.getAccount().getId())) {
                userReactionByPost.put(postId, emoji);
            }
        }

        // Final result: postId → ReactionSummaryDTO
        Map<Long, ReactionSummaryDTO> result = new HashMap<>();

        for (Map.Entry<Long, Map<String, Integer>> entry : countsByPost.entrySet()) {
            Long postId = entry.getKey();
            Map<String, Integer> emojiCounts = entry.getValue();
            String userEmoji = userReactionByPost.get(postId);

            result.put(postId, new ReactionSummaryDTO(emojiCounts, userEmoji));
        }

        return result;
    }

    /**
     * Get all the posts.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public PageDTO<NkPost> findAll(String query, Pageable pageable) {
        log.debug("Request to get all Posts");
        if (query.length() > 5) {
            return fullTextSearch(query, pageable);
        }
        Slice<NkPost> page = postRepository.findByStatusOrderByAtDesc(Status.VALIDATED, pageable);

        List<NkComment> comments = commentService.findTopComments(page.getContent(), 10);
        page.getContent().stream().forEach(p -> {
            p.setComments(comments.stream().filter(c -> c.getPost().equals(p)).collect(Collectors.toSet()));
        });
        return new PageDTO<>(page);
    }

    /**
     * Get one post by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<NkPost> findOne(Long id) {
        log.debug("Request to get Post : {}", id);
        return postRepository.findById(id).map(existingPost -> {
            existingPost.getFiles().removeIf(e -> e.getDeletedAt() != null);
            return existingPost;
        });
    }

    /**
     * Delete the post by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Post : {}", id);
        throw new RuntimeException("Not Implemented...");
        // postRepository.deleteById(id);
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
    @Transactional(readOnly = true)
    public Slice<NkPost> getRecommendedPosts(Pageable pageable) {
        log.debug("Post to get recommended Post");
        return postRepository.findByStatusOrderByAtDesc(Status.VALIDATED, pageable);
    }

    @Transactional(readOnly = true)
    public PageDTO<NkPost> fullTextSearch(String fullText, Pageable pageable) {
        String sqlQuery = "SELECT " +
                "  full_search.*, " +
                "  p.id AS post_reference_id, " +
                "  p.title AS post_reference_title, " +
                "  p.content AS post_reference_content, " +
                "  a.name AS account_name " +
                "FROM ( " +
                "  SELECT p.* FROM ( " +
                "    SELECT *, ts_rank_cd(textsearchable_index_col, query) AS rank " +
                "    FROM nk_post, websearch_to_tsquery('french', :fullText) query " +
                "    WHERE status = 'VALIDATED' AND textsearchable_index_col @@ query " +
                "    ) AS p " +
                "  LEFT JOIN (SELECT id, ts_rank_cd(textsearchable_index_col, query) AS rank " +
                "  FROM nk_post, websearch_to_tsquery('french', :fullText) query " +
                "  WHERE textsearchable_index_col @@ query) AS a " +
                "  ON p.account_id = a.id " +
                "  ORDER BY a.rank,p.rank DESC " +
                "  LIMIT :limit " +
                "  OFFSET :offset " +
                ") AS full_search " +
                "LEFT JOIN nk_post AS p ON full_search.post_reference_id = p.id " +
                "LEFT JOIN nk_account AS a ON a.id = p.account_id";
        Query query = entityManager.createNativeQuery(sqlQuery, Tuple.class);
        query.setParameter("fullText", fullText);
        query.setParameter("limit", pageable.getPageSize());
        query.setParameter("offset", pageable.getOffset());
        List<Tuple> result = query.getResultList();
        List<NkPost> posts = result.stream()
                .map(t -> {
                    NkPost post = new NkPost();
                    // java.time.Instant
                    post.id(t.get("id", Long.class))
                            .keywords(t.get("keywords", String.class))
                            .at(t.get("at", Instant.class))
                            .lastUpdate(t.get("last_update", Instant.class))
                            .visibility(Visibility.valueOf(t.get("visibility", String.class)))
                            .content(t.get("content", String.class))
                            .status(Status.valueOf(t.get("status", String.class)))
                            .account(
                                    new NkAccount().id(t.get("id", Long.class))
                                            .name(t.get("account_name", String.class)))
                            .postReply(
                                    new NkPost()
                                            .id(t.get("post_reference_id", Long.class))
                                            .content(t.get("post_reference_content", String.class)));
                    return post;
                })
                .collect(Collectors.toList());
        Page<NkPost> page = new PageImpl<>(posts, pageable, posts.size());
        return new PageDTO<>(page);
    }
}
