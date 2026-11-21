package com.atm.cliente.controller;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

final class Formatos {

    private static final Locale BRASIL = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Pattern COM_MILHAR = Pattern.compile("\\d{1,3}(\\.\\d{3})+(,\\d{1,2})?");
    private static final Pattern SEM_MILHAR = Pattern.compile("\\d+([.,]\\d{1,2})?");

    private Formatos() {
    }

    static String moeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(BRASIL).format(valor);
    }

    static String dataHora(LocalDateTime dataHora) {
        return dataHora.format(DATA_HORA);
    }

    /** Aceita "50", "50,5", "50.50" e "1.234,56". Vazio quando o texto não é um valor válido. */
    static Optional<BigDecimal> lerValor(String texto) {
        String limpo = texto.trim();
        if (COM_MILHAR.matcher(limpo).matches()) {
            return Optional.of(new BigDecimal(limpo.replace(".", "").replace(',', '.')));
        }
        if (SEM_MILHAR.matcher(limpo).matches()) {
            return Optional.of(new BigDecimal(limpo.replace(',', '.')));
        }
        return Optional.empty();
    }
}
