# Heading Predictor
An engine for predicting section headings in articles.

The model takes a paragraph as input, and generates a heading as output.

It is evaluated on three tasks.

1. Given a paragraph, predict the heading of the section that paragraph came from.
2. Given all paragraphs in an article, predict the set of unique headings used in an article.
3. Given all paragraphs in an article, cluster the paragraphs such that two paragraphs from 
the same section are in the same cluster.

## Building
Build with maven
```
mvn clean package
```
Or use the included maven wrapper
```
./mvnw clean package
```

## Preparing the data (c01)
```
#!/bin/bash
mkdir -p ./kb-corpus
cp /data/trec-car-2022/en-wiki-01012022/unprocessedAllButBenchmarkPackage/fold-*-unprocessedAllButBenchmark.jsonl-splits ./kb-corpus/
find ./kb-corpus -name *.gz | xargs gzip -d
mkdir -p ./eval-corpus
cp /data/trec-car-2022/en-wiki-01012022/benchmarks/car-train-large/car-train-large.train/fold-*-train.pages.jsonl-splits ./eval-corpus/
find ./eval-corpus -name *.gz | xargs gzip -d

mkdir -p results
```

## Preparing the data (local)
```
#!/bin/bash
mkdir -p ./kb-corpus
rsync -e 'ssh -p 2281' -a clp1034@c01.cs.unh.edu:/data/trec-car-2022/en-wiki-01012022/unprocessedAllButBenchmarkPackage/fold-*-unprocessedAllButBenchmark.jsonl-splits ./kb-corpus/
find ./kb-corpus -name *.gz | xargs gzip -d
mkdir -p ./eval-corpus
rsync -e 'ssh -p 2281' -a clp1034@c01.cs.unh.edu:/data/trec-car-2022/en-wiki-01012022/benchmarks/benchmarkY1/benchmarkY1.test/fold-*-train.pages.jsonl-splits ./eval-corpus/
find ./eval-corpus -name *.gz | xargs gzip -d

mkdir -p results
```

## Create the knowledge-base index
```
java -jar target/outlines-1.jar kb-index <new-index-location> <jsonl-files>...
```

example (for this dataset): 
```
find ./kb-corpus -name *.jsonl | xargs java -jar target/outlines-1.jar kb-index ./kb
```

`<new-index-location>` is the directory that will be created to store the index

`<jsonl-files>` is a list of files containing wikipedia articles in CAR jsonl format.
Any number of files can be specified, and they will all be parsed into the index.

## Run all benchmarks
```
rm results/*

java -jar target/outlines-1.jar benchmark <index-location> <max-articles> <jsonl-files>...
```

example (for this dataset): 
```
rm results/*

find ./eval-corpus -name *.jsonl | xargs java -jar target/outlines-1.jar benchmark ./kb 200
```

`<index-location>` is the location of an existing knowledge-base index.

`<max-articles>` is the maximum number of articles that should be used to generate queries.

`<jsonl-files>` jsonl files containing articles used to generate test queries.

## Evaluate the benchmarks

### Heading prediction
Given a paragraph, predict the heading of section that paragraph was taken from.
This task can be used to derive clusters (paragraphs with the same predicted section heading),
or to classify paragraphs or sections (these paragraphs are about "applications in aerospace").
```
java -jar target/outlines-1.jar paragraph-heading-eval results/predictedParagraphHeadings.log
```

### Clustering
Given a set of paragraphs in an article, assign them each to disjoint sets which each represent
a section from the original article.
```
java -jar target/outlines-1.jar cluster-eval results/predictedClusters.log
```

### Heading-Set
Given the paragraphs that compose an article, predict the collection of headings that article contains.
This task can be leveraged for query expansion, by indirectly identifying the major topics
discussed in the article.
```
java -jar target/outlines-1.jar heading-set-eval results/predictedHeadingSets.log
```