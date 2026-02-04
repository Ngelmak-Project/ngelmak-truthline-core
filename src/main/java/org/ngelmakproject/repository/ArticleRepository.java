package org.ngelmakproject.repository;

import java.util.List;
import java.util.Optional;

import org.ngelmakproject.domain.NkAccount;
import org.ngelmakproject.domain.NkArticle;
import org.ngelmakproject.domain.enumeration.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.Tuple;

/**
 * Spring Data JPA repository for the NkArticle entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ArticleRepository extends JpaRepository<NkArticle, Long> {
  // @Query(value = "SELECT " +
  // " full_search.*, " +
  // " p.id AS article_reference_id, " +
  // " p.title AS article_reference_title, " +
  // " p.content AS article_reference_content, " +
  // " a.name AS account_name " +
  // "FROM ( " +
  // " SELECT p.* FROM ( " +
  // " SELECT *, ts_rank_cd(textsearchable_index_col, query) AS rank " +
  // " FROM nk_article, to_tsquery('french', :fullText) query " +
  // " WHERE status = 'VALIDATED' AND textsearchable_index_col @@ query " +
  // " ) AS p " +
  // " LEFT JOIN (SELECT id, ts_rank_cd(textsearchable_index_col, query) AS rank "
  // +
  // " FROM nk_article, to_tsquery('french', :fullText) query " +
  // " WHERE textsearchable_index_col @@ query) AS a " +
  // " ON p.account_id = a.id " +
  // " ORDER BY a.rank,p.rank DESC " +
  // " LIMIT :limit " +
  // " OFFSET :offset " +
  // ") AS full_search " +
  // "LEFT JOIN nk_article AS p ON full_search.article_reference_id = p.id " +
  // "LEFT JOIN nk_account AS a ON a.id = p.account_id", nativeQuery = true)
  // List<Tuple> fullTextSearch(@Param("fullText") String fullText,
  // @Param("limit") Integer limit,
  // @Param("offset") Long offset);
  // JOIN FETCH article.comments comments JOIN FETCH article.attachments attachments

  // @Query("SELECT article FROM NkArticle article " +
  //     "LEFT JOIN FETCH article.account account " +
  //     "LEFT JOIN FETCH article.articleReply articleReply " +
  //     "LEFT JOIN FETCH article.attachments attachments " +
  //     "WHERE article.id = ?1")
  Optional<NkArticle> findById(Long id);

  @Query("SELECT a FROM NkArticle a WHERE a.account.id = :accountId")
  Slice<NkArticle> findByAccount(@Param("accountId") Long accountId, Pageable pageable);

  Slice<NkArticle> findByAccount(NkAccount account, Pageable pageable);

  Slice<NkArticle> findByStatusOrderByAtDesc(Status status, Pageable pageable);
}
