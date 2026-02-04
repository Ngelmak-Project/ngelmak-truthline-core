package org.ngelmakproject.repository;

import java.util.Optional;

import org.ngelmakproject.domain.NkAccount;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the NkAccount entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AccountRepository extends JpaRepository<NkAccount, Long> {
    Optional<NkAccount> findOneByUser(Long id);

    Boolean existsByIdentifier(String identifier);

}
