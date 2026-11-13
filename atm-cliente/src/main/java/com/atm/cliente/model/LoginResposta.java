package com.atm.cliente.model;

import java.util.List;

public record LoginResposta(String token, String nome, List<String> contas) {
}
