package org.ngelmakproject.domain;

import java.io.Serializable;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
 * A NkMembership.
 */
@Entity
@Table(name = "nk_membership")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class NkMembership implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "at")
    private Instant at;

    @Column(name = "activate_notification")
    private Boolean activateNotification;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "configuration", "user", "reports", "owners", "comments", "memberships",
            "subscriptions", "posts", "reviews" }, allowSetters = true)
    private NkAccount following;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "configuration", "user", "reports", "owners", "comments", "memberships",
            "subscriptions", "posts", "reviews" }, allowSetters = true)
    private NkAccount follower; // The owner of the membership

    public Long getId() {
        return this.id;
    }

    public NkMembership id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getAt() {
        return this.at;
    }

    public NkMembership at(Instant at) {
        this.setAt(at);
        return this;
    }

    public void setAt(Instant at) {
        this.at = at;
    }

    public Boolean getActivateNotification() {
        return this.activateNotification;
    }

    public NkMembership activateNotification(Boolean activateNotification) {
        this.setActivateNotification(activateNotification);
        return this;
    }

    public void setActivateNotification(Boolean activateNotification) {
        this.activateNotification = activateNotification;
    }

    public NkAccount getFollower() {
        return this.follower;
    }

    public void setFollower(NkAccount nkAccount) {
        this.follower = nkAccount;
    }

    public NkMembership follower(NkAccount nkAccount) {
        this.setFollower(nkAccount);
        return this;
    }

    public NkAccount getFollowing() {
        return this.following;
    }

    public void setFollowing(NkAccount nkAccount) {
        this.following = nkAccount;
    }

    public NkMembership following(NkAccount nkAccount) {
        this.setFollowing(nkAccount);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NkMembership)) {
            return false;
        }
        return getId() != null && getId().equals(((NkMembership) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "NkMembership{" +
                "id=" + getId() +
                ", at='" + getAt() + "'" +
                ", activateNotification='" + getActivateNotification() + "'" +
                "}";
    }
}
