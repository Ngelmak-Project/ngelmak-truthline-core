package org.ngelmakproject.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.ngelmakproject.domain.NkAccount;
import org.ngelmakproject.repository.NkAccountRepository;
import org.ngelmakproject.service.AccountService;
import org.ngelmakproject.web.rest.dto.AccountDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * REST controller for managing
 * {@link org.ngelmakproject.domain.NkAccount}.
 */
@RestController
@RequestMapping("/truthline-ingres/accounts")
public class NkAccountResource {

    @ResponseStatus(HttpStatus.NOT_FOUND) // Or @ResponseStatus(HttpStatus.NO_CONTENT)
    private static class AccountResourceException extends RuntimeException {
        private AccountResourceException(String message) {
            super(message);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(NkAccountResource.class);

    private static final String ENTITY_NAME = "nkAccount";

    @Value("${ngelmak.clientApp.name}")
    private String applicationName;

    private final AccountService nkAccountService;

    private final NkAccountRepository nkAccountRepository;

    public NkAccountResource(AccountService nkAccountService, NkAccountRepository nkAccountRepository) {
        this.nkAccountService = nkAccountService;
        this.nkAccountRepository = nkAccountRepository;
    }

    /**
     * {@code POST  /accounts} : Create a new nkAccount.
     *
     * @param nkAccount the nkAccount to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with
     *         body the new nkAccount, or with status {@code 400 (Bad Request)} if
     *         the nkAccount has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<NkAccount> createNkAccount(@Valid @RequestBody AccountDTO nkAccountDTO)
            throws URISyntaxException {
        log.debug("REST request to save NkAccount : {}", nkAccountDTO);
        if (nkAccountDTO.getId() != null) {
            throw new BadRequestAlertException("A new nkAccount cannot already have an ID", ENTITY_NAME, "idexists");
        }
        NkAccount nkAccount = nkAccountService.save(nkAccountDTO);
        return ResponseEntity.created(new URI("/truthline-ingres/accounts/" + nkAccount.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, ENTITY_NAME,
                        nkAccount.getId().toString()))
                .body(nkAccount);
    }

    /**
     * {@code PUT  /accounts/:id} : Updates an existing nkAccount.
     *
     * @param id        the id of the nkAccount to save.
     * @param nkAccount the nkAccount to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated nkAccount,
     *         or with status {@code 400 (Bad Request)} if the nkAccount is not
     *         valid,
     *         or with status {@code 500 (Internal Server Error)} if the nkAccount
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("")
    public ResponseEntity<NkAccount> updateNkAccount(
            @Valid @RequestBody NkAccount nkAccount) throws URISyntaxException {
        log.debug("REST request to update NkAccount : {}", nkAccount);
        if (nkAccount.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        nkAccount = nkAccountService.update(nkAccount);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, ENTITY_NAME,
                        nkAccount.getId().toString()))
                .body(nkAccount);
    }

    /**
     * {@code PATCH  /accounts/:id} : Partial updates given fields of an existing
     * nkAccount, field will ignore if it is null
     *
     * @param id        the id of the nkAccount to save.
     * @param nkAccount the nkAccount to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated nkAccount,
     *         or with status {@code 400 (Bad Request)} if the nkAccount is not
     *         valid,
     *         or with status {@code 404 (Not Found)} if the nkAccount is not found,
     *         or with status {@code 500 (Internal Server Error)} if the nkAccount
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<NkAccount> partialUpdateNkAccount(
            @PathVariable(value = "id", required = false) final Long id,
            @NotNull @RequestBody NkAccount nkAccount) throws URISyntaxException {
        log.debug("REST request to partial update NkAccount partially : {}, {}", id, nkAccount);
        if (nkAccount.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, nkAccount.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!nkAccountRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<NkAccount> result = nkAccountService.partialUpdate(nkAccount);

        return ResponseUtil.wrapOrNotFound(
                result,
                HeaderUtil.createEntityUpdateAlert(applicationName, ENTITY_NAME, nkAccount.getId().toString()));
    }

    /**
     * {@code GET  /accounts} : get all the nkAccounts.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of nkAccounts in body.
     */
    @GetMapping("")
    public ResponseEntity<List<NkAccount>> getAllNkAccounts(Pageable pageable) {
        log.debug("REST request to get a page of NkAccounts");
        Page<NkAccount> page = nkAccountService.findAll(pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(page, ServletUriComponentsBuilder.fromCurrentRequest().toString());
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /accounts/:id} : get the "id" nkAccount.
     *
     * @param id the id of the nkAccount to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the nkAccount, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<NkAccount> getNkAccount(@PathVariable("id") Long id) {
        log.debug("REST request to get NkAccount : {}", id);
        Optional<NkAccount> nkAccount = nkAccountService.findOne(id);
        return ResponseUtil.wrapOrNotFound(nkAccount);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<NkAccount> findByUser(@PathVariable("id") Long id) {
        log.debug("REST request to get NkAccount by user id : {}", id);
        Optional<NkAccount> nkAccount = nkAccountRepository.findOneByUser(id);
        return ResponseUtil.wrapOrNotFound(nkAccount);
    }

    @GetMapping("/authicated-user")
    public ResponseEntity<NkAccount> findByCurrentUser() {
        log.debug("REST request to get NkAccount by current user");
        Optional<NkAccount> nkAccount = nkAccountService.findOneByCurrentUser();
        return ResponseUtil.wrapOrNotFound(nkAccount);
    }

    /**
     * {@code DELETE  /accounts/:id} : delete the "id" nkAccount.
     *
     * @param id the id of the nkAccount to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNkAccount(@PathVariable("id") Long id) {
        log.debug("REST request to delete NkAccount : {}", id);
        nkAccountService.delete(id);
        return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, ENTITY_NAME, id.toString()))
                .build();
    }

    /**
     * {@code PUT   /account/upload-avatar} : Upload an avatar image for the current
     * user account.
     * 
     * @param file
     * @return the current user.
     */
    @PutMapping("/upload-avatar")
    // @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<NkAccount> updateAvatar(@RequestParam("file") MultipartFile file) {
        log.debug("REST request to upload the user's account avatar");
        return ResponseEntity.ok().body(nkAccountService.updateAvatar(file));
    }

    /**
     * {@code PUT   /account/upload-banner} : Upload an banner image for the current
     * user account.
     * 
     * @param file
     * @return the current user.
     */
    @PutMapping("/upload-banner")
    // @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<NkAccount> updateBanner(@RequestParam("file") MultipartFile file) {
        log.debug("REST request to upload the user's account banner");
        return ResponseEntity.ok().body(nkAccountService.updateBanner(file));
    }
}
