package org.ngelmakproject.web.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.ngelmakproject.domain.NkAccount;
import org.ngelmakproject.domain.NkArticle;
import org.ngelmakproject.domain.NkAttachment;
import org.ngelmakproject.repository.ArticleRepository;
import org.ngelmakproject.service.ArticleService;
import org.ngelmakproject.web.rest.dto.PageDTO;
import org.ngelmakproject.web.rest.errors.BadRequestAlertException;
import org.ngelmakproject.web.rest.util.HeaderUtil;
import org.ngelmakproject.web.rest.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;

/**
 * REST controller for managing {@link org.ngelmakproject.domain.NkArticle}.
 */
@RestController
@RequestMapping("/truthline-ingres/articles")
public class ArticleResource {

    private static final Logger log = LoggerFactory.getLogger(ArticleResource.class);

    private static final String ENTITY_NAME = "post";

    @Value("${ngelmak.clientApp.name}")
    private String applicationName;

    private final ArticleService postService;

    private final ArticleRepository postRepository;

    public ArticleResource(ArticleService postService, ArticleRepository postRepository) {
        this.postService = postService;
        this.postRepository = postRepository;
    }

    /**
     * {@code POST  /posts} : Create a new post.
     *
     * @param post the post to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with
     *         body the new post, or with status {@code 400 (Bad Request)} if the
     *         post has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<NkArticle> createArticle(@RequestPart NkArticle post,
            @RequestPart List<NkAttachment> attachments,
            @RequestPart(required = false) List<MultipartFile> files,
            @RequestPart(required = false) List<MultipartFile> posters)
            throws URISyntaxException {
        log.debug("REST request to save NkArticle : {}", post);
        if (post.getId() != null) {
            throw new BadRequestAlertException("A new post cannot already have an ID", ENTITY_NAME, "idexists");
        }
        post = postService.save(post, attachments, files, posters);
        return ResponseEntity.created(new URI("/truthline-ingres/posts/" + post.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, ENTITY_NAME,
                        post.getId().toString()))
                .body(post);
    }

    /**
     * {@code PUT  /posts/:id} : Updates an existing post.
     *
     * @param id   the id of the post to save.
     * @param post the post to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated post,
     *         or with status {@code 400 (Bad Request)} if the post is not valid,
     *         or with status {@code 500 (Internal Server Error)} if the post
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     * @throws IOException
     */
    @PutMapping("")
    public ResponseEntity<NkArticle> updateArticle(
            @RequestPart NkArticle post,
            @RequestPart List<NkAttachment> attachments,
            @RequestPart(required = false) List<NkAttachment> deletedAttachments,
            @RequestPart(required = false) List<MultipartFile> files,
            @RequestPart(required = false) List<MultipartFile> posters)
            throws URISyntaxException, IOException {
        log.debug("REST request to update NkArticle : {}", post);
        post = postService.update(post, attachments, deletedAttachments, files, posters);
        return ResponseEntity.ok()
                .headers(
                        HeaderUtil.createEntityUpdateAlert(applicationName, ENTITY_NAME, post.getId().toString()))
                .body(post);
    }

    /**
     * {@code PATCH  /posts/:id} : Partial updates given fields of an existing post,
     * field will ignore if it is null
     *
     * @param id   the id of the post to save.
     * @param post the post to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated post,
     *         or with status {@code 400 (Bad Request)} if the post is not valid,
     *         or with status {@code 404 (Not Found)} if the post is not found,
     *         or with status {@code 500 (Internal Server Error)} if the post
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<NkArticle> partialUpdateArticle(
            @PathVariable(value = "id", required = false) final Long id,
            @NotNull @RequestBody NkArticle post) throws URISyntaxException {
        log.debug("REST request to partial update NkArticle partially : {}, {}", id, post);
        if (post.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, post.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!postRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<NkArticle> result = postService.partialUpdate(post);

        return ResponseUtil.wrapOrNotFound(
                result,
                HeaderUtil.createEntityUpdateAlert(applicationName, ENTITY_NAME, post.getId().toString()));
    }

    /**
     * {@code GET  /posts?q=} : get all the posts.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of posts in body.
     */
    @GetMapping("")
    public ResponseEntity<PageDTO<NkArticle>> getAllArticles(@RequestParam(value = "q", defaultValue = "") String query,
            Pageable pageable) {
        log.debug("REST request to get a page of Articles : {}", query);
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
                .body(postService.findAll(query, pageable));
    }

    /**
     * {@code GET  /posts/nk-account/:id} : get all the posts.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of posts in body.
     */
    @GetMapping("/nk-account/{id}")
    public ResponseEntity<PageDTO<NkArticle>> getArticleByAccount(@PathVariable("id") Long id, Pageable pageable) {
        log.debug("REST request to get a page of Articles by NkAccount : {}", id);
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
                .body(new PageDTO<NkArticle>(postRepository.findByAccount(new NkAccount().id(id), pageable)));
    }

    // /**
    // * {@code GET /posts/search?q=} : search posts that match the query.
    // *
    // * @param pageable the pagination information.
    // * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the
    // list
    // * of posts in body.
    // */
    // @GetMapping("/search")
    // public ResponseEntity<PageDTO<NkArticle>> fullTextSearch(@RequestParam("q")
    // String
    // query,
    // @ParameterObject Pageable pageable) {
    // log.debug("REST request to search NkArticle : {}", query);
    // return ResponseEntity.ok().cacheControl(CacheControl.maxAge(60,
    // TimeUnit.SECONDS))
    // .body(postService.fullTextSearch(query, pageable));
    // }

    /**
     * {@code GET  /posts/:id} : get the "id" post.
     *
     * @param id the id of the post to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the post, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<NkArticle> getArticle(@PathVariable("id") Long id) {
        log.debug("REST request to get NkArticle : {}", id);
        Optional<NkArticle> post = postService.findOne(id);
        return ResponseUtil.wrapOrNotFound(post);
    }

    /**
     * {@code DELETE  /posts/:id} : delete the "id" post.
     *
     * @param id the id of the post to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable("id") Long id) {
        log.debug("REST request to delete NkArticle : {}", id);
        postService.delete(id);
        return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, ENTITY_NAME, id.toString()))
                .build();
    }
}