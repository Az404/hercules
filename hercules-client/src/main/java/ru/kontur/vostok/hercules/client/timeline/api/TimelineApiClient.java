package ru.kontur.vostok.hercules.client.timeline.api;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import ru.kontur.vostok.hercules.client.CommonHeaders;
import ru.kontur.vostok.hercules.client.CommonParameters;
import ru.kontur.vostok.hercules.client.LogicalShardState;
import ru.kontur.vostok.hercules.protocol.TimelineContent;
import ru.kontur.vostok.hercules.protocol.TimelineReadState;
import ru.kontur.vostok.hercules.protocol.decoder.Decoder;
import ru.kontur.vostok.hercules.protocol.decoder.EventReader;
import ru.kontur.vostok.hercules.protocol.decoder.SizeOf;
import ru.kontur.vostok.hercules.protocol.decoder.TimelineContentReader;
import ru.kontur.vostok.hercules.protocol.encoder.Encoder;
import ru.kontur.vostok.hercules.protocol.encoder.TimelineReadStateWriter;
import ru.kontur.vostok.hercules.util.throwable.ThrowableUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.function.Supplier;

/**
 * TimelineApiClient
 *
 * @author Kirill Sulim
 */
public class TimelineApiClient {

    private static final TimelineReadStateWriter STATE_WRITER = new TimelineReadStateWriter();
    private static final TimelineContentReader CONTENT_READER = new TimelineContentReader(EventReader.readAllTags());

    private final URI server;
    private final LogicalShardState shardState;
    private final String apiKey;
    private final CloseableHttpClient httpClient;

    public TimelineApiClient(
            final Supplier<CloseableHttpClient> httpClient,
            final URI server,
            final LogicalShardState shardState,
            final String apiKey
    ) {
        this.server = server;
        this.shardState = shardState;
        this.apiKey = apiKey;

        this.httpClient = httpClient.get();
    }

    public TimelineContent getTimelineContent(
            final String timeline,
            final TimelineReadState timelineReadState,
            final TimeInterval timeInterval,
            final int count
    ) {
        URI uri = ThrowableUtil.toUnchecked(() -> new URIBuilder(server.resolve(Resources.TIMELINE_READ))
                .addParameter(Parameters.TIMELINE, timeline)
                .addParameter(Parameters.RESPONSE_EVENTS_COUNT, String.valueOf(count))
                .addParameter(CommonParameters.LOGICAL_SHARD_ID, String.valueOf(shardState.getShardId()))
                .addParameter(CommonParameters.LOGICAL_SHARD_COUNT, String.valueOf(shardState.getShardCount()))
                .addParameter(Parameters.LEFT_TIME_BOUND, String.valueOf(timeInterval.getFrom()))
                .addParameter(Parameters.RIGHT_TIME_BOUND, String.valueOf(timeInterval.getTo()))
                .build());

        ByteArrayOutputStream bytes = new ByteArrayOutputStream(calculateReadStateSize(timelineReadState.getShardCount()));
        STATE_WRITER.write(new Encoder(bytes), timelineReadState);

        HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeader(CommonHeaders.API_KEY, apiKey);
        httpPost.setEntity(new ByteArrayEntity(bytes.toByteArray()));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream((int) entity.getContentLength());
            entity.writeTo(outputStream);
            return CONTENT_READER.read(new Decoder(outputStream.toByteArray()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean ping() {
        HttpGet httpGet = new HttpGet(server.resolve(Resources.PING));

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            return 200 == response.getStatusLine().getStatusCode();
        } catch (IOException e) {
            return false;
        }
    }

    private static int calculateReadStateSize(int shardCount) {
        return SizeOf.VECTOR_LENGTH + shardCount * (SizeOf.INTEGER + SizeOf.LONG + 2 * SizeOf.LONG);
    }

    private static class Resources {
        /**
         * Get stream content
         */
        static final URI TIMELINE_READ = URI.create("./timeline/read");

        /**
         * Ping stream API
         */
        static final URI PING = URI.create("./ping");
    }

    private static class Parameters {
        /**
         * Timeline
         */
        static final String TIMELINE = "timeline";

        /**
         * Event count
         */
        static final String RESPONSE_EVENTS_COUNT = "take";

        /**
         * Left inclusive time bound
         */
        static final String LEFT_TIME_BOUND = "from";

        /**
         * Right exclusive time bound
         */
        static final String RIGHT_TIME_BOUND = "to";
    }
}
