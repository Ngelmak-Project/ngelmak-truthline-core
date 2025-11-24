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
 * A NkConfig.
 */
@Entity
@Table(name = "nk_config")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class NkConfig implements Serializable {

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
    private NkAccount nkAccount;

    public Long getId() {
        return this.id;
    }

    public NkConfig id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getLastUpdate() {
        return this.lastUpdate;
    }

    public NkConfig lastUpdate(Instant lastUpdate) {
        this.setLastUpdate(lastUpdate);
        return this;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Accessibility getDefaultAccessibility() {
        return this.defaultAccessibility;
    }

    public NkConfig defaultAccessibility(Accessibility defaultAccessibility) {
        this.setDefaultAccessibility(defaultAccessibility);
        return this;
    }

    public void setDefaultAccessibility(Accessibility defaultAccessibility) {
        this.defaultAccessibility = defaultAccessibility;
    }

    public Visibility getDefaultVisibility() {
        return this.defaultVisibility;
    }

    public NkConfig defaultVisibility(Visibility defaultVisibility) {
        this.setDefaultVisibility(defaultVisibility);
        return this;
    }

    public void setDefaultVisibility(Visibility defaultVisibility) {
        this.defaultVisibility = defaultVisibility;
    }

    public NkAccount getNkAccount() {
        return this.nkAccount;
    }

    public void setNkAccount(NkAccount nkAccount) {
        if (this.nkAccount != null) {
            this.nkAccount.setConfiguration(null);
        }
        if (nkAccount != null) {
            nkAccount.setConfiguration(this);
        }
        this.nkAccount = nkAccount;
    }

    public NkConfig nkAccount(NkAccount nkAccount) {
        this.setNkAccount(nkAccount);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and
    // setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NkConfig)) {
            return false;
        }
        return getId() != null && getId().equals(((NkConfig) o).getId());
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
        return "NkConfig{" +
                "id=" + getId() +
                ", lastUpdate='" + getLastUpdate() + "'" +
                ", defaultAccessibility='" + getDefaultAccessibility() + "'" +
                ", defaultVisibility='" + getDefaultVisibility() + "'" +
                "}";
    }
}
