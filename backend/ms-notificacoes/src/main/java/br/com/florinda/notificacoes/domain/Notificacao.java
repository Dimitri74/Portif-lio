package br.com.florinda.notificacoes.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Notificacao {

    public enum Tipo {
        PEDIDO_CRIADO,
        PEDIDO_CONFIRMADO,
        PEDIDO_PREPARANDO,
        PEDIDO_SAIU_PARA_ENTREGA,
        PEDIDO_ENTREGUE,
        PEDIDO_CANCELADO,
        PAGAMENTO_APROVADO,
        PAGAMENTO_FALHOU
    }

    public enum Canal {
        LOG,      // Fase 2: apenas log
        PUSH,     // Fase futura: push notification
        EMAIL,    // Fase futura: e-mail
        SMS       // Fase futura: SMS
    }

    /**
     * Representa uma notificação processada.
     * Em fases futuras, persiste em banco e dispara canais reais.
     */
    public record Registro(
            UUID id,
            UUID clienteId,
            UUID pedidoId,
            Tipo tipo,
            Canal canal,
            String mensagem,
            boolean enviado,
            OffsetDateTime criadoEm
    ) {
        public static Registro criar(UUID clienteId, UUID pedidoId,
                                     Tipo tipo, String mensagem) {
            return new Registro(
                    UUID.randomUUID(),
                    clienteId,
                    pedidoId,
                    tipo,
                    Canal.LOG,
                    mensagem,
                    true,
                    OffsetDateTime.now()
            );
        }
    }
}
