package org.ngelmakproject.domain;

import java.io.Serializable;
import java.time.Instant;

import org.ngelmakproject.domain.enumeration.Accessibility;
import org.ngelmakproject.domain.enumeration.Visibility;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * A Config.
 */
@Entity
@Table(name = "nk_config")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Config implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "last_update")
    private Instant lastUpdate;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_accessibility")
    private Accessibility defaultAccessibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_visibility")
    private Visibility defaultVisibility;

    @JsonIgnore
    @OneToOne(mappedBy = "configuration")
    private Account nkAccount;

    public Long getId() {
        return this.id;
    }

    public Config id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getLastUpdate() {
        return this.lastUpdate;
    }

    public Config lastUpdate(Instant lastUpdate) {
        this.setLastUpdate(lastUpdate);
        return this;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Accessibility getDefaultAccessibility() {
        return this.defaultAccessibility;
    }

    public Config defaultAccessibility(Accessibility defaultAccessibility) {
        this.setDefaultAccessibility(defaultAccessibility);
        return this;
    }

    public void setDefaultAccessibility(Accessibility defaultAccessibility) {
        this.defaultAccessibility = defaultAccessibility;
    }

    public Visibility getDefaultVisibility() {
        return this.defaultVisibility;
    }

    public Config defaultVisibility(Visibility defaultVisibility) {
        this.setDefaultVisibility(defaultVisibility);
        return this;
    }

    public void setDefaultVisibility(Visibility defaultVisibility) {
        this.defaultVisibility = defaultVisibility;
    }

    public Account getAccount() {
        return this.nkAccount;
    }

    public void setAccount(Account nkAccount) {
        if (this.nkAccount != null) {
            this.nkAccount.setConfiguration(null);
        }
        if (nkAccount != null) {
            nkAccount.setConfiguration(this);
        }
        this.nkAccount = nkAccount;
    }

    public Config nkAccount(Account nkAccount) {
        this.setAccount(nkAccount);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Config)) {
            return false;
        }
        return getId() != null && getId().equals(((Config) o).getId());
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
        return "Config{" +
                "id=" + getId() +
                ", lastUpdate='" + getLastUpdate() + "'" +
                ", defaultAccessibility='" + getDefaultAccessibility() + "'" +
                ", defaultVisibility='" + getDefaultVisibility() + "'" +
                "}";
    }
}
