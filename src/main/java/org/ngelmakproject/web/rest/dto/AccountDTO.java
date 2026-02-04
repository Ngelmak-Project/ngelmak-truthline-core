package org.ngelmakproject.web.rest.dto;

import org.ngelmakproject.domain.NkAccount;

public record AccountDTO(
        Long id,
        String identifier,
        String name,
        String description,
        String avatar,
        String banner,
        Long userId) {
    public static AccountDTO from(NkAccount a) {
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
