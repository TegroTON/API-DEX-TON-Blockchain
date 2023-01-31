# ton-indexer

### Blocks Table

```
CREATE TABLE `ton_blocks`
(
    `workchain` Int32,
    `shard` UInt64,
    `seqno` UInt32,
    `root_hash` String,
    `file_hash` String,
    `time` Datetime,
    `start_lt` UInt64,
    `end_lt` UInt64,
    `key_block` Bool,
    `master_ref` String,
    `validator_list_hash_short` UInt32,
    `catchain_seqno` UInt32,
    `min_ref_mc_seqno` UInt32,
    `prev_key_block_seqno` UInt32,
    `after_merge` Bool,
    `after_split` Bool,
    `before_split` Bool,
    `want_merge` Bool,
    `want_split` Bool,
    PRIMARY KEY (`root_hash`)
);
```
