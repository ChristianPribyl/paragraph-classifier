#!/bin/bash
mkdir -p ./data
for i in 0 1 2 3 4
do
  cp /data/trec-car-2022/enwiki-2022-01-01-package/trainLargePackage/fold-${i}-train.pages.jsonl-splits/pages-1.jsonl.gz ./data/articles-${i}.jsonl.gz
  gzip -d articles-${i}.jsonl.gz
  rm ./data/articles-${i}.jsonl.gz
  ls -l ./data/articles-${i}.jsonl
done