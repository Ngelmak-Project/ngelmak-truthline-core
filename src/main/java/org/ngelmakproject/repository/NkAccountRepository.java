package org.ngelmakproject.repository;

import java.util.Optional;

import org.ngelmakproject.domain.NkAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the NkAccount entity.
 */
@SuppressWarnings("unused")
@Repository
public interface NkAccountRepository extends JpaRepository<NkAccount, Long> {
    Optional<NkAccount> findOneByUser(Long user_id);
}
