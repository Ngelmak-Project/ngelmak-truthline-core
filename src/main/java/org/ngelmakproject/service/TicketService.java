package org.ngelmakproject.service;

import java.util.Optional;

import org.ngelmakproject.domain.Ticket;
import org.ngelmakproject.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link org.ngelmakproject.domain.Ticket}.
 */
@Service
@Transactional
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * Save a ticket.
     *
     * @param ticket the entity to save.
     * @return the persisted entity.
     */
    public Ticket save(Ticket ticket) {
        log.debug("Request to save Ticket : {}", ticket);
        return ticketRepository.save(ticket);
    }

    /**
     * Update a ticket.
     *
     * @param ticket the entity to save.
     * @return the persisted entity.
     */
    public Ticket update(Ticket ticket) {
        log.debug("Request to update Ticket : {}", ticket);
        return ticketRepository.save(ticket);
    }

    /**
     * Partially update a ticket.
     *
     * @param ticket the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<Ticket> partialUpdate(Ticket ticket) {
        log.debug("Request to partially update Ticket : {}", ticket);

        return ticketRepository
            .findById(ticket.getId())
            .map(existingTicket -> {
                if (ticket.getObject() != null) {
                    existingTicket.setObject(ticket.getObject());
                }
                if (ticket.getType() != null) {
                    existingTicket.setType(ticket.getType());
                }
                if (ticket.getAt() != null) {
                    existingTicket.setAt(ticket.getAt());
                }
                if (ticket.getClosed() != null) {
                    existingTicket.setClosed(ticket.getClosed());
                }
                if (ticket.getContent() != null) {
                    existingTicket.setContent(ticket.getContent());
                }

                return existingTicket;
            })
            .map(ticketRepository::save);
    }

    /**
     * Get all the tickets.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Ticket> findAll(Pageable pageable) {
        log.debug("Request to get all Tickets");
        return ticketRepository.findAll(pageable);
    }

    /**
     * Get one ticket by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Ticket> findOne(Long id) {
        log.debug("Request to get Ticket : {}", id);
        return ticketRepository.findById(id);
    }

    /**
     * Delete the ticket by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Ticket : {}", id);
        ticketRepository.deleteById(id);
    }
}
