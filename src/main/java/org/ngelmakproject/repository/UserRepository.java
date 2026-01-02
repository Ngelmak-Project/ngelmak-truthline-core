package org.ngelmakproject.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.ngelmakproject.domain.NkUser;
import org.ngelmakproject.domain.enumeration.CertificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link NkUser} entity.
 */
@Repository
public interface UserRepository extends JpaRepository<NkUser, Long> {
    String USERS_BY_LOGIN_CACHE = "usersByLogin";

    String USERS_BY_EMAIL_CACHE = "usersByEmail";

    Optional<NkUser> findOneByActivationKey(String activationKey);

    List<NkUser> findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant dateTime);

    Optional<NkUser> findOneByResetKey(String resetKey);

    Optional<NkUser> findOneByEmailIgnoreCase(String email);

    Optional<NkUser> findOneByLogin(String login);

    Optional<NkUser> findOneByDocId(String docId);

    Optional<NkUser> findOneByDocIdAndCertificationStatusIn(String docId, CertificationStatus[] status);

    Optional<NkUser> findOneByLoginAndCertificationStatus(String docId, CertificationStatus status);

    @EntityGraph(attributePaths = "authorities")
    Optional<NkUser> findOneWithAuthoritiesByLogin(String login);

    @EntityGraph(attributePaths = "authorities")
    Optional<NkUser> findOneWithAuthoritiesByEmailIgnoreCase(String email);

    Page<NkUser> findAllByIdNotNullAndActivatedIsTrue(Pageable pageable);
}
