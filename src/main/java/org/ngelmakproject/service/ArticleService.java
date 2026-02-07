package org.ngelmakproject.service;

import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.ngelmakproject.domain.NkAccount;
import org.ngelmakproject.domain.NkArticle;
import org.ngelmakproject.domain.NkAttachment;
import org.ngelmakproject.domain.enumeration.Status;
import org.ngelmakproject.domain.enumeration.Subject;
import org.ngelmakproject.domain.enumeration.Visibility;
import org.ngelmakproject.repository.ArticleRepository;
import org.ngelmakproject.web.rest.dto.PageDTO;
import org.ngelmakproject.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;

/**
 * Service Implementation for managing
 * {@link org.ngelmakproject.domain.NkArticle}.
 */
@Service
@Transactional
public class ArticleService {

    private static final Logger log = LoggerFactory.getLogger(ArticleService.class);

    private static final String ENTITY_NAME = "article";

    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private AttachmentService attachmentService;
    @Autowired
    private AccountService nkAccountService;

    @Autowired
    private EntityManager entityManager;

    /**
     * Save a article.
     *
     * @param article the entity to save.
     * @return the persisted entity.
     */
    public NkArticle save(NkArticle article, List<NkAttachment> attachments, List<MultipartFile> files, List<MultipartFile> articleers) {
        log.debug("Request to save NkArticle : {}", article);
        // [TODO] we will need to change the default status to match with the fact that
        // some users can create articles that bypass some step validations.
        log.debug("Request to save NkArticle : {}", article);
        article.setStatus(Status.VALIDATED); // default status is PENDING
        article.setAt(Instant.now()); // set the current time
        article.setAccount(nkAccountService.findOneByCurrentUser().get()); // set the current connected user as
                                                                // creater of the article.
        article = articleRepository.save(article);
        attachments = attachmentService.save(article, attachments, files, articleers);
        article.setAttachments(new HashSet<NkAttachment>(attachments));
        return article;
    }

