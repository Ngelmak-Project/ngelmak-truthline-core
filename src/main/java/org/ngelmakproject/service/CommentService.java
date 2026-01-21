package org.ngelmakproject.service;

import java.net.MalformedURLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.ngelmakproject.domain.NkComment;
import org.ngelmakproject.domain.NkFile;
import org.ngelmakproject.domain.NkPost;
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
    private final AccountService accountService;
    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository, FileService fileService,
            AccountService accountService) {
        this.commentRepository = commentRepository;
        this.fileService = fileService;
        this.accountService = accountService;
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
        return accountService.findOneByCurrentUser().map(account -> {
            /* 1. we start by saving the files if exists */
            List<MultipartFile> medias = media.map(m -> Arrays.asList(m)).orElse(List.of());
            List<NkFile> files = fileService.save(medias);
            /* 2. then save the Comment with the attachment */
            comment
                    .at(Instant.now()) // set the current time
                    .file(files.stream().findFirst().orElse(null)) // attach the file is exists.
                    .account(account); // set the current connected user as owner of the comment.
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

    @Transactional(readOnly = true)
    public List<NkComment> findTopComments(List<NkPost> posts, int limit) {
        log.debug("Request to get all Comments");
        return commentRepository.findTopCommentsForPosts(posts.stream().map(NkPost::getId).toList(), limit);
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
                    .ifPresent(commentRepository::delete);
                    return null;
        }).orElseThrow(AccountNotFoundException::new);
    }
}
