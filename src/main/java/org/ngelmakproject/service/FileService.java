package org.ngelmakproject.service;

import java.net.URL;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ngelmakproject.domain.NkFile;
import org.ngelmakproject.repository.FileRepository;
import org.ngelmakproject.service.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service Implementation for managing
 * {@link org.ngelmakproject.domain.NkFile}.
 */
@Service
@Transactional
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private final FileRepository fileRepository;
    private final FileStorageService fileStorageService;

    public FileService(FileRepository fileRepository,
            FileStorageService fileStorageService) {
        this.fileRepository = fileRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<NkFile> save(List<MultipartFile> medias) {
        log.debug("Request to save {}x file(s)", medias.size());
        if (medias.isEmpty()) {
            return List.of();
        }
        List<NkFile> allFiles = new ArrayList<>();
        Map<String, MultipartFile> hashToMedia = new HashMap<>();
        MultipartFile _media = null;
        // Convert all multipart files to NkFile objects with hash
        for (int i = 0; i < medias.size(); i++) {
            _media = medias.get(i);
            NkFile media = fromMultipartToFile(_media);
            allFiles.add(media);
            hashToMedia.put(media.getHash(), _media);
        }

        // Query DB once for existing hashes
        Map<String, NkFile> existing = fileRepository.findByHashIn(hashToMedia.keySet())
                .stream()
                .collect(Collectors.toMap(NkFile::getHash, f -> f));

        // Filter out existing files → keep only new ones
        List<NkFile> newFiles = allFiles.stream()
                .filter(f -> !existing.containsKey(f.getHash()))
                // Save the medias locally.
                .map(file -> {
                    URL url = fileStorageService.store(hashToMedia.get(file.getHash()), true, file.getFilename());
                    file.setUrl(url.toString()); // update the url to the file.
                    return file;
                })
                .map(fileRepository::save) // Persist new files in one DB call
                .toList();

        // Build final result list (existing + new)
        List<NkFile> files = new ArrayList<>(newFiles); // add new saved files.
        existing.forEach((hash, file) -> files.add(file)); // add existing files to the list.

        return files;
    }

    public List<NkFile> save(List<MultipartFile> medias, List<MultipartFile> covers) {
        log.debug("Request to save {}x file(s) and {}x cover(s)", medias.size(), covers.size());
        if (medias.isEmpty()) {
            return List.of();
        }
        List<NkFile> allFiles = new ArrayList<>();
        MultipartFile _media, _cover = null;
        Map<String, MultipartFile> hashToMedia = new HashMap<>();
        // Convert all multipart files to NkFile objects with hash
        for (int i = 0; i < medias.size(); i++) {
            // MEDIA
            _media = medias.get(i);
            NkFile media = fromMultipartToFile(_media);
            hashToMedia.put(media.getHash(), _media);
            allFiles.add(media);
            // COVER (optional)
            _cover = covers.get(i);
            if (_cover != null && !_cover.isEmpty()) {
                NkFile cover = fromMultipartToFile(_cover);
                hashToMedia.put(cover.getHash(), _cover);
                media.setCover(cover); // set it as cover.
                allFiles.add(cover);
            }
        }

        // Query DB once for existing hashes
        Map<String, NkFile> existing = fileRepository.findByHashIn(hashToMedia.keySet())
                .stream()
                .collect(Collectors.toMap(NkFile::getHash, f -> f));

        // Filter out existing files → keep only new ones
        List<NkFile> newFiles = allFiles.stream()
                .filter(f -> !existing.containsKey(f.getHash()))
                // Save the medias locally.
                .map(file -> {
                    URL url = fileStorageService.store(hashToMedia.get(file.getHash()), true, file.getFilename());
                    file.setUrl(url.toString()); // update the url to the file.
                    return file;
                })
                .map(fileRepository::save) // Persist new files in one DB call
                .toList();

        // Build final result list (existing + new)
        List<NkFile> files = new ArrayList<>(newFiles); // add new saved files.
        existing.forEach((hash, file) -> files.add(file)); // add existing files to the list.

        return files;
    }

    /**
     * [TODO] Mark a file as to be deleted first then a cron would terminate the job
     * later.
     * 
     * @param files
     */
    public void delete(List<NkFile> files) {
        this.deletePermenently(files);
    }

    public void deletePermenently(List<NkFile> files) {
        for (NkFile file : files) {
            if (!file.getUrl().isEmpty()) {
                fileStorageService.delete(file.getUrl());
            }
        }
        fileRepository.deleteAll(files);
    }

    public void deletePermenentlyByIds(List<Long> fileIds) {
        List<NkFile> files = fileRepository.findAllById(fileIds);
        deletePermenently(files);
    }

    public void delete(Long id) {
        fileRepository.deleteById(id);
    }

    private NkFile fromMultipartToFile(MultipartFile media) {
        String name = media.getOriginalFilename();
        String ext = (name != null && name.contains(".")) ? name.substring(name.lastIndexOf('.') + 1).toLowerCase()
                : "";
        NkFile file = new NkFile();
        String hash = computeHash(media);
        // Format name
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd
        String shortHash = hash.substring(0, 8);
        String filename = "Nk-" + date + "-" + shortHash + "." + ext;
        
        file.setHash(hash);
        file.setFilename(filename);
        file.setType(media.getContentType());
        file.setSize(media.getSize());
        return file;
    }

    private String computeHash(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(file.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Unable to compute hash", e);
        }
    }

}
