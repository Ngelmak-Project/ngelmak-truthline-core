package org.ngelmakproject.service;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.ngelmakproject.domain.NkArticle;
import org.ngelmakproject.domain.NkAttachment;
import org.ngelmakproject.domain.enumeration.AttachmentCategory;
import org.ngelmakproject.domain.enumeration.Status;
import org.ngelmakproject.repository.AttachmentRepository;
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
 * {@link org.ngelmakproject.domain.NkAttachment}.
 */
@Service
@Transactional
public class AttachmentService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentService.class);

    @Autowired
    private AttachmentRepository attachmentRepository;
    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Save a attachment.
     *
     * @param attachment the entity to save.
     * @return the persisted entity.
     */
    public NkAttachment save(NkAttachment attachment) {
        log.debug("Request to save NkAttachment : {}", attachment);
        return attachmentRepository.save(attachment);
    }

    /**
     * Save a attachment.
     *
     * @param attachment the entity to save.
     * @return the persisted entity.
     */
    public List<NkAttachment> save(NkArticle article, List<NkAttachment> attachments, List<MultipartFile> files,
            List<MultipartFile> articleers) {
        log.debug("Request to save NkAttachment : {}", attachments);
        NkAttachment attachment;
        MultipartFile file;
        MultipartFile articleer;
        URL url = null;
        URL articleerUrl = null;
        String filename = null;
        String[] dirs = { "media", "attachments" }; // path where to save the attachment file.
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String format = date.format(formatter);
        for (int i = 0; i < attachments.size(); i++) {
            attachment = attachments.get(i);
            file = files.get(i);
            articleer = articleers.get(i);
            if (file != null) {
                filename = String.format("Ngelmak-%s-%s-%s",
                        StringUtils.capitalize(attachment.getCategory().toString()), format,
                        StringUtils.capitalize(file.getOriginalFilename()));
                url = fileStorageService.store(file, true, filename, dirs);
                attachment.size(file.getSize())
                        .url(url.toString());
            }
            if (articleer != null) {
                filename = String.format("Ngelmak-Articleer-%s-%s-%s",
                        StringUtils.capitalize(AttachmentCategory.IMAGE.toString()), format,
                        StringUtils.capitalize(file.getOriginalFilename().replaceFirst(".[a-zA-Z0-9]+$", ".png")));
                articleerUrl = fileStorageService.store(articleer, true, filename, dirs);
                attachment.setUrl(articleerUrl.toString());
            }
            attachment.setArticle(article);
        }
        return attachmentRepository.saveAll(attachments);
    }

    /**
     * Get all the attachments.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<NkAttachment> findAll(Pageable pageable) {
        log.debug("Request to get all Attachments");
        return attachmentRepository.findAll(pageable);
    }

    /**
     * Get one attachment by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<NkAttachment> findOne(Long id) {
        log.debug("Request to get NkAttachment : {}", id);
        return attachmentRepository.findById(id);
    }

    /**
     * Get attachment's resource by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     * @throws IOException
     */
    @Transactional(readOnly = true)
    public Optional<byte[]> getResource(Long id) throws IOException {
        log.debug("Request to get the actual resource of NkAttachment : {}", id);
        Optional<NkAttachment> optional = attachmentRepository.findById(id);
        if (optional.isEmpty()) {
            return Optional.empty();
        }
        NkAttachment attachment = optional.get();
        Resource resource = fileStorageService.loadAsResource(attachment.getUrl());
        return Optional.of(resource.getContentAsByteArray());
    }

    /**
     * Delete given attachments and there files if exist.
     * The deleting process in the first place marks items as deleted by putting the
     * datetime on which they have been delete. Later, a crontab goes through all
     * that have expired to permenently delete them from the system and the
     * database.
     * This helps for a rollback.
     *
     * @throws IOException
     */
    public void delete(NkArticle article, List<NkAttachment> attachments) throws IOException {
        log.debug("Request to delete NkAttachment : {}", attachments);
        Instant now = Instant.now();
        if (!article.getStatus().equals(Status.PENDING)) {
            // Mark the attachment as to be deleted by the cron.
            attachments = attachmentRepository.findAllById(attachments.stream().map(NkAttachment::getId).toList())
                    .stream().map(existingAttachement -> existingAttachement.deletedAt(now)).toList();
            attachmentRepository.saveAll(attachments);
        } else {
            // [WARN] This action cannot be cancelled.
            this.deletePermenently(attachments);
        }
    }

    public void deletePermenently(List<NkAttachment> attachments) throws IOException {
        log.debug("Request to delete NkAttachment : {}", attachments);
        for (NkAttachment attachment : attachments) {
            if (!attachment.getCategory().equals(AttachmentCategory.TEXT)) {
                fileStorageService.delete(attachment.getUrl());
                fileStorageService.delete(attachment.getArticleerUrl());
            }
        }
        attachmentRepository.deleteAll(attachments);
    }

    /**
     * Delete the attachment by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete NkAttachment : {}", id);
        attachmentRepository.deleteById(id);
    }

}
