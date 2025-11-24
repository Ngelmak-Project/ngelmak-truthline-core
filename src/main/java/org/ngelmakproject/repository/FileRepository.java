package org.ngelmakproject.repository;

import org.ngelmakproject.domain.NkFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the NkFile entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FileRepository extends JpaRepository<NkFile, Long> {
}
