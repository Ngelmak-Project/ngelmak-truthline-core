package org.ngelmakproject.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.ngelmakproject.domain.NkTicket;
import org.ngelmakproject.repository.TicketRepository;
import org.ngelmakproject.service.TicketService;
import org.ngelmakproject.web.rest.errors.BadRequestAlertException;
import org.ngelmakproject.web.rest.util.HeaderUtil;
import org.ngelmakproject.web.rest.util.PaginationUtil;
import org.ngelmakproject.web.rest.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * REST controller for managing {@link org.ngelmakproject.domain.NkTicket}.
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketResource {

    private static final Logger log = LoggerFactory.getLogger(TicketResource.class);

    private static final String ENTITY_NAME = "ticket";

    @Value("${spring.application.name}")
    private String applicationName;

    private final TicketService ticketService;

    private final TicketRepository ticketRepository;

    public TicketResource(TicketService ticketService, TicketRepository ticketRepository) {
        this.ticketService = ticketService;
        this.ticketRepository = ticketRepository;
    }

    /**
     * {@code POST  /tickets} : Create a new ticket.
     *
     * @param ticket the ticket to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with
     *         body the new ticket, or with status {@code 400 (Bad Request)} if the
     *         ticket has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<NkTicket> createTicket(@Valid @RequestBody NkTicket ticket) throws URISyntaxException {
        log.debug("REST request to save NkTicket : {}", ticket);
        if (ticket.getId() != null) {
            throw new BadRequestAlertException("A new ticket cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ticket = ticketService.save(ticket);
        return ResponseEntity.created(new URI("/api/tickets/" + ticket.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, ENTITY_NAME, ticket.getId().toString()))
                .body(ticket);
    }

    /**
     * {@code PUT  /tickets/:id} : Updates an existing ticket.
     *
     * @param id     the id of the ticket to save.
     * @param ticket the ticket to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated ticket,
     *         or with status {@code 400 (Bad Request)} if the ticket is not valid,
     *         or with status {@code 500 (Internal Server Error)} if the ticket
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<NkTicket> updateTicket(
            @PathVariable(value = "id", required = false) final Long id,
            @Valid @RequestBody NkTicket ticket) throws URISyntaxException {
        log.debug("REST request to update NkTicket : {}, {}", id, ticket);
        if (ticket.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, ticket.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!ticketRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        ticket = ticketService.update(ticket);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, ENTITY_NAME, ticket.getId().toString()))
                .body(ticket);
    }

    /**
     * {@code PATCH  /tickets/:id} : Partial updates given fields of an existing
     * ticket, field will ignore if it is null
     *
     * @param id     the id of the ticket to save.
     * @param ticket the ticket to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated ticket,
     *         or with status {@code 400 (Bad Request)} if the ticket is not valid,
     *         or with status {@code 404 (Not Found)} if the ticket is not found,
     *         or with status {@code 500 (Internal Server Error)} if the ticket
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<NkTicket> partialUpdateTicket(
            @PathVariable(value = "id", required = false) final Long id,
            @NotNull @RequestBody NkTicket ticket) throws URISyntaxException {
        log.debug("REST request to partial update NkTicket partially : {}, {}", id, ticket);
        if (ticket.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, ticket.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!ticketRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<NkTicket> result = ticketService.partialUpdate(ticket);

        return ResponseUtil.wrapOrNotFound(
                result,
                HeaderUtil.createEntityUpdateAlert(applicationName, ENTITY_NAME, ticket.getId().toString()));
    }

    /**
     * {@code GET  /tickets} : get all the tickets.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of tickets in body.
     */
    @GetMapping("")
    public ResponseEntity<List<NkTicket>> getAllTickets(Pageable pageable) {
        log.debug("REST request to get a page of Tickets");
        Page<NkTicket> page = ticketService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page,
                ServletUriComponentsBuilder.fromCurrentRequest().toString());
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /tickets/:id} : get the "id" ticket.
     *
     * @param id the id of the ticket to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the ticket, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<NkTicket> getTicket(@PathVariable("id") Long id) {
        log.debug("REST request to get NkTicket : {}", id);
        Optional<NkTicket> ticket = ticketService.findOne(id);
        return ResponseUtil.wrapOrNotFound(ticket);
    }

    /**
     * {@code DELETE  /tickets/:id} : delete the "id" ticket.
     *
     * @param id the id of the ticket to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable("id") Long id) {
        log.debug("REST request to delete NkTicket : {}", id);
        ticketService.delete(id);
        return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, ENTITY_NAME, id.toString()))
                .build();
    }
}
