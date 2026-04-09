package br.com.florinda.pedidos.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "historico_status_pedido")
public class HistoricoStatusPedido extends PanacheEntityBase {

    @Id
    @Column(columnDefinition = "CHAR(36)")
    public UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    public Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_de")
    public StatusPedido statusDe;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_para", nullable = false)
    public StatusPedido statusPara;

    @Column(length = 300)
    public String motivo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    public OffsetDateTime criadoEm = OffsetDateTime.now();

    public static HistoricoStatusPedido registrar(Pedido pedido,
                                                   StatusPedido de,
                                                   StatusPedido para,
                                                   String motivo) {
        HistoricoStatusPedido h = new HistoricoStatusPedido();
        h.pedido    = pedido;
        h.statusDe  = de;
        h.statusPara = para;
        h.motivo    = motivo;
        return h;
    }
}
