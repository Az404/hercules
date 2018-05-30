package ru.kontur.vostok.hercules.gateway;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;

import java.util.Deque;
import java.util.Optional;

/**
 * @author Gregory Koshelev
 */
public class ExchangeUtil {
    public static int extractContentLength(HttpServerExchange exchange) {
        HeaderValues header = exchange.getRequestHeaders().get("Content-Length");
        if (header == null || header.isEmpty()) {
            return -1;
        }

        String value = header.getFirst();
        if (value == null || value.isEmpty()) {
            return -1;
        }

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    public static Optional<String> extractPathParam(HttpServerExchange exchange, String name) {
        Deque<String> values = exchange.getPathParameters().get(name);
        return getFirstValueIfExists(values);
    }

    public static Optional<String> extractHeaderValue(HttpServerExchange exchange, String name) {
        HeaderValues header = exchange.getRequestHeaders().get(name);
        if (header == null || header.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(header.getFirst());
    }

    public static Optional<String> extractQueryParam(HttpServerExchange exchange, String name) {
        Deque<String> values = exchange.getQueryParameters().get(name);
        return getFirstValueIfExists(values);
    }

    private static Optional<String> getFirstValueIfExists(Deque<String> values) {
        if (values == null || values.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(values.getFirst());
    }
}
