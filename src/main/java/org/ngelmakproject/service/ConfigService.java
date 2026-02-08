package org.ngelmakproject.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.ngelmakproject.domain.Config;
import org.ngelmakproject.repository.ConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link org.open.ngelmakproject.domain.Config}.
 */
@Service
@Transactional
public class ConfigService {

    private static final Logger log = LoggerFactory.getLogger(ConfigService.class);

    private final ConfigRepository configRepository;

    public ConfigService(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    /**
     * Save a config.
     *
     * @param config the entity to save.
     * @return the persisted entity.
     */
    public Config save(Config config) {
        log.debug("Request to save Config : {}", config);
        return configRepository.save(config);
    }

    /**
     * Update a config.
     *
     * @param config the entity to save.
     * @return the persisted entity.
     */
    public Config update(Config config) {
        log.debug("Request to update Config : {}", config);
        return configRepository.save(config);
    }

    /**
     * Partially update a config.
     *
     * @param config the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<Config> partialUpdate(Config config) {
        log.debug("Request to partially update Config : {}", config);

        return configRepository
            .findById(config.getId())
            .map(existingConfig -> {
                if (config.getLastUpdate() != null) {
                    existingConfig.setLastUpdate(config.getLastUpdate());
                }
                if (config.getDefaultAccessibility() != null) {
                    existingConfig.setDefaultAccessibility(config.getDefaultAccessibility());
                }
                if (config.getDefaultVisibility() != null) {
                    existingConfig.setDefaultVisibility(config.getDefaultVisibility());
                }

                return existingConfig;
            })
            .map(configRepository::save);
    }

    /**
     * Get all the configs.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Config> findAll(Pageable pageable) {
        log.debug("Request to get all Configs");
        return configRepository.findAll(pageable);
    }

    /**
     *  Get all the configs where Account is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<Config> findAllWhereAccountIsNull() {
        log.debug("Request to get all configs where Account is null");
        return StreamSupport.stream(configRepository.findAll().spliterator(), false)
            .filter(config -> config.getAccount() == null)
            .toList();
    }

    /**
     * Get one config by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Config> findOne(Long id) {
        log.debug("Request to get Config : {}", id);
        return configRepository.findById(id);
    }

    /**
     * Delete the config by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Config : {}", id);
        configRepository.deleteById(id);
    }
}
