package org.ngelmakproject.service;

import java.net.MalformedURLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.ngelmakproject.domain.NkComment;
import org.ngelmakproject.domain.NkFile;
import org.ngelmakproject.repository.CommentRepository;
import org.ngelmakproject.web.rest.errors.AccountNotFoundException;
import org.ngelmakproject.web.rest.errors.BadRequestAlertException;
import org.ngelmakproject.web.rest.errors.ResourceNotFoundException;
import org.ngelmakproject.web.rest.errors.UnauthorizedResourceAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final String ENTITY_NAME = "comment";
    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    private final FileService fileService;
    private final PostService postService;
    private final AccountService accountService;
    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository, FileService fileService,
            AccountService accountService, PostService postService) {
        this.commentRepository = commentRepository;
        this.fileService = fileService;
        this.accountService = accountService;
        this.postService = postService;
    }

    /**
     * Save a comment.
     *
     * @param comment the entity to save.
     * @return the persisted entity.
     * @throws MalformedURLException
     */
    public NkComment save(NkComment comment, Optional<MultipartFile> media) throws MalformedURLException {
        log.debug("Request to save Comment : {} | {}x file", comment, media.map(e -> 1).orElse(0));
        if (comment.getContent().length() > 1000) {
            throw new BadRequestAlertException("Contenu trop long > 1000 caractères.", ENTITY_NAME, "contentTooLong");
        }
        // [TODO] This action should be done asynchronously with redis database
        return accountService.findOneByCurrentUser().map(account -> {
            /* 1. we start by saving the files if exists */
            List<MultipartFile> medias = media.map(m -> Arrays.asList(m)).orElse(List.of());
            List<NkFile> files = fileService.save(medias);
            /* 2. then save the Comment with the attachment */
            comment
                    .at(Instant.now()) // set the current time
                    .file(files.stream().findFirst().orElse(null)) // attach the file is exists.
                    .account(account); // set the current connected user as owner of the comment.
            // [TODO] Use Redis to record the changes.
            if (comment.getPost() != null) {
                this.postService.updateCommmentCount(comment.getPost().getId(), 1);
            } else if (comment.getReplyTo() != null) {
                this.updateReplyCount(comment.getReplyTo().getId(), 1);
            } else {
                throw new BadRequestAlertException(
                        "A comment must always refer to at least one Post or Comment, but none have been provided.",
                        ENTITY_NAME, "missingPostOrComment");
            }

            return commentRepository.save(comment);
        }).orElseThrow(AccountNotFoundException::new);
    }

    /**
     * Update a comment.
     *
     * @param comment the entity to update partially.
     * @return the persisted entity.
     */
    public NkComment update(NkComment comment, Optional<MultipartFile> media, Optional<NkFile> deletedFile) {
        log.debug("Request to save Comment : {} | {}x file", comment, media.map(e -> 1).orElse(0));
        if (comment.getContent().length() > 1000) {
            throw new BadRequestAlertException("Contenu trop long > 1000 caractères.", ENTITY_NAME, "contentTooLong");
        }
        return accountService.findOneByCurrentUser().map(account -> {
            return commentRepository
                    .findById(comment.getId())
                    .map(existingComment -> {
                        if (account.getId() != existingComment.getAccount().getId()) {
                            throw new UnauthorizedResourceAccessException(account.getUser(), existingComment.getId(),
                                    ENTITY_NAME);
                        }
                        existingComment.setLastUpdate(Instant.now());
                        if (comment.getContent() != null) {
                            existingComment.setContent(comment.getContent());
                        }
                        if (media.isPresent()) {
                            /* 1. we start by saving the files if exists */
                            List<MultipartFile> medias = media.map(m -> Arrays.asList(m)).orElse(List.of());
                            List<NkFile> files = fileService.save(medias);
                            // 2. attach the file is exists.
                            if (deletedFile.isPresent()) {
                                this.fileService.delete(Arrays.asList(deletedFile.get()));
                            }
                            existingComment.setFile(files.stream().findFirst().orElse(null));
                        }
                        return this.commentRepository.save(existingComment);
                    })
                    .orElseThrow(() -> new ResourceNotFoundException("Entity not found", ENTITY_NAME, "idnotfound"));
        }).orElseThrow(AccountNotFoundException::new);
    }

    /**
     * Update reply comments.
     * 
     * <p>
     * This method is responsible of tracking and updating total replies on a
     * comment.
     * <\p>
     * 
     * [TODO] This method later should consider reading from Redis database and
     * update automatically the comment count.
     * It should be handle by a cron
     * 
     * @param commentId
     * @param count     could be a positive or negative number.
     */
    // @Scheduled(cron = "0 0 2 * * *") // Run daily at 2 AM
    public void updateReplyCount(Long commentId, Integer count) {
        this.commentRepository.updateReplyCount(commentId, count);
    }

    /**
     * Delete the comment by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Comment : {}", id);
        accountService.findOneByCurrentUser().map(account -> {
            commentRepository
                    .findById(id)
                    .map(deletingComment -> {
                        if (account.getId() != deletingComment.getAccount().getId()) {
                            throw new UnauthorizedResourceAccessException(account.getUser(), id,
                                    ENTITY_NAME);
                        }
                        deletingComment.setDeleteAt(Instant.now());
                        // 2. attach the file is exists.
                        if (deletingComment.getFile() != null) {
                            this.fileService.delete(Arrays.asList(deletingComment.getFile()));
                        }
                        return deletingComment;
                    })
                    .ifPresent(deletingComment -> {
                        // [TODO] Use Redis to record the changes.
                        commentRepository.delete(deletingComment);
                        if (deletingComment.getPost() != null) {
                            this.postService.updateCommmentCount(deletingComment.getPost().getId(), -1);
                        } else if (deletingComment.getReplyTo() != null) {
                            this.updateReplyCount(deletingComment.getReplyTo().getId(), -1);
                        } else {
                            // Nothing to do.
                        }
                    });
            return null;
        }).orElseThrow(AccountNotFoundException::new);
    }
}