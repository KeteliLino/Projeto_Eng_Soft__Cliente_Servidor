-- Banco do caixa eletrônico. Pode ser executado de novo sem apagar dados existentes.
CREATE DATABASE IF NOT EXISTS caixa_eletronico CHARACTER SET utf8mb4;
USE caixa_eletronico;

CREATE TABLE IF NOT EXISTS cliente (
    cartao VARCHAR(19)  NOT NULL PRIMARY KEY,
    nome   VARCHAR(100) NOT NULL,
    pin    VARCHAR(120) NOT NULL COMMENT 'Hash PBKDF2 do PIN, nunca o PIN em si'
);

CREATE TABLE IF NOT EXISTS conta (
    numero         VARCHAR(20)   NOT NULL PRIMARY KEY,
    saldo          DECIMAL(15,2) NOT NULL DEFAULT 0,
    cliente_cartao VARCHAR(19)   NOT NULL,
    CONSTRAINT fk_conta_cliente FOREIGN KEY (cliente_cartao) REFERENCES cliente (cartao),
    CONSTRAINT ck_conta_saldo CHECK (saldo >= 0)
);

CREATE TABLE IF NOT EXISTS transacao (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    conta_numero VARCHAR(20)               NOT NULL,
    tipo         ENUM('SAQUE', 'DEPOSITO') NOT NULL,
    valor        DECIMAL(15,2)             NOT NULL,
    data_hora    DATETIME                  NOT NULL,
    CONSTRAINT fk_transacao_conta FOREIGN KEY (conta_numero) REFERENCES conta (numero),
    CONSTRAINT ck_transacao_valor CHECK (valor > 0),
    INDEX idx_transacao_conta (conta_numero, id)
);

-- Dados de teste. PIN de Maria: 1234 / PIN de João: 4321
INSERT IGNORE INTO cliente (cartao, nome, pin) VALUES
    ('1111222233334444', 'Maria Silva', '100000:pQbOXyuxRRk6BJyO6/y1pQ==:cTMsq488CM1W9aCYmcR4IYkKU0jwfx/glYayA1tNtbU='),
    ('5555666677778888', 'João Souza',  '100000:u2i0CPGhu44/ZfmgyKDEHw==:YMLdYoLnYk25cFb4L1D0IPqVP57vtxLJbYcpQn5E588=');

INSERT IGNORE INTO conta (numero, saldo, cliente_cartao) VALUES
    ('0001-1', 1000.00, '1111222233334444'),
    ('0001-2',  250.00, '1111222233334444'),
    ('0002-1',  500.00, '5555666677778888');
