package br.com.florinda.pedidos.domain;

import java.util.Set;

public enum StatusPedido {

    PENDENTE,
    CONFIRMADO,
    PREPARANDO,
    SAIU_PARA_ENTREGA,
    ENTREGUE,
    CANCELADO;

    // -----------------------------------------------------------
    // Regra de negócio: transições de status válidas (RN02, RN03)
    // -----------------------------------------------------------
    public boolean podeTransicionarPara(StatusPedido destino) {
        return switch (this) {
            case PENDENTE          -> Set.of(CONFIRMADO, CANCELADO).contains(destino);
            case CONFIRMADO        -> Set.of(PREPARANDO, CANCELADO).contains(destino);
            case PREPARANDO        -> Set.of(SAIU_PARA_ENTREGA).contains(destino);
            case SAIU_PARA_ENTREGA -> Set.of(ENTREGUE).contains(destino);
            case ENTREGUE,
                 CANCELADO         -> false; // estados finais
        };
    }

    public boolean podeSerCancelado() {
        return this == PENDENTE || this == CONFIRMADO;
    }
}
