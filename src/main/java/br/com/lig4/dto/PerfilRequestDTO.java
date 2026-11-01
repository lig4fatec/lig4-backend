package br.com.lig4.dto;

import br.com.lig4.domain.CoresSala;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PerfilRequestDTO(

        @NotBlank(message = "O nick é obrigatório")
        @Size(max = 20, message = "O nick deve ter no máximo 20 caracteres")
        String nick,

        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nomeCompleto,

        @Size(max = 280, message = "A bio deve ter no máximo 280 caracteres")
        String bio,

        @NotBlank(message = "A cor é obrigatória")
        @Pattern(regexp = CoresSala.REGEX,
                message = "Cor inválida. Use blue, red, purple, green, orange, yellow ou brown")
        String cor
) {
}