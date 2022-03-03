* Building
mvnw clean package

* Prepare data (c01)

#!/bin/bash
mkdir -p ./data
for i in 0 1 2 3 4
do
  cp /data/trec-car-2022/enwiki-2022-01-01-package/trainLargePackage/fold-${i}-train.pages.jsonl-splits/pages-1.jsonl.gz ./data/articles-${i}.jsonl.gz
  gzip -d articles-${i}.jsonl.gz
  rm ./data/articles-${i}.jsonl.gz
  ls -l ./data/articles-${i}.jsonl
done

* Create index

`java -jar target/outlines-1.jar index <new-index-location> <jsonl-files>...`

example: `java -jar target/outlines-1.jar index ./index.lucene ./data/articles-1.jsonl ./data/articles-2.jsonl ./data/articles-3.jsonl ./data/articles-4.jsonl`

`<num-index-location>` is the directory that will be created to store the index

`<jsonl-files>` is a list of files containing wikipedia articles in CAR jsonl format.
Any number of files can be specified and they will all be parsed into the index.

* Run outline predictor

`java -jar target/outlines-1.jar query <index-location> <num-queries> <jsonl-files>...`

example: `java -jar target/outlines-1.jar query ./index.lucene 100 ./data/articles-0.jsonl`

`<index-location>` is the location of an existing lucene index

`<num-queries>` is the maximum number of queries that should be run

`<jsonl-files>` same as above.  I used the jsonl files extracted from
trec-car-2022/enwiki-2022-01-01-package/trainLargePackage/fold-<N>-train.pages.jsonl-splits/pages-1.jsonl.gz

The fold-0 file is used to generate queries, and all the others are used to create the index.