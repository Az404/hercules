package ru.kontur.vostok.hercules.protocol.encoder;

public class ArrayWriter<T> extends CollectionWriter<T> {
    public ArrayWriter(Writer<T> elementWriter) {
        super(elementWriter);
    }

    @Override
    protected void writeLength(Encoder encoder, int length) {
        encoder.writeInteger(length);
    }
}
