package org.ngelmakproject.web.rest.dto;

import org.ngelmakproject.domain.Account;

public record AccountDTO(
        Long id,
        String identifier,
        String name,
        String description,
        String avatar,
        String banner,
        Long userId) {
    public static AccountDTO from(Account a) {
        if (a == null)
            return null;
        return new AccountDTO(
                a.getId(),
                a.getIdentifier(),
                a.getName(),
                a.getDescription(),
                a.getAvatar(),
                a.getBanner(),
                a.getUser());
    }
}
