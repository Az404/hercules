package ru.kontur.vostok.hercules.gateway;

import io.undertow.server.HttpServerExchange;
import ru.kontur.vostok.hercules.auth.AuthManager;
import ru.kontur.vostok.hercules.meta.stream.StreamRepository;
import ru.kontur.vostok.hercules.protocol.Event;
import ru.kontur.vostok.hercules.protocol.decoder.Decoder;
import ru.kontur.vostok.hercules.protocol.decoder.EventReader;
import ru.kontur.vostok.hercules.protocol.decoder.ReaderIterator;
import ru.kontur.vostok.hercules.uuid.Marker;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Gregory Koshelev
 */
public class SendHandler extends GatewayHandler {
    public SendHandler(AuthManager authManager, EventSender eventSender, StreamRepository streamRepository) {
        super(authManager, eventSender, streamRepository);
    }

    @Override
    public void send(HttpServerExchange exchange, Marker marker, String topic, Set<String> tags, int partitions, String[] shardingKey) {
        exchange.getRequestReceiver().receiveFullBytes(
                (exch, bytes) -> {
                    exch.dispatch(() -> {
                        ReaderIterator<Event> reader = new ReaderIterator<>(new Decoder(bytes), EventReader.readTags(tags));
                        AtomicInteger pendingEvents = new AtomicInteger(reader.getTotal());
                        AtomicBoolean processed = new AtomicBoolean(false);
                        while (reader.hasNext()) {
                            Event event = reader.next();
                            eventSender.send(
                                    event,
                                    event.getId(),
                                    topic,
                                    partitions,
                                    shardingKey,
                                    () -> {
                                        if (pendingEvents.decrementAndGet() == 0 && processed.compareAndSet(false, true)) {
                                            exch.setStatusCode(200);
                                            exch.endExchange();
                                        }
                                    },
                                    () -> {
                                        if (processed.compareAndSet(false, true)) {
                                            exch.setStatusCode(422);
                                            exch.endExchange();
                                        }
                                    });
                        }
                    });
                });
    }
}
