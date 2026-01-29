package org.ngelmakproject.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.ngelmakproject.domain.NkAccount;
import org.ngelmakproject.domain.NkConfig;
import org.ngelmakproject.domain.NkMembership;
import org.ngelmakproject.domain.enumeration.Accessibility;
import org.ngelmakproject.domain.enumeration.Visibility;
import org.ngelmakproject.repository.AccountRepository;
import org.ngelmakproject.repository.MembershipRepository;
import org.ngelmakproject.security.UserPrincipal;
import org.ngelmakproject.web.rest.errors.AccountNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service Implementation for managing
 * {@link org.ngelmakproject.domain.NkAccount}.
 */
@Service
@Transactional
public class AccountService {

    private static final String ENTITY_NAME = "account";

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final MembershipRepository membershipRepository;
    private final ConfigService configService;
    private final FileService fileService;

    public AccountService(AccountRepository accountRepository,
            MembershipRepository membershipRepository,
            ConfigService configService,
            FileService fileService) {
        this.accountRepository = accountRepository;
        this.membershipRepository = membershipRepository;
        this.configService = configService;
        this.fileService = fileService;
    }

    /**
     * Save a account.
     *
     * @param account the entity to save.
     * @return the persisted entity.
     */
    public NkAccount save(NkAccount account) {
        log.info("Request to save Account : {}", account);
        /* 1. account creation */
        String identifier = account.getName().toLowerCase().trim()
                .replaceAll("[^a-z0-9]+", "-") // replace groups of non-alphanumerics
                .replaceAll("^-|-$", ""); // remove leading/trailing hyphens
        int counter = 1;
        String base = identifier;
        while (accountRepository.existsByIdentifier(identifier)) {
            identifier = base + "-" + counter++;
        }
        account.identifier(identifier).createdAt(Instant.now());

        /* 2. default config for the account */
        NkConfig defaultConfig = new NkConfig();
        defaultConfig.lastUpdate(Instant.now());
        defaultConfig.defaultAccessibility(Accessibility.DEFAULT);
        defaultConfig.defaultVisibility(Visibility.PRIVATE);
        defaultConfig = configService.save(defaultConfig);
        account.setConfiguration(defaultConfig);
        return accountRepository.save(account);
    }

    /**
     * Update a account.
     *
     * @param account the entity to save.
     * @return the persisted entity.
     */
    public NkAccount update(NkAccount account) {
        log.debug("Request to update Account : {}", account);

        return findOneByCurrentUser().map(existingNkAccount -> {
            if (account.getIdentifier() != null) {
                existingNkAccount.setIdentifier(account.getIdentifier());
            }
            if (account.getName() != null) {
                existingNkAccount.setName(account.getName());
            }
            if (account.getAvatar() != null) {
                existingNkAccount.setAvatar(account.getAvatar());
            }
            if (account.getBanner() != null) {
                existingNkAccount.setBanner(account.getBanner());
            }
            if (account.getVisibility() != null) {
                existingNkAccount.setVisibility(account.getVisibility());
            }
            if (account.getCreatedAt() != null) {
                existingNkAccount.setCreatedAt(account.getCreatedAt());
            }
            if (account.getDescription() != null) {
                existingNkAccount.setDescription(account.getDescription());
            }

            return existingNkAccount;
        }).map(accountRepository::save)
                .orElseThrow(AccountNotFoundException::new);
    }

    /**
     * Get all the accounts.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<NkAccount> findAll(Pageable pageable) {
        log.debug("Request to get all Accounts");
        return accountRepository.findAll(pageable);
    }

    /**
     * Get one account by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<NkAccount> findOne(Long id) {
        log.debug("Request to get Account : {}", id);
        return accountRepository.findById(id);
    }

    /**
     * Retrieves the Account associated with the currently authenticated user.
     *
     * <p>
     * This method is designed to be safe even when invoked in contexts where
     * authentication is not guaranteed (e.g., unsecured endpoints). It performs
     * several defensive checks to avoid runtime exceptions such as
     * {@link ClassCastException} or {@link NullPointerException}.
     * </p>
     *
     * @return an {@code Optional<NkAccount>} for the authenticated user, or empty
     *         if
     *         no valid authenticated user is present.
     */
    @Transactional(readOnly = true)
    public Optional<NkAccount> findOneByCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // No authentication available
        if (authentication == null) {
            return Optional.empty();
        }
        // Anonymous or not authenticated
        if (!authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        // Principal is not your expected custom user type
        if (!(principal instanceof UserPrincipal userPrincipal)) {
            return Optional.empty();
        }
        // [TODO] Save the account if exists into cache.
        return accountRepository.findOneByUser(userPrincipal.getUserId());
    }

    /**
     * Delete the account by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Account : {}", id);
        accountRepository.deleteById(id);
    }

    /**
     * Save or update ngelmak account avatar.
     *
     * @return the updated account.
     */
    public NkAccount updateAvatar(MultipartFile media) {
        log.debug("Request to update Account avatar");
        return this.findOneByCurrentUser().map(
                account -> {
                    String existingAvatar = account.getAvatar();
                    var file  = fileService.save(List.of(media)).get(0);
                    account.setAvatar(file.getUrl());
                    accountRepository.save(account);
                    if (existingAvatar != null && !existingAvatar.isEmpty()) {
                        // [TODO] Make sure to delete the older file.
                        // fileService.(existingAvatar);
                    }
                    log.debug("Changed Information for NkAccount: {}", account);
                    return account;
                }).orElseThrow(AccountNotFoundException::new);
    }

    /**
     * Save or update ngelmak account banner.
     *
     * @return the updated account.
     */
    public NkAccount updateBanner(MultipartFile media) {
        log.debug("Request to update Account banner");
        return this.findOneByCurrentUser().map(
                account -> {
                    String existingBanner = account.getBanner();
                    var file = fileService.save(List.of(media)).get(0);
                    account.setBanner(file.getUrl());
                    accountRepository.save(account);
                    if (existingBanner != null && !existingBanner.isEmpty())
                        // [TODO] Make sure to delete the older file.
                        // fileService.delete(existingBanner);
                    log.debug("Changed information for NkAccount: {}", account);
                    return account;
                }).orElseThrow(AccountNotFoundException::new);
    }

    public NkAccount followUser(Long targetAccountId) {
        log.debug("Request to follow an Account");
        return this.findOneByCurrentUser().map(
                currAccount -> {
                    NkAccount followed = this.accountRepository.findById(targetAccountId)
                            .orElseThrow(AccountNotFoundException::new);
                    NkMembership membership = new NkMembership().follower(currAccount).following(followed)
                            .at(Instant.now());
                    membershipRepository.save(membership);
                    log.debug("A new relationship is created between {} and {}", currAccount, followed);
                    return currAccount;
                }).orElseThrow(AccountNotFoundException::new);
    }

    public NkAccount unfollowUser(Long targetAccountId) {
        log.debug("Request to unfollow an Account");
        return this.findOneByCurrentUser().map(
                currAccount -> {
                    NkAccount followed = new NkAccount().id(targetAccountId);
                    membershipRepository.findOneByFollowingAndFollower(followed, currAccount)
                            .ifPresent(membership -> this.membershipRepository.delete(membership));
                    log.debug("NkMembership is now removed.", currAccount);
                    return currAccount;
                }).orElseThrow(AccountNotFoundException::new);
    }
}
