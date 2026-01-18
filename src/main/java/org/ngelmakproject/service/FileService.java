package org.ngelmakproject.service;

import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
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

    public List<NkFile> save(List<MultipartFile> medias, List<MultipartFile> covers) {
        log.debug("Request to save {}x file(s) and {}x cover(s)", medias.size(), covers.size());
        if (medias.isEmpty()) {
            return List.of();
        }
        List<NkFile> allFiles = new ArrayList<>();
        MultipartFile _media, _cover = null;
        URL url = null;
        // Convert all multipart files to NkFile objects with hash
        for (int i = 0; i < medias.size(); i++) {
            // MEDIA
            _media = medias.get(i);
            NkFile media = MultipartToFile(_media);
            url = fileStorageService.store(_media, true, media.getFilename());
            media.setUrl(url.toString());
            allFiles.add(media);
            // COVER (optional)
            _cover = covers.get(i);
            if (_cover != null && !_cover.isEmpty()) {
                NkFile cover = MultipartToFile(_cover);
                url = fileStorageService.store(_cover, true, cover.getFilename());
                cover.setUrl(url.toString());
                media.setCover(cover); // set it as cover.
                allFiles.add(cover);
            }
        }

        // Gather all hashes
        List<String> hashes = allFiles.stream()
                .map(NkFile::getHash)
                .distinct()
                .toList();

        // Query DB once for existing hashes
        Map<String, NkFile> existing = fileRepository.findByHashIn(hashes)
                .stream()
                .collect(Collectors.toMap(NkFile::getHash, f -> f));

        // Filter out existing files → keep only new ones
        List<NkFile> newFiles = allFiles.stream()
                .filter(f -> !existing.containsKey(f.getHash()))
                .toList();

        // Persist new files in one DB call
        newFiles = fileRepository.saveAll(newFiles);

        // Build final result list (existing + new)
        List<NkFile> files = new ArrayList<>(newFiles); // add new saved files.
        existing.forEach((hash, file) -> files.add(file)); // add existing files to the list.

        return files;
    }

    private NkFile MultipartToFile(MultipartFile media) {
        String name = media.getOriginalFilename();
        String ext = (name != null && name.contains(".")) ? name.substring(name.lastIndexOf('.') + 1).toLowerCase()
                : "";
        NkFile file = new NkFile();
        String hash = computeHash(media);
        file.setHash(hash);
        file.setFilename(hash + "." + ext);
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
