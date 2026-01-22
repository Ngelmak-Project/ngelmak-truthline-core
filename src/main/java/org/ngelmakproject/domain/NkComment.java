package org.ngelmakproject.domain;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * A NkComment.
 */
@Entity
@Table(name = "nk_comment")
@JsonIgnoreProperties(ignoreUnknown = true)
public class NkComment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "at", nullable = false)
    private Instant at;

    @Column(name = "last_update")
    private Instant lastUpdate;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "content", length = 1000, nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.REMOVE)
    @JsonIncludeProperties(value = { "id" })
    private NkPost post;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JsonIncludeProperties(value = { "id" })
    private NkComment replyTo;

    @Column(name = "reply_count")
    private Integer replyCount = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.REMOVE)
    @NotNull
    @JsonIncludeProperties(value = { "id" })
    private NkAccount account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JsonIncludeProperties(value = { "id", "url" })
    private NkFile file;

    /**
     * a ticket can be related to a abusive comment.
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "commentRelated")
    @JsonIgnore
    private Set<NkTicket> reports = new HashSet<>();

    /**
     * a comment can have multiple subcomments (reply), each issued by one user.
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "replyTo")
    @JsonIgnore
    private Set<NkComment> comments = new HashSet<>();

    public NkComment() {
    }

    public Long getId() {
        return this.id;
    }

    public NkComment id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getAt() {
        return this.at;
    }

    public NkComment at(Instant at) {
        this.setAt(at);
        return this;
    }

    public void setAt(Instant at) {
        this.at = at;
    }

    public Instant getLastUpdate() {
        return this.lastUpdate;
    }

    public NkComment lastUpdate(Instant lastUpdate) {
        this.setLastUpdate(lastUpdate);
        return this;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Instant getDeleteAt() {
        return this.deletedAt;
    }

    public NkComment deletedAt(Instant deletedAt) {
        this.setDeleteAt(deletedAt);
        return this;
    }

    public void setDeleteAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getContent() {
        return this.content;
    }

    public NkComment content(String content) {
        this.setContent(content);
        return this;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Set<NkTicket> getReports() {
        return this.reports;
    }

    public void setReports(Set<NkTicket> tickets) {
        if (this.reports != null) {
            this.reports.forEach(i -> i.setCommentRelated(null));
        }
        if (tickets != null) {
            tickets.forEach(i -> i.setCommentRelated(this));
        }
        this.reports = tickets;
    }

    public NkComment reports(Set<NkTicket> tickets) {
        this.setReports(tickets);
        return this;
    }

    public NkComment addReports(NkTicket ticket) {
        this.reports.add(ticket);
        ticket.setCommentRelated(this);
        return this;
    }

    public NkComment removeReports(NkTicket ticket) {
        this.reports.remove(ticket);
        ticket.setCommentRelated(null);
        return this;
    }

    public Set<NkComment> getComments() {
        return this.comments;
    }

    public void setComments(Set<NkComment> comments) {
        if (this.comments != null) {
            this.comments.forEach(i -> i.setReplyto(null));
        }
        if (comments != null) {
            comments.forEach(i -> i.setReplyto(this));
        }
        this.comments = comments;
    }

    public NkComment comments(Set<NkComment> comments) {
        this.setComments(comments);
        return this;
    }

    public NkComment addComment(NkComment comment) {
        this.comments.add(comment);
        comment.setReplyto(this);
        return this;
    }

    public NkComment removeComment(NkComment comment) {
        this.comments.remove(comment);
        comment.setReplyto(null);
        return this;
    }

    public NkPost getPost() {
        return this.post;
    }

    public void setPost(NkPost post) {
        this.post = post;
    }

    public NkComment post(NkPost post) {
        this.setPost(post);
        return this;
    }

    public NkComment getReplyto() {
        return this.replyTo;
    }

    public void setReplyto(NkComment comment) {
        this.replyTo = comment;
    }

    public NkComment replyTo(NkComment comment) {
        this.setReplyto(comment);
        return this;
    }

    public void setReplyCount(Integer replyCount) {
        this.replyCount = replyCount;
    }

    public Integer getReplyCount() {
        return this.replyCount;
    }

    public NkComment replyCount(Integer replyCount) {
        this.setReplyCount(replyCount);
        return this;
    }

    public NkFile getFile() {
        return this.file;
    }

    public void setFile(NkFile file) {
        this.file = file;
    }

    public NkComment file(NkFile file) {
        this.setFile(file);
        return this;
    }

    public NkAccount getAccount() {
        return this.account;
    }

    public void setAccount(NkAccount account) {
        this.account = account;
    }

    public NkComment account(NkAccount account) {
        this.setAccount(account);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NkComment)) {
            return false;
        }
        return getId() != null && getId().equals(((NkComment) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "NkComment{" +
                "id=" + getId() +
                ", at='" + getAt() + "'" +
                ", lastUpdate='" + getLastUpdate() + "'" +
                ", content='" + getContent() + "'" +
                "}";
    }
}
