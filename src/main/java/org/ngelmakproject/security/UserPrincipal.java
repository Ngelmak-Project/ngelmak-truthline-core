package org.ngelmakproject.security;

import java.util.Set;

/**
 * Internal representation of the authenticated user.
 */
public class UserPrincipal {

    private final Long id;
    private final String username;
    private final Set<String> authorities;

    public UserPrincipal(Long id, String username, Set<String> authorities) {
        this.id = id;
        this.username = username;
        this.authorities = authorities;
    }

    public Long getUserId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    @Override
    public String toString() {
        return "UserPrincipal [id=" + id + ", username=" + username + ", authorities=" + authorities + "]";
    }

}

