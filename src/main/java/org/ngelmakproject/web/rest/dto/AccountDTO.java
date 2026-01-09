package org.ngelmakproject.web.rest.dto;

import java.io.Serializable;

import org.ngelmakproject.domain.enumeration.Accessibility;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull
    private String name;

    private Accessibility visibility;

    private Long userId;

}
