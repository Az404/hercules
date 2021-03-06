# Hercules Gate
Gate is used to transmit events from clients to Apache Kafka.

## Settings
Application is configured through properties file.

### HTTP Server settings
`http.server.host` - server host, default value: `0.0.0.0`

`http.server.port` - server port, default value: `6306`

`http.server.maxContentLength` - max Content-Length in POST-request

`http.server.throttling.capacity` - default value: `100000000`

`http.server.throttling.requestTimeout` - timeout for request, which capacity throttling more then permissible, default value: `5000`

### Kafka Producer settings
See Producer's Config from Apache Kafka documentation. Main settings are presented below.

`producer.bootstrap.servers`

`producer.acks`

`producer.batch.size`

`producer.linger.ms`

`producer.buffer.memory`

`producer.retries`

`producer.retry.backoff.ms`

### Apache Curator settings
See Apache Curator Config from Apache Curator documentation. Main settings are presented below.

`curator.connectString` - default value: `localhost:2181`

`curator.connectionTimeout` - default value: `10000`

`curator.sessionTimeout` - default value: `30000`

`curator.retryPolicy.baseSleepTime` - default value: `1000`

`curator.retryPolicy.maxRetries` - default value: `5`

`curator.retryPolicy.maxSleepTime` - default value: `8000`

### Graphite metrics reporter settings
`metrics.graphite.server.addr` - hostname of graphite instance to which metrics are sent, default value: `localhost`

`metrics.graphite.server.port` - port of graphite instance, default value: `2003`

`metrics.graphite.prefix` - prefix added to metric name

`metrics.period` - the period with which metrics are sent to graphite, default value: `60`

### Application context settings
`context.instance.id` - id of instance

`context.environment` - id of environment

`context.zone` - id of zone

## Command line
`java $JAVA_OPTS -jar hercules-gate.jar application.properties=file://path/to/file/application.properties`

Also, ZooKeeper can be used as source of `application.properties` file:  
```
zk://zk_host_1:port[,zk_host_2:port,...]/path/to/znode/application.properties
```

## Quick start
### Initialization
Gate uses Stream's metadata and auth rules from ZooKeeper. Thus, ZK should be configured by [Hercules Init](../hercules-init/README.md). See Hercules Init for details.

### `application.properties` sample:
```properties
http.server.host=0.0.0.0
http.server.port=6306
http.server.maxContentLength=25165824
http.server.throttling.capacity=1073741824
http.server.throttling.requestTimeout=10000

producer.bootstrap.servers=localhost:9092
producer.acks=all
producer.batch.size=65536
producer.linger.ms=1
producer.buffer.memory=335544320
producer.retries=4
producer.retry.backoff.ms=250

curator.connectString=localhost:2181
curator.connectionTimeout=10000
curator.sessionTimeout=30000
curator.retryPolicy.baseSleepTime=1000
curator.retryPolicy.maxRetries=3
curator.retryPolicy.maxSleepTime=3000

metrics.graphite.server.addr=localhost
metrics.graphite.server.port=2003
metrics.graphite.prefix=hercules
metrics.period=60

context.instance.id=1
context.environment=dev
context.zone=default
```
