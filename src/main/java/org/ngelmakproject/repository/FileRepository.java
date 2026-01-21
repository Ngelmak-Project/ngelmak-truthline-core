package org.ngelmakproject.repository;

import java.util.List;
import java.util.Optional;

import org.ngelmakproject.domain.NkFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the NkFile entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FileRepository extends JpaRepository<NkFile, Long> {
  Optional<NkFile> findByHash(String hash);
  List<NkFile> findByHashIn(Iterable<String> hashes);
}
