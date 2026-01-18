package org.ngelmakproject.service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.ngelmakproject.domain.NkAccount;
import org.ngelmakproject.domain.NkComment;
import org.ngelmakproject.domain.NkFile;
import org.ngelmakproject.domain.NkPost;
import org.ngelmakproject.domain.enumeration.Status;
import org.ngelmakproject.domain.enumeration.Visibility;
import org.ngelmakproject.repository.PostRepository;
import org.ngelmakproject.web.rest.dto.PageDTO;
import org.ngelmakproject.web.rest.errors.AccountNotFoundException;
import org.ngelmakproject.web.rest.errors.ResourceNotFoundException;
import org.ngelmakproject.web.rest.errors.UnauthorizedResourceAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentService commentService;
    @Autowired
    private FileService fileService;
    @Autowired
    private AccountService accountService;

    @Autowired
    private EntityManager entityManager;

    /**
     * Save a post.
     *
     * @param post the entity to save.
     * @return the persisted entity.
     */
    public NkPost save(NkPost post, List<MultipartFile> medias, List<MultipartFile> covers) {
        log.debug("Request to save Post : {} | {}x file(s) and {}x cover(s)", post, medias.size(), covers.size());
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
        log.debug("Request to update Post : {} | {}x file(s), {}x cover(s), and {}x to be deleted", post, medias.size(), covers.size(), deletedMedias.size());
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
                        // [WARN] make sure to delete files only when all other actions are successfully completed. Since the deleted actions of file may have actions that cannot be cancelled, like removing files.
                        fileService.delete(deletedMedias);

                        return existingPost;
                    })
                    .orElseThrow(() -> new ResourceNotFoundException("Entity not found", ENTITY_NAME, "idnotfound"));
        }).orElseThrow(AccountNotFoundException::new);
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
        Page<NkPost> page = postRepository.findByStatusOrderByAtDesc(Status.VALIDATED, pageable);

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
