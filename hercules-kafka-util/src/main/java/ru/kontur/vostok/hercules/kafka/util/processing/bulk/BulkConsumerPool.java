package ru.kontur.vostok.hercules.kafka.util.processing.bulk;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kontur.vostok.hercules.configuration.util.PropertiesUtil;
import ru.kontur.vostok.hercules.health.MetricsCollector;
import ru.kontur.vostok.hercules.kafka.util.processing.NamedThreadFactory;
import ru.kontur.vostok.hercules.kafka.util.processing.SinkStatusFsm;
import ru.kontur.vostok.hercules.protocol.Event;
import ru.kontur.vostok.hercules.util.PatternMatcher;
import ru.kontur.vostok.hercules.util.properties.PropertyDescription;
import ru.kontur.vostok.hercules.util.properties.PropertyDescriptions;
import ru.kontur.vostok.hercules.util.validation.Validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * BulkConsumerPool
 *
 * @author Kirill Sulim
 */
@Deprecated
public class BulkConsumerPool {

    private static class Props {
        static final String CONSUMER_POOL_SCOPE = "consumerPool";

        static final PropertyDescription<Integer> POOL_SIZE = PropertyDescriptions
                .integerProperty("size")
                .withDefaultValue(2)
                .withValidator(Validators.greaterThan(0))
                .build();

        static final PropertyDescription<Integer> SHUTDOWN_TIMEOUT_MS = PropertyDescriptions
                .integerProperty("shutdownTimeoutMs")
                .withDefaultValue(5_000)
                .withValidator(Validators.greaterOrEquals(0))
                .build();

        static final PropertyDescription<List<String>> PATTERN = PropertyDescriptions
                .listOfStringsProperty("pattern")
                .build();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(BulkConsumerPool.class);

    private final AtomicLong id = new AtomicLong(0);

    private final int poolSize;
    private final int shutdownTimeoutMs;

    private final ExecutorService pool;
    private final Supplier<BulkConsumer> bulkConsumerSupplier;
    private final List<BulkConsumer> consumers;

    public BulkConsumerPool(
            String destinationName,
            Properties consumerProperties,
            Properties sinkProperties,
            SinkStatusFsm status,
            MetricsCollector metricsCollector,
            Supplier<BulkSender<Event>> senderSupplier
    ) {
        final Properties consumerPoolProperties = PropertiesUtil.ofScope(sinkProperties, Props.CONSUMER_POOL_SCOPE);

        this.poolSize = Props.POOL_SIZE.extract(consumerPoolProperties);
        this.shutdownTimeoutMs = Props.SHUTDOWN_TIMEOUT_MS.extract(consumerPoolProperties);

        final List<PatternMatcher> patterns = Props.PATTERN.extract(consumerPoolProperties).stream()
            .map(PatternMatcher::new)
            .collect(Collectors.toList());

        final String groupId = ConsumerUtil.toGroupId(destinationName, patterns);

        final Meter receivedEventsMeter = metricsCollector.meter("receivedEvents");
        final Meter receivedEventsSizeMeter = metricsCollector.meter("receivedEventsSizeBytes");
        final Meter processedEventsMeter = metricsCollector.meter("processedEvents");
        final Meter droppedEventsMeter = metricsCollector.meter("droppedEvents");
        final Timer processTimeTimer = metricsCollector.timer("processTimeMs");

        this.pool = Executors.newFixedThreadPool(poolSize, new NamedThreadFactory("consumer-pool"));
        this.consumers = new ArrayList<>(poolSize);
        this.bulkConsumerSupplier = () -> new BulkConsumer(
                String.valueOf(id.getAndIncrement()),
                consumerProperties,
                sinkProperties,
                patterns,
                groupId,
                status,
                senderSupplier,
                receivedEventsMeter,
                receivedEventsSizeMeter,
                processedEventsMeter,
                droppedEventsMeter,
                processTimeTimer
        );
    }

    public void start() {
        for (int i = 0; i < poolSize; ++i) {
            BulkConsumer consumer = bulkConsumerSupplier.get();
            consumers.add(consumer);
            pool.execute(consumer);
        }
    }

    public void stop() throws InterruptedException {
        consumers.forEach(BulkConsumer::wakeup);
        pool.shutdown();
        if (!pool.awaitTermination(shutdownTimeoutMs, TimeUnit.MILLISECONDS)) {
            LOGGER.warn("Consumer pool was terminated by force");
        }
    }
}
