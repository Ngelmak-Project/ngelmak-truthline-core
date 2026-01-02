package org.ngelmakproject.repository;

import org.ngelmakproject.domain.NkAuthority;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the NkAuthority entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AuthorityRepository extends JpaRepository<NkAuthority, String> {}
