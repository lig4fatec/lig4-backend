package br.com.lig4.dto;

import jakarta.validation.constraints.NotBlank;

public record LojaCompraRequestDTO(

        @NotBlank(message = "O item é obrigatório")
        String itemId
) {
}