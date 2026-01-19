package org.ngelmakproject.web.rest.dto;

import org.ngelmakproject.domain.NkAccount;

public record AccountDTO(
        Long id,
        String identifier,
        String name,
        String avatar,
        Long userId) {
    public static AccountDTO from(NkAccount account) {
        return new AccountDTO(
                account.getId(),
                account.getIdentifier(),
                account.getName(),
                account.getAvatar(),
                account.getUser());
    }
}
