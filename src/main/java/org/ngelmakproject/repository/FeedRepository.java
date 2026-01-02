package org.ngelmakproject.repository;

import java.util.List;

import org.ngelmakproject.domain.NkAccount;
import org.ngelmakproject.domain.NkFeed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the NkFeed entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FeedRepository extends JpaRepository<NkFeed, Long> {
  Page<NkFeed> findByFeedOwner(NkAccount feedOwner, Pageable pageable);
  Page<NkFeed> findByFeedOwnerIn(List<NkAccount> feedOwners, Pageable pageable);
  @Query("SELECT f FROM NkFeed f ORDER BY f.post.at")
  Page<NkFeed> findByOrderByDateDesc(Pageable pageable);
}
