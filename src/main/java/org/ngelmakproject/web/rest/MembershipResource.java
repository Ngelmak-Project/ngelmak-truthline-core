package org.ngelmakproject.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.ngelmakproject.domain.NkMembership;
import org.ngelmakproject.repository.MembershipRepository;
import org.ngelmakproject.service.MembershipService;
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
 * REST controller for managing {@link org.ngelmakproject.domain.NkMembership}.
 */
@RestController
@RequestMapping("/truthline-ingres/memberships")
public class MembershipResource {

    private static final Logger log = LoggerFactory.getLogger(MembershipResource.class);

    private static final String ENTITY_NAME = "membership";

    @Value("${ngelmak.clientApp.name}")
    private String applicationName;

    private final MembershipService membershipService;

    private final MembershipRepository membershipRepository;

    public MembershipResource(MembershipService membershipService, MembershipRepository membershipRepository) {
        this.membershipService = membershipService;
        this.membershipRepository = membershipRepository;
    }

    /**
     * {@code POST  /memberships} : Create a new membership.
     *
     * @param membership the membership to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new membership, or with status {@code 400 (Bad Request)} if the membership has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<NkMembership> createMembership(@Valid @RequestBody NkMembership membership) throws URISyntaxException {
        log.debug("REST request to save NkMembership : {}", membership);
        if (membership.getId() != null) {
            throw new BadRequestAlertException("A new membership cannot already have an ID", ENTITY_NAME, "idexists");
        }
        membership = membershipService.save(membership);
        return ResponseEntity.created(new URI("/truthline-ingres/memberships/" + membership.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, ENTITY_NAME, membership.getId().toString()))
            .body(membership);
    }

    /**
     * {@code PUT  /memberships/:id} : Updates an existing membership.
     *
     * @param id the id of the membership to save.
     * @param membership the membership to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated membership,
     * or with status {@code 400 (Bad Request)} if the membership is not valid,
     * or with status {@code 500 (Internal Server Error)} if the membership couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<NkMembership> updateMembership(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody NkMembership membership
    ) throws URISyntaxException {
        log.debug("REST request to update NkMembership : {}, {}", id, membership);
        if (membership.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, membership.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!membershipRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        membership = membershipService.update(membership);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, ENTITY_NAME, membership.getId().toString()))
            .body(membership);
    }

    /**
     * {@code PATCH  /memberships/:id} : Partial updates given fields of an existing membership, field will ignore if it is null
     *
     * @param id the id of the membership to save.
     * @param membership the membership to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated membership,
     * or with status {@code 400 (Bad Request)} if the membership is not valid,
     * or with status {@code 404 (Not Found)} if the membership is not found,
     * or with status {@code 500 (Internal Server Error)} if the membership couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<NkMembership> partialUpdateMembership(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody NkMembership membership
    ) throws URISyntaxException {
        log.debug("REST request to partial update NkMembership partially : {}, {}", id, membership);
        if (membership.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, membership.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!membershipRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<NkMembership> result = membershipService.partialUpdate(membership);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, ENTITY_NAME, membership.getId().toString())
        );
    }

    /**
     * {@code GET  /memberships} : get all the memberships.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of memberships in body.
     */
    @GetMapping("")
    public ResponseEntity<List<NkMembership>> getAllMemberships(Pageable pageable) {
        log.debug("REST request to get a page of Memberships");
        Page<NkMembership> page = membershipService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, ServletUriComponentsBuilder.fromCurrentRequest().toString());
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /memberships/:id} : get the "id" membership.
     *
     * @param id the id of the membership to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the membership, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<NkMembership> getMembership(@PathVariable("id") Long id) {
        log.debug("REST request to get NkMembership : {}", id);
        Optional<NkMembership> membership = membershipService.findOne(id);
        return ResponseUtil.wrapOrNotFound(membership);
    }

    /**
     * {@code DELETE  /memberships/:id} : delete the "id" membership.
     *
     * @param id the id of the membership to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMembership(@PathVariable("id") Long id) {
        log.debug("REST request to delete NkMembership : {}", id);
        membershipService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, ENTITY_NAME, id.toString()))
            .build();
    }
}
