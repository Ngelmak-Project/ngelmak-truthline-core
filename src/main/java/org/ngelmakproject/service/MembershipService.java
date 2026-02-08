package org.ngelmakproject.service;

import java.util.Optional;

import org.ngelmakproject.domain.Membership;
import org.ngelmakproject.repository.MembershipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link org.ngelmakproject.domain.Membership}.
 */
@Service
@Transactional
public class MembershipService {

    private static final Logger log = LoggerFactory.getLogger(MembershipService.class);

    private final MembershipRepository membershipRepository;

    public MembershipService(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    /**
     * Save a membership.
     *
     * @param membership the entity to save.
     * @return the persisted entity.
     */
    public Membership save(Membership membership) {
        log.debug("Request to save Membership : {}", membership);
        return membershipRepository.save(membership);
    }

    /**
     * Update a membership.
     *
     * @param membership the entity to save.
     * @return the persisted entity.
     */
    public Membership update(Membership membership) {
        log.debug("Request to update Membership : {}", membership);
        return membershipRepository.save(membership);
    }

    /**
     * Partially update a membership.
     *
     * @param membership the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<Membership> partialUpdate(Membership membership) {
        log.debug("Request to partially update Membership : {}", membership);

        return membershipRepository
            .findById(membership.getId())
            .map(existingMembership -> {
                if (membership.getAt() != null) {
                    existingMembership.setAt(membership.getAt());
                }
                if (membership.getActivateNotification() != null) {
                    existingMembership.setActivateNotification(membership.getActivateNotification());
                }

                return existingMembership;
            })
            .map(membershipRepository::save);
    }

    /**
     * Get all the memberships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Membership> findAll(Pageable pageable) {
        log.debug("Request to get all Memberships");
        return membershipRepository.findAll(pageable);
    }

    /**
     * Get one membership by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Membership> findOne(Long id) {
        log.debug("Request to get Membership : {}", id);
        return membershipRepository.findById(id);
    }

    /**
     * Delete the membership by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Membership : {}", id);
        membershipRepository.deleteById(id);
    }
}
