package org.ngelmakproject.service;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.ngelmakproject.domain.NkFile;
import org.ngelmakproject.repository.FileRepository;
import org.ngelmakproject.service.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service Implementation for managing
 * {@link org.ngelmakproject.domain.NkFile}.
 */
@Service
@Transactional
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    @Autowired
    private FileRepository attachmentRepository;
    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Save a file.
     *
     * @param file the entity to save.
     * @return the persisted entity.
     */
    public NkFile save(NkFile file) {
        log.debug("Request to save NkFile : {}", file);
        return attachmentRepository.save(file);
    }

    /**
     * Save a file.
     *
     * @param file the entity to save.
     * @return the persisted entity.
     */
    public List<NkFile> save(List<MultipartFile> medias, List<MultipartFile> covers) {
        log.debug("Request to save {}x file(s) and {}x cover(s)", medias.size(), covers.size());
        if (medias.isEmpty())
        {
            return new ArrayList<>();
        }
        NkFile file, cover;
        List<NkFile> files = new ArrayList<>();
        MultipartFile mediaFile, mediaCover;
        URL url = null;
        String filename = null;
        String[] dirs = { "media", "postfiles" }; // path where to save the file media.
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String format = date.format(formatter);
        for (int i = 0; i < medias.size(); i++) {
            mediaFile = medias.get(i);
            file = new NkFile();
            filename = String.format("Ngelmak-%s-%s", format, StringUtils.capitalize(mediaFile.getOriginalFilename()));
            url = fileStorageService.store(mediaFile, true, filename, dirs);
            file.setSize(mediaFile.getSize());
            file.setUrl(url.toString());
            file.setFilename(filename);
            mediaCover = covers.get(i);
            if (mediaCover != null) {
                cover = new NkFile();
                filename = String.format("Ngelmak-poster-%s-%s", format,
                        StringUtils.capitalize(mediaFile.getOriginalFilename().replaceFirst(".[a-zA-Z0-9]+$", ".png")));
                url = fileStorageService.store(mediaCover, true, filename, dirs);
                cover.setUrl(url.toString());
                cover.setSize(mediaFile.getSize());
                cover.setUrl(url.toString());
                cover.setFilename(filename);
                file.setCover(cover);
            }
            files.add(file);
        }
        return attachmentRepository.saveAll(files);
    }

    /**
     * Get all the files.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<NkFile> findAll(Pageable pageable) {
        log.debug("Request to get all NkFiles");
        return attachmentRepository.findAll(pageable);
    }

    /**
     * Get one file by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<NkFile> findOne(Long id) {
        log.debug("Request to get NkFile : {}", id);
        return attachmentRepository.findById(id);
    }

    /**
     * Get file's resource by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     * @throws IOException
     */
    @Transactional(readOnly = true)
    public Optional<byte[]> getResource(Long id) throws IOException {
        log.debug("Request to get the actual resource of NkFile : {}", id);
        Optional<NkFile> optional = attachmentRepository.findById(id);
        if (optional.isEmpty()) {
            return Optional.empty();
        }
        NkFile file = optional.get();
        Resource resource = fileStorageService.loadAsResource(file.getUrl());
        return Optional.of(resource.getContentAsByteArray());
    }

    /**
     * Delete given files and there multipartFiles if exist.
     * The deleting process in the first place marks items as deleted by putting the
     * datetime on which they have been delete. Later, a crontab goes through all
     * that have expired to permenently delete them from the system and the
     * database.
     * This helps for a rollback.
     */
    public void delete(List<NkFile> files) {
        log.debug("Request to delete NkFile : {}", files);
        this.deletePermenently(files);
    }

    public void deletePermenently(List<NkFile> files) {
        log.debug("Request to delete NkFile : {}", files);
        for (NkFile file : files) {
            if (!file.getUrl().isEmpty()) {
                fileStorageService.delete(file.getUrl());
            }
        }
        attachmentRepository.deleteAll(files);
    }

    /**
     * Delete the file by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete NkFile : {}", id);
        attachmentRepository.deleteById(id);
    }

}
