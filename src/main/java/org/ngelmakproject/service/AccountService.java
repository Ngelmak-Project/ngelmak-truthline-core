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
import org.ngelmakproject.web.rest.dto.AccountDTO;
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
        account.setIdentifier(identifier);
        account.setCreatedAt(Instant.now());

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
    public Page<AccountDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Accounts");
        return accountRepository.findAll(pageable).map(AccountDTO::from);
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
     * Updates the avatar image of the current user's account.
     *
     * <p>
     * The method uploads the provided media file, updates the account's avatar URL,
     * and removes the previously stored avatar if one existed.
     * </p>
     *
     * @param media the new avatar file to upload
     * @return the updated {@link NkAccount}
     * @throws AccountNotFoundException if the current user's account cannot be
     *                                  found
     */
    public NkAccount updateAvatar(MultipartFile media) {
        return this.findOneByCurrentUser().map(
            account -> {
                    log.info("Request to update Account avatar : {}", account);
                    String deletedAvatarUrl = account.getAvatar();
                    var file = fileService.save(List.of(media)).get(0);
                    account.setAvatar(file.getUrl());
                    accountRepository.save(account);
                    if (deletedAvatarUrl != null && !deletedAvatarUrl.isEmpty()) {
                        fileService.deleteByUrls(List.of(deletedAvatarUrl));
                    }
                    log.debug("Changed Information for Account: {}", account);
                    return account;
                }).orElseThrow(AccountNotFoundException::new);
    }

    /**
     * Updates the banner image of the current user's account.
     *
     * <p>
     * The method uploads the provided media file, updates the account's banner URL,
     * and removes the previously stored banner if one existed.
     * </p>
     *
     * @param media the new banner file to upload
     * @return the updated {@link NkAccount}
     * @throws AccountNotFoundException if the current user's account cannot be
     *                                  found
     */
    public NkAccount updateBanner(MultipartFile media) {
        log.debug("Request to update Account banner");
        return this.findOneByCurrentUser().map(
                account -> {
                    String deletedBannerUrl = account.getBanner();
                    var file = fileService.save(List.of(media)).get(0);
                    account.setBanner(file.getUrl());
                    accountRepository.save(account);
                    if (deletedBannerUrl != null && !deletedBannerUrl.isEmpty())
                        fileService.deleteByUrls(List.of(deletedBannerUrl));
                    log.debug("Changed information for Account: {}", account);
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
                    membershipRepository.findOneByFollowingAndFollower(targetAccountId, currAccount.getId())
                            .ifPresent(membership -> this.membershipRepository.delete(membership));
                    log.debug("NkMembership is now removed.", currAccount);
                    return currAccount;
                }).orElseThrow(AccountNotFoundException::new);
    }
}
