-- V2__seed_conhecimento_base.sql
-- Dados iniciais de FAQ para alimentar o RAG
-- Os embeddings são gerados pelo serviço em runtime (não armazenados aqui)

-- A tabela será populada via endpoint POST /v1/ia/admin/ingerir
-- Este seed apenas registra os textos sem embedding (embedding = NULL)
-- O serviço processa os registros com embedding NULL na inicialização

INSERT INTO knowledge_embeddings (conteudo, fonte, fonte_id, metadata) VALUES

-- FAQ — Pedidos
('Para rastrear seu pedido, acesse o app e clique em "Meus Pedidos". Você verá o status em tempo real: Confirmado, Em preparo, Saiu para entrega ou Entregue.',
 'faq', 'faq-001', '{"categoria": "pedido", "topico": "rastreamento"}'),

('O tempo médio de entrega é de 30 a 60 minutos dependendo da distância e da demanda do restaurante. Você recebe notificações a cada mudança de status.',
 'faq', 'faq-002', '{"categoria": "pedido", "topico": "tempo_entrega"}'),

('Para cancelar um pedido, acesse "Meus Pedidos" e clique em Cancelar. O cancelamento só é possível enquanto o pedido estiver como Pendente ou Confirmado. Após o restaurante iniciar o preparo, não é mais possível cancelar.',
 'faq', 'faq-003', '{"categoria": "pedido", "topico": "cancelamento"}'),

('O pedido mínimo na plataforma é de R$ 15,00. Pedidos abaixo deste valor não são aceitos.',
 'faq', 'faq-004', '{"categoria": "pedido", "topico": "valor_minimo"}'),

-- FAQ — Pagamentos
('Aceitamos Cartão de Crédito, Cartão de Débito, PIX e Vale Refeição. O PIX é processado instantaneamente e tem aprovação imediata.',
 'faq', 'faq-005', '{"categoria": "pagamento", "topico": "metodos"}'),

('Em caso de pagamento recusado, verifique os dados do cartão e o limite disponível. Você pode tentar novamente com outro método de pagamento. Após 3 tentativas sem sucesso, o pedido é cancelado automaticamente.',
 'faq', 'faq-006', '{"categoria": "pagamento", "topico": "recusa"}'),

('O estorno é processado em até 7 dias úteis para cartão de crédito e instantaneamente para PIX. Estornos só são realizados para pedidos cancelados com pagamento já aprovado.',
 'faq', 'faq-007', '{"categoria": "pagamento", "topico": "estorno"}'),

-- FAQ — Restaurantes
('Os restaurantes exibidos como Abertos estão disponíveis para receber pedidos no momento. Restaurantes Fechados ou Suspensos não aceitam pedidos até reabrirem.',
 'faq', 'faq-008', '{"categoria": "restaurante", "topico": "status"}'),

('Cada restaurante possui seu próprio cardápio com preços, descrições e informações nutricionais. Itens marcados como vegetariano, vegano ou sem glúten atendem às respectivas restrições alimentares.',
 'faq', 'faq-009', '{"categoria": "restaurante", "topico": "cardapio"}'),

-- Políticas
('Nossa política de privacidade garante que seus dados pessoais são utilizados apenas para processar pedidos e melhorar sua experiência. Não compartilhamos dados com terceiros sem seu consentimento.',
 'politica', 'pol-001', '{"categoria": "privacidade"}'),

('Em caso de problemas com seu pedido, como itens faltando ou qualidade insatisfatória, entre em contato via chat em até 24 horas após a entrega para solicitar reembolso parcial ou total.',
 'politica', 'pol-002', '{"categoria": "qualidade"}');
