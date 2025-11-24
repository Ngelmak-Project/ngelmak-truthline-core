package org.ngelmakproject.repository;

import java.util.List;

import org.ngelmakproject.domain.NkArticle;
import org.ngelmakproject.domain.NkAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the NkAttachment entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AttachmentRepository extends JpaRepository<NkAttachment, Long> {
  List<NkAttachment> findByArticle(NkArticle post);
}
