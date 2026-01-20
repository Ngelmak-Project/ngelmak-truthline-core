package org.ngelmakproject.repository;

import java.util.List;

import org.ngelmakproject.domain.NkAccount;
import org.ngelmakproject.domain.NkFeed;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the NkFeed entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FeedRepository extends JpaRepository<NkFeed, Long> {
  Slice<NkFeed> findByFeedOwnerIn(List<NkAccount> feedOwners, Pageable pageable);

  @EntityGraph(attributePaths = { "post", "post.account", "post.files" })
  Slice<NkFeed> findByFeedOwner(NkAccount feedOwner, Pageable pageable);
}
