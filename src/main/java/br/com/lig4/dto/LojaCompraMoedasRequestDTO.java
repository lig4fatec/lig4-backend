package br.com.lig4.dto;

import jakarta.validation.constraints.NotBlank;

public record LojaCompraMoedasRequestDTO(

        @NotBlank(message = "O pacote é obrigatório")
        String pacoteId
) {
}
