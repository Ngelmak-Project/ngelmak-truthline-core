package org.ngelmakproject.repository;

import java.util.List;

import org.ngelmakproject.domain.NkComment;
import org.ngelmakproject.domain.NkPost;
import org.ngelmakproject.domain.enumeration.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the NkComment entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CommentRepository extends JpaRepository<NkComment, Long> {
	// @Query("""
	// SELECT c FROM NkComment c
	// LEFT JOIN FETCH c.account
	// LEFT JOIN FETCH c.file
	// WHERE c.post.id = :id
	// AND c.deletedAt IS NULL
	// ORDER BY c.at
	// """)
	// Slice<NkComment> findByPostOrderByAt(@Param("id") Long id, Pageable
	// pageable);

	// @Query("""
	// select new NkComment(
	// c.id,
	// c.opinion,
	// c.at,
	// c.lastUpdate,
	// c.deletedAt,
	// c.content,
	// c.url,
	// c.post,
	// c.replayto,
	// c.account
	// )
	// from NkComment c
	// where c.id = :id
	// """)
	// Optional<NkComment> findById(@Param("id") Long id);

	// @Modifying
	// @Query("update NkComment c set c.content = :content and c.url = :url where
	// u.id < :id")
	// void update(@Param("id") Long id, @Param("content") String content,
	// @Param("url") String url);

	@Query(value = """
			SELECT c.*
			FROM nk_post p
			JOIN LATERAL (
			    SELECT *
			    FROM nk_comment c
			    WHERE c.post_id = p.id
			    ORDER BY c.at DESC
			    LIMIT :limit
			) c ON TRUE
			WHERE p.id IN :postIds
			ORDER BY p.id, c.at DESC
			""", nativeQuery = true)
	List<NkComment> findTopCommentsForPosts(@Param("postIds") List<Long> postIds, @Param("limit") Integer limit);

	@Query("""
			SELECT c FROM NkComment c
			LEFT JOIN FETCH c.post
			LEFT JOIN FETCH c.account
			LEFT JOIN FETCH c.file
			WHERE c.post.id = :postId AND c.replyTo IS NULL AND c.deletedAt IS NULL
			ORDER BY c.at DESC
			""")
	Slice<NkComment> findTopLevelCommentsByPost(@Param("postId") Long postId, Pageable pageable);

	@Query("""
			SELECT c FROM NkComment c
			LEFT JOIN FETCH c.account
			LEFT JOIN FETCH c.file
			WHERE c.replyTo.id = :commentId AND c.deletedAt IS NULL
			ORDER BY c.at ASC
			""")
	List<NkComment> findRepliesByComment(@Param("commentId") Long commentId);

	@Modifying
	@Query("""
			UPDATE NkComment c
			SET c.replyCount = (SELECT COUNT(c.id) FROM NkComment c2
			WHERE c2.replyTo.id = c.id)
			""")
	void updateAllReplyCounts();

	@Modifying
	@Query("""
			  UPDATE NkComment c
			  SET c.replyCount=(SELECT COUNT(c.id) FROM NkComment c
			  WHERE c.replyTo.id = :commentId)
			""")
	void updateReplyCount(@Param("commentId") Long commentId);

	@Modifying
	@Query("""
			UPDATE NkComment c
			SET c.replyCount = GREATEST(0, c.commentCount + :countChange)
			WHERE c.id = :commentId
			""")
	void updateReplyCount(@Param("commentId") Long commentId, @Param("countChange") Integer countChange);
}
