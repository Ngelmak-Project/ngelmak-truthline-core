package org.ngelmakproject.repository;

import java.util.Optional;

import org.ngelmakproject.domain.NkAccount;
import org.ngelmakproject.domain.NkPost;
import org.ngelmakproject.domain.enumeration.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the NkPost entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PostRepository extends JpaRepository<NkPost, Long> {
  // @Query(value = "SELECT " +
  // " full_search.*, " +
  // " p.id AS post_reference_id, " +
  // " p.title AS post_reference_title, " +
  // " p.content AS post_reference_content, " +
  // " a.name AS account_name " +
  // "FROM ( " +
  // " SELECT p.* FROM ( " +
  // " SELECT *, ts_rank_cd(textsearchable_index_col, query) AS rank " +
  // " FROM nk_post, to_tsquery('french', :fullText) query " +
  // " WHERE status = 'VALIDATED' AND textsearchable_index_col @@ query " +
  // " ) AS p " +
  // " LEFT JOIN (SELECT id, ts_rank_cd(textsearchable_index_col, query) AS rank "
  // +
  // " FROM nk_post, to_tsquery('french', :fullText) query " +
  // " WHERE textsearchable_index_col @@ query) AS a " +
  // " ON p.account_id = a.id " +
  // " ORDER BY a.rank,p.rank DESC " +
  // " LIMIT :limit " +
  // " OFFSET :offset " +
  // ") AS full_search " +
  // "LEFT JOIN nk_post AS p ON full_search.post_reference_id = p.id " +
  // "LEFT JOIN nk_account AS a ON a.id = p.account_id", nativeQuery = true)
  // List<Tuple> fullTextSearch(@Param("fullText") String fullText,
  // @Param("limit") Integer limit,
  // @Param("offset") Long offset);
  // JOIN FETCH post.comments comments JOIN FETCH post.attachments attachments

  // @Query("SELECT post FROM NkPost post " +
  // "LEFT JOIN FETCH post.account account " +
  // "LEFT JOIN FETCH post.postReply postReply " +
  // "LEFT JOIN FETCH post.comments comments " +
  // "LEFT JOIN FETCH post.attachments attachments " +
  // "WHERE post.id = ?1")
  Optional<NkPost> findById(Long id);

  Slice<NkPost> findByAccount(NkAccount account, Pageable pageable);

  /**
   * Use an @EntityGraph to fetch account + files in one go:
   * 
   * @param status
   * @param pageable
   * @return
   */
  @EntityGraph(attributePaths = { "account", "files" })
  Slice<NkPost> findByStatusOrderByAtDesc(Status status, Pageable pageable);

  @Query("""
          SELECT p FROM NkPost p
          LEFT JOIN FETCH p.account
          LEFT JOIN FETCH p.files
          WHERE p.status = 'PUBLISHED'
      """)
  Slice<NkPost> findAllWithRelations(Pageable pageable);

  @Modifying
  @Query("""
      UPDATE NkPost p
      SET p.commentCount = (SELECT COUNT(c.id) FROM NkComment c
      WHERE c.post.id = p.id)
      """)
  void updateAllPostCommentCounts();

  @Modifying
  @Query("""
        UPDATE NkPost p
        SET p.commentCount=(SELECT COUNT(c.id) FROM NkComment c
        WHERE c.post.id = :postId)
      """)
  void updatePostCommentCount(@Param("postId") Long postId);

  @Modifying
  @Query("""
      UPDATE NkPost p
      SET p.commentCount = GREATEST(0, p.commentCount + :countChange)
      WHERE p.id = :postId
      """)
  void updatePostCommentCount(@Param("postId") Long postId, @Param("countChange") Integer countChange);

}
