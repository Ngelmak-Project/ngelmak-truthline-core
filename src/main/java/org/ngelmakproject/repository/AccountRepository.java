package org.ngelmakproject.repository;

import java.util.Optional;

import org.ngelmakproject.domain.Account;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Account entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findOneByUser(Long id);

    Boolean existsByIdentifier(String identifier);

}
