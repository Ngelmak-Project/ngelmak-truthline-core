package org.ngelmakproject.web.rest.dto;

import org.ngelmakproject.domain.NkAccount;

public record AccountDTO(
        Long id,
        String identifier,
        String name,
        String avatar,
        Long userId) {
    public static AccountDTO from(NkAccount a) {
        if (a == null)
            return null;
        return new AccountDTO(
                a.getId(),
                a.getIdentifier(),
                a.getName(),
                a.getAvatar(),
                a.getUser());
    }
}
