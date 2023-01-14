CREATE TABLE block_ids
(
    id        UUID                     NOT NULL,
    workchain INTEGER                  NOT NULL,
    shard     BIGINT                   NOT NULL,
    seqno     INTEGER                  NOT NULL,
    root_hash BYTEA                    NOT NULL,
    file_hash BYTEA                    NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_block_ids PRIMARY KEY (id)
);

CREATE TABLE exchange_pair_admins
(
    id        UUID                     NOT NULL,
    address   BYTEA                    NOT NULL,
    admin     BYTEA                    NOT NULL,
    block_id  UUID                     NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_exchange_pair_admins PRIMARY KEY (id)
);

CREATE TABLE exchange_pair_tokens
(
    id                 UUID                     NOT NULL,
    address            BYTEA                    NOT NULL,
    base               BYTEA                    NOT NULL,
    quote              BYTEA                    NOT NULL,
    block_id           UUID                     NOT NULL,
    timestamp          TIMESTAMP WITH TIME ZONE NOT NULL,
    liquidity_token_id UUID,
    base_token_id      UUID,
    quote_token_id     UUID,
    CONSTRAINT pk_exchange_pair_tokens PRIMARY KEY (id)
);

CREATE TABLE exchange_pairs
(
    id        UUID                     NOT NULL,
    address   BYTEA                    NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    admin_id  UUID,
    token_id  UUID,
    CONSTRAINT pk_exchange_pairs PRIMARY KEY (id)
);

CREATE TABLE liquidity
(
    id                    UUID                     NOT NULL,
    address               BYTEA                    NOT NULL,
    owner                 BYTEA                    NOT NULL,
    exchange_pair_address BYTEA                    NOT NULL,
    balance               numeric                  NOT NULL,
    block_id              UUID                     NOT NULL,
    timestamp             TIMESTAMP WITH TIME ZONE NOT NULL,
    exchange_pair_id      UUID,
    CONSTRAINT pk_liquidity PRIMARY KEY (id)
);

CREATE TABLE reserves
(
    id               UUID                     NOT NULL,
    address          BYTEA                    NOT NULL,
    base             numeric                  NOT NULL,
    quote            numeric                  NOT NULL,
    block_id         UUID                     NOT NULL,
    timestamp        TIMESTAMP WITH TIME ZONE NOT NULL,
    exchange_pair_id UUID,
    CONSTRAINT pk_reserves PRIMARY KEY (id)
);

CREATE TABLE swaps
(
    id            UUID                     NOT NULL,
    destination   BYTEA                    NOT NULL,
    base_amount   numeric                  NOT NULL,
    exchange_pair BYTEA                    NOT NULL,
    quote_amount  numeric                  NOT NULL,
    inverse       BOOLEAN                  NOT NULL,
    referrer      BYTEA                    NOT NULL,
    query_id      BIGINT                   NOT NULL,
    transaction   BYTEA                    NOT NULL,
    block_id      UUID                     NOT NULL,
    timestamp     TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_swaps PRIMARY KEY (id)
);

CREATE TABLE token_contracts
(
    id           UUID                     NOT NULL,
    address      BYTEA                    NOT NULL,
    total_supply numeric                  NOT NULL,
    mintable     BOOLEAN                  NOT NULL,
    admin        BYTEA                    NOT NULL,
    content      BYTEA                    NOT NULL,
    wallet_code  BYTEA                    NOT NULL,
    block_id     UUID                     NOT NULL,
    timestamp    TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_token_contracts PRIMARY KEY (id)
);

CREATE TABLE token_metadata
(
    id          UUID                     NOT NULL,
    address     BYTEA                    NOT NULL,
    uri         TEXT,
    name        TEXT,
    description TEXT,
    image       TEXT,
    image_data  BYTEA,
    symbol      TEXT,
    decimals    INTEGER                  NOT NULL,
    block_id    UUID                     NOT NULL,
    timestamp   TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_token_metadata PRIMARY KEY (id)
);

CREATE TABLE token_supplies
(
    id        UUID                     NOT NULL,
    address   BYTEA                    NOT NULL,
    supply    numeric                  NOT NULL,
    block_id  UUID                     NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    token_id  UUID,
    CONSTRAINT pk_token_supplies PRIMARY KEY (id)
);

CREATE TABLE tokens
(
    id          UUID                     NOT NULL,
    address     BYTEA                    NOT NULL,
    timestamp   TIMESTAMP WITH TIME ZONE NOT NULL,
    contract_id UUID,
    metadata_id UUID,
    CONSTRAINT pk_tokens PRIMARY KEY (id)
);

ALTER TABLE block_ids
    ADD CONSTRAINT uc_2f37de35795d0216b0421abd2 UNIQUE (workchain, shard, seqno);

ALTER TABLE exchange_pair_tokens
    ADD CONSTRAINT uc_e3ccc366dac9b0db3da1fe77d UNIQUE (base, quote);

