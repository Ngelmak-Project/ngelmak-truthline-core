package org.ngelmakproject.repository;

import org.ngelmakproject.domain.NkReview;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the NkReview entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ReviewRepository extends JpaRepository<NkReview, Long> {}
