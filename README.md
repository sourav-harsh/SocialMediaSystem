# social_media_system

## Run the Kafka Server

```bash

Generate a cluster ID: bin/kafka-storage.sh random-uuid; EX: MkU3OEVBNTcwNTJENDM2Qk

Format the storage: bin/kafka-storage.sh format -t MkU3OEVBNTcwNTJENDM2Qk -c config/server.properties

Start the server: bin/kafka-server-start.sh config/server.properties

```
