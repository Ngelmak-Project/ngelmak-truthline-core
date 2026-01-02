package org.ngelmakproject.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.ngelmakproject.domain.NkConfig;
import org.ngelmakproject.repository.ConfigRepository;
import org.ngelmakproject.service.ConfigService;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


/**
 * REST controller for managing {@link org.ngelmakproject.domain.NkConfig}.
 */
@RestController
@RequestMapping("/truthline-ingres/configs")
public class ConfigResource {

    private static final Logger log = LoggerFactory.getLogger(ConfigResource.class);

    private static final String ENTITY_NAME = "config";

    @Value("${ngelmak.clientApp.name}")
    private String applicationName;

    private final ConfigService configService;

    private final ConfigRepository configRepository;

    public ConfigResource(ConfigService configService, ConfigRepository configRepository) {
        this.configService = configService;
        this.configRepository = configRepository;
    }

    /**
     * {@code POST  /configs} : Create a new config.
     *
     * @param config the config to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new config, or with status {@code 400 (Bad Request)} if the config has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<NkConfig> createConfig(@RequestBody NkConfig config) throws URISyntaxException {
        log.debug("REST request to save NkConfig : {}", config);
        if (config.getId() != null) {
            throw new BadRequestAlertException("A new config cannot already have an ID", ENTITY_NAME, "idexists");
        }
        config = configService.save(config);
        return ResponseEntity.created(new URI("/truthline-ingres/configs/" + config.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, ENTITY_NAME, config.getId().toString()))
            .body(config);
    }

    /**
     * {@code PUT  /configs/:id} : Updates an existing config.
     *
     * @param id the id of the config to save.
     * @param config the config to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated config,
     * or with status {@code 400 (Bad Request)} if the config is not valid,
     * or with status {@code 500 (Internal Server Error)} if the config couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<NkConfig> updateConfig(@PathVariable(value = "id", required = false) final Long id, @RequestBody NkConfig config)
        throws URISyntaxException {
        log.debug("REST request to update NkConfig : {}, {}", id, config);
        if (config.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, config.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!configRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        config = configService.update(config);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, ENTITY_NAME, config.getId().toString()))
            .body(config);
    }

    /**
     * {@code PATCH  /configs/:id} : Partial updates given fields of an existing config, field will ignore if it is null
     *
     * @param id the id of the config to save.
     * @param config the config to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated config,
     * or with status {@code 400 (Bad Request)} if the config is not valid,
     * or with status {@code 404 (Not Found)} if the config is not found,
     * or with status {@code 500 (Internal Server Error)} if the config couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<NkConfig> partialUpdateConfig(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody NkConfig config
    ) throws URISyntaxException {
        log.debug("REST request to partial update NkConfig partially : {}, {}", id, config);
        if (config.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, config.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!configRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<NkConfig> result = configService.partialUpdate(config);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, ENTITY_NAME, config.getId().toString())
        );
    }

    /**
     * {@code GET  /configs} : get all the configs.
     *
     * @param pageable the pagination information.
     * @param filter the filter of the request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of configs in body.
     */
    @GetMapping("")
    public ResponseEntity<List<NkConfig>> getAllConfigs(Pageable pageable,
        @RequestParam(name = "filter", required = false) String filter
    ) {
        if ("ngelmakaccount-is-null".equals(filter)) {
            log.debug("REST request to get all Configs where nkAccount is null");
            return new ResponseEntity<>(configService.findAllWhereNkAccountIsNull(), HttpStatus.OK);
        }
        log.debug("REST request to get a page of Configs");
        Page<NkConfig> page = configService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, ServletUriComponentsBuilder.fromCurrentRequest().toString());
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /configs/:id} : get the "id" config.
     *
     * @param id the id of the config to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the config, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<NkConfig> getConfig(@PathVariable("id") Long id) {
        log.debug("REST request to get NkConfig : {}", id);
        Optional<NkConfig> config = configService.findOne(id);
        return ResponseUtil.wrapOrNotFound(config);
    }

    /**
     * {@code DELETE  /configs/:id} : delete the "id" config.
     *
     * @param id the id of the config to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConfig(@PathVariable("id") Long id) {
        log.debug("REST request to delete NkConfig : {}", id);
        configService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, ENTITY_NAME, id.toString()))
            .build();
    }
}
