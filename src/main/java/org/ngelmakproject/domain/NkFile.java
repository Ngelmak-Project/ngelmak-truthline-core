package org.ngelmakproject.domain;

import java.io.Serializable;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * A NkFile.
 */
@Entity
@Table(name = "nk_file")
public class NkFile implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "filename")
    private String filename;

    @Column(name = "size")
    private Long size;

    @Column(name = "duration")
    private Integer duration = 0;

    @Column(name = "url")
    private String url;

    @NotNull
    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @ManyToOne(optional = true)
    @NotNull
    @JsonIncludeProperties(value = {"id"})
    private NkFile cover;

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public NkFile getCover() {
        return cover;
    }

    public void setCover(NkFile cover) {
        this.cover = cover;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NkFile{");
        sb.append("id=").append(id);
        sb.append(", filename=").append(filename);
        sb.append(", size=").append(size);
        sb.append(", duration=").append(duration);
        sb.append(", url=").append(url);
        sb.append(", type=").append(type);
        sb.append(", deletedAt=").append(deletedAt);
        sb.append(", cover=").append(cover);
        sb.append('}');
        return sb.toString();
    }

}
