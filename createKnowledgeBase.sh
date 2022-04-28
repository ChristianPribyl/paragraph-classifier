./mvnw clean package
find ./kb-corpus -name *.jsonl | xargs java -jar target/outlines-1.jar kb-index kb