package org.ngelmakproject.domain;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.ngelmakproject.domain.enumeration.Status;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * A NkReview.
 */
@Entity
@Table(name = "nk_review")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class NkReview implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "at", nullable = false)
    private Instant at;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    /**
     * number of minutes to wait before timeout.
     */
    @NotNull
    @Column(name = "timeout", nullable = false)
    private Integer timeout;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "replyto")
    @JsonIgnoreProperties(value = { "reviews", "account", "ticket", "replyto" }, allowSetters = true)
    private Set<NkReview> reviews = new HashSet<>();

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "configuration", "user", "reports", "owners", "comments", "memberships",
            "subscriptions", "posts", "reviews" }, allowSetters = true)
    private NkAccount account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "reviews", "postRelated", "commentRelated", "accountRelated",
            "issuedby" }, allowSetters = true)
    private NkTicket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "reviews", "account", "ticket", "replyto" }, allowSetters = true)
    private NkReview replyto;

    public Long getId() {
        return this.id;
    }

    public NkReview id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getAt() {
        return this.at;
    }

    public NkReview at(Instant at) {
        this.setAt(at);
        return this;
    }

    public void setAt(Instant at) {
        this.at = at;
    }

    public Status getStatus() {
        return this.status;
    }

    public NkReview status(Status status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Integer getTimeout() {
        return this.timeout;
    }

    public NkReview timeout(Integer timeout) {
        this.setTimeout(timeout);
        return this;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Set<NkReview> getReviews() {
        return this.reviews;
    }

    public void setReviews(Set<NkReview> reviews) {
        if (this.reviews != null) {
            this.reviews.forEach(i -> i.setReplyto(null));
        }
        if (reviews != null) {
            reviews.forEach(i -> i.setReplyto(this));
        }
        this.reviews = reviews;
    }

    public NkReview reviews(Set<NkReview> reviews) {
        this.setReviews(reviews);
        return this;
    }

    public NkReview addReview(NkReview review) {
        this.reviews.add(review);
        review.setReplyto(this);
        return this;
    }

    public NkReview removeReview(NkReview review) {
        this.reviews.remove(review);
        review.setReplyto(null);
        return this;
    }

    public NkAccount getAccount() {
        return this.account;
    }

    public void setAccount(NkAccount nkAccount) {
        this.account = nkAccount;
    }

    public NkReview account(NkAccount nkAccount) {
        this.setAccount(nkAccount);
        return this;
    }

    public NkTicket getTicket() {
        return this.ticket;
    }

    public void setTicket(NkTicket ticket) {
        this.ticket = ticket;
    }

    public NkReview ticket(NkTicket ticket) {
        this.setTicket(ticket);
        return this;
    }

    public NkReview getReplyto() {
        return this.replyto;
    }

    public void setReplyto(NkReview review) {
        this.replyto = review;
    }

    public NkReview replyto(NkReview review) {
        this.setReplyto(review);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NkReview)) {
            return false;
        }
        return getId() != null && getId().equals(((NkReview) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "NkReview{" +
                "id=" + getId() +
                ", at='" + getAt() + "'" +
                ", status='" + getStatus() + "'" +
                ", timeout=" + getTimeout() +
                "}";
    }
}