    /**
     * Update a article.
     * This function can eventually delete some attachments through the given
     * deletedAttachments variable.
     *
     * @param article the entity to save.
     * @return the persisted entity.
     * @throws IOException
     */
    public NkArticle update(NkArticle article, List<NkAttachment> attachments, List<NkAttachment> deletedAttachments,
            List<MultipartFile> files, List<MultipartFile> articleers) throws IOException {
        log.debug("Request to update NkArticle : {}", article);
        if (article.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!articleRepository.existsById(article.getId())) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }
        // article.setStatus(Status.PENDING);
        article.setStatus(Status.VALIDATED);
        article.setLastUpdate(Instant.now());
        this.partialUpdate(article);
        attachments = attachmentService.save(article, attachments,
                files, articleers);
        // [WARN] make sure to delete attachments only when all other actions are
        // successfully completed. Since the deleted actions of attachment may have
        // actions that cannot be cancelled, like removing files.
        attachmentService.delete(article, deletedAttachments);
        article.setAttachments(new HashSet<NkAttachment>(attachments));
        return article;
    }

    /**
     * Partially update a article.
     *
     * @param article the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<NkArticle> partialUpdate(NkArticle article) {
        log.debug("Request to partially update NkArticle : {}", article);

        return articleRepository
                .findById(article.getId())
                .map(existingArticle -> {
                    if (article.getTitle() != null) {
                        existingArticle.setTitle(article.getTitle());
                    }
                    if (article.getSubtitle() != null) {
                        existingArticle.setSubtitle(article.getSubtitle());
                    }
                    if (article.getKeywords() != null) {
                        existingArticle.setKeywords(article.getKeywords());
                    }
                    if (article.getSubject() != null) {
                        existingArticle.setSubject(article.getSubject());
                    }
                    if (article.getAt() != null) {
                        existingArticle.setAt(article.getAt());
                    }
                    if (article.getLastUpdate() != null) {
                        existingArticle.setLastUpdate(article.getLastUpdate());
                    }
                    if (article.getVisibility() != null) {
                        existingArticle.setVisibility(article.getVisibility());
                    }
                    if (article.getContent() != null) {
                        existingArticle.setContent(article.getContent());
                    }
                    if (article.getStatus() != null) {
                        existingArticle.setStatus(article.getStatus());
                    }

                    return existingArticle;
                })
                .map(articleRepository::save);
    }

    /**
     * Get all the articles.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public PageDTO<NkArticle> findAll(String query, Pageable pageable) {
        log.debug("Request to get all Articles");
        if (query.length() > 5) {
            return fullTextSearch(query, pageable);
        }
        return PageDTO.from(articleRepository.findByStatusOrderByAtDesc(Status.VALIDATED, pageable));
    }

    /**
     * Get one article by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<NkArticle> findOne(Long id) {
        log.debug("Request to get NkArticle : {}", id);
        return articleRepository.findById(id).map(existingArticle -> {
            existingArticle.getAttachments().removeIf(e -> e.getDeletedAt() != null);
            return existingArticle;
        });
    }

    /**
     * Delete the article by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete NkArticle : {}", id);
        articleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PageDTO<NkArticle> fullTextSearch(String fullText, Pageable pageable) {
        String sqlQuery = "SELECT " +
                "  full_search.*, " +
                "  p.id AS article_reference_id, " +
                "  p.title AS article_reference_title, " +
                "  p.content AS article_reference_content, " +
                "  a.name AS account_name " +
                "FROM ( " +
                "  SELECT p.* FROM ( " +
                "    SELECT *, ts_rank_cd(textsearchable_index_col, query) AS rank " +
                "    FROM nk_article, websearch_to_tsquery('french', :fullText) query " +
                "    WHERE status = 'VALIDATED' AND textsearchable_index_col @@ query " +
                "    ) AS p " +
                "  LEFT JOIN (SELECT id, ts_rank_cd(textsearchable_index_col, query) AS rank " +
                "  FROM nk_article, websearch_to_tsquery('french', :fullText) query " +
                "  WHERE textsearchable_index_col @@ query) AS a " +
                "  ON p.account_id = a.id " +
                "  ORDER BY a.rank,p.rank DESC " +
                "  LIMIT :limit " +
                "  OFFSET :offset " +
                ") AS full_search " +
                "LEFT JOIN nk_article AS p ON full_search.article_reference_id = p.id " +
                "LEFT JOIN nk_account AS a ON a.id = p.account_id";
        Query query = entityManager.createNativeQuery(sqlQuery, Tuple.class);
        query.setParameter("fullText", fullText);
        query.setParameter("limit", pageable.getPageSize());
        query.setParameter("offset", pageable.getOffset());
        List<Tuple> result = query.getResultList();
        List<NkArticle> articles = result.stream()
                .map(t -> {
                    NkArticle article = new NkArticle();
                    // java.time.Instant
                    article.setId(t.get("id", Long.class));
                    article.setTitle(t.get("title", String.class));
                    article.setSubtitle(t.get("subtitle", String.class));
                    article.setKeywords(t.get("keywords", String.class));
                    article.setSubject(Subject.valueOf((t.get("subject", String.class))));
                    article.setAt(t.get("at", Instant.class));
                    article.setLastUpdate(t.get("last_update", Instant.class));
                    article.setVisibility(Visibility.valueOf(t.get("visibility", String.class)));
                    article.setContent(t.get("content", String.class));
                    article.setStatus(Status.valueOf(t.get("status", String.class)));
                    var account = new NkAccount();
                    account.setId(t.get("id", Long.class));
                    account.setName(t.get("account_name", String.class));
                    article.setAccount(account);
                    // article.articleReply(
                    //                 new NkArticle()
                    //                         .id(t.get("article_reference_id", Long.class))
                    //                         .title(t.get("article_reference_title", String.class))
                    //                         .content(t.get("article_reference_content", String.class)));
                    return article;
                })
                .collect(Collectors.toList());
        Page<NkArticle> page = new PageImpl<>(articles, pageable, articles.size());
        return PageDTO.from(page);
    }
}
