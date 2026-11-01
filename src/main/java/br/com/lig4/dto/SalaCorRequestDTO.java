package br.com.lig4.dto;

import br.com.lig4.domain.CoresSala;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SalaCorRequestDTO(

        @NotNull(message = "O id do jogador é obrigatório")
        Long jogadorId,

        @NotBlank(message = "A cor é obrigatória")
        @Pattern(regexp = CoresSala.REGEX,
                message = "Cor inválida. Use blue, red, purple, green, orange, yellow ou brown")
        String cor
) {
}
