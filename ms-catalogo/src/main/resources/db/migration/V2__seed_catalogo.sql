-- V2__seed_catalogo.sql
-- Dados iniciais para desenvolvimento e testes

INSERT INTO restaurantes (id, nome, descricao, categoria, status,
    endereco_logradouro, endereco_numero, endereco_bairro,
    endereco_cidade, endereco_uf, endereco_cep,
    horario_abertura, horario_fechamento)
VALUES
    ('a1b2c3d4-0000-0000-0000-000000000001',
     'Churrascaria do Zé', 'O melhor churrasco da cidade',
     'CHURRASCARIA', 'ABERTO',
     'Rua das Carnes', '100', 'Centro',
     'Juazeiro do Norte', 'CE', '63010-000',
     '11:00', '23:00'),

    ('a1b2c3d4-0000-0000-0000-000000000002',
     'Pizzaria Bella Napoli', 'Pizzas artesanais com forno a lenha',
     'PIZZARIA', 'ABERTO',
     'Av. Leão Sampaio', '450', 'Triângulo',
     'Juazeiro do Norte', 'CE', '63040-000',
     '18:00', '00:00'),

    ('a1b2c3d4-0000-0000-0000-000000000003',
     'Sushi Nordeste', 'Fusão de sabores japoneses e nordestinos',
     'JAPONES', 'FECHADO',
     'Rua São Pedro', '22', 'Salesiano',
     'Juazeiro do Norte', 'CE', '63050-000',
     '12:00', '22:00');

INSERT INTO cardapios (id, restaurante_id, nome, ativo)
VALUES
    ('b1b2c3d4-0000-0000-0000-000000000001',
     'a1b2c3d4-0000-0000-0000-000000000001', 'Cardápio Principal', TRUE),
    ('b1b2c3d4-0000-0000-0000-000000000002',
     'a1b2c3d4-0000-0000-0000-000000000002', 'Cardápio Principal', TRUE),
    ('b1b2c3d4-0000-0000-0000-000000000003',
     'a1b2c3d4-0000-0000-0000-000000000003', 'Cardápio Principal', TRUE);

INSERT INTO itens_cardapio (id, cardapio_id, nome, descricao, preco, disponivel, vegetariano, calorias)
VALUES
    ('c1000000-0000-0000-0000-000000000001',
     'b1b2c3d4-0000-0000-0000-000000000001',
     'Picanha na brasa', '300g com acompanhamentos', 89.90, TRUE, FALSE, 850),
    ('c1000000-0000-0000-0000-000000000002',
     'b1b2c3d4-0000-0000-0000-000000000001',
     'Costela assada', '500g assada no bafo 12h', 75.00, TRUE, FALSE, 1100),

    ('c1000000-0000-0000-0000-000000000003',
     'b1b2c3d4-0000-0000-0000-000000000002',
     'Pizza Margherita', 'Molho, mussarela e manjericão', 49.90, TRUE, TRUE, 720),
    ('c1000000-0000-0000-0000-000000000004',
     'b1b2c3d4-0000-0000-0000-000000000002',
     'Pizza Calabresa', 'Molho, mussarela e calabresa', 54.90, TRUE, FALSE, 890),

    ('c1000000-0000-0000-0000-000000000005',
     'b1b2c3d4-0000-0000-0000-000000000003',
     'Combinado Nordestino', '15 peças com camarão e carne de sol', 79.90, TRUE, FALSE, 680);
