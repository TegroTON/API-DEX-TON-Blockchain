ALTER TABLE swaps
    RENAME exchange_pair TO exchange_pair_address;

ALTER TABLE swaps
    ADD exchange_pair_id UUID;

ALTER TABLE swaps
    ADD CONSTRAINT FK_SWAPS_ON_EXCHANGE_PAIR FOREIGN KEY (exchange_pair_id) REFERENCES exchange_pairs (id);

UPDATE swaps
SET exchange_pair_id = exchange_pairs.id
FROM exchange_pairs
WHERE exchange_pairs.address = swaps.exchange_pair_address;
