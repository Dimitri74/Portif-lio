package br.com.florinda.pagamentos.domain;

public enum StatusPagamento {
    PENDENTE,
    PROCESSANDO,
    APROVADO,
    REJEITADO,
    ESTORNADO;

    public boolean podeSerEstornado() {
        return this == APROVADO;
    }

    public boolean isFinal() {
        return this == APROVADO || this == REJEITADO || this == ESTORNADO;
    }
}
