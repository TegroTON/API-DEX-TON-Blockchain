# ton-indexer

### Blocks Table

```
CREATE TABLE `blocks`
(
    `workchain` Int32,
    `shard` UInt64,
    `seqno` UInt32,
    `root_hash` String,
    `file_hash` String,
    PRIMARY KEY (`root_hash`, `seqno`, `shard`, `workchain`)
);
```
