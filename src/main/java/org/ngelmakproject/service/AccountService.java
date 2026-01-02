package org.ngelmakproject.service;

import java.net.URL;
import java.time.Instant;
import java.util.Optional;

import org.ngelmakproject.domain.NkAccount;
import org.ngelmakproject.domain.NkConfig;
import org.ngelmakproject.domain.NkMembership;
import org.ngelmakproject.domain.enumeration.Accessibility;
import org.ngelmakproject.domain.enumeration.Visibility;
import org.ngelmakproject.repository.MembershipRepository;
import org.ngelmakproject.repository.NkAccountRepository;
import org.ngelmakproject.service.storage.FileStorageService;
import org.ngelmakproject.web.rest.dto.AccountDTO;
import org.ngelmakproject.web.rest.errors.AccountNotFoundException;
import org.ngelmakproject.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    private static final String ENTITY_NAME = "ngelmak-account";

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final NkAccountRepository nkAccountRepository;

    @Autowired
    private MembershipRepository membershipRepository;
    @Autowired
    private ConfigService configService;
    @Autowired
    private FileStorageService fileStorageService;

    public AccountService(NkAccountRepository nkAccountRepository) {
        this.nkAccountRepository = nkAccountRepository;
    }

    /**
     * Save a nkAccount.
     *
     * @param nkAccount the entity to save.
     * @return the persisted entity.
     */
    public NkAccount save(AccountDTO nkAccountDTO) {
        log.info("Request to save NkAccount : {}", nkAccountDTO);

        // NkUser currentUser = userService.getUserWithAuthorities()
        //         .orElseThrow(() -> new BadRequestAlertException("A new should always be attach to a user", ENTITY_NAME,
        //                 "userNotFound"));

        NkAccount nkAccount = new NkAccount()
                .createdAt(Instant.now())
                .name(nkAccountDTO.getName())
                .visibility(nkAccountDTO.getVisibility())
                .user(0l); // [TODO]: retrieve the current user id
        NkConfig defaultConfig = new NkConfig();
        defaultConfig.lastUpdate(Instant.now());
        defaultConfig.defaultAccessibility(Accessibility.DEFAULT);
        defaultConfig.defaultVisibility(Visibility.PRIVATE);
        defaultConfig = configService.save(defaultConfig);
        nkAccount.setConfiguration(defaultConfig);
        return nkAccountRepository.save(nkAccount);
    }

    /**
     * Update a nkAccount.
     *
     * @param nkAccount the entity to save.
     * @return the persisted entity.
     */
    public NkAccount update(NkAccount nkAccount) {
        log.debug("Request to update NkAccount : {}", nkAccount);
        if (!nkAccountRepository.existsById(nkAccount.getId())) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }
        return nkAccountRepository.save(nkAccount);
    }

    /**
     * Partially update a nkAccount.
     *
     * @param nkAccount the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<NkAccount> partialUpdate(NkAccount nkAccount) {
        log.debug("Request to partially update NkAccount : {}", nkAccount);

        return this.findOneByCurrentUser()
                .map(existingNkAccount -> {
                    if (nkAccount.getIdentifier() != null) {
                        existingNkAccount.setIdentifier(nkAccount.getIdentifier());
                    }
                    if (nkAccount.getName() != null) {
                        existingNkAccount.setName(nkAccount.getName());
                    }
                    if (nkAccount.getAvatar() != null) {
                        existingNkAccount.setAvatar(nkAccount.getAvatar());
                    }
                    if (nkAccount.getBanner() != null) {
                        existingNkAccount.setBanner(nkAccount.getBanner());
                    }
                    if (nkAccount.getVisibility() != null) {
                        existingNkAccount.setVisibility(nkAccount.getVisibility());
                    }
                    if (nkAccount.getCreatedAt() != null) {
                        existingNkAccount.setCreatedAt(nkAccount.getCreatedAt());
                    }
                    if (nkAccount.getDescription() != null) {
                        existingNkAccount.setDescription(nkAccount.getDescription());
                    }

                    return existingNkAccount;
                })
                .map(nkAccountRepository::save);
    }

    /**
     * Get all the nkAccounts.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<NkAccount> findAll(Pageable pageable) {
        log.debug("Request to get all NkAccounts");
        return nkAccountRepository.findAll(pageable);
    }

    /**
     * Get one nkAccount by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<NkAccount> findOne(Long id) {
        log.debug("Request to get NkAccount : {}", id);
        return nkAccountRepository.findById(id);
    }

    /**
     * Get one nkAccount by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<NkAccount> findOneByCurrentUser() {
        // Optional<NkUser> optional = userService.getUserWithAuthorities();
        // if (optional.isEmpty()) {
        //     return Optional.empty();
        // }
        return nkAccountRepository.findOneByUser(null);
    }

    /**
     * Get one nkAccount by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public NkAccount findByCurrentUser() {
        // NkUser user = userService.getUserWithAuthorities().orElseThrow(UserNotFoundException::new);
        return nkAccountRepository.findOneByUser(null).orElseThrow(AccountNotFoundException::new);
    }

    /**
     * Delete the nkAccount by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete NkAccount : {}", id);
        nkAccountRepository.deleteById(id);
    }

    /**
     * Save or update ngelmak account avatar.
     *
     * @return the updated account.
     */
    public NkAccount updateAvatar(MultipartFile file) {
        log.debug("Request to update NkAccount avatar");
        return this.findOneByCurrentUser().map(
                nkAccount -> {
                    String existingAvatar = nkAccount.getAvatar();
                    String[] dirs = { "public", "avatars", };
                    URL url = fileStorageService.store(file, true, file.getOriginalFilename(), dirs);
                    nkAccount.setAvatar(url.toString());
                    nkAccountRepository.save(nkAccount);
                    if (existingAvatar != null && !existingAvatar.isEmpty())
                        fileStorageService.delete(existingAvatar);
                    log.debug("Changed Information for NkAccount: {}", nkAccount);
                    return nkAccount;
                }).orElseThrow(AccountNotFoundException::new);
    }

    /**
     * Save or update ngelmak account banner.
     *
     * @return the updated account.
     */
    public NkAccount updateBanner(MultipartFile file) {
        log.debug("Request to update NkAccount banner");
        return this.findOneByCurrentUser().map(
                nkAccount -> {
                    String existingBanner = nkAccount.getBanner();
                    String[] dirs = { "public", "banners", };
                    URL url = fileStorageService.store(file, true, file.getOriginalFilename(), dirs);
                    nkAccount.setBanner(url.toString());
                    nkAccountRepository.save(nkAccount);
                    if (existingBanner != null && !existingBanner.isEmpty())
                        fileStorageService.delete(existingBanner);
                    log.debug("Changed information for NkAccount: {}", nkAccount);
                    return nkAccount;
                }).orElseThrow(AccountNotFoundException::new);
    }

    public NkAccount followUser(Long targetAccountId) {
        log.debug("Request to follow an account");
        return this.findOneByCurrentUser().map(
                currAccount -> {
                    NkAccount followed = this.nkAccountRepository.findById(targetAccountId)
                            .orElseThrow(AccountNotFoundException::new);
                    NkMembership membership = new NkMembership().follower(currAccount).following(followed).at(Instant.now());
                    membershipRepository.save(membership);
                    log.debug("A new relationship is created between {} and {}", currAccount, followed);
                    return currAccount;
                }).orElseThrow(AccountNotFoundException::new);
    }

    public NkAccount unfollowUser(Long targetAccountId) {
        log.debug("Request to unfollow an account");
        return this.findOneByCurrentUser().map(
                currAccount -> {
                    NkAccount followed = new NkAccount().id(targetAccountId);
                    membershipRepository.findOneByFollowingAndFollower(followed, currAccount).ifPresent(membership -> this.membershipRepository.delete(membership));
                    log.debug("NkMembership is now removed.", currAccount);
                    return currAccount;
                }).orElseThrow(AccountNotFoundException::new);
    }
}
