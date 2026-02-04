package org.ngelmakproject.repository;

import java.util.List;

import org.ngelmakproject.domain.NkDonation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the NkDonation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DonationRepository extends JpaRepository<NkDonation, Long> {
  List<NkDonation> findTop20ByOrderByCreatedAtDesc();
}
