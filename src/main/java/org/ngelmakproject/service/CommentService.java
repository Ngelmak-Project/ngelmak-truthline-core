package org.ngelmakproject.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.ngelmakproject.domain.NkAccount;
import org.ngelmakproject.domain.NkComment;
import org.ngelmakproject.domain.NkPost;
import org.ngelmakproject.domain.enumeration.Opinion;
import org.ngelmakproject.repository.CommentRepository;
import org.ngelmakproject.service.storage.FileStorageService;
import org.ngelmakproject.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service Implementation for managing
 * {@link org.ngelmakproject.domain.NkComment}.
 */
@Service
@Transactional
public class CommentService {

    @Value("${server.host}")
    private String host;

    @Value("${server.port}")
    private Integer port;

    private static final String ENTITY_NAME = "comment";
    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private AccountService nkAccountService;

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * Save a comment.
     *
     * @param comment the entity to save.
     * @return the persisted entity.
     * @throws MalformedURLException
     */
    public NkComment save(NkComment comment, MultipartFile file) throws MalformedURLException {
        log.debug("Request to save NkComment : {}", comment);
        NkAccount nkAccount = nkAccountService.findOneByCurrentUser().get();
        if (comment.getOpinion() == null)
            comment.setOpinion(Opinion.DEFAULT);
        comment.account(nkAccount).setAt(Instant.now());
        if (file != null) {
            String[] dirs = { "media", "comments" };
            URL url = fileStorageService.store(file, true, file.getOriginalFilename(), dirs);
            comment.setUrl(url.toString());
        }
        return commentRepository.save(comment);
    }

    /**
     * Update a comment.
     *
     * @param comment the entity to update partially.
     * @return the persisted entity.
     */
    public NkComment update(NkComment comment, MultipartFile file) {
        log.info("Request to update NkComment : {}", comment);
        return commentRepository
                .findById(comment.getId())
                .map(existingComment -> {
                    String deleteImageUrl = existingComment.hasUrl() ? existingComment.getUrl() : "";
                    String url = comment.hasUrl() ? comment.getUrl() : "";
                    if (deleteImageUrl.equals(url)) {
                        deleteImageUrl = ""; // nothing to do.
                    }
                    existingComment.setLastUpdate(Instant.now());
                    if (comment.getOpinion() != null) {
                        existingComment.setOpinion(comment.getOpinion());
                    }
                    if (comment.getContent() != null) {
                        existingComment.setContent(comment.getContent());
                    }
                    if (file != null) {
                        String[] dirs = { "comments", "media" };
                        URL newUrl = fileStorageService.store(file, true, file.getOriginalFilename(), dirs);
                        deleteImageUrl = existingComment.hasUrl() ? existingComment.getUrl() : "";
                        existingComment.setUrl(newUrl.toString());
                    }
                    this.commentRepository.save(existingComment);
                    if (!deleteImageUrl.isEmpty()) {
                        this.fileStorageService.delete(deleteImageUrl);
                    }
                    return existingComment;
                })
                .orElseThrow(() -> new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));

    }

    /**
     * Get all the comments.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<NkComment> findAll(Pageable pageable) {
        log.debug("Request to get all Comments");
        return commentRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<NkComment> findTopComments(List<NkPost> posts, int limit) {
        log.debug("Request to get all Comments");
        return commentRepository.findTopCommentsForPosts(posts.stream().map(NkPost::getId).toList(), limit);
    }

    /**
     * Get one comment by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<NkComment> findOne(Long id) {
        log.debug("Request to get NkComment : {}", id);
        return commentRepository.findById(id);
    }

    /**
     * Delete the comment by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete NkComment : {}", id);
        commentRepository
                .findById(id)
                .map(deletingComment -> {
                    if (deletingComment.hasUrl()) {
                        this.fileStorageService.delete(deletingComment.getUrl());
                    }
                    return deletingComment;
                }).ifPresent(commentRepository::delete);
    }
}
