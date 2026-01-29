package org.ngelmakproject.domain;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.ngelmakproject.domain.enumeration.Status;
import org.ngelmakproject.domain.enumeration.Visibility;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * The NkPost entity.
 */
@Entity
@Table(name = "nk_post")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class NkPost implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "keywords")
    private String keywords;

    @NotNull
    @Column(name = "at", nullable = false)
    private Instant at;

    @JsonIgnore
    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "last_update")
    private Instant lastUpdate;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility")
    private Visibility visibility;

    @Column(name = "content", nullable = false)
    private String content;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIncludeProperties(value = { "id", "content" })
    private NkPost postReply;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @NotNull
    @JsonIncludeProperties(value = { "id", "identifier", "name", "avatar" })
    private NkAccount account;

    @ManyToMany(cascade = CascadeType.REMOVE) // [TODO] Make sure not to delete the file before checking first if it has
                                              // being used by another resource.
    @JoinTable(name = "nk_post_file", joinColumns = {
            @JoinColumn(name = "post_id", referencedColumnName = "id") }, inverseJoinColumns = {
                    @JoinColumn(name = "file_id", referencedColumnName = "id") })
    private Set<NkFile> files = new HashSet<>();

    /**
     * a post can be signal as going against our policies.
     */
    @OneToMany(mappedBy = "postRelated")
    @JsonIgnore
    private Set<NkTicket> reports = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post")
    @JsonIncludeProperties(value = { "id" })
    private Set<NkComment> comments = new HashSet<>();

    @Column(name = "comment_count")
    private Integer commentCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<NkReaction> reactions = new HashSet<>();

    public Long getId() {
        return this.id;
    }

    public NkPost id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKeywords() {
        return this.keywords;
    }

    public NkPost keywords(String keywords) {
        this.setKeywords(keywords);
        return this;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public Instant getAt() {
        return this.at;
    }

    public NkPost at(Instant at) {
        this.setAt(at);
        return this;
    }

    public void setAt(Instant at) {
        this.at = at;
    }

    public Instant getDeletedAt() {
        return this.deletedAt;
    }

    public NkPost deletedAt(Instant deletedAt) {
        this.setDeletedAt(deletedAt);
        return this;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Instant getLastUpdate() {
        return this.lastUpdate;
    }

    public NkPost lastUpdate(Instant lastUpdate) {
        this.setLastUpdate(lastUpdate);
        return this;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Visibility getVisibility() {
        return this.visibility;
    }

    public NkPost visibility(Visibility visibility) {
        this.setVisibility(visibility);
        return this;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public String getContent() {
        return this.content;
    }

    public NkPost content(String content) {
        this.setContent(content);
        return this;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Status getStatus() {
        return this.status;
    }

    public NkPost status(Status status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Set<NkFile> getFiles() {
        return this.files;
    }

    public void setFiles(Set<NkFile> files) {
        this.files = files;
    }

    public NkPost files(Set<NkFile> files) {
        this.setFiles(files);
        return this;
    }

    public Set<NkTicket> getReports() {
        return this.reports;
    }

    public void setReports(Set<NkTicket> tickets) {
        if (this.reports != null) {
            this.reports.forEach(i -> i.setPostRelated(null));
        }
        if (tickets != null) {
            tickets.forEach(i -> i.setPostRelated(this));
        }
        this.reports = tickets;
    }

    public NkPost reports(Set<NkTicket> tickets) {
        this.setReports(tickets);
        return this;
    }

    public NkPost addReports(NkTicket ticket) {
        this.reports.add(ticket);
        ticket.setPostRelated(this);
        return this;
    }

    public NkPost removeReports(NkTicket ticket) {
        this.reports.remove(ticket);
        ticket.setPostRelated(null);
        return this;
    }

    public Set<NkComment> getComments() {
        return this.comments;
    }

    public void setComments(Set<NkComment> comments) {
        if (this.comments != null) {
            this.comments.forEach(i -> i.setPost(null));
        }
        if (comments != null) {
            comments.forEach(i -> i.setPost(this));
        }
        this.comments = comments;
    }

    public NkPost comments(Set<NkComment> comments) {
        this.setComments(comments);
        return this;
    }

    public NkPost addComment(NkComment comment) {
        this.comments.add(comment);
        comment.setPost(this);
        return this;
    }

    public NkPost removeComment(NkComment comment) {
        this.comments.remove(comment);
        comment.setPost(null);
        return this;
    }

    public Integer getCommentCount() {
        return this.commentCount;
    }

    public NkPost commentCount(Integer commentCount) {
        this.setCommentCount(commentCount);
        return this;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public NkPost getPostReply() {
        return this.postReply;
    }

    public void setPostReply(NkPost postReply) {
        this.postReply = postReply;
    }

    public NkPost postReply(NkPost postReply) {
        this.setPostReply(postReply);
        return this;
    }

    public NkAccount getAccount() {
        return this.account;
    }

    public void setAccount(NkAccount nkAccount) {
        this.account = nkAccount;
    }

    public NkPost account(NkAccount nkAccount) {
        this.setAccount(nkAccount);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NkPost)) {
            return false;
        }
        return getId() != null && getId().equals(((NkPost) o).getId());
    }

    @Override
    public int hashCode() {
        // see
        // https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "NkPost{" +
                "id=" + getId() +
                ", keywords='" + getKeywords() + "'" +
                ", at='" + getAt() + "'" +
                ", lastUpdate='" + getLastUpdate() + "'" +
                ", visibility='" + getVisibility() + "'" +
                ", content='" + getContent() + "'" +
                ", status='" + getStatus() + "'" +
                "}";
    }
}
