package org.ngelmakproject.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.ngelmakproject.domain.NkAccount;
import org.ngelmakproject.repository.AccountRepository;
import org.ngelmakproject.security.UserPrincipal;
import org.ngelmakproject.service.AccountService;
import org.ngelmakproject.web.rest.errors.BadRequestAlertException;
import org.ngelmakproject.web.rest.errors.UnauthorizedResourceAccessException;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/accounts")
public class AccountResource {

    @ResponseStatus(HttpStatus.NOT_FOUND) // Or @ResponseStatus(HttpStatus.NO_CONTENT)
    private static class AccountResourceException extends RuntimeException {
        private AccountResourceException(String message) {
            super(message);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(AccountResource.class);

    private static final String ENTITY_NAME = "account";

    @Value("${spring.application.name}")
    private String applicationName;

    private final AccountService accountService;

    private final AccountRepository accountRepository;

    public AccountResource(AccountService accountService, AccountRepository accountRepository) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
    }

    /**
     * {@code POST  /accounts} : Create a new account.
     *
     * @param account the account to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with
     *         body the new account, or with status {@code 400 (Bad Request)} if
     *         the account has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NkAccount> createAccount(Authentication authentication,
            @Valid @RequestBody NkAccount account)
            throws URISyntaxException {
        log.debug("REST request to save Account : {}", account);
        if (account.getId() != null) {
            throw new BadRequestAlertException("A new account cannot already have an ID", ENTITY_NAME, "idexists");
        }
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        if (account.getUser() != null && account.getUser() != principal.getUserId()) {
            throw new UnauthorizedResourceAccessException(principal.getUserId(), account.getId(), ENTITY_NAME);
        }
        account.setUser(principal.getUserId());
        var newAccount = accountService.save(account);
        return ResponseEntity.created(new URI("/api/accounts/" + newAccount.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, ENTITY_NAME,
                        newAccount.getId().toString()))
                .body(newAccount);
    }

    /**
     * {@code PUT  /accounts/:id} : Updates an existing account.
     *
     * @param id      the id of the account to save.
     * @param account the account to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated account,
     *         or with status {@code 400 (Bad Request)} if the account is not
     *         valid,
     *         or with status {@code 500 (Internal Server Error)} if the account
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NkAccount> updateAccount(@Valid @RequestBody NkAccount account) throws URISyntaxException {
        log.debug("REST request to update Account : {}", account);
        if (account.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        var newAccount = accountService.update(account);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, ENTITY_NAME,
                        newAccount.getId().toString()))
                .body(newAccount);
    }

    /**
     * {@code PATCH  /accounts/:id} : Partial updates given fields of an existing
     * account, field will ignore if it is null
     *
     * @param id      the id of the account to save.
     * @param account the account to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated account,
     *         or with status {@code 400 (Bad Request)} if the account is not
     *         valid,
     *         or with status {@code 404 (Not Found)} if the account is not found,
     *         or with status {@code 500 (Internal Server Error)} if the account
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<NkAccount> partialUpdateAccount(
            @PathVariable(value = "id", required = false) final Long id,
            @NotNull @RequestBody NkAccount account) throws URISyntaxException {
        log.debug("REST request to partial update NkAccount partially : {}, {}", id, account);
        if (account.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, account.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!accountRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<NkAccount> result = accountService.partialUpdate(account);

        return ResponseUtil.wrapOrNotFound(
                result,
                HeaderUtil.createEntityUpdateAlert(applicationName, ENTITY_NAME, account.getId().toString()));
    }

    /**
     * {@code GET  /accounts} : get all the accounts.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of accounts in body.
     */
    @GetMapping("")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<NkAccount>> getAllAccounts(Pageable pageable) {
        log.debug("REST request to get a page of NkAccounts");
        Page<NkAccount> page = accountService.findAll(pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(page, ServletUriComponentsBuilder.fromCurrentRequest().toString());
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /accounts/me} : get the connected user account.
     *
     * @param id the id of the account to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the account, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NkAccount> personalAccount(Authentication authentication) {
        log.debug("REST request to get connected Account");
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("​🦋 User details {}", principal);
        Optional<NkAccount> account = accountService.findOneByCurrentUser();
        return ResponseUtil.wrapOrNotFound(account);
    }

    /**
     * {@code GET  /accounts/:id} : get the "id" account.
     *
     * @param id the id of the account to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the account, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<NkAccount> getAccount(@PathVariable("id") Long id) {
        log.debug("REST request to get Account : {}", id);
        Optional<NkAccount> account = accountService.findOne(id);
        return ResponseUtil.wrapOrNotFound(account);
    }

    /**
     * {@code DELETE  /accounts/:id} : delete the "id" account.
     *
     * @param id the id of the account to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> blockAccount(@PathVariable("id") Long id) {
        log.debug("REST request to delete Account : {}", id);
        accountService.delete(id);
        return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, ENTITY_NAME, id.toString()))
                .build();
    }

    /**
     * {@code DELETE  /accounts/:id} : delete the "id" account.
     *
     * @param id the id of the account to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteAccount(@PathVariable("id") Long id) {
        log.debug("REST request to delete Account : {}", id);
        accountService.delete(id);
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
        return ResponseEntity.ok().body(accountService.updateAvatar(file));
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
        return ResponseEntity.ok().body(accountService.updateBanner(file));
    }
}