ALTER TABLE exchange_pair_admins
    ADD CONSTRAINT uc_exchange_pair_admins_address UNIQUE (address);

ALTER TABLE exchange_pair_tokens
    ADD CONSTRAINT uc_exchange_pair_tokens_address UNIQUE (address);

ALTER TABLE exchange_pairs
    ADD CONSTRAINT uc_exchange_pairs_address UNIQUE (address);

ALTER TABLE exchange_pairs
    ADD CONSTRAINT uc_exchange_pairs_admin UNIQUE (admin_id);

ALTER TABLE exchange_pairs
    ADD CONSTRAINT uc_exchange_pairs_token UNIQUE (token_id);

ALTER TABLE token_contracts
    ADD CONSTRAINT uc_token_contracts_address UNIQUE (address);

ALTER TABLE token_metadata
    ADD CONSTRAINT uc_token_metadata_address UNIQUE (address);

ALTER TABLE tokens
    ADD CONSTRAINT uc_tokens_address UNIQUE (address);

ALTER TABLE tokens
    ADD CONSTRAINT uc_tokens_contract UNIQUE (contract_id);

ALTER TABLE tokens
    ADD CONSTRAINT uc_tokens_metadata UNIQUE (metadata_id);

ALTER TABLE exchange_pairs
    ADD CONSTRAINT FK_EXCHANGE_PAIRS_ON_ADMIN FOREIGN KEY (admin_id) REFERENCES exchange_pair_admins (id);

ALTER TABLE exchange_pairs
    ADD CONSTRAINT FK_EXCHANGE_PAIRS_ON_TOKEN FOREIGN KEY (token_id) REFERENCES exchange_pair_tokens (id);

ALTER TABLE exchange_pair_admins
    ADD CONSTRAINT FK_EXCHANGE_PAIR_ADMINS_ON_BLOCK FOREIGN KEY (block_id) REFERENCES block_ids (id);

ALTER TABLE exchange_pair_tokens
    ADD CONSTRAINT FK_EXCHANGE_PAIR_TOKENS_ON_BASE_TOKEN FOREIGN KEY (base_token_id) REFERENCES tokens (id);

ALTER TABLE exchange_pair_tokens
    ADD CONSTRAINT FK_EXCHANGE_PAIR_TOKENS_ON_BLOCK FOREIGN KEY (block_id) REFERENCES block_ids (id);

ALTER TABLE exchange_pair_tokens
    ADD CONSTRAINT FK_EXCHANGE_PAIR_TOKENS_ON_LIQUIDITY_TOKEN FOREIGN KEY (liquidity_token_id) REFERENCES tokens (id);

ALTER TABLE exchange_pair_tokens
    ADD CONSTRAINT FK_EXCHANGE_PAIR_TOKENS_ON_QUOTE_TOKEN FOREIGN KEY (quote_token_id) REFERENCES tokens (id);

ALTER TABLE liquidity
    ADD CONSTRAINT FK_LIQUIDITY_ON_BLOCK FOREIGN KEY (block_id) REFERENCES block_ids (id);

ALTER TABLE liquidity
    ADD CONSTRAINT FK_LIQUIDITY_ON_EXCHANGE_PAIR FOREIGN KEY (exchange_pair_id) REFERENCES exchange_pairs (id);

ALTER TABLE reserves
    ADD CONSTRAINT FK_RESERVES_ON_BLOCK FOREIGN KEY (block_id) REFERENCES block_ids (id);

ALTER TABLE reserves
    ADD CONSTRAINT FK_RESERVES_ON_EXCHANGE_PAIR FOREIGN KEY (exchange_pair_id) REFERENCES exchange_pairs (id);

ALTER TABLE swaps
    ADD CONSTRAINT FK_SWAPS_ON_BLOCK FOREIGN KEY (block_id) REFERENCES block_ids (id);

ALTER TABLE tokens
    ADD CONSTRAINT FK_TOKENS_ON_CONTRACT FOREIGN KEY (contract_id) REFERENCES token_contracts (id);

ALTER TABLE tokens
    ADD CONSTRAINT FK_TOKENS_ON_METADATA FOREIGN KEY (metadata_id) REFERENCES token_metadata (id);

ALTER TABLE token_contracts
    ADD CONSTRAINT FK_TOKEN_CONTRACTS_ON_BLOCK FOREIGN KEY (block_id) REFERENCES block_ids (id);

ALTER TABLE token_metadata
    ADD CONSTRAINT FK_TOKEN_METADATA_ON_BLOCK FOREIGN KEY (block_id) REFERENCES block_ids (id);

ALTER TABLE token_supplies
    ADD CONSTRAINT FK_TOKEN_SUPPLIES_ON_BLOCK FOREIGN KEY (block_id) REFERENCES block_ids (id);

ALTER TABLE token_supplies
    ADD CONSTRAINT FK_TOKEN_SUPPLIES_ON_TOKEN FOREIGN KEY (token_id) REFERENCES tokens (id);
