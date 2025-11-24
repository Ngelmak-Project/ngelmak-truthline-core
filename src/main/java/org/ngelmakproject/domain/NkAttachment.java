package org.ngelmakproject.domain;

import java.io.Serializable;
import java.time.Instant;

import org.ngelmakproject.domain.enumeration.AttachmentCategory;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * A NkAttachment.
 */
@Entity
@Table(name = "nk_attachment")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class NkAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private AttachmentCategory category;

    @NotNull
    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "filename")
    private String filename;

    @Column(name = "caption")
    private String caption;

    @Column(name = "size")
    private Long size;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "url")
    private String url;

    @Column(name = "poster_url")
    private String posterUrl;

    @Column(name = "text_content", length = 2000, nullable = true)
    private String textContent;

    @NotNull
    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIncludeProperties(value = { "id" })
    private NkArticle article;

    public Long getId() {
        return this.id;
    }

    public NkAttachment id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AttachmentCategory getCategory() {
        return this.category;
    }

    public NkAttachment category(AttachmentCategory category) {
        this.setCategory(category);
        return this;
    }

    public void setCategory(AttachmentCategory category) {
        this.category = category;
    }

    public Integer getPosition() {
        return this.position;
    }

    public NkAttachment position(Integer position) {
        this.position = position;
        return this;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getCaption() {
        return this.caption;
    }

    public NkAttachment caption(String caption) {
        this.caption = caption;
        return this;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getFilename() {
        return this.filename;
    }

    public NkAttachment filename(String filename) {
        this.filename = filename;
        return this;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getTextContent() {
        return this.textContent;
    }

    public NkAttachment textContent(String textContent) {
        this.setTextContent(textContent);
        return this;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public String getType() {
        return this.type;
    }

    public NkAttachment type(String type) {
        this.type = type;
        return this;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getSize() {
        return this.size;
    }

    public NkAttachment size(Long size) {
        this.size = size;
        return this;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Integer getDuration() {
        return this.duration;
    }

    public NkAttachment duration(Integer duration) {
        this.duration = duration;
        return this;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getUrl() {
        return this.url;
    }

    public NkAttachment url(String url) {
        this.url = url;
        return this;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getArticleerUrl() {
        return this.posterUrl;
    }

    public NkAttachment posterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
        return this;
    }

    public void setArticleerUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public Instant getDeletedAt() {
        return this.deletedAt;
    }

    public NkAttachment deletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
        return this;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public NkArticle getArticle() {
        return this.article;
    }

    public void setArticle(NkArticle article) {
        this.article = article;
    }

    public NkAttachment article(NkArticle article) {
        this.setArticle(article);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NkAttachment)) {
            return false;
        }
        return getId() != null && getId().equals(((NkAttachment) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "NkAttachment{" +
                "id=" + getId() +
                ", type='" + getType() + "'" +
                ", caption='" + getCaption() + "'" +
                ", content='" + getTextContent() + "'" +
                ", type='" + getType() + "'" +
                "}";
    }
}
