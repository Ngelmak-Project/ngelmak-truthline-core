package org.ngelmakproject.repository;

import java.util.List;

import org.ngelmakproject.domain.NkComment;
import org.ngelmakproject.domain.NkPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the NkComment entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CommentRepository extends JpaRepository<NkComment, Long> {
  List<NkComment> findByPost(NkPost post);

  // @Query("""
  //   select new NkComment(
  //     c.id,
  //     c.opinion,
  //     c.at,
  //     c.lastUpdate,
  //     c.deletedAt,
  //     c.content,
  //     c.url,
  //     c.post,
  //     c.replayto,
  //     c.account
  //   )
  //   from NkComment c
  //   where c.id = :id
  //   """)
  // Optional<NkComment> findById(@Param("id") Long id);

  // @Modifying
  // @Query("update NkComment c set c.content = :content and c.url = :url where u.id < :id")
  // void update(@Param("id") Long id, @Param("content") String content, @Param("url") String url);
}
