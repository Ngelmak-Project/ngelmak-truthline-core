package org.ngelmakproject.domain;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.ngelmakproject.domain.enumeration.Accessibility;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * The Compte entity.
 */
@Entity
@Table(name = "nk_account")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class NkAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    /* User (auth-service) that own the account */
    @Column(name = "user_id", unique = true, nullable = false)
    private Long user;

    /**
     * A string or code used in URLs
     * /account/acme-corp
     * identifier = "acme-corp"
     */
    @Column(name = "identifier", length = 30, unique = true)
    private String identifier;

    @NotNull
    @NotBlank
    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    /**
     * Avatar or profile image.
     */
    @Column(name = "avatar")
    private String avatar;

    /**
     * Background image url.
     */
    @Column(name = "banner")
    private String banner;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility")
    private Accessibility visibility;

    @Column(name = "created_at")
    private Instant created_at;

    /**
     * a default configuration can be set for visibility of posts and their eventual
     * attachments.
     */
    @OneToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(unique = true)
    private NkConfig configuration;

    /**
     * a ticket could be also related to a an account.
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "accountRelated")
    @JsonIgnore
    private Set<NkTicket> reports = new HashSet<>();

    /**
     * must be is issued by a user account.
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "issuedby")
    @JsonIgnore
    private Set<NkTicket> owners = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account")
    @JsonIgnore
    private Set<NkComment> comments = new HashSet<>();

    /**
     * any user can subscribe to any other user's account which my eventually have
     * any subscriber
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "follower")
    @JsonIgnore
    private Set<NkMembership> memberships = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "following")
    @JsonIgnore
    private Set<NkMembership> subscriptions = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account")
    @JsonIgnore
    private Set<NkPost> posts = new HashSet<>();

    /**
     * a review is done by a user
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account")
    @JsonIgnore
    private Set<NkReview> reviews = new HashSet<>();

    public Long getId() {
        return this.id;
    }

    public NkAccount id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long user) {
        this.user = user;
    }
    
    public Long getUser() {
        return this.id;
    }

    public NkAccount user(Long user) {
        this.setUser(user);
        return this;
    }

    public void setUser(Long user) {
        this.user = user;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public NkAccount identifier(String identifier) {
        this.setIdentifier(identifier);
        return this;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return this.name;
    }

    public NkAccount name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public NkAccount description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public NkAccount avatar(String avatar) {
        this.setAvatar(avatar);
        return this;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getBanner() {
        return this.banner;
    }

    public NkAccount banner(String banner) {
        this.setBanner(banner);
        return this;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public Accessibility getVisibility() {
        return this.visibility;
    }

    public NkAccount visibility(Accessibility visibility) {
        this.setVisibility(visibility);
        return this;
    }

    public void setVisibility(Accessibility visibility) {
        this.visibility = visibility;
    }

    public Instant getCreatedAt() {
        return this.created_at;
    }

    public NkAccount createdAt(Instant created_at) {
        this.setCreatedAt(created_at);
        return this;
    }

    public void setCreatedAt(Instant created_at) {
        this.created_at = created_at;
    }

    public NkConfig getConfiguration() {
        return this.configuration;
    }

    public void setConfiguration(NkConfig config) {
        this.configuration = config;
    }

    public NkAccount configuration(NkConfig config) {
        this.setConfiguration(config);
        return this;
    }

    public Set<NkTicket> getReports() {
        return this.reports;
    }

    public void setReports(Set<NkTicket> tickets) {
        if (this.reports != null) {
            this.reports.forEach(i -> i.setAccountRelated(null));
        }
        if (tickets != null) {
            tickets.forEach(i -> i.setAccountRelated(this));
        }
        this.reports = tickets;
    }

    public NkAccount reports(Set<NkTicket> tickets) {
        this.setReports(tickets);
        return this;
    }

    public NkAccount addReports(NkTicket ticket) {
        this.reports.add(ticket);
        ticket.setAccountRelated(this);
        return this;
    }

    public NkAccount removeReports(NkTicket ticket) {
        this.reports.remove(ticket);
        ticket.setAccountRelated(null);
        return this;
    }

    public Set<NkTicket> getOwners() {
        return this.owners;
    }

    public void setOwners(Set<NkTicket> tickets) {
        if (this.owners != null) {
            this.owners.forEach(i -> i.setIssuedby(null));
        }
        if (tickets != null) {
            tickets.forEach(i -> i.setIssuedby(this));
        }
        this.owners = tickets;
    }

    public NkAccount owners(Set<NkTicket> tickets) {
        this.setOwners(tickets);
        return this;
    }

    public NkAccount addOwners(NkTicket ticket) {
        this.owners.add(ticket);
        ticket.setIssuedby(this);
        return this;
    }

    public NkAccount removeOwners(NkTicket ticket) {
        this.owners.remove(ticket);
        ticket.setIssuedby(null);
        return this;
    }

    public Set<NkComment> getComments() {
        return this.comments;
    }

    public void setComments(Set<NkComment> comments) {
        if (this.comments != null) {
            this.comments.forEach(i -> i.setAccount(null));
        }
        if (comments != null) {
            comments.forEach(i -> i.setAccount(this));
        }
        this.comments = comments;
    }

    public NkAccount comments(Set<NkComment> comments) {
        this.setComments(comments);
        return this;
    }

    public NkAccount addComment(NkComment comment) {
        this.comments.add(comment);
        comment.setAccount(this);
        return this;
    }

    public NkAccount removeComment(NkComment comment) {
        this.comments.remove(comment);
        comment.setAccount(null);
        return this;
    }

    public Set<NkMembership> getMemberships() {
        return this.memberships;
    }

    public void setMemberships(Set<NkMembership> memberships) {
        if (this.memberships != null) {
            this.memberships.forEach(i -> i.setFollower(null));
        }
        if (memberships != null) {
            memberships.forEach(i -> i.setFollower(this));
        }
        this.memberships = memberships;
    }

    public NkAccount memberships(Set<NkMembership> memberships) {
        this.setMemberships(memberships);
        return this;
    }

    public NkAccount addMemberships(NkMembership membership) {
        this.memberships.add(membership);
        membership.setFollower(this);
        return this;
    }

    public NkAccount removeMemberships(NkMembership membership) {
        this.memberships.remove(membership);
        membership.setFollower(null);
        return this;
    }

    public Set<NkMembership> getSubscriptions() {
        return this.subscriptions;
    }

    public void setSubscriptions(Set<NkMembership> memberships) {
        if (this.subscriptions != null) {
            this.subscriptions.forEach(i -> i.setFollower(null));
        }
        if (memberships != null) {
            memberships.forEach(i -> i.setFollower(this));
        }
        this.subscriptions = memberships;
    }

    public NkAccount subscriptions(Set<NkMembership> memberships) {
        this.setSubscriptions(memberships);
        return this;
    }

    public NkAccount addSubscriptions(NkMembership membership) {
        this.subscriptions.add(membership);
        membership.setFollower(this);
        return this;
    }

    public NkAccount removeSubscriptions(NkMembership membership) {
        this.subscriptions.remove(membership);
        membership.setFollower(null);
        return this;
    }

    public Set<NkPost> getPosts() {
        return this.posts;
    }

    public void setPosts(Set<NkPost> posts) {
        if (this.posts != null) {
            this.posts.forEach(i -> i.setAccount(null));
        }
        if (posts != null) {
            posts.forEach(i -> i.setAccount(this));
        }
        this.posts = posts;
    }

    public NkAccount posts(Set<NkPost> posts) {
        this.setPosts(posts);
        return this;
    }

    public NkAccount addPost(NkPost post) {
        this.posts.add(post);
        post.setAccount(this);
        return this;
    }

    public NkAccount removePost(NkPost post) {
        this.posts.remove(post);
        post.setAccount(null);
        return this;
    }

    public Set<NkReview> getReviews() {
        return this.reviews;
    }

    public void setReviews(Set<NkReview> reviews) {
        if (this.reviews != null) {
            this.reviews.forEach(i -> i.setAccount(null));
        }
        if (reviews != null) {
            reviews.forEach(i -> i.setAccount(this));
        }
        this.reviews = reviews;
    }

    public NkAccount reviews(Set<NkReview> reviews) {
        this.setReviews(reviews);
        return this;
    }

    public NkAccount addReview(NkReview review) {
        this.reviews.add(review);
        review.setAccount(this);
        return this;
    }

    public NkAccount removeReview(NkReview review) {
        this.reviews.remove(review);
        review.setAccount(null);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NkAccount)) {
            return false;
        }
        return getId() != null && getId().equals(((NkAccount) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "NkAccount{" +
                "id=" + getId() +
                ", identifier='" + getIdentifier() + "'" +
                ", name='" + getName() + "'" +
                ", description='" + getDescription() + "'" +
                ", avatar='" + getAvatar() + "'" +
                ", banner='" + getBanner() + "'" +
                ", visibility='" + getVisibility() + "'" +
                ", created_at='" + getCreatedAt() + "'" +
                "}";
    }
}
