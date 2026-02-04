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

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JsonIgnore
    @Column(name = "deleted_at", nullable = true)
    private Instant deletedAt;

    /**
     * a default configuration can be set for visibility of posts and their eventual
     * attachments.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = true)
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
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUser() {
        return user;
    }

    public void setUser(Long user) {
        this.user = user;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public Accessibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Accessibility visibility) {
        this.visibility = visibility;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public NkConfig getConfiguration() {
        return configuration;
    }

    public void setConfiguration(NkConfig configuration) {
        this.configuration = configuration;
    }

    public Set<NkTicket> getReports() {
        return reports;
    }

    public void setReports(Set<NkTicket> reports) {
        this.reports = reports;
    }

    public Set<NkTicket> getOwners() {
        return owners;
    }

    public void setOwners(Set<NkTicket> owners) {
        this.owners = owners;
    }

    public Set<NkComment> getComments() {
        return comments;
    }

    public void setComments(Set<NkComment> comments) {
        this.comments = comments;
    }

    public Set<NkMembership> getMemberships() {
        return memberships;
    }

    public void setMemberships(Set<NkMembership> memberships) {
        this.memberships = memberships;
    }

    public Set<NkMembership> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<NkMembership> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Set<NkPost> getPosts() {
        return posts;
    }

    public void setPosts(Set<NkPost> posts) {
        this.posts = posts;
    }

    public Set<NkReview> getReviews() {
        return reviews;
    }

    public void setReviews(Set<NkReview> reviews) {
        this.reviews = reviews;
    }

    
}
