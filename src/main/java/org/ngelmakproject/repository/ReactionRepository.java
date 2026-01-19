package org.ngelmakproject.repository;

import java.util.List;
import java.util.Optional;

import org.ngelmakproject.domain.NkReaction;
import org.ngelmakproject.domain.NkReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the NkReaction entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ReactionRepository extends JpaRepository<NkReaction, Long> {
  @Query("SELECT r FROM NkReaction r WHERE r.post.id IN :postIds")
  List<NkReaction> findByPostIds(List<Long> postIds);
}
