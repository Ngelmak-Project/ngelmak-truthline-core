package org.ngelmakproject.web.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.ngelmakproject.domain.NkAccount;
import org.ngelmakproject.domain.NkFile;
import org.ngelmakproject.domain.NkPost;
import org.ngelmakproject.repository.PostRepository;
import org.ngelmakproject.service.PostService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for managing {@link org.ngelmakproject.domain.NkPost}.
 */
@RestController
@RequestMapping("/api/posts")
public class PostResource {

    private static final Logger log = LoggerFactory.getLogger(PostResource.class);

    private static final String ENTITY_NAME = "post";

    @Value("${spring.application.name}")
    private String applicationName;

    private final PostService postService;

    private final PostRepository postRepository;

    public PostResource(PostService postService, PostRepository postRepository) {
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
    public ResponseEntity<NkPost> createPost(@RequestPart NkPost post,
            @RequestPart(required = false) Optional<List<MultipartFile>> _medias,
            @RequestPart(required = false) Optional<List<MultipartFile>> _covers)
            throws URISyntaxException {
        List<MultipartFile> medias = _medias.orElse(List.of());
        List<MultipartFile> covers = _covers.orElse(List.of());
        log.info("REST request to save Post : {} + {}x media(s) and {}x cover(s)", post, medias.size(), covers.size());
        if (post.getId() != null) {
            throw new BadRequestAlertException("A new post cannot already have an ID", ENTITY_NAME, "idexists");
        }
        post = postService.save(post, medias, covers);
        return ResponseEntity.created(new URI("/api/posts/" + post.getId()))
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
    public ResponseEntity<NkPost> updatePost(
            @RequestPart NkPost post,
            @RequestPart(required = false) Optional<List<NkFile>> _deletedFiles,
            @RequestPart(required = false) Optional<List<MultipartFile>> _medias,
            @RequestPart(required = false) Optional<List<MultipartFile>> _covers)
            throws URISyntaxException, IOException {
        List<NkFile> deletedFiles = _deletedFiles.orElse(List.of());
        List<MultipartFile> medias = _medias.orElse(List.of());
        List<MultipartFile> covers = _covers.orElse(List.of());
        log.info("REST request to save Post : {} | {}x media(s), {}x cover(s), and {}x to be deleted", post,
                medias.size(), covers.size(), deletedFiles.size());
        if (post.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        post = postService.update(post, deletedFiles, medias, covers);
        return ResponseEntity.ok()
                .headers(
                        HeaderUtil.createEntityUpdateAlert(applicationName, ENTITY_NAME, post.getId().toString()))
                .body(post);
    }

    /**
     * {@code GET  /posts?q=} : get all the posts.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of posts in body.
     */
    @GetMapping("")
    public ResponseEntity<PageDTO<NkPost>> getAllPosts(@RequestParam(value = "q", defaultValue = "") String query,
            Pageable pageable) {
        log.debug("REST request to get a page of Posts : {}", query);
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
                .body(postService.findAll(query, pageable));
    }

    /**
     * {@code GET  /posts/account/:id} : get all the posts.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of posts in body.
     */
    @GetMapping("/account/{id}")
    public ResponseEntity<PageDTO<NkPost>> getPostByAccount(@PathVariable("id") Long id, Pageable pageable) {
        log.debug("REST request to get a page of Posts by NkAccount : {}", id);
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
                .body(new PageDTO<NkPost>(postRepository.findByAccount(new NkAccount().id(id), pageable)));
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
    // public ResponseEntity<PageDTO<NkPost>> fullTextSearch(@RequestParam("q")
    // String
    // query,
    // Pageable pageable) {
    // log.debug("REST request to search Post : {}", query);
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
    public ResponseEntity<NkPost> getPost(@PathVariable("id") Long id) {
        log.debug("REST request to get Post : {}", id);
        Optional<NkPost> post = postService.findOne(id);
        return ResponseUtil.wrapOrNotFound(post);
    }

    /**
     * {@code DELETE  /posts/:id} : delete the "id" post.
     *
     * @param id the id of the post to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable("id") Long id) {
        log.debug("REST request to delete Post : {}", id);
        postService.delete(id);
        return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, ENTITY_NAME, id.toString()))
                .build();
    }
}