package org.ngelmakproject.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.ngelmakproject.domain.Review;
import org.ngelmakproject.repository.ReviewRepository;
import org.ngelmakproject.service.ReviewService;
import org.ngelmakproject.web.rest.errors.BadRequestAlertException;
import org.ngelmakproject.web.rest.util.HeaderUtil;
import org.ngelmakproject.web.rest.util.PaginationUtil;
import org.ngelmakproject.web.rest.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * REST controller for managing {@link org.ngelmakproject.domain.Review}.
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewResource {

    private static final Logger log = LoggerFactory.getLogger(ReviewResource.class);

    private static final String ENTITY_NAME = "review";

    @Value("${spring.application.name}")
    private String applicationName;

    private final ReviewService reviewService;

    private final ReviewRepository reviewRepository;

    public ReviewResource(ReviewService reviewService, ReviewRepository reviewRepository) {
        this.reviewService = reviewService;
        this.reviewRepository = reviewRepository;
    }

    /**
     * {@code POST  /reviews} : Create a new review.
     *
     * @param review the review to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with
     *         body the new review, or with status {@code 400 (Bad Request)} if the
     *         review has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Review> createReview(@Valid @RequestBody Review review) throws URISyntaxException {
        log.debug("REST request to save Review : {}", review);
        if (review.getId() != null) {
            throw new BadRequestAlertException("A new review cannot already have an ID", ENTITY_NAME, "idexists");
        }
        review = reviewService.save(review);
        return ResponseEntity.created(new URI("/api/reviews/" + review.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, ENTITY_NAME, review.getId().toString()))
                .body(review);
    }

    /**
     * {@code PUT  /reviews/:id} : Updates an existing review.
     *
     * @param id     the id of the review to save.
     * @param review the review to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated review,
     *         or with status {@code 400 (Bad Request)} if the review is not valid,
     *         or with status {@code 500 (Internal Server Error)} if the review
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(
            @PathVariable(value = "id", required = false) final Long id,
            @Valid @RequestBody Review review) throws URISyntaxException {
        log.debug("REST request to update Review : {}, {}", id, review);
        if (review.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, review.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!reviewRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        review = reviewService.update(review);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, ENTITY_NAME, review.getId().toString()))
                .body(review);
    }

    /**
     * {@code PATCH  /reviews/:id} : Partial updates given fields of an existing
     * review, field will ignore if it is null
     *
     * @param id     the id of the review to save.
     * @param review the review to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated review,
     *         or with status {@code 400 (Bad Request)} if the review is not valid,
     *         or with status {@code 404 (Not Found)} if the review is not found,
     *         or with status {@code 500 (Internal Server Error)} if the review
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Review> partialUpdateReview(
            @PathVariable(value = "id", required = false) final Long id,
            @NotNull @RequestBody Review review) throws URISyntaxException {
        log.debug("REST request to partial update Review partially : {}, {}", id, review);
        if (review.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, review.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!reviewRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Review> result = reviewService.partialUpdate(review);

        return ResponseUtil.wrapOrNotFound(
                result,
                HeaderUtil.createEntityUpdateAlert(applicationName, ENTITY_NAME, review.getId().toString()));
    }

    /**
     * {@code GET  /reviews} : get all the reviews.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of reviews in body.
     */
    @GetMapping("")
    public ResponseEntity<List<Review>> getAllReviews(Pageable pageable) {
        log.debug("REST request to get a page of Reviews");
        Page<Review> page = reviewService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page,
                ServletUriComponentsBuilder.fromCurrentRequest().toString());
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /reviews/:id} : get the "id" review.
     *
     * @param id the id of the review to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the review, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReview(@PathVariable("id") Long id) {
        log.debug("REST request to get Review : {}", id);
        Optional<Review> review = reviewService.findOne(id);
        return ResponseUtil.wrapOrNotFound(review);
    }

    /**
     * {@code DELETE  /reviews/:id} : delete the "id" review.
     *
     * @param id the id of the review to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable("id") Long id) {
        log.debug("REST request to delete Review : {}", id);
        reviewService.delete(id);
        return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, ENTITY_NAME, id.toString()))
                .build();
    }
}
