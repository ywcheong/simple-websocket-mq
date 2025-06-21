docker exec -it smq-broker-kafka /bin/bash
/opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic smq-topic