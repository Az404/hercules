package ru.kontur.vostok.hercules.protocol;

import org.junit.Test;
import ru.kontur.vostok.hercules.protocol.decoder.TimelineReadStateReader;
import ru.kontur.vostok.hercules.protocol.encoder.TimelineReadStateWriter;

import java.util.UUID;

public class TimelineReadStateWriteReadTest {

    @Test
    public void shouldWriteRead() {
        WriteReadPipe.init(new TimelineReadStateWriter(), new TimelineReadStateReader())
                .process(new TimelineReadState(new TimelineShardReadState[]{
                        new TimelineShardReadState(1, 123_456_789L, new UUID(1, 2)),
                        new TimelineShardReadState(2, 123_456_789L, new UUID(3, 4))
                }))
                .assertEquals(HerculesProtocolAssert::assertEquals);
    }
}
