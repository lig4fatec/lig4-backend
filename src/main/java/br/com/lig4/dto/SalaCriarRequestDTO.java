package br.com.lig4.dto;

import br.com.lig4.domain.CoresSala;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SalaCriarRequestDTO(

        @NotNull(message = "O id do jogador é obrigatório")
        Long jogadorId,

        /** CLASSICO ou CAOS (sem acento ou com, em qualquer caixa). Vazio vale CLASSICO. */
        String modo,

        @NotBlank(message = "A cor é obrigatória")
        @Pattern(regexp = CoresSala.REGEX,
                message = "Cor inválida. Use blue, red, purple, green, orange, yellow ou brown")
        String cor
) {
}