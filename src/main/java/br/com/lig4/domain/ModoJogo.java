package br.com.lig4.domain;

import java.text.Normalizer;
import java.util.Locale;

/** Modo da partida. Define, por enquanto, as moedas do fim da partida (e, no Caos, os poderes). */
public enum ModoJogo {
    CLASSICO,
    CAOS;

    /**
     * Converte o texto recebido do front-end. Aceita maiúsculas, minúsculas e acentos
     * ("CLASSICO", "classico", "Clássico"). Texto vazio vale CLASSICO. Devolve nulo se não for um modo.
     */
    public static ModoJogo de(String texto) {
        if (texto == null || texto.isBlank()) {
            return CLASSICO;
        }
        String normalizado = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
        for (ModoJogo modo : values()) {
            if (modo.name().equals(normalizado)) {
                return modo;
            }
        }
        return null;
    }
}
