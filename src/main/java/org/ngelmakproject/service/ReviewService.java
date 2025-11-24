package org.ngelmakproject.service;

import java.util.Optional;

import org.ngelmakproject.domain.NkReview;
import org.ngelmakproject.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link org.ngelmakproject.domain.NkReview}.
 */
@Service
@Transactional
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    /**
     * Save a review.
     *
     * @param review the entity to save.
     * @return the persisted entity.
     */
    public NkReview save(NkReview review) {
        log.debug("Request to save NkReview : {}", review);
        return reviewRepository.save(review);
    }

    /**
     * Update a review.
     *
     * @param review the entity to save.
     * @return the persisted entity.
     */
    public NkReview update(NkReview review) {
        log.debug("Request to update NkReview : {}", review);
        return reviewRepository.save(review);
    }

    /**
     * Partially update a review.
     *
     * @param review the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<NkReview> partialUpdate(NkReview review) {
        log.debug("Request to partially update NkReview : {}", review);

        return reviewRepository
            .findById(review.getId())
            .map(existingReview -> {
                if (review.getAt() != null) {
                    existingReview.setAt(review.getAt());
                }
                if (review.getStatus() != null) {
                    existingReview.setStatus(review.getStatus());
                }
                if (review.getTimeout() != null) {
                    existingReview.setTimeout(review.getTimeout());
                }

                return existingReview;
            })
            .map(reviewRepository::save);
    }

    /**
     * Get all the reviews.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<NkReview> findAll(Pageable pageable) {
        log.debug("Request to get all Reviews");
        return reviewRepository.findAll(pageable);
    }

    /**
     * Get one review by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<NkReview> findOne(Long id) {
        log.debug("Request to get NkReview : {}", id);
        return reviewRepository.findById(id);
    }

    /**
     * Delete the review by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete NkReview : {}", id);
        reviewRepository.deleteById(id);
    }
}
