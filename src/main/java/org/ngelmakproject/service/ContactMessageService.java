package org.ngelmakproject.service;

import org.ngelmakproject.domain.ContactMessage;
import org.ngelmakproject.repository.ContactMessageRepository;
import org.ngelmakproject.web.rest.dto.PageDTO;
import org.ngelmakproject.web.rest.errors.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing
 * {@link org.ngelmakproject.domain.ContactMessage}.
 */
@Service
@Transactional
public class ContactMessageService {

    private static final String ENTITY_NAME = "comment";
    private static final Logger log = LoggerFactory.getLogger(ContactMessageService.class);

    private final ContactMessageRepository membershipRepository;

    public ContactMessageService(ContactMessageRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    /**
     * Save a membership.
     *
     * @param membership the entity to save.
     * @return the persisted entity.
     */
    public ContactMessage save(ContactMessage membership) {
        log.debug("Request to save ContactMessage : {}", membership);
        return membershipRepository.save(membership);
    }

    /**
     * Update a membership.
     *
     * @param membership the entity to save.
     * @return the persisted entity.
     */
    public ContactMessage update(ContactMessage membership) {
        log.debug("Request to update ContactMessage : {}", membership);
        return membershipRepository
                .findById(membership.getId())
                .map(existingContactMessage -> {
                    if (membership.getEmail() != null) {
                        existingContactMessage.setEmail(membership.getEmail());
                    }
                    if (membership.getSubject() != null) {
                        existingContactMessage.setSubject(membership.getSubject());
                    }
                    if (membership.getMessage() != null) {
                        existingContactMessage.setMessage(membership.getMessage());
                    }
                    if (membership.getMessage() != null) {
                        existingContactMessage.setMessage(membership.getMessage());
                    }
                    if (membership.getStatus() != null) {
                        existingContactMessage.setStatus(membership.getStatus());
                    }
                    return existingContactMessage;
                })
                .map(membershipRepository::save)
                .orElseThrow(() -> new ResourceNotFoundException("Entity not found", ENTITY_NAME, "idnotfound"));
    }

    /**
     * Get all the memberships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public PageDTO<ContactMessage> findAllUntreatedContactMessage(Pageable pageable) {
        log.debug("Request to get all ContactMessages");
        var page = membershipRepository.findUnclosedContactMessageOrderByCreatedAt(pageable);
        return PageDTO.from(page);
    }

    /**
     * Delete the membership by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete ContactMessage : {}", id);
        membershipRepository.deleteById(id);
    }
}
