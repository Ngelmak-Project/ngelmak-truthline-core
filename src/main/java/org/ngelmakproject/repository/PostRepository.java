package org.ngelmakproject.repository;

import java.util.List;
import java.util.Optional;

import org.ngelmakproject.domain.NkAccount;
import org.ngelmakproject.domain.NkPost;
import org.ngelmakproject.domain.enumeration.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.Tuple;

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
  //     "LEFT JOIN FETCH post.account account " +
  //     "LEFT JOIN FETCH post.postReply postReply " +
  //     "LEFT JOIN FETCH post.comments comments " +
  //     "LEFT JOIN FETCH post.attachments attachments " +
  //     "WHERE post.id = ?1")
  Optional<NkPost> findById(Long id);

  Page<NkPost> findByAccount(NkAccount account, Pageable pageable);

  Page<NkPost> findByStatusOrderByAtDesc(Status status, Pageable pageable);
}
