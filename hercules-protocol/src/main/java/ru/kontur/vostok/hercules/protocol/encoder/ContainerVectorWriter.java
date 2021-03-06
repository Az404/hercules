package ru.kontur.vostok.hercules.protocol.encoder;

import ru.kontur.vostok.hercules.protocol.Container;

/**
 * Hercules Protocol Writer for vector of containers
 *
 * @author Daniil Zhenikhov
 */
public class ContainerVectorWriter implements Writer<Container[]> {
    public static final ContainerVectorWriter INSTANCE = new ContainerVectorWriter();

    private static final ContainerWriter CONTAINER_WRITER = ContainerWriter.INSTANCE;

    /**
     * Write containers' array with encoder.
     *
     * @param encoder Encoder for write data and pack with specific format
     * @param value   Array of containers which are must be written
     */
    @Override
    public void write(Encoder encoder, Container[] value) {
        encoder.writeVectorLength(value.length);

        for (Container container : value) {
            CONTAINER_WRITER.write(encoder, container);
        }
    }
}
